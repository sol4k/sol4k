# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE) [![Twitter](https://img.shields.io/twitter/follow/_sol4k?style=flat&color=green&logo=x&logoColor=white)](https://x.com/_sol4k)

<a href="https://github.com/sol4k/sol4k?tab=readme-ov-file#sol4k----">English</a> │ 한국어 │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a>

Sol4k는 Java 또는 다른 JVM 언어뿐만 아니라 Android에서도 사용할 수 있는 Solana용 Kotlin 클라이언트입니다. 이 클라이언트를 사용하여 RPC 노드와 통신하고 블록체인에서 정보를 쿼리하며, 계정을 생성하고 데이터를 읽고, 다양한 유형의 트랜잭션을 전송하고, 키 쌍 및 공개 키와 작업할 수 있습니다. 또한 개발자가 매끄럽고 간편하게 작업할 수 있도록 편리한 API도 제공합니다.

## 가져오는 방법

Gradle:
```groovy
implementation 'org.sol4k:sol4k:0.7.0'
```

Maven:
```xml
<dependency>
    <groupId>org.sol4k</groupId>
    <artifactId>sol4k</artifactId>
    <version>0.7.0</version>
</dependency>
```

## 사용 방법

연결을 생성하고 최신 블록 해시를 요청한 후, 한 계정에서 다른 계정으로 SOL 전송 트랜잭션을 제출합니다.
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

sol4k API의 Java 예제를 찾으려면 [sol4k-examples](https://github.com/sol4k/sol4k-examples) 저장소를 참조하십시오.

## API들

### 키 쌍 및 공개 키 작업

키 쌍 생성.
```kotlin
val generatedKeypair = Keypair.generate()
```

기존 비밀키에서 키 쌍 생성.
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

문자열에서 공개 키 생성.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

바이트 배열에서 공개 키 생성.

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

SPL 토큰의 관련 토큰 계정 주소 얻기.

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

공개 키를 문자열로 변환.


```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx
```

### Base 58 인코딩

데이터 디코딩.

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")
```

데이터 인코딩.

```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### 서명 작업

메시지 서명.

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

검증.

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```


### RPC 함수

RPC 호출은 [JSON RPC 메서드](https://docs.solana.com/api/http)를 반영하는 함수를 노출하는 `Connection` 클래스를 통해 수행됩니다. HTTP URL과 커밋을 사용하여 연결을 생성할 수 있습니다.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

커밋을 지정하지 않으면 기본적으로 `FINALIZED`가 사용됩니다.

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

RPC URL을 문자열로 전달할 수도 있습니다.

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

커밋이 필요한 RPC 메서드는 연결 생성 시 지정된 커밋을 사용하거나 추가 인수로 커밋을 전달하여 재정의할 수 있습니다.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// a blockhash with the 'confirmed' commitment
val blockhash = connection.getLatestBlockhash() 
// commitment is overridden by 'finalized'
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

지원되는 API들:
- `getAccountInfo`
- `getBalance`
- `getEpochInfo`
- `getFeeForMessage`
- `getHealth`
- `getIdentity`
- `getLatestBlockhash`
- `getMinimumBalanceForRentExemption`
- `getMultipleAccounts`
- `getRecentPrioritizationFees`
- `getTokenAccountBalance`
- `getTokenSupply`
- `getTransactionCount`
- `getVersion`
- `isBlockhashValid`
- `requestAirdrop`
- `sendTransaction`
- `simulateTransaction`

### 트랜잭션

Sol4k는 `VersionedTransaction` 및 `Transaction` 클래스를 통해 버전화된 트랜잭션과 레거시 트랜잭션을 지원합니다. 두 클래스는 유사한 API를 지원하며 Solana 트랜잭션을 생성, 서명, 직렬화, 역직렬화 및 전송하는 데 사용할 수 있습니다. 새로운 코드에서는 `VersionedTransaction` 을 사용하는 것이 권장됩니다. 트랜잭션은 최신 블록해시, 하나 이상의 명령어 및 수수료 지불자를 지정하여 생성할 수 있습니다.

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

다중 명령어 버전화된 트랜잭션:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

레거시 트랜잭션:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`명령어`는 다음 데이터를 필요로 하는 인터페이스입니다:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
`명령어` 인터페이스에는 `TransferInstruction`, `SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction` 및 임의의 트랜잭션을 보내는 데 사용되는 `BaseInstruction`과 같은 여러 구현이 있습니다.

`AccountMeta`는 명령어에서 사용되는 계정의 메타데이터를 지정할 수 있는 클래스입니다. 공개 키와 두 개의 boolean 값(`signer` 및 `writable`)을 필요로 합니다.

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
다른 속성 조합으로 객체를 생성하는 세 가지 편리한 함수도 제공합니다.

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

다음을 모두 결합한 예: 게임 프로그램에 정보를 보내는 트랜잭션 생성.

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

다음은 다른 예제 입니다: 사용자의 지갑에 연관된 토큰 계정 생성.

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
val signature = connection.sendTransaction(transaction)
```

[프로젝트 테스트](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt)에서 더 많은 예제를 찾을 수 있습니다.

## 개발 설정

sol4k를 로컬에서 빌드하고 실행하려면 다음 단계를 수행하십시오:
- 기본 JDK 버전으로 JDK 11 이상을 설치합니다(`java --version`을 실행하여 11 이상이 출력되는지 확인).
-  JDK 8을 설치하지만 기본값으로 선택하지 않습니다. macOS를 사용하는 경우 `/usr/libexec/java_home -V`를 실행하여 Java 8이 목록에 있는지 확인할 수 있습니다.
-  `./gradlew publishToMavenLocal`을 실행합니다.

이렇게 하면 sol4k가 로컬 Maven 저장소에 설치되며 다른 프로젝트에서 가져올 수 있습니다
([당신의 Maven 저장소에](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14)`mavenLocal`이 포함되어 있는지 확인하십시오).
`gradle.properties`의 currentVersion을 조정하여 빌드한 버전을 가져오고 있는지 확인하십시오.

종단 간 테스트를 실행하려면 두 개의 환경 변수를 설정해야 합니다: RPC URL과 테스트에 사용할 계정의 Base-58로 인코딩된 비밀 키입니다.

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

테스트를 실행합니다:

```shell
./gradlew integrationTest
```

테스트를 실행하려면 계정에 Devnet SOL과 Devnet USDC가 필요합니다.
둘 다 이[faucet](https://spl-token-faucet.com/?token-name=USDC-Dev)을 사용하여 받을 수 있습니다..

테스트용으로 새 계정을 생성하려면 다음과 같이 할 수 있습니다:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

환경 변수가 설정되지 않은 경우 종단 간 테스트는 `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF`를 사용하여 블록체인과 상호작용합니다. 이 비밀 키는 소스 코드에 공개되어 있으므로 이를 사용하려면 Devnet USDC와 SOL이 있는지 확인하십시오.

## Support

If you like sol4k and want the project to keep going, consider sponsoring it
[via GitHub Sponsors](https://github.com/sponsors/Shpota) or directly to the wallet address:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
