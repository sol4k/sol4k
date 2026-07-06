# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k/blob/main/docs/README.md#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_HI.md#sol4k----">हिंदी</a> │ اردو │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_AR.md#sol4k----">العربية</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ID.md#sol4k----">Indonesian</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_TR.md#sol4k----">Türkçe</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ES.md#sol4k----">Español</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_PT.md#sol4k----">Português</a>

Sol4k، Solana کے لیے ایک Kotlin کلائنٹ ہے جسے Java یا کسی بھی دیگر JVM زبان کے ساتھ ساتھ Android پر بھی استعمال کیا جا سکتا ہے۔ یہ RPC نوڈ کے ساتھ مواصلات کی سہولت فراہم کرتا ہے، جس سے صارفین بلاک چین سے معلومات حاصل کر سکتے ہیں، اکاؤنٹس بنا سکتے ہیں، ان سے ڈیٹا پڑھ سکتے ہیں، مختلف اقسام کے لین دین بھیج سکتے ہیں اور کی ۔جوڑیوں (key pairs) اور پبلک کیز (public keys) کے ساتھ کام کر سکتے ہیں۔ یہ کلائنٹ ڈویلپر کے تجربے کو ہموار اور سیدھا بنانے کے لیے آسان API بھی فراہم کرتا ہے۔

## امپورٹ (Import) کیسے کریں

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

## استعمال (Use) کیسے کریں

ایک کنکشن بنائیں، تازہ ترین blockhash کی درخواست کریں اور ایک اکاؤنٹ سے دوسرے اکاؤنٹ میں SOL ٹرانسفر لین دین بھیجیں
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

sol4k API کی تیار استعمال Java مثالوں کے لیے [sol4k-examples](https://github.com/sol4k/sol4k-examples) ریپوزٹری دیکھیں۔

## API

### کی ۔جوڑیوں (key pairs) اور پبلک کیز (public keys) کے ساتھ کام کرنا۔

کی ۔جوڑی بنانا۔
```kotlin
val generatedKeypair = Keypair.generate()
```

موجودہ سیکرٹ (secret) سے کی ۔جوڑی بنانا۔
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

ایک سٹرنگ سے پبلک کی بنانا۔

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

بائٹ ایرے سے پبلک کی بنانا۔

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

SPL ٹوکن کے لیے منسلک ٹوکن اکاؤنٹ ایڈریس حاصل کرنا۔

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

پبلک کی کو سٹرنگ میں تبدیل کرنا۔

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
```

### Base 58 انکوڈنگ

ڈیٹا ڈیکوڈ کرنا۔

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

ڈیٹا انکوڈ کرنا۔
```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### دستخط (signatures) کے ساتھ کام کرنا

ایک پیغام پر دستخط کرنا۔

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

تصدیق (verifying)۔

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```

### RPC فنکشنز

RPC کالز `Connection` کلاس کے ذریعے کی جاتی ہیں، جو [JSON RPC methods](https://docs.solana.com/api/http) کو عکس (mirror) کرنے والے فنکشنز کو ظاہر کرتی ہے۔ RPC نوڈ کے HTTP URL اور ایک commitment کے ساتھ ایک کنکشن بنایا جا سکتا ہے۔

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

اگر commitment کی وضاحت نہیں کی گئی ہے، تو ڈیفالٹ طور پر `FINALIZED` استعمال کیا جاتا ہے۔

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

آپ RPC URL کو سٹرنگ کے طور پر بھی پاس کر سکتے ہیں۔

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

جن RPC طریقوں کو commitment کی ضرورت ہوتی ہے، وہ کنکشن کی تخلیق کے دوران متعین کردہ والے کا استعمال کریں گے یا اضافی دلیل کے طور پر commitment پاس کر کے اسے اوور رائیڈ (override) کیا جا سکتا ہے۔

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// 'confirmed' commitment والا ایک blockhash
val blockhash = connection.getLatestBlockhash() 
// commitment کو 'finalized' سے اوور رائیڈ کیا گیا
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

سپورٹڈ API:
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

### لین دین (Transactions)

Sol4k بالترتیب `VersionedTransaction` اور `Transaction` کلاسوں کے ساتھ versioned اور legacy لین دین کی حمایت کرتا ہے۔ دونوں کلاسیں Similar API کی حمایت کرتی ہیں اور Solana لین دین کو بنانے، سائن کرنے، سیریلائز کرنے، ڈی سیریلائز کرنے اور بھیجنے کے لیے استعمال کی جا سکتی ہیں۔ نئے کوڈ میں `VersionedTransaction` استعمال کرنے کی سفارش کی جاتی ہے۔ تازہ ترین blockhash، ایک یا زیادہ ہدایات (instructions) اور ایک فی پیئر (fee payer) کی وضاحت کر کے ایک لین دین بنایا جا سکتا ہے۔

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

متعدد ہدایات والا ایک versioned لین دین:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

Legacy لین دین:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction` ایک انٹرفیس ہے جس میں مندرجہ ذیل ڈیٹا ہونا ضروری ہے:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
`Instruction` انٹرفیس کے کئی نفاذ ہیں جیسے `TransferInstruction`, `SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction` اور `BaseInstruction` (جسے بے ترتیب لین دین بھیجنے کے لیے استعمال کیا جاتا ہے)۔

`AccountMeta` ایک کلاس ہے جو کسی ہدایت میں استعمال ہونے والے اکاؤنٹس کے لیے میٹا ڈیٹا کی وضاحت کرنے دیتی ہے۔ اسے ایک پبلک کی اور دو بولین (boolean) اقدار کی ضرورت ہوتی ہے: `signer` اور `writable`۔

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
اس میں مختلف خصوصیات کے امتزاج کے ساتھ آبجیکٹ بنانے کے لیے تین سہولت فنکشنز بھی ہیں۔

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

سب کچھ کو اکٹھا ملا کر ایک مثال: ایک گیم پروگرام کو معلومات بھیجنے والا لین دین بنانا۔

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

یہاں ایک اور مثال ہے: کسی صارف کے والیٹ کے لیے ایک منسلک ٹوکن اکاؤنٹ بنانا۔

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

آپ [پروجیکٹ کے ٹیسٹوں](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt) میں مزید مثالیں پا سکتے ہیں۔

## ڈویلپمنٹ سیٹ اپ (Development setup)

sol4k کو مقامی طور پر بنانے اور چلانے کے لیے، مندرجہ ذیل اقدامات پر عمل کریں:
- JDK 11 یا اس سے نیا ورژن کو JDK کا ڈیفالٹ ورژن کے طور پر انسٹال کریں (`java --version` چلانے پر 11 یا اس سے نیا ورژن پرنٹ ہونا چاہیے)۔
- JDK 8 انسٹال کریں، لیکن اسے ڈیفالٹ کے طور پر منتخب نہ کریں۔ اگر آپ macOS استعمال کر رہے ہیں، تو `/usr/libexec/java_home -V` چلا کر فہرست میں Java 8 چیک کر سکتے ہیں۔
- `./gradlew publishToMavenLocal` چلا ئیں۔

یہ sol4k کو آپ کی مقامی Maven ریپوزٹری میں انسٹال کر دے گا، اور آپ اسے دیگر پروجیکٹس میں امپورٹ کر سکیں گے (یقینی بنائیں کہ `mavenLocal` [آپ کی Maven ریپوزٹریز](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14) میں شامل ہے)۔ اس بات کو یقینی بنانے کے لیے کہ آپ جس ورژن کو بنایا ہے وہی امپورٹ ہو رہا ہے، `gradle.properties` میں `currentVersion` ایڈجسٹ کریں۔

اینڈ ٹو اینڈ ٹیسٹ چلانے کے لیے، آپ کو دو ماحولیاتی متغیر (environment variables) سیٹ کرنے کی ضرورت ہے: ایک RPC URL اور ایک ایسے اکاؤنٹ کی Base-58 انکوڈڈ سیکرٹ کی جس کا آپ ٹیسٹنگ کے لیے استعمال کریں گے۔

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

ٹیسٹ چلا ئیں:

```shell
./gradlew integrationTest
```

ٹیسٹ چلانے کے لیے اکاؤنٹ میں کچھ Devnet SOL کے ساتھ ساتھ Devnet USDC بھی ہونا ضروری ہے۔ آپ دونوں کو [اس faucet](https://spl-token-faucet.com/?token-name=USDC-Dev) کا استعمال کر کے airdrop کر سکتے ہیں۔

اگر آپ ٹیسٹنگ کے لیے ایک نیا اکاؤنٹ بنانا چاہتے ہیں، تو آپ یوں کر سکتے ہیں:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

اگر کوئی ماحولیاتی متغیر سیٹ نہیں ہیں، تو اینڈ ٹو اینڈ ٹیسٹ بلاک چین کے ساتھ انٹرایکٹ کرنے کے لیے `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF` کا استعمال کریں گے۔ اس کی سیکرٹ کی سورس کوڈ میں عوامی طور پر دستیاب ہے، اس لیے اگر آپ اس پر بھروسہ کرنا چاہتے ہیں تو یقینی بنائیں کہ اس میں Devnet USDC اور SOL موجود ہیں۔

## معاونت (Support)

اگر آپ کو sol4k پسند ہے اور آپ چاہتے ہیں کہ پروجیکٹ چلتا رہے، تو [GitHub Sponsors](https://github.com/sponsors/Shpota) کے ذریعے یا سیدھے والیٹ ایڈریس پر اسپانسر کرنے پر غور کریں:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
