# sol4k [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.sol4k/sol4k/badge.svg)](https://search.maven.org/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

Sol4k is a Kotlin client for Solana that can be used with
Java or any other JVM language, as well as in Android.
It allows you to communicate with an RPC node, query the information
from the blockchain, create accounts and read data from them, create transactions
for transferring SOL or SPL, or create arbitrary
transactions and send them to the RPC node.
Sol4k also lets you generate key pairs, public keys, encode and
decode them in the Base 58 format. Besides, it exposes
convenient APIs to make the developer experience smooth and straight forward.

## How to import

Gradle:
```groovy
implementation 'org.sol4k:sol4k:0.2.1'
```

Maven:
```xml
<dependency>
    <groupId>org.sol4k</groupId>
    <artifactId>sol4k</artifactId>
    <version>0.2.1</version>
</dependency>
```

## How to use

Create a connection, request the latest blockhash, and submit
a SOL transfer transaction from one account to another
```kotlin
val connection = Connection("https://api.devnet.solana.com")
val (blockhash) = connection.getLatestBlockhash()
val sender = Keypair.fromSecretKey(secretKeyBytes)
val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
val instruction = TransferInstruction(sender.publicKey, receiver, lamports = 1000)
val transaction = Transaction(blockhash, instruction, feePayer = sender.publicKey)
transaction.sign(sender)
val signature = connection.sendTransaction(transaction)
```
Check the [sol4k-demo](https://github.com/sol4k/sol4k-demo) to see other examples.

## APIs and examples

### Working with key pairs and public keys.

Generating a keypair.
```kotlin
val generatedKeypair = Keypair.generate()
```

Creating a keypair from an existing secret.
```kotlin
val secretKey: ByteArray = ...
val keypairFromSecretKey = Keypair.fromSecretKey(secretKey)
```

Creating a public key from string.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

Creating a public key from a byte array.

```kotlin
val publicKeyBytes: ByteArray = ...
val publicKey = PublicKey(publicKeyBytes)
```

Obtaining an associated token account address for an SPL token.

```kotlin
val holderAddress: PublicKey = ...
val tokenMintAddress: PublicKey = ...
val keypairFromSecretKey = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
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

### RPC functions

RPC calls are performed via a `Connection` class that exposes functions that mirror
[the JSON RPC methods](https://docs.solana.com/api/http). A connection can be created
with an HTTP URL of an RPC node and a commitment.

```kotlin
val connection = Connection("https://api.devnet.solana.com", Commitment.PROCESSED)
```

If commitment is not specified, `FINALIZED` is used by default.

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

RPC methods that require commitment will use the one specified during the connection
creation or can be overridden passing commitment as an additional argument.

```kotlin
val connection = Connection("https://api.devnet.solana.com", Commitment.CONFIRMED)
// a blockhash with the 'confirmed' commitment
val blockhash = connection.getLatestBlockhash() 
// commitment is overridden by 'finalized'
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

Supported APIs:
- `getAccountInfo`
- `getBalance`
- `getHealth`
- `getIdentity`
- `getLatestBlockhash`
- `getTokenAccountBalance`
- `isBlockhashValid`
- `requestAirdrop`
- `sendTransaction`
- `simulateTransaction`

### Transactions

A sol4k Transaction is a class that can be used to building, signing, serializing, and sending
Solana transactions. Transaction can be created specifying the latest blockhash, one or several
instructions, and a fee payer.

```kotlin
val transaction = Transaction(blockhash, instruction, feePayer)
```

A transaction with multiple instructions:
```kotlin
val instructions: List<Instruction> = ...
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
The `Instaruction`interface has several implementations such as `TransferInstruction`,
`SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction`, and `BaseInstruction`
(the one used for sending arbitrary transactions).

`AccountMeta` is a class that lets you specify metadata for the accounts used in an instruction.
It requires a public key, and two boolean values: `signer` and `writable`.

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
It also has three convenience functions to construct object with different combinations of
properties.

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

Example of combining all together. Here a transactions is created that sends information
to a game program. 

```kotlin
val instructionData = byteArrayOf(-3, -42, 48, -55, 100, -55, -29, -37) 
val accounts = listOf(
    AccountMeta.writable(gameAccount),
    AccountMeta.signerAndWritable(playerPublicKey)
)
val joinGameInstruction = BaseInstruction(instructionData, accounts, programId)
val (blockhash) = connection.getLatestBlockhash()
val joinGameTransaction = Transaction(
    blockhash,
    instruction = joinGameInstruction,
    feePayer = playerPublicKey,
)
joinGameTransaction.sign(playerKeypair)
val signature = connection.sendTransaction(joinGameTransaction)
```

Another example. Here an associated token account is created for a users' wallet.
```kotlin
val (blockhash) = connection.getLatestBlockhash()
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

## Contacts

If you have any questions reach out to email `contact@sol4k.org`. 

