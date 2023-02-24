package org.sol4k

import org.junit.jupiter.api.Test
import org.sol4k.instruction.CreateAssociatedTokenAccountInstruction
import org.sol4k.instruction.SplTransferInstruction
import org.sol4k.instruction.TransferInstruction

internal class ConnectionTest {
    private val rpcUrl = System.getProperty("E2E_RPC_URL")
    private val secretKey = System.getProperty("E2E_SECRET_KEY")

    @Test
    fun shouldGetBalance() {
        val connection = Connection(rpcUrl)
        val wallet = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")

        val balance = connection.getBalance(wallet)

        println("shouldGetBalance: Balance: $balance")
    }

    @Test
    fun shouldGetLatestBlockhash() {
        val connection = Connection(rpcUrl)

        val hash = connection.getLatestBlockhash()

        println("shouldGetLatestBlockhash: hash: ${hash.blockhash}")
    }

    @Test
    fun shouldSendTransaction() {
        val connection = Connection(rpcUrl)
        val (blockhash) = connection.getLatestBlockhash()
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
    fun shouldSendTowInstructionsInOneTransaction() {
        val connection = Connection(rpcUrl)
        val (blockhash) = connection.getLatestBlockhash()
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
        val (blockhash) = connection.getLatestBlockhash()
        val payer = Keypair.fromSecretKey(Base58.decode(secretKey))
        val usdc = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")
        val owner = Keypair.generate().publicKey
        val (associatedAccount) = PublicKey.findProgramDerivedAddress(owner, usdc)
        val instruction = CreateAssociatedTokenAccountInstruction(
            payer.publicKey,
            associatedAccount,
            owner,
            usdc,
        )
        val transaction = Transaction(
            blockhash,
            instruction,
            payer.publicKey,
        )
        transaction.sign(payer)

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
    fun shouldGetAccountInfoWhenAccountDoesNotExist() {
        val connection = Connection(rpcUrl)
        val publicKey = Keypair.generate().publicKey

        val accountInfo = connection.getAccountInfo(publicKey)

        println("shouldGetAccountInfoWhenAccountDoesNotExist: accountInfo: $accountInfo")
    }

    @Test
    fun shouldSendSpl() {
        val connection = Connection(rpcUrl)
        val (blockhash) = connection.getLatestBlockhash()
        val holder = Keypair.fromSecretKey(Base58.decode(secretKey))
        val usdc = PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr")
        val receiverAssociatedAccount = PublicKey("8r2iVNBQgJi59YCdj2YXipguirWZhdysWpL4cEGorN1v")
        val (holderAssociatedAccount) = PublicKey.findProgramDerivedAddress(holder.publicKey, usdc)
        val splTransferInstruction = SplTransferInstruction(
            holderAssociatedAccount,
            receiverAssociatedAccount,
            holder.publicKey,
            100,
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

    @Test
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
}
