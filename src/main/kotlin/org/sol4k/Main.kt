package org.sol4k

import org.bitcoinj.core.Base58


fun main() {
    val connection = Connection("https://api.devnet.solana.com")
    val latestBlockhash = connection.getLatestBlockhash()
    val sender = Keypair.fromSecretKey(
        Base58.decode("2WGcYYau2gLu2DUq68SxxXQmCgi77n8hFqqLNbNyg6Xfh2m3tvg8LF5Lgh69CFDux41LUKV1ak1ERHUqiBZnyshz"),
    )
    val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
    val instruction = TransferInstruction(sender.publicKey, receiver, 1000)
    val transaction = Transaction(
        recentBlockhash = latestBlockhash.blockhash,
        instruction = instruction,
        feePayer = sender.publicKey,
    )
    transaction.sign(sender)
    val signature = connection.sendTransaction(transaction)
    println(signature)
}
