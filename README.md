# sol4k [![build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

Sol4k is a Kotlin client for Solana. It can be used with Java
or any JVM language as well as in Android.

This project is in development. If you want to contribute,
please check opened issues or submit a pull request.

## How to use

```kotlin
val connection = Connection("https://api.devnet.solana.com")
val latestBlockhash = connection.getLatestBlockhash()
val sender = Keypair.fromSecretKey(Base58.decode("2WGc...."))
val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
val instruction = TransferInstruction(sender.publicKey, receiver, 1000)
val transaction = Transaction(
    recentBlockhash = latestBlockhash.blockhash,
    instruction = instruction,
    feePayer = sender.publicKey,
)
transaction.sign(sender)
val signature = connection.sendTransaction(transaction)
```
