package org.sol4k

import org.junit.jupiter.api.Test
import org.sol4k.api.*
import org.sol4k.instruction.CreateAssociatedTokenAccountInstruction
import org.sol4k.instruction.SplTransferInstruction
import org.sol4k.instruction.TransferInstruction
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ConnectionTest {
    private val rpcUrl = getRpcUrl()
    private val secretKey = getSecretKey()

    @Test
    fun shouldGetBalance() {
        val connection = Connection(rpcUrl)
        val wallet = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")

        val balance = connection.getBalance(wallet)

        println("shouldGetBalance: Balance: $balance")
    }

    @Test
    fun shouldGetBalanceOptional() {
        val connection = Connection(rpcUrl)
        val wallet = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")

        val balance = connection.getBalance(wallet, mapOf(
            "commitment" to Commitment.FINALIZED.toString(),
            "minContextSlot" to 3))

        println("shouldGetBalance with optional: Balance: $balance")
    }

    @Test
    fun shouldGetLatestBlockhash() {
        val connection = Connection(rpcUrl)

        val hash = connection.getLatestBlockhash()

        println("shouldGetLatestBlockhash: hash: $hash")
    }

    @Test
    fun shouldGetLatestBlockhashOptional() {
        val connection = Connection(rpcUrl)

        val hash = connection.getLatestBlockhash(mapOf(
            "commitment" to Commitment.FINALIZED.toString(),
            "minContextSlot" to 3))

        println("shouldGetLatestBlockhash with optional: hash: $hash")
    }

    @Test
    fun shouldGetLatestBlockhashExtended() {
        val connection = Connection(rpcUrl)

        val blockhash = connection.getLatestBlockhashExtended()

        println("shouldGetLatestBlockhashExtended: hash: $blockhash")
    }

    @Test
    fun shouldGetLatestBlockhashExtendedOptional() {
        val connection = Connection(rpcUrl)

        val blockhash = connection.getLatestBlockhash(mapOf(
            "commitment" to Commitment.FINALIZED.toString(),
            "minContextSlot" to 3))

        println("shouldGetLatestBlockhashExtended with optional: hash: $blockhash")
    }

    @Test
    fun shouldSendTransaction() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val sender = Keypair.fromSecretKey(Base58.decode(secretKey))
        val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val instruction = TransferInstruction(sender.publicKey, receiver, 1000)
        val transaction = Transaction(
            blockhash,
            instruction,
            sender.publicKey
        )
        transaction.sign(sender)

        val signature = connection.sendTransaction(transaction)

        println("shouldSendTransaction: signature: $signature")
    }

    @Test
    fun shouldSendTransactionOptional() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val sender = Keypair.fromSecretKey(Base58.decode(secretKey))
        val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val instruction = TransferInstruction(sender.publicKey, receiver, 1000)
        val transaction = Transaction(
            blockhash,
            instruction,
            sender.publicKey
        )
        transaction.sign(sender)

       val signature = connection.sendTransaction(transaction, mapOf(
            "skipPreflight " to true,
            "encoding" to "base64",
            "maxRetries" to 2
        ))

        println("shouldSendTransaction with optional: signature: $signature")
    }

    @Test
    fun shouldSendVersionedTransaction() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val sender = Keypair.fromSecretKey(Base58.decode(secretKey))
        val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val instruction = TransferInstruction(sender.publicKey, receiver, 1000)
        val message = TransactionMessage.newMessage(
            sender.publicKey,
            blockhash,
            listOf(instruction),
        )
        val transaction = VersionedTransaction(message)
        transaction.sign(sender)

        val signature = connection.sendTransaction(transaction.serialize())

        println("shouldSendVersionedTransaction: signature: $signature")
    }

    @Test
    fun shouldSimulateTransaction() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val sender = Keypair.fromSecretKey(Base58.decode(secretKey))
        val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val instruction = TransferInstruction(sender.publicKey, receiver, 1000)
        val transaction = Transaction(
            blockhash,
            instruction,
            sender.publicKey
        )
        transaction.sign(sender)

        val simulation = connection.simulateTransaction(transaction)

        assertTrue("Simulation must be successful") {
            simulation is TransactionSimulationSuccess
        }
        assertEquals(2, (simulation as TransactionSimulationSuccess).logs.size)
    }

    @Test
    fun shouldSimulateTransactionOptional() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val sender = Keypair.fromSecretKey(Base58.decode(secretKey))
        val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val instruction = TransferInstruction(sender.publicKey, receiver, 1000)
        val transaction = Transaction(
            blockhash,
            instruction,
            sender.publicKey
        )
        transaction.sign(sender)

        val simulation = connection.simulateTransaction(transaction, mapOf(
            "replaceRecentBlockhash" to true,
            "encoding" to "base64",
            "innerInstructions" to true
        ))
        assertTrue("Simulation with optional must be successful") {
            simulation is TransactionSimulationSuccess
        }
        assertEquals(2, (simulation as TransactionSimulationSuccess).logs.size)
    }

    @Test
    fun shouldSimulateTransactionWithAnError() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val senderWithNoSol = Keypair.generate()
        val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val instruction = TransferInstruction(senderWithNoSol.publicKey, receiver, 1000)
        val transaction = Transaction(
            blockhash,
            instruction,
            senderWithNoSol.publicKey
        )
        transaction.sign(senderWithNoSol)

        val simulation = connection.simulateTransaction(transaction)

        assertTrue("Simulation must produce an error") { simulation is TransactionSimulationError }
        assertEquals("AccountNotFound", (simulation as TransactionSimulationError).error)
    }

    @Test
    fun shouldSendTwoInstructionsInOneTransaction() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val sender = Keypair.fromSecretKey(
            Base58.decode(secretKey)
        )
        val firstReceiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val secondReceiver = PublicKey("Hb2zfRfn5RwBq2DNWhee2iTVprfGHgiuK7KsiDA4HfMW")
        val transaction = Transaction(
            blockhash,
            listOf(
                TransferInstruction(sender.publicKey, firstReceiver, 1000),
                TransferInstruction(sender.publicKey, secondReceiver, 1000),
            ),
            sender.publicKey
        )
        transaction.sign(sender)

        val signature = connection.sendTransaction(transaction)

        println("shouldSendTowInstructionsInOneTransaction: signature: $signature")
    }

    @Test
    fun shouldSendCreateAssociatedTokenTransaction() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val payerWallet = Keypair.fromSecretKey(Base58.decode(secretKey))
        val usdcMintAddress = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")
        val destinationWallet = Keypair.generate().publicKey
        val (associatedAccount) = PublicKey.findProgramDerivedAddress(destinationWallet, usdcMintAddress)
        val instruction = CreateAssociatedTokenAccountInstruction(
            payer = payerWallet.publicKey,
            associatedToken = associatedAccount,
            owner = destinationWallet,
            mint = usdcMintAddress,
        )
        val transaction = Transaction(
            blockhash,
            instruction,
            feePayer = payerWallet.publicKey,
        )
        transaction.sign(payerWallet)
        val signature = connection.sendTransaction(transaction)

        println("shouldSendCreateAssociatedTokenTransaction: signature: $signature")
    }

    @Test
    fun shouldGetAccountInfo() {
        val usdc = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")
        val connection = Connection(rpcUrl)

        val accountInfo = connection.getAccountInfo(usdc)

        println("shouldGetAccountInfo: accountInfo: $accountInfo")
    }

    @Test
    fun shouldGetAccountInfoOptional() {
        val usdc = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")
        val connection = Connection(rpcUrl)

        val accountInfo = connection.getAccountInfo(usdc, mapOf(
            "encoding" to "base64",
            "minContextSlot" to 3))

        println("shouldGetAccountInfo with optional: accountInfo: $accountInfo")
    }

    @Test
    fun shouldGetAccountInfoWhenAccountDoesNotExist() {
        val connection = Connection(rpcUrl)
        val publicKey = Keypair.generate().publicKey

        val accountInfo = connection.getAccountInfo(publicKey)

        println("shouldGetAccountInfoWhenAccountDoesNotExist: accountInfo: $accountInfo")
    }

    @Test
    fun shouldGetMinimumBalanceForRentExemption() {
        val connection = Connection(rpcUrl)
        val space = 50

        val minimumBalanceForRentExemption = connection.getMinimumBalanceForRentExemption(space)

        println("shouldGetMinimumBalanceForRentExemption minimumBalanceForRentExemption $minimumBalanceForRentExemption")
    }

    @Test
    fun shouldGetTokenSupply() {
        val connection = Connection(rpcUrl)
        val usdc = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")

        val tokenAmount = connection.getTokenSupply(usdc.toBase58())

        println("shouldGetTokenSupply tokenAmount $tokenAmount")
    }

    @Test
    fun shouldSendSpl() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val holder = Keypair.fromSecretKey(Base58.decode(secretKey))
        val usdc = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")
        val receiverAssociatedAccount = PublicKey("8r2iVNBQgJi59YCdj2YXipguirWZhdysWpL4cEGorN1v")
        val (holderAssociatedAccount) = PublicKey.findProgramDerivedAddress(holder.publicKey, usdc)
        val splTransferInstruction = SplTransferInstruction(
            holderAssociatedAccount,
            receiverAssociatedAccount,
            usdc,
            holder.publicKey,
            emptyList(),
            100,
            6,
        )
        val transaction = Transaction(
            blockhash,
            splTransferInstruction,
            holder.publicKey
        )
        transaction.sign(holder)

        val signature = connection.sendTransaction(transaction)

        println("shouldSendSpl: signature: $signature")
    }

    @Test
    fun shouldGetHealth() {
        val connection = Connection(rpcUrl)

        val health = connection.getHealth()

        println("shouldGetHealth: health: $health")
    }

    // TODO: find a way to fight rate limiting issue for airdrops
    // @Test
    fun shouldRequestAirdrop() {
        val connection = Connection(rpcUrl)
        val receiver = Keypair.fromSecretKey(Base58.decode(secretKey)).publicKey

        val signature = connection.requestAirdrop(receiver, 1000000000)

        println("shouldRequestAirdrop: signature: $signature")
    }

    @Test
    fun shouldGetIdentity() {
        val connection = Connection(rpcUrl)

        val identity = connection.getIdentity()

        println("shouldGetIdentity: identity: $identity")
    }

    @Test
    fun shouldGetTokenAccountBalance() {
        val connection = Connection(rpcUrl)
        val receiverAssociatedAccount = PublicKey("73d3sqQPLsiwKvdJt2XnnLEzNiEjfn2nreqLujM7zXiT")

        val (amount, decimals, uiAmount) = connection.getTokenAccountBalance(receiverAssociatedAccount)

        assertEquals(BigInteger("123"), amount)
        assertEquals(6, decimals)
        assertEquals("0.000123", uiAmount)
    }

    @Test
    fun shouldGetParsedTokenAccountsByOwner() {
        val connection = Connection(rpcUrl)
        val accountAddress = PublicKey("4sZmtJHNuT7v1Dy1BpjdutqdJap4Jqt1Ekp6jYKsLbhu")

        val tokenAccountsByOwner = connection.getParsedTokenAccountsByOwner(accountAddress,
            mapOf(TokenAccountsByOwnerParams.PROGRAM_ID to Constants.TOKEN_PROGRAM_ID.toString())

        )
        println("shouldGetTokenAccountsByOwner: tokenAccountsByOwner: $tokenAccountsByOwner")
    }

    @Test
    fun shouldGetTokenAccountsByOwner() {
        val connection = Connection(rpcUrl)
        val accountAddress = PublicKey("4sZmtJHNuT7v1Dy1BpjdutqdJap4Jqt1Ekp6jYKsLbhu")

        val tokenAccountsByOwner = connection.getTokenAccountsByOwner(accountAddress,
            mapOf(TokenAccountsByOwnerParams.PROGRAM_ID to Constants.TOKEN_PROGRAM_ID.toString())

        )
        println("shouldGetTokenAccountsByOwner: tokenAccountsByOwner: $tokenAccountsByOwner")
    }

    @Test
    fun shouldVerifyIfBlockhashValid() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()

        val result = connection.isBlockhashValid(blockhash)

        assertTrue("blockhash must be valid") { result }
    }

    @Test
    fun shouldVerifyIfBlockhashValidOptional() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()

        val result = connection.isBlockhashValid(blockhash,  mapOf(
            "commitment" to Commitment.FINALIZED.toString(),
            "minContextSlot" to 3))

        assertTrue("blockhash with optional must be valid") { result }
    }

    @Test
    fun shouldVerifyIfBlockhashValidGivenInvalidBlockhash() {
        val connection = Connection(rpcUrl)
        val anOutdatedBlockhash = "3dseDCjWBhwFxuukMuiRofHSZaNozXYQKAYFj9vDSoca"

        val result = connection.isBlockhashValid(anOutdatedBlockhash)

        assertFalse("blockhash must be invalid") { result }
    }

    @Test
    fun shouldGetEpochInfo() {
        val connection = Connection(rpcUrl)

        val result = connection.getEpochInfo()

        println("shouldGetEpochInfo: result: $result")
    }

    @Test
    fun shouldGetEpochInfoOptional() {
        val connection = Connection(rpcUrl)

        val result = connection.getEpochInfo(mapOf("minContextSlot" to 3 ))

        println("shouldGetEpochInfo with optional: result: $result")
    }

    @Test
    fun shouldGetTransactionCount() {
        val connection = Connection(rpcUrl)

        val count = connection.getTransactionCount()

        println("shouldGetTransactionCount: count: $count")
    }

    @Test
    fun shouldGetTransactionCountOptional() {
        val connection = Connection(rpcUrl)

        val count = connection.getTransactionCount(mapOf("minContextSlot" to 3 ))

        println("shouldGetTransactionCount with optional: count: $count")
    }

    private fun getRpcUrl(): String {
        val rpcUrl = System.getProperty("E2E_RPC_URL")
        return if (rpcUrl.isNullOrEmpty()) "https://api.devnet.solana.com" else rpcUrl
    }

    private fun getSecretKey(): String {
        val secretKey = System.getProperty("E2E_SECRET_KEY")
        return if (secretKey.isNullOrEmpty()) {
            // Public Key: EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF
            // Make sure it has Devnet SOL & Devnet USDC if you rely on it
            "28bMpVHJQjuxo3fWw4cBa6Gz7QELgYkx4cjMxU87aPx9Hn6amZZQwH2J5UNCzSYM1jDjcj7TndiK4gpGSiYyLcPy"
        } else {
            secretKey
        }
    }
}
