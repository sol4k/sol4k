# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k/blob/main/docs/README.md#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a> │ हिंदी │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_UR.md#sol4k----">اردو</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_AR.md#sol4k----">العربية</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ID.md#sol4k----">Indonesian</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_TR.md#sol4k----">Türkçe</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ES.md#sol4k----">Español</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_PT.md#sol4k----">Português</a>

Sol4k, Solana के लिए एक Kotlin क्लाइंट है जिसका उपयोग Java या किसी अन्य JVM भाषा के साथ-साथ Android पर भी किया जा सकता है। यह RPC नोड के साथ संचार की सुविधा प्रदान करता है, जिससे उपयोगकर्ता ब्लॉकचेन से जानकारी प्राप्त कर सकते हैं, खाते बना सकते हैं, उनसे डेटा पढ़ सकते हैं, विभिन्न प्रकार के लेनदेन भेज सकते हैं और कुंजी-जोड़ियों (key pairs) व सार्वजनिक कुंजियों (public keys) के साथ काम कर सकते हैं। यह क्लाइंट डेवलपर के अनुभव को सुचारू और सरल बनाने के लिए सुविधाजनक API भी प्रदान करता है।

## आयात (Import) कैसे करें

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

## उपयोग (Use) कैसे करें

एक कनेक्शन बनाएं, नवीनतम blockhash का अनुरोध करें और एक खाते से दूसरे खाते में SOL ट्रांसफर लेनदेन भेजें
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

sol4k API के तैयार-उपयोग Java उदाहरणों के लिए [sol4k-examples](https://github.com/sol4k/sol4k-examples) रिपॉजिटरी देखें।

## API

### कुंजी-जोड़ियों (key pairs) और सार्वजनिक कुंजियों (public keys) के साथ कार्य करना।

कुंजी-जोड़ी उत्पन्न करना।
```kotlin
val generatedKeypair = Keypair.generate()
```

मौजूदा रहस्य (secret) से कुंजी-जोड़ी बनाना।
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

एक स्ट्रिंग से सार्वजनिक कुंजी बनाना।

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

बाइट ऐरे से सार्वजनिक कुंजी बनाना।

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

SPL टोकन के लिए संबद्ध टोकन खाता पता प्राप्त करना।

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

सार्वजनिक कुंजी को स्ट्रिंग में बदलना।

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
```

### Base 58 एन्कोडिंग

डेटा डिकोड करना।

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

डेटा एन्कोड करना।
```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### हस्ताक्षर (signatures) के साथ कार्य करना

एक संदेश पर हस्ताक्षर करना।

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

सत्यापन (verifying)।

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```

### RPC फ़ंक्शन

RPC कॉल `Connection` क्लास के माध्यम से की जाती हैं, जो [JSON RPC विधियों](https://docs.solana.com/api/http) को प्रतिबिंबित करने वाले फ़ंक्शन उजागर करता है। RPC नोड के HTTP URL और एक commitment के साथ एक कनेक्शन बनाया जा सकता है।

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

यदि commitment निर्दिष्ट नहीं है, तो डिफ़ॉल्ट रूप से `FINALIZED` का उपयोग किया जाता है।

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

आप RPC URL को स्ट्रिंग के रूप में भी पास कर सकते हैं।

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

जिन RPC विधियों के लिए commitment की आवश्यकता होती है, वे कनेक्शन निर्माण के दौरान निर्दिष्ट वाले का उपयोग करेंगी या अतिरिक्त तर्क के रूप में commitment पास करके उसे ओवरराइड किया जा सकता है।

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// 'confirmed' commitment वाला एक blockhash
val blockhash = connection.getLatestBlockhash() 
// commitment को 'finalized' से ओवरराइड किया गया
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

समर्थित API:
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

### लेनदेन (Transactions)

Sol4k क्रमशः `VersionedTransaction` और `Transaction` क्लासों के साथ संस्करणित (versioned) और पुराने (legacy) लेनदेन का समर्थन करता है। दोनों क्लास समान API का समर्थन करते हैं और Solana लेनदेन को बनाने, हस्ताक्षर करने, अनुक्रमित करने, विअनुक्रमित करने और भेजने के लिए उपयोग किए जा सकते हैं। नए कोड में `VersionedTransaction` का उपयोग करने की अनुशंसा की जाती है। नवीनतम blockhash, एक या अधिक निर्देश (instructions) और एक शुल्क भुगतानकर्ता (fee payer) निर्दिष्ट करके एक लेनदेन बनाया जा सकता है।

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

कई निर्देशों वाला एक संस्करणित लेनदेन:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

पुराना (legacy) लेनदेन:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction` एक इंटरफ़ेस है जिसमें निम्नलिखित डेटा होना आवश्यक है:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
`Instruction` इंटरफ़ेस के कई कार्यान्वयन हैं जैसे `TransferInstruction`, `SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction` और `BaseInstruction` (जिसका उपयोग 임의 के लेनदेन भेजने के लिए किया जाता है)।

`AccountMeta` एक क्लास है जो किसी निर्देश में उपयोग किए गए खातों के लिए मेटाडेटा निर्दिष्ट करने देता है। इसमें एक सार्वजनिक कुंजी और दो बूलियन मानों की आवश्यकता होती है: `signer` और `writable`।

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
इसमें विभिन्न गुण संयोजनों के साथ ऑब्जेक्ट्स बनाने के लिए तीन सुविधा फ़ंक्शन भी हैं।

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

सब कुछ को एक साथ जोड़ने का एक उदाहरण: एक गेम प्रोग्राम को जानकारी भेजने वाला लेनदेन बनाना।

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

यहाँ एक और उदाहरण है: किसी उपयोगकर्ता के वॉलेट के लिए एक संबद्ध टोकन खाता बनाना।

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

आप [प्रोजेक्ट के परीक्षणों](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt) में और अधिक उदाहरण पा सकते हैं।

## विकास सेटअप (Development setup)

sol4k को स्थानीय रूप से बनाने और चलाने के लिए, निम्नलिखित चरणों का पालन करें:
- JDK 11 या नए संस्करण को JDK का डिफ़ॉल्ट संस्करण के रूप में स्थापित करें (`java --version` चलाने पर 11 या नया संस्करण प्रिंट होना चाहिए)।
- JDK 8 स्थापित करें, लेकिन इसे डिफ़ॉल्ट के रूप में न चुनें। यदि आप macOS का उपयोग कर रहे हैं, तो `/usr/libexec/java_home -V` चलाकर सूची में Java 8 की जाँच कर सकते हैं।
- `./gradlew publishToMavenLocal` निष्पादित करें।

यह sol4k को आपके स्थानीय Maven रिपॉजिटरी में स्थापित कर देगा, और आप इसे अन्य प्रोजेक्ट्स में आयात कर पाएँगे (सुनिश्चित करें कि `mavenLocal` [आपके Maven रिपॉजिटरीज](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14) में शामिल है)। यह सुनिश्चित करने के लिए कि आप जिस संस्करण का निर्माण किया है वही आयात हो रहा है, `gradle.properties` में `currentVersion` समायोजित करें।

एंड-टू-एंड परीक्षण चलाने के लिए, आपको दो पर्यावरण चर सेट करने की आवश्यकता है: एक RPC URL और एक ऐसे खाते की Base-58 एन्कोडेड गोपनीय कुंजी जिसका उपयोग आप परीक्षण के लिए करेंगे।

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

परीक्षण चलाएँ:

```shell
./gradlew integrationTest
```

परीक्षण चलाने के लिए खाते में कुछ Devnet SOL के साथ-साथ Devnet USDC भी होना आवश्यक है। आप दोनों को [इस faucet](https://spl-token-faucet.com/?token-name=USDC-Dev) का उपयोग करके airdrop कर सकते हैं।

यदि आप परीक्षण के लिए एक नया खाता जनरेट करना चाहते हैं, तो आप ऐसा कर सकते हैं:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

यदि कोई पर्यावरण चर सेट नहीं है, तो एंड-टू-एंड परीक्षण ब्लॉकचेन के साथ इंटरैक्ट करने के लिए `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF` का उपयोग करेंगे। इसकी गोपनीय कुंजी स्रोत कोड में सार्वजनिक रूप से उपलब्ध है, इसलिए यदि आप इस पर भरोसा करना चाहते हैं तो सुनिश्चित करें कि इसमें Devnet USDC और SOL है।

## सहायता (Support)

यदि आपको sol4k पसंद है और आप चाहते हैं कि प्रोजेक्ट चलता रहे, तो [GitHub Sponsors](https://github.com/sponsors/Shpota) के माध्यम से या सीधे वॉलेट पते पर प्रायोजित करने पर विचार करें:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
