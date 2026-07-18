# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k/blob/main/docs/README.md#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_HI.md#sol4k----">हिंदी</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_UR.md#sol4k----">اردو</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_AR.md#sol4k----">العربية</a> │ Indonesian │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_TR.md#sol4k----">Türkçe</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ES.md#sol4k----">Español</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_PT.md#sol4k----">Português</a>

Sol4k adalah klien Kotlin untuk Solana yang dapat digunakan dengan Java atau bahasa JVM lainnya, serta di Android. Ini memungkinkan komunikasi dengan node RPC, sehingga pengguna dapat mengambil informasi dari blockchain, membuat akun, membaca data darinya, mengirim berbagai jenis transaksi, dan bekerja dengan pasangan kunci serta kunci publik. Klien ini juga menyediakan API yang nyaman untuk membuat pengalaman pengembang menjadi lancar dan mudah.

## Cara mengimpor

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

## Cara menggunakan

Buat koneksi, minta blockhash terbaru, dan kirim transaksi transfer SOL dari satu akun ke akun lainnya
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

Periksa repositori [sol4k-examples](https://github.com/sol4k/sol4k-examples) untuk menemukan contoh Java siap pakai dari API sol4k.

## API

### Bekerja dengan pasangan kunci dan kunci publik.

Membuat pasangan kunci.
```kotlin
val generatedKeypair = Keypair.generate()
```

Membuat pasangan kunci dari rahasia yang ada.
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

Membuat kunci publik dari string.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

Membuat kunci publik dari array byte.

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

Mendapatkan alamat akun token terkait untuk token SPL.

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

Mengonversi kunci publik menjadi string.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
```

### Pengodean Base 58

Mendekode data.

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

Mengodekan data.
```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### Bekerja dengan tanda tangan

Menandatangani pesan.

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

Memverifikasi.

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```

### Fungsi RPC

Panggilan RPC dilakukan melalui kelas `Connection` yang mengekspos fungsi yang mencerminkan [metode JSON RPC](https://docs.solana.com/api/http). Sebuah koneksi dapat dibuat dengan URL HTTP dari node RPC dan sebuah commitment.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

Jika commitment tidak ditentukan, `FINALIZED` digunakan secara default.

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

Anda juga dapat meneruskan URL RPC sebagai string.

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

Metode RPC yang memerlukan commitment akan menggunakan yang ditentukan saat pembuatan koneksi atau dapat diganti dengan meneruskan commitment sebagai argumen tambahan.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// sebuah blockhash dengan commitment 'confirmed'
val blockhash = connection.getLatestBlockhash() 
// commitment diganti dengan 'finalized'
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

API yang didukung:
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

### Transaksi

Sol4k mendukung transaksi versioned dan transaksi legacy dengan kelas `VersionedTransaction` dan `Transaction` masing-masing. Kedua kelas mendukung API serupa dan dapat digunakan untuk membangun, menandatangani, membuat serial, mendeserialisasi, dan mengirim transaksi Solana. Disarankan menggunakan `VersionedTransaction` dalam kode baru. Sebuah transaksi dapat dibuat dengan menentukan blockhash terbaru, satu atau beberapa instruksi, dan pembayar biaya.

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

Sebuah transaksi versioned dengan beberapa instruksi:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

Transaksi legacy:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction` adalah sebuah antarmuka yang memerlukan data berikut:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
Antarmuka `Instruction` memiliki beberapa implementasi seperti `TransferInstruction`, `SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction`, dan `BaseInstruction` (yang digunakan untuk mengirim transaksi apa pun).

`AccountMeta` adalah kelas yang memungkinkan Anda menentukan metadata untuk akun yang digunakan dalam sebuah instruksi. Ini memerlukan kunci publik dan dua nilai boolean: `signer` dan `writable`.

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
Ini juga memiliki tiga fungsi kemudahan untuk membuat objek dengan kombinasi properti yang berbeda.

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

Berikut adalah contoh menggabungkan semuanya: membuat transaksi yang mengirim informasi ke program game.

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

Berikut adalah contoh lain: membuat akun token terkait untuk dompet pengguna.

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

Anda dapat menemukan contoh lainnya [dalam pengujian proyek](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt).

## Penyiapan pengembangan

Untuk membangun dan menjalankan sol4k secara lokal, lakukan langkah-langkah berikut:
- Instal JDK 11 atau lebih baru sebagai versi JDK default (menjalankan `java --version` harus menampilkan 11 atau lebih baru).
- Instal JDK 8, tetapi jangan pilih sebagai default. Jika Anda menggunakan macOS, Anda dapat memeriksa Java 8 dalam daftar dengan menjalankan `/usr/libexec/java_home -V`.
- Jalankan `./gradlew publishToMavenLocal`.

Ini akan menginstal sol4k di repositori Maven lokal Anda, dan Anda dapat mengimpornya di proyek lain (pastikan `mavenLocal` ada di antara [repositori Maven Anda](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14)). Sesuaikan `currentVersion` di `gradle.properties` untuk memastikan Anda mengimpor versi yang telah Anda bangun.

Untuk menjalankan pengujian end-to-end, Anda perlu mengatur dua variabel lingkungan: URL RPC dan kunci rahasia yang dikodekan Base-58 dari akun yang akan Anda gunakan untuk pengujian.

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

Jalankan pengujian:

```shell
./gradlew integrationTest
```

Akun perlu memiliki sedikit SOL Devnet serta USDC Devnet untuk menjalankan pengujian. Anda dapat melakukan airdrop keduanya menggunakan [faucet ini](https://spl-token-faucet.com/?token-name=USDC-Dev).

Jika Anda ingin membuat akun baru untuk pengujian, Anda dapat melakukannya seperti ini:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

Jika tidak ada variabel lingkungan yang diatur, pengujian end-to-end akan menggunakan `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF` untuk berinteraksi dengan blockchain. Kunci rahasianya tersedia secara publik dalam kode sumber, jadi pastikan akun tersebut memiliki USDC dan SOL Devnet jika Anda ingin mengandalkannya.

## Dukungan

Jika Anda menyukai sol4k dan ingin proyek ini terus berjalan, pertimbangkan untuk mensponsori melalui [GitHub Sponsors](https://github.com/sponsors/Shpota) atau langsung ke alamat dompet:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
