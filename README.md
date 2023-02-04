# sol4k [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.sol4k/sol4k/badge.png)](https://search.maven.org/artifact/org.sol4k/sol4k) [![Code Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

Sol4k is a Kotlin client for Solana that can be used with
Java or any other JVM language, as well as in Android.

This project is under development. If you would like to
contribute, please check the open issues or submit
a pull request.


## How to import

Gradle:
```groovy
implementation 'org.sol4k:sol4k:0.1.6'
```

Maven:
```xml
<dependency>
    <groupId>org.sol4k</groupId>
    <artifactId>sol4k</artifactId>
    <version>0.1.6</version>
</dependency>
```

## How to use

Kotlin:
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
