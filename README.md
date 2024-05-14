# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

Sol4k is a Kotlin client for Solana that can be used with Java or any other JVM
language, as well as on Android. It enables communication with an RPC node,
allowing users to query information from the blockchain, create accounts, read
data from them, send different types of transactions, and work with key pairs
and public keys. The client also exposes convenient APIs to make the developer
experience smooth and straightforward.

## How to import

Gradle:
```groovy
implementation 'org.sol4k:sol4k:0.4.2'
```

Maven:
```xml
<dependency>
    <groupId>org.sol4k</groupId>
    <artifactId>sol4k</artifactId>
    <version>0.4.2</version>
</dependency>
```

## How to use

Create a connection, request the latest blockhash, and submit
a SOL transfer transaction from one account to another
```kotlin
val connection = Connection(RpcUrl.DEVNET)
val blockhash = connection.getLatestBlockhash()
val sender = Keypair.fromSecretKey(secretKeyBytes)
val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
val instruction = TransferInstruction(sender.publicKey, receiver, lamports = 1000)
val transaction = Transaction(blockhash, instruction, feePayer = sender.publicKey)
transaction.sign(sender)
val signature = connection.sendTransaction(transaction)
```

Check the [sol4k-examples](https://github.com/sol4k/sol4k-examples) repository to find
ready-to-use Java examples of sol4k APIs.

## APIs

### Working with key pairs and public keys.

Generating a keypair.
```kotlin
val generatedKeypair = Keypair.generate()
```

Creating a keypair from an existing secret.
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

Creating a public key from string.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

Creating a public key from a byte array.

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

Obtaining an associated token account address for an SPL token.

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

Converting a public key to a string.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx
```

### Base 58 encoding

Decoding data.

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

Encoding data.
```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### Working with signatures

Signing a message.

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

Verifying.

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```


### RPC functions

RPC calls are performed via a `Connection` class that exposes functions that mirror
[the JSON RPC methods](https://docs.solana.com/api/http). A connection can be created
with an HTTP URL of an RPC node and a commitment.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

If commitment is not specified, `FINALIZED` is used by default.

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

You can also pass RPC URL as a string.

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

RPC methods that require commitment will use the one specified during the connection
creation or can be overridden by passing commitment as an additional argument.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// a blockhash with the 'confirmed' commitment
val blockhash = connection.getLatestBlockhash() 
// commitment is overridden by 'finalized'
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

Supported APIs:
- `getAccountInfo`
- `getBalance`
- `getEpochInfo`
- `getHealth`
- `getIdentity`
- `getLatestBlockhash`
- `getTokenAccountBalance`
- `getTransactionCount`
- `isBlockhashValid`
- `requestAirdrop`
- `sendTransaction`
- `simulateTransaction`

### Transactions

A sol4k Transaction is a class that can be used to build, sign, serialize,
and send Solana transactions. A transaction can be created by specifying
the latest blockhash, one or several instructions, and a fee payer.

```kotlin
val transaction = Transaction(blockhash, instruction, feePayer)
```

A transaction with multiple instructions:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instaruction` is an interface that requires having the following data:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
The `Instaruction` interface has several implementations such as `TransferInstruction`,
`SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction`, and `BaseInstruction`
(the one used for sending arbitrary transactions).

`AccountMeta` is a class that lets you specify metadata for the accounts used in an instruction.
It requires a public key and two boolean values: `signer` and `writable`.

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
It also has three convenience functions to construct objects with different combinations of
properties.

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

Here's an example of combining everything together: creating a transaction that sends
information to a game program.

```kotlin
val instructionData = byteArrayOf(-3, -42, 48, -55, 100, -55, -29, -37) 
val accounts = listOf(
    AccountMeta.writable(gameAccount),
    AccountMeta.signerAndWritable(playerPublicKey)
)
val joinGameInstruction = BaseInstruction(instructionData, accounts, programId)
val blockhash = connection.getLatestBlockhash()
val joinGameTransaction = Transaction(
    blockhash,
    instruction = joinGameInstruction,
    feePayer = playerPublicKey,
)
joinGameTransaction.sign(playerKeypair)
val signature = connection.sendTransaction(joinGameTransaction)
```

Here's another example: creating an associated token account for a user's wallet.

```kotlin
val blockhash = connection.getLatestBlockhash()
val payerWallet = Keypair.fromSecretKey(secretKeyBytes)
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
```

You can find more examples [in the project tests](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt).

## Notes

This project is actively developed. If you would like to
contribute, please check the open issues or submit
a pull request.

## Development setup

In order to build and run sol4k locally, perform the next steps:
- Install JDK 11 or newer as a default version of JDK (running `java --version` should print 11 or newer).
- Install JDK 8, but do not select it as a default. If you're using macOS, you can check for Java 8 in the list by running `/usr/libexec/java_home -V`.
- Execute `./gradlew publishToMavenLocal -x signMavenPublication`.

This would install sol4k in your local Maven repository, and you would be able to import it in other projects (make sure
`mavenLocal` is among [your Maven repositories](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14)).
Adjust `currentVersion` in `gradle.properties` to be sure you are importing the version that
you have built.

In order to run end-to-end tests, you need to set two environment variables: an RPC URL and a Base-58 encoded
secret key of an account that you would be using for testing.

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

Execute the tests:

```shell
./gradlew integrationTest
```

The account needs to have some Devnet SOL as well as Devnet USDC in order to run the tests.
You can aridrop them both using [this faucet](https://spl-token-faucet.com/?token-name=USDC-Dev).

If you want to generate a new account for testing, you can do it like this:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

If no environment variables are set, end-to-end tests would use `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF`
to interact with the blockchain. Its secret key is publicly available in the source code, that's why make sure
it has Devnet USDC and SOL if you want to rely on it.

## Contacts

If you have any questions reach out to email `contact@sol4k.org`.
