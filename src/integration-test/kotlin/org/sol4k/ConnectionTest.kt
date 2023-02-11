package org.sol4k

import org.junit.jupiter.api.Test
import java.math.BigInteger.ZERO
import kotlin.test.assertTrue

internal class ConnectionTest {

    private val rpcUrl = "https://api.devnet.solana.com"

    @Test
    fun shouldGetBalance() {
        val connection = Connection(rpcUrl)
        val wallet = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")

        val balance = connection.getBalance(wallet)

        assertTrue("balance must not be blank") { balance > ZERO }
    }

    @Test
    fun shouldGetLatestBlockhash() {
        val connection = Connection(rpcUrl)

        val hash = connection.getLatestBlockhash()

        assertTrue("blockhash must not be blank") { hash.blockhash.isNotBlank() }
    }

    @Test
    fun shouldSendTransaction() {
        val connection = Connection(rpcUrl)
        val (blockhash) = connection.getLatestBlockhash()
        val sender = Keypair.fromSecretKey(
            Base58.decode("2WGcYYau2gLu2DUq68SxxXQmCgi77n8hFqqLNbNyg6Xfh2m3tvg8LF5Lgh69CFDux41LUKV1ak1ERHUqiBZnyshz")
        )
        val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
        val instruction = TransferInstruction(sender.publicKey, receiver, 1000)
        val transaction = Transaction(
            blockhash,
            instruction,
            sender.publicKey
        )
        transaction.sign(sender)

        val signature = connection.sendTransaction(transaction)

        assertTrue("signature must not be blank") { signature.isNotBlank() }
    }
}
