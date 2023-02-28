# sol4k [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.sol4k/sol4k/badge.svg)](https://search.maven.org/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

Sol4k is a Kotlin client for Solana that can be used with
Java or any other JVM language, as well as in Android.
Sol4k allows you to perform operations with transactions
such as creating a transaction, signing it, and sending it
to the blockchain. It can communicate with a blockchain node
and request a blockhash or a wallet balance.
It also lets you generate key pairs, public keys, encode and
decode them in the Base 58 format. Besides, it exposes convenient
APIs for creating instructions, passing account metadata,
and performing byte operations.

## How to import

Gradle:
```groovy
implementation 'org.sol4k:sol4k:0.1.15'
```

Maven:
```xml
<dependency>
    <groupId>org.sol4k</groupId>
    <artifactId>sol4k</artifactId>
    <version>0.1.15</version>
</dependency>
```

## How to use

Create a connection, request the latest blockhash, and submit
a SOL transfer transaction from one account to another
```kotlin
val connection = Connection("https://api.devnet.solana.com")
val latestBlockhash = connection.getLatestBlockhash()
val sender = Keypair.fromSecretKey(Base58.decode("2WGc...."))
val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
val instruction = TransferInstruction(sender.publicKey, receiver, lamports = 1000)
val transaction = Transaction(
    recentBlockhash = latestBlockhash.blockhash,
    instruction = instruction,
    feePayer = sender.publicKey,
)
transaction.sign(sender)
val signature = connection.sendTransaction(transaction)
```
Check the [sol4k-demo](https://github.com/sol4k/sol4k-demo) to see a Java example
of transferring SOL.

## Further development

The following functionality will be added in the future:
 - WebSocket APIs
 - More RPC functions
 - Account Management APIs

## Notes

This project is under development. If you would like to
contribute, please check the open issues or submit
a pull request.

## Contacts

If you have any questions reach out to email `sasha@shpota.com`. 

