# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k?tab=readme-ov-file#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ 日本語

Sol4kは、Javaや他のJVM言語、Androidでも使用できるSolana用のKotlinクライアントです。このクライアントを使用すると、RPCノードと通信し、ブロックチェーンから情報をクエリし、アカウントを作成し、データを読み取り、さまざまな種類のトランザクションを送信し、キーペアや公開鍵を操作できます。また、開発者がスムーズかつ簡単に作業できるように便利なAPIも提供しています。

## インポート方法

Gradle:
```groovy
implementation 'org.sol4k:sol4k:0.5.17'
```

Maven:
```xml
<dependency>
    <groupId>org.sol4k</groupId>
    <artifactId>sol4k</artifactId>
    <version>0.5.17</version>
</dependency>
```

## 使用方法

接続を作成し、最新のブロックハッシュをリクエストし、1つのアカウントから別のアカウントにSOL転送トランザクションを送信します。
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

sol4k APIのJavaの例を見つけるには、[sol4k-examples](https://github.com/sol4k/sol4k-examples)リポジトリを参照してください。

## API

### キーペアと公開鍵の操作

キーペアの生成。
```kotlin
val generatedKeypair = Keypair.generate()
```

既存の秘密鍵からキーペアを作成。
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

文字列から公開鍵を作成。

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

バイト配列から公開鍵を作成。

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

SPLトークンの関連トークンアカウントアドレスを取得。

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

公開鍵を文字列に変換。

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx
```

### Base 58エンコーディング

データのデコード。

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

データのエンコード。

```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### 署名の操作

メッセージの署名。

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

検証。

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```


### RPC関数

RPC呼び出しは、`Connection`クラスを介して行われ、このクラスは[JSON RPCメソッド](https://docs.solana.com/api/http)を反映する関数を公開します。接続は、HTTP URLとコミットメントを使用して作成できます。

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

コミットメントが指定されていない場合、デフォルトで`FINALIZED`が使用されます。

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

RPC URLを文字列として渡すこともできます。

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

コミットメントが必要なRPCメソッドは、接続作成時に指定されたコミットメントを使用するか、追加の引数としてコミットメントを渡して上書きできます。

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// 'confirmed'コミットメントのブロックハッシュ
val blockhash = connection.getLatestBlockhash() 
// コミットメントは'finalized'で上書きされます
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

サポートされているAPI:
- `getAccountInfo`
- `getBalance`
- `getEpochInfo`
- `getHealth`
- `getIdentity`
- `getLatestBlockhash`
- `getMinimumBalanceForRentExemption`
- `getMultipleAccounts`
- `getTokenAccountBalance`
- `getTokenSupply`
- `getTransactionCount`
- `isBlockhashValid`
- `requestAirdrop`
- `sendTransaction`
- `simulateTransaction`

### トランザクション

sol4kは、`VersionedTransaction`および`Transaction`クラスを使用して、バージョン化されたトランザクションとレガシートランザクションをサポートします。両方のクラスは類似したAPIをサポートしており、Solanaトランザクションの構築、署名、シリアル化、逆シリアル化、および送信に使用できます。新しいコードでは`VersionedTransaction`を使用することをお勧めします。トランザクションは、最新のブロックハッシュ、1つ以上の命令、および手数料支払者を指定して作成できます。

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

複数の命令を持つバージョン化されたトランザクション:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

レガシートランザクション:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction`は、次のデータを必要とするインターフェースです:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
`Instruction`インターフェースには、`TransferInstruction`、`SplTransferInstruction`、`CreateAssociatedTokenAccountInstruction`、および任意のトランザクションを送信するために使用される`BaseInstruction`などのいくつかの実装があります。

`AccountMeta`は、命令で使用されるアカウントのメタデータを指定できるクラスです。公開鍵と2つのboolean値（`signer`および`writable`）が必要です。

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
異なるプロパティの組み合わせでオブジェクトを作成するための3つの便利な関数も提供しています。

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

すべてを組み合わせた例: ゲームプログラムに情報を送信するトランザクションの作成。

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

別の例: ユーザーのウォレットに関連するトークンアカウントを作成する。

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

[プロジェクトテスト](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt)でさらに多くの例を見つけることができます。

## 開発環境の設定

sol4kをローカルでビルドして実行するには、次の手順を実行します:
- デフォルトのJDKバージョンとしてJDK 11以上をインストールします（`java --version`を実行して11以上が表示されることを確認）。
- JDK 8をインストールしますが、デフォルトとして選択しません。macOSを使用している場合は、`/usr/libexec/java_home -V`を実行してJava 8がリストにあるか確認できます。
- `./gradlew publishToMavenLocal`を実行します。

これにより、sol4kがローカルのMavenリポジトリにインストールされ、他のプロジェクトでインポートできるようになります（[Mavenリポジトリ](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14)に`mavenLocal`が含まれていることを確認してください）。`gradle.properties`のcurrentVersionを調整して、ビルドしたバージョンをインポートしていることを確認してください。

エンドツーエンドテストを実行するには、2つの環境変数を設定する必要があります: RPC URLとテストに使用するアカウントのBase-58でエンコードされた秘密鍵です。

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

テストを実行します:

```shell
./gradlew integrationTest
```

テストを実行するには、アカウントにDevnet SOLとDevnet USDCが必要です。どちらもこの[faucet](https://spl-token-faucet.com/?token-name=USDC-Dev)を使用して受け取ることができます。

テスト用に新しいアカウントを作成する場合は、次のように行うことができます:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

環境変数が設定されていない場合、エンドツーエンドテストは`EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF`を使用してブロックチェーンと対話します。この秘密鍵はソースコードで公開されているため、これを使用する場合はDevnet USDCとSOLがあることを確認してください。

## サポート

sol4kが気に入ってプロジェクトを継続してほしい場合は、[GitHub Sponsors](https://github.com/sponsors/Shpota)を通じてスポンサーになるか、次のウォレットアドレスに直接寄付を検討してください:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
