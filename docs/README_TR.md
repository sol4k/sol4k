# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k/blob/main/docs/README.md#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_HI.md#sol4k----">हिंदी</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_UR.md#sol4k----">اردو</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_AR.md#sol4k----">العربية</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ID.md#sol4k----">Indonesian</a> │ Türkçe │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ES.md#sol4k----">Español</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_PT.md#sol4k----">Português</a>

Sol4k, Solana için Java veya başka herhangi bir JVM diliyle ve ayrıca Android üzerinde kullanılabilen bir Kotlin istemcisidir. Bir RPC düğümüyle iletişim kurmayı sağlar; böylece kullanıcılar blokzincirden bilgi sorgulayabilir, hesaplar oluşturabilir, bunlardan veri okuyabilir, farklı türde işlemler gönderebilir ve anahtar çiftleri ile genel anahtarlarla çalışabilir. İstemci ayrıca geliştirici deneyimini sorunsuz ve basit hale getirmek için kullanışlı API'ler sunar.

## Nasıl içe aktarılır

Gradle:
```groovy
implementation 'org.sol4k:sol4k:0.8.1'
```

Maven:
```xml
<dependency>
    <groupId>org.sol4k</groupId>
    <artifactId>sol4k</artifactId>
    <version>0.8.1</version>
</dependency>
```

## Nasıl kullanılır

Bir bağlantı oluşturun, en son blockhash'i isteyin ve bir hesaptan diğerine bir SOL transfer işlemi gönderin
```kotlin
val connection = Connection(RpcUrl.DEVNET)
val blockhash = connection.getLatestBlockhash()
val sender = Keypair.fromSecretKey(secretKeyBytes)
val receiver = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
val instruction = TransferInstruction(sender.publicKey, receiver, lamports = 1000)
val message = TransactionMessage.newMessage(sender.publicKey, blockhash, instruction)
val transaction = VersionedTransaction(message)
transaction.sign(sender)
val signature = connection.sendTransaction(transaction)
```

Hazır kullanılabilir sol4k API'leri için Java örneklerini bulmak üzere [sol4k-examples](https://github.com/sol4k/sol4k-examples) deposuna bakın.

## API'ler

### Anahtar çiftleri ve genel anahtarlarla çalışma.

Bir anahtar çifti oluşturma.
```kotlin
val generatedKeypair = Keypair.generate()
```

Mevcut bir gizli anahtardan anahtar çifti oluşturma.
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

Bir string'ten genel anahtar oluşturma.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

Bir byte dizisinden genel anahtar oluşturma.

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

Bir SPL token'ı için ilişkili token hesabı adresi edinme.

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

Bir genel anahtarı string'e dönüştürme.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
```

### Base 58 kodlama

Veri çözme.

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

Veri kodlama.
```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### İmzalarla çalışma

Bir mesajı imzalama.

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

Doğrulama.

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```

### RPC fonksiyonları

RPC çağrıları, [JSON RPC yöntemlerini](https://docs.solana.com/api/http) yansıtan fonksiyonlar sunan bir `Connection` sınıfı aracılığıyla gerçekleştirilir. Bir bağlantı, bir RPC düğümünün HTTP URL'si ve bir commitment ile oluşturulabilir.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

Eğer commitment belirtilmezse, varsayılan olarak `FINALIZED` kullanılır.

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

RPC URL'sini bir string olarak da geçirebilirsiniz.

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

Commitment gerektiren RPC yöntemleri, bağlantı oluşturma sırasında belirtilen commitment'i kullanır veya ek bir argüman olarak commitment geçirerek geçersiz kılınabilir.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// 'confirmed' commitment'lı bir blockhash
val blockhash = connection.getLatestBlockhash() 
// commitment 'finalized' ile geçersiz kılınır
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

Desteklenen API'ler:
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

### İşlemler

Sol4k, sırasıyla `VersionedTransaction` ve `Transaction` sınıflarıyla sürümlemeli ve eski (legacy) işlemleri destekler. Her iki sınıf da benzer API'leri destekler ve Solana işlemlerini oluşturmak, imzalamak, serileştirmek, seri durumdan çıkarmak ve göndermek için kullanılabilir. Yeni kodda `VersionedTransaction` kullanılması önerilir. Bir işlem, en son blockhash, bir veya birden fazla talimat ve bir ücret ödeyici belirtilerek oluşturulabilir.

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

Birden fazla talimat içeren sürümlemeli bir işlem:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

Eski işlem:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction`, aşağıdaki verilere sahip olmayı gerektiren bir arayüzdür:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
`Instruction` arayüzünün `TransferInstruction`, `SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction` ve `BaseInstruction` (rastgele işlemler göndermek için kullanılan) gibi çeşitli uygulamaları vardır.

`AccountMeta`, bir talimatta kullanılan hesaplar için meta veri belirtmenizi sağlayan bir sınıftır. Bir genel anahtar ve iki boolean değer gerektirir: `signer` ve `writable`.

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
Ayrıca farklı özellikk kombinasyonlarına sahip nesneler oluşturmak için üç uygunluk fonksiyonu vardır.

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

Her şeyi bir araya getiren bir örnek: bir oyun programına bilgi gönderen bir işlem oluşturma.

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

İşte bir diğer örnek: bir kullanıcının cüzdanı için ilişkili bir token hesabı oluşturma.

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

Daha fazla örneği [projenin testlerinde](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt) bulabilirsiniz.

## Geliştirme kurulumu

Sol4k'yı yerelde derlemek ve çalıştırmak için aşağıdaki adımları izleyin:
- Varsayılan JDK sürümü olarak JDK 11 veya daha yenisini yükleyin (`java --version` çalıştırıldığında 11 veya daha yenisi yazdırılmalıdır).
- JDK 8'i yükleyin, ancak onu varsayılan olarak seçmeyin. macOS kullanıyorsanız, `/usr/libexec/java_home -V` çalıştırarak listede Java 8'i kontrol edebilirsiniz.
- `./gradlew publishToMavenLocal` komutunu çalıştırın.

Bu, sol4k'yı yerel Maven deponuza yükler ve onu diğer projelerde içe aktarabilmenizi sağlar ([Maven depolarınız](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14) arasında `mavenLocal`'ın olduğundan emin olun). Hangi sürümü derlediğinizden emin olmak için `gradle.properties` içindeki `currentVersion`'ı ayarlayın.

Uçtan uca testleri çalıştırmak için iki ortam değişkeni ayarlamanız gerekir: bir RPC URL'si ve test için kullanacağınız bir hesabın Base-58 kodlu gizli anahtarı.

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

Testleri çalıştırın:

```shell
./gradlew integrationTest
```

Hesabın, testleri çalıştırmak için biraz Devnet SOL'una ve ayrıca Devnet USDC'sine sahip olması gerekir. İkisine de [bu faucet](https://spl-token-faucet.com/?token-name=USDC-Dev) kullanarak airdrop yapabilirsiniz.

Test için yeni bir hesap oluşturmak istiyorsanız, bunu şu şekilde yapabilirsiniz:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

Ortam değişkenleri ayarlanmazsa, uçtan uca testler blokzincirle etkileşim kurmak için `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF` adresini kullanır. Gizli anahtarı kaynak kodunda herkese açık olarak mevcuttur; bu nedenle ona güvenmek istiyorsanız Devnet USDC ve SOL'a sahip olduğundan emin olun.

## Destek

Eğer sol4k'yı beğeniyorsanız ve projenin devam etmesini istiyorsanız, [GitHub Sponsors](https://github.com/sponsors/Shpota) üzerinden veya doğrudan cüzdan adresine bağış yaparak sponsor olmayı düşünün:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
