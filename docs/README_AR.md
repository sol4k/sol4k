# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k/blob/main/docs/README.md#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_HI.md#sol4k----">हिंदी</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_UR.md#sol4k----">اردو</a> │ العربية │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ID.md#sol4k----">Indonesian</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_TR.md#sol4k----">Türkçe</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ES.md#sol4k----">Español</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_PT.md#sol4k----">Português</a>

Sol4k هو عميل Kotlin لـ Solana يمكن استخدامه مع Java أو أي لغة JVM أخرى، وكذلك على Android. يتيح التواصل مع عقدة RPC، مما يسمح للمستخدمين بالاستعلام عن المعلومات من البلوكتشين، وإنشاء الحسابات، وقراءة البيانات منها، وإرسال أنواع مختلفة من المعاملات، والعمل مع أزواج المفاتيح (key pairs) والمفاتيح العامة (public keys). كما يوفر العميل واجهات برمجة تطبيقات ملائمة لجعل تجربة المطور سلسة ومباشرة.

## كيفية الاستيراد

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

## كيفية الاستخدام

أنشئ اتصالاً، واطلب أحدث blockhash، وأرسل معاملة تحويل SOL من حساب إلى آخر
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

تحقق من مستودع [sol4k-examples](https://github.com/sol4k/sol4k-examples) للعثور على أمثلة Java جاهزة للاستخدام لواجهات برمجة تطبيقات sol4k.

## واجهات برمجة التطبيقات (APIs)

### العمل مع أزواج المفاتيح والمفاتيح العامة.

توليد زوج مفاتيح.
```kotlin
val generatedKeypair = Keypair.generate()
```

إنشاء زوج مفاتيح من سر موجود.
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

إنشاء مفتاح عام من سلسلة نصية.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

إنشاء مفتاح عام من مصفوفة بايت.

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

الحصول على عنوان حساب توكن مرتبط (associated token account) لرمز SPL.

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

تحويل مفتاح عام إلى سلسلة نصية.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
```

### ترميز Base 58

فك ترميز البيانات.

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

ترميز البيانات.
```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### العمل مع التوقيعات

توقيع رسالة.

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

التحقق.

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```

### دوال RPC

تتم مكالمات RPC من خلال فئة `Connection` تكشف دوال تعكس [طرق JSON RPC](https://docs.solana.com/api/http). يمكن إنشاء اتصال باستخدام عنوان URL HTTP لعقدة RPC والتزام (commitment).

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

إذا لم يتم تحديد التزام، يتم استخدام `FINALIZED` افتراضيًا.

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

يمكنك أيضًا تمرير عنوان URL الخاص بـ RPC كسلسلة نصية.

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

ستستخدم طرق RPC التي تتطلب التزامًا ما تم تحديده أثناء إنشاء الاتصال أو يمكن تجاوزه (override) عن طريق تمرير التزام كوسيط إضافي.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// blockhash مع التزام 'confirmed'
val blockhash = connection.getLatestBlockhash() 
// تم تجاوز الالتزام بـ 'finalized'
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

واجهات برمجة التطبيقات المدعومة:
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

### المعاملات

يدعم Sol4k المعاملات ذات الإصدار (versioned) والمعاملات القديمة (legacy) من خلال فئتي `VersionedTransaction` و `Transaction` على التوالي. تدعم كلتا الفئتين واجهات برمجة تطبيقات مشابهة ويمكن استخدامهما لبناء وتوقيع وتسلسل (serialize) وإلغاء تسلسل (deserialize) وإرسال معاملات Solana. يوصى باستخدام `VersionedTransaction` في الكود الجديد. يمكن إنشاء معاملة عن طريق تحديد أحدث blockhash، وتعليمة واحدة أو عدة تعليمات، ودافع الرسوم (fee payer).

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

معاملة ذات إصدار بتعليمات متعددة:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

معاملة قديمة:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction` هي واجهة تتطلب وجود البيانات التالية:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
تحتوي واجهة `Instruction` على عدة تطبيقات مثل `TransferInstruction`، `SplTransferInstruction`، `CreateAssociatedTokenAccountInstruction`، و `BaseInstruction` (التي تُستخدم لإرسال معاملات عشوائية).

`AccountMeta` هي فئة تتيح لك تحديد البيانات الوصفية (metadata) للحسابات المستخدمة في تعليمة. تتطلب مفتاحًا عامًا وقيمتين منطقيتين (boolean): `signer` و `writable`.

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
كما أنها تحتوي على ثلاث دوال مساعدة لبناء كائنات بمجموعات مختلفة من الخصائص.

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

إليك مثالًا يجمع كل شيء معًا: إنشاء معاملة ترسل معلومات إلى برنامج لعبة.

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

إليك مثالًا آخر: إنشاء حساب توكن مرتبط لمحفظة مستخدم.

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

يمكنك العثور على المزيد من الأمثلة [في اختبارات المشروع](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt).

## إعداد التطوير

لبناء وتشغيل sol4k محليًا، قم بالخطوات التالية:
- قم بتثبيت JDK 11 أو أحدث كإصدار افتراضي لـ JDK (يجب أن يطبع تشغيل `java --version` 11 أو أحدث).
- قم بتثبيت JDK 8، ولكن لا تحدده كافتراضي. إذا كنت تستخدم macOS، يمكنك التحقق من Java 8 في القائمة عن طريق تشغيل `/usr/libexec/java_home -V`.
- نفذ `./gradlew publishToMavenLocal`.

سيؤدي هذا إلى تثبيت sol4k في مستودع Maven المحلي الخاص بك، وستتمكن من استيراده في مشاريع أخرى (تأكد من أن `mavenLocal` ضمن [مستودعات Maven الخاصة بك](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14)). اضبط `currentVersion` في `gradle.properties` للتأكد من استيراد الإصدار الذي قمت ببنائه.

لتشغيل اختبارات النهاية إلى النهاية، تحتاج إلى تعيين متغيرين للبيئة: عنوان URL لـ RPC ومفتاح سري مشفر بـ Base-58 لحساب ستستخدمه للاختبار.

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

نفذ الاختبارات:

```shell
./gradlew integrationTest
```

يحتاج الحساب إلى بعض من Devnet SOL وكذلك Devnet USDC لتشغيل الاختبارات. يمكنك إجراء airdrop لكليهما باستخدام [هذا الصنبور (faucet)](https://spl-token-faucet.com/?token-name=USDC-Dev).

إذا كنت ترغب في توليد حساب جديد للاختبار، يمكنك القيام بذلك كما يلي:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

إذا لم يتم تعيين متغيرات البيئة، فستستخدم اختبارات النهاية إلى النهاية `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF` للتفاعل مع البلوكتشين. مفتاحها السري متاح publicly في الكود المصدري، لذلك تأكد من أن لديها Devnet USDC و SOL إذا كنت ترغب في الاعتماد عليها.

## الدعم

إذا كنت تحب sol4k وتريد استمرار المشروع، ففكر في رعايته [عبر GitHub Sponsors](https://github.com/sponsors/Shpota) أو مباشرة إلى عنوان المحفظة:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
