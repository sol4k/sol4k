package org.sol4k

import org.junit.jupiter.api.Test
import org.sol4k.Constants.TOKEN_2022_PROGRAM_ID
import org.sol4k.api.TransactionSimulationError
import org.sol4k.api.TransactionSimulationSuccess
import org.sol4k.instruction.CreateAssociatedToken2022AccountInstruction
import org.sol4k.instruction.CreateAssociatedTokenAccountInstruction
import org.sol4k.instruction.SetComputeUnitLimitInstruction
import org.sol4k.instruction.SetComputeUnitPriceInstruction
import org.sol4k.instruction.SplTransferInstruction
import org.sol4k.instruction.TransferInstruction
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ConnectionTest {
    private val rpcUrl = getRpcUrl()
    private val secretKey = getSecretKey()

    @Test
    fun shouldGetBalance() {
        val connection = Connection(rpcUrl)
        val wallet = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")

        val balance = connection.getBalance(wallet)

        Logger.info("Balance: $balance")
    }

    @Test
    fun shouldGetLatestBlockhash() {
        val connection = Connection(rpcUrl)

        val hash = connection.getLatestBlockhash()

        Logger.info("hash: $hash")
    }

    @Test
    fun shouldGetLatestBlockhashExtended() {
        val connection = Connection(rpcUrl)

        val blockhash = connection.getLatestBlockhashExtended()

        Logger.info("hash: $blockhash")
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
            sender.publicKey,
        )
        transaction.sign(sender)

        val signature = connection.sendTransaction(transaction)

        Logger.info("signature: $signature")
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

        val signature = connection.sendTransaction(transaction)

        Logger.info("signature: $signature")
    }

    @Test
    fun shouldSendVersionedTransactionWithComputeUnitPriceAndLimit() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val sender = Keypair.fromSecretKey(Base58.decode(secretKey))
        val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val transferInstruction = TransferInstruction(sender.publicKey, receiver, 1000)
        val setLimitInstruction = SetComputeUnitLimitInstruction(50_000L)
        val setPriceInstruction = SetComputeUnitPriceInstruction(2000L)
        val message = TransactionMessage.newMessage(
            sender.publicKey,
            blockhash,
            listOf(transferInstruction, setLimitInstruction, setPriceInstruction),
        )
        val transaction = VersionedTransaction(message)
        transaction.sign(sender)

        val signature = connection.sendTransaction(transaction)

        Logger.info("signature: $signature")
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
            sender.publicKey,
        )
        transaction.sign(sender)

        val simulation = connection.simulateTransaction(transaction)

        assertTrue("Simulation must be successful") {
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
            senderWithNoSol.publicKey,
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
            Base58.decode(secretKey),
        )
        val firstReceiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val secondReceiver = PublicKey("Hb2zfRfn5RwBq2DNWhee2iTVprfGHgiuK7KsiDA4HfMW")
        val transaction = Transaction(
            blockhash,
            listOf(
                TransferInstruction(sender.publicKey, firstReceiver, 1000),
                TransferInstruction(sender.publicKey, secondReceiver, 1000),
            ),
            sender.publicKey,
        )
        transaction.sign(sender)

        val signature = connection.sendTransaction(transaction)

        Logger.info("signature: $signature")
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
        val message = TransactionMessage.newMessage(payerWallet.publicKey, blockhash, listOf(instruction))
        val transaction = VersionedTransaction(message)
        transaction.sign(payerWallet)

        val signature = connection.sendTransaction(transaction)

        Logger.info("signature: $signature")
    }

    @Test
    fun shouldCreateAssociatedToken2022Account() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()
        val payerWallet = Keypair.fromSecretKey(Base58.decode(secretKey))
        val token2022Mint = PublicKey("2M8uRtM7rHVtEgY8bAW6tg7o8S1PQPLPcxJ1ug9PF11g")
        val destinationWallet = Keypair.generate().publicKey
        val (associatedAccount) = PublicKey.findProgramDerivedAddress(
            destinationWallet,
            token2022Mint,
            TOKEN_2022_PROGRAM_ID,
        )
        val instruction = CreateAssociatedToken2022AccountInstruction(
            payer = payerWallet.publicKey,
            associatedToken = associatedAccount,
            owner = destinationWallet,
            mint = token2022Mint,
        )
        val message = TransactionMessage.newMessage(payerWallet.publicKey, blockhash, listOf(instruction))
        val transaction = VersionedTransaction(message)
        transaction.sign(payerWallet)

        val signature = connection.sendTransaction(transaction)

        Logger.info("signature: $signature")
    }

    @Test
    fun shouldGetAccountInfo() {
        val usdc = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")
        val connection = Connection(rpcUrl)

        val accountInfo = connection.getAccountInfo(usdc)

        Logger.info("accountInfo: $accountInfo")
    }

    @Test
    fun shouldGetMultipleAccounts() {
        val connection = Connection(rpcUrl)
        val usdcMint = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")
        val nonExistentAccount = Keypair.generate().publicKey
        val tokenAccount = PublicKey("73d3sqQPLsiwKvdJt2XnnLEzNiEjfn2nreqLujM7zXiT")
        val accounts = connection.getMultipleAccounts(listOf(usdcMint, nonExistentAccount, tokenAccount))

        assertEquals(3, accounts.size)

        assertNotNull(accounts[0])
        assertNull(accounts[1])
        assertNotNull(accounts[2])

        Logger.info("USDC Mint Account Info: ${accounts[0]}")
        Logger.info("Non-existent Account Info: ${accounts[1]}")
        Logger.info("Token Account Info: ${accounts[2]}")

        val usdcAccount = accounts[0]!!
        assertFalse(usdcAccount.executable)
        assertTrue(usdcAccount.data.isNotEmpty())

        val tokenAccountInfo = accounts[2]!!
        assertFalse(tokenAccountInfo.executable)
        assertTrue(tokenAccountInfo.data.isNotEmpty())
    }

    @Test
    fun shouldGetAccountInfoWhenAccountDoesNotExist() {
        val connection = Connection(rpcUrl)
        val publicKey = Keypair.generate().publicKey

        val accountInfo = connection.getAccountInfo(publicKey)

        Logger.info("accountInfo: $accountInfo")
    }

    @Test
    fun shouldGetMinimumBalanceForRentExemption() {
        val connection = Connection(rpcUrl)
        val space = 50

        val minimumBalanceForRentExemption = connection.getMinimumBalanceForRentExemption(space)

        Logger.info("minimumBalanceForRentExemption $minimumBalanceForRentExemption")
    }

    @Test
    fun shouldGetTokenSupply() {
        val connection = Connection(rpcUrl)
        val usdc = "Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr"

        val tokenAmount = connection.getTokenSupply(usdc)

        Logger.info("tokenAmount $tokenAmount")
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
            100,
            6,
        )
        val transaction = Transaction(
            blockhash,
            splTransferInstruction,
            holder.publicKey,
        )
        transaction.sign(holder)

        val signature = connection.sendTransaction(transaction)

        Logger.info("signature: $signature")
    }

    @Test
    fun shouldGetHealth() {
        val connection = Connection(rpcUrl)

        val health = connection.getHealth()

        Logger.info("health: $health")
    }

    // TODO: find a way to fight rate limiting issue for airdrops
    // @Test
    fun shouldRequestAirdrop() {
        val connection = Connection(rpcUrl)
        val receiver = Keypair.fromSecretKey(Base58.decode(secretKey)).publicKey

        val signature = connection.requestAirdrop(receiver, 1000000000)

        Logger.info("signature: $signature")
    }

    @Test
    fun shouldGetIdentity() {
        val connection = Connection(rpcUrl)

        val identity = connection.getIdentity()

        Logger.info("identity: $identity")
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
    fun shouldVerifyIfBlockhashValid() {
        val connection = Connection(rpcUrl)
        val blockhash = connection.getLatestBlockhash()

        val result = connection.isBlockhashValid(blockhash)

        assertTrue("blockhash must be valid") { result }
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

        Logger.info("result: $result")
    }

    @Test
    fun shouldGetTransactionCount() {
        val connection = Connection(rpcUrl)

        val count = connection.getTransactionCount()

        Logger.info("count: $count")
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
