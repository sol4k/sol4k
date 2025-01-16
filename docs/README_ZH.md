# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k?tab=readme-ov-file#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어 </a> │ 中文 │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a>

Sol4k 是 Solana 的 Kotlin 客户端，可与 Java 或任何其他 JVM 语言以及 Android 一起使用。它支持与 RPC 节点通信，允许用户从区块链查询信息、创建帐户、从帐户读取数据、发送不同类型的交易以及使用密钥对和公钥。客户端还公开了方便的 API，使开发人员的体验顺畅而直接。

## 如何导入

Gradle:

```groovy
implementation 'org.sol4k:sol4k:0.5.5'
```

Maven:

```xml
<dependency>
    <groupId>org.sol4k</groupId>
    <artifactId>sol4k</artifactId>
    <version>0.5.5</version>
</dependency>
```

## 何如使用

创建连接，请求最新的区块哈希，并提交从一个账户到另一个账户的 SOL 转账交易

```kotlin
val connection = Connection(RpcUrl.DEVNET)
val blockhash = connection.getLatestBlockhash()
val sender = Keypair.fromSecretKey(secretKeyBytes)
val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
val instruction = TransferInstruction(sender.publicKey, receiver, lamports = 1000)
val message = TransactionMessage.newMessage(sender.publicKey, blockhash, instruction)
val transaction = VersionedTransaction(message)
transaction.sign(sender)
val signature = connection.sendTransaction(transaction)
```

检查 [sol4k-examples](https://github.com/sol4k/sol4k-examples) 存储库以查找 立即使用的 sol4k API Java 示例

## 支持的 API:

### 使用密钥对和公钥。

生成密钥对。

```kotlin
val generatedKeypair = Keypair.generate()
```

从现有密钥创建密钥对。

```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

从字符串创建公钥。

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

从字节数组创建公钥。

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

获取SPL令牌的关联令牌帐户地址。

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

将公钥转换为字符串。

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx
```

### Base 58编码

解码数据。

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

编码数据。

```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### 处理签名

签署消息。

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

验证

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```

### RPC 函数功能

RPC 调用是通过“Connection”类执行的，该类公开了镜像的函数
[JSON RPC 方法](https://docs.solana.com/api/http)。
可以创建连接带有 RPC 节点的 HTTP URL 承诺级别。

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

如果没有指定承诺级别，默认使用 FINALIZED（最终确定）。

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

你也可以将 RPC URL 作为字符串传递。

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

需要承诺级别的 RPC 方法将使用在连接创建时指定的承诺级别，
或者可以通过传递承诺级别作为额外参数来覆盖该默认设置。


```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// 一个具有'confirmed' (已确认) 承诺级别的区块哈希。


val blockhash = connection.getLatestBlockhash() 
// 承诺级别被 'finalized'（已最终确定）覆盖。
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

支持的 API:

- `getAccountInfo`：获取账户信息，返回指定账户的详细数据，包括余额、状态等信息。
- `getBalance`：获取指定账户的余额。
- `getEpochInfo`：获取当前epoch的相关信息，包括epoch编号、slot范围等。
- `getHealth`：检查节点的健康状态，通常用于了解节点是否正常运行。
- `getIdentity`：获取节点的身份信息，通常包含节点的公钥和一些其他的元数据。
- `getLatestBlockhash`：获取当前网络最新区块的哈希，用于提交交易时指定区块哈希。
- `getMinimumBalanceForRentExemption`：获取不需要支付租金的账户余额的最小值，即账户需要持有的最低余额，以避免被清除。
- `getTokenAccountBalance`：获取指定代币账户的余额，通常用于获取SPL代币（Solana的代币标准）的余额。
- `getTokenSupply`：获取指定代币的总供应量，显示该代币的发行量。
- `getTransactionCount`：获取指定账户的交易计数（nonce），该计数用于确保交易顺序和避免重放攻击。
- `isBlockhashValid`：验证一个区块哈希是否有效，通常用来检查某个区块哈希是否可以用于提交交易。
- `requestAirdrop`：请求空投指定数量的代币（通常是SOL），用于开发测试目的。
- `sendTransaction`：发送交易，将交易信息提交到网络，通常是进行某些操作（如转账、合约调用等）。
- `simulateTransaction`：模拟执行交易，但不实际提交到网络，用于检查交易是否会成功执行，帮助开发者调试。

### 交易

Sol4k 支持版本化交易和传统交易，分别通过 VersionedTransaction（版本化交易）和 Transaction（传统交易）类来实现这两个类提供了相似的 API，可以用于构建、签名、序列化、反序列化以及发送 Solana 交易。建议在新代码中使用 VersionedTransaction（版本化交易）。交易可以通过指定最新的区块哈希、一个或多个操作指令，以及费用支付者来创建。



```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

具有多个指令的版本化交易：

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

传统交易:

```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction` （指令）是一个接口，要求包含以下数据：

```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```

 `Instruction` （指令）接口有多个实现，例如`TransferInstruction`（转账指令）、
`SplTransferInstruction`（Spl转账指令）、 `CreateAssociatedTokenAccountInstruction`创建关联代币账户指令）和BaseInstruction（基础指令，用于发送任意交易事务）。

`AccountMeta`（账户元数据）是一个类，用于指定在指令中使用的账户的元数据。它需要一个公钥和两个布尔值：`signer`（签名者）和 `writable`（可写）。



```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```

它还提供了三个方法，用于构造具有不同属性组合的对象。

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

这是将所有内容组合在一起的示例：创建一个向游戏程序发送信息的交易。


```kotlin
val instructionData = byteArrayOf(-3, -42, 48, -55, 100, -55, -29, -37) 
val accounts = listOf(
    AccountMeta.writable(gameAccount),
    AccountMeta.signerAndWritable(playerPublicKey)
)
val joinGameInstruction = BaseInstruction(instructionData, accounts, programId)
val blockhash = connection.getLatestBlockhash()
val joinGameMessage = TransactionMessage.newMessage(playerPublicKey, blockhash, joinGameInstruction)
val joinGameTransaction = VersionedTransaction(joinGameMessage)
joinGameTransaction.sign(playerKeypair)
val signature = connection.sendTransaction(joinGameTransaction)
```

这是另一个示例：为用户的钱包创建一个关联代币账户。

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
val message = TransactionMessage.newMessage(payerWallet.publicKey, blockhash, instruction)
val transaction = Transaction(message)
transaction.sign(payerWallet)
val signature = connection.sendTransaction(transaction)
```

你可以在[项目的测试中]找到更多示例。
(https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt).

## 开发环境设置

为了在本地构建和运行 sol4k，请执行以下步骤：

- 安装 JDK 11 或更高版本作为默认 JDK 版本（运行 `java --version` 应显示 11 或更高版本）。
- 安装 JDK 8，但不要将其设置为默认版本。如果你使用的是 macOS，可以通过运行 `/usr/libexec/java_home -V`来查看 Java 8 是否在列表中。
- 执行 `./gradlew publishToMavenLocal`.

这将把 sol4k 安装到你本地的 Maven 仓库中，你可以在其他项目中导入它
`mavenLocal` is among [ 你的 Maven 仓库](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14)).
在 `gradle.properties` 文件中调整 `currentVersion`，以确保你导入的是你构建的版本。



为了运行端到端测试，你需要设置两个环境变量：
一个 RPC URL和一个用于测试的账户的 Base-58 编码的密钥。

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

执行测试：

```shell
./gradlew integrationTest
```

账户需要有一些 Devnet SOL 和 Devnet USDC 才能运行测试。你可以使用 [这个水龙头](https://spl-token-faucet.com/?token-name=USDC-Dev) 来空投这两者。

如果你想为测试生成一个新账户，可以按照以下方式操作：

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

如果没有设置环境变量，端到端测试将使用`EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF` 与区块链进行交互。该账户的私钥在源代码中是公开的，因此如果你打算使用该账户进行测试，请确保它已经拥有 Devnet USDC 和 SOL。

## 支持我们

如果你喜欢 sol4k 并希望这个项目继续发展，可以考虑通过
[GitHub Sponsors](https://github.com/sponsors/Shpota) 进行赞助，或者直接向以下钱包地址捐赠：

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
