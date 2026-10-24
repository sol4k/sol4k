# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k/blob/main/docs/README.md#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_HI.md#sol4k----">हिंदी</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_UR.md#sol4k----">اردو</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_AR.md#sol4k----">العربية</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ID.md#sol4k----">Indonesian</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_TR.md#sol4k----">Türkçe</a> │ Español │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_PT.md#sol4k----">Português</a>

Sol4k es un cliente de Kotlin para Solana que se puede usar con Java o cualquier otro lenguaje JVM, así como en Android. Permite la comunicación con un nodo RPC, lo que permite a los usuarios consultar información de la blockchain, crear cuentas, leer datos de ellas, enviar diferentes tipos de transacciones y trabajar con pares de claves y claves públicas. El cliente también expone APIs convenientes para hacer que la experiencia del desarrollador sea fluida y sencilla.

## Cómo importar

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

## Cómo usar

Crea una conexión, solicita el último blockhash y envía una transacción de transferencia de SOL de una cuenta a otra
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

Consulta el repositorio [sol4k-examples](https://github.com/sol4k/sol4k-examples) para encontrar ejemplos de Java listos para usar de las APIs de sol4k.

## APIs

### Trabajar con pares de claves y claves públicas.

Generar un par de claves.
```kotlin
val generatedKeypair = Keypair.generate()
```

Crear un par de claves a partir de un secreto existente.
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

Crear una clave pública a partir de una cadena.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

Crear una clave pública a partir de un array de bytes.

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

Obtener una dirección de cuenta de token asociada para un token SPL.

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

Convertir una clave pública a una cadena.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
```

### Codificación Base 58

Decodificar datos.

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

Codificar datos.
```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### Trabajar con firmas

Firmar un mensaje.

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

Verificar.

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```

### Funciones RPC

Las llamadas RPC se realizan a través de una clase `Connection` que expone funciones que reflejan [los métodos JSON RPC](https://docs.solana.com/api/http). Una conexión se puede crear con una URL HTTP de un nodo RPC y un commitment.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

Si no se especifica el commitment, se utiliza `FINALIZED` por defecto.

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

También puedes pasar la URL RPC como una cadena.

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

Los métodos RPC que requieren commitment usarán el especificado durante la creación de la conexión o se pueden anular pasando commitment como un argumento adicional.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// un blockhash con el commitment 'confirmed'
val blockhash = connection.getLatestBlockhash() 
// el commitment se anula con 'finalized'
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

APIs compatibles:
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

### Transacciones

Sol4k admite transacciones versionadas y transacciones heredadas con las clases `VersionedTransaction` y `Transaction` respectivamente. Ambas clases admiten APIs similares y se pueden utilizar para construir, firmar, serializar, deserializar y enviar transacciones de Solana. Se recomienda usar `VersionedTransaction` en el código nuevo. Una transacción se puede crear especificando el último blockhash, una o varias instrucciones y un pagador de comisiones.

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

Una transacción versionada con múltiples instrucciones:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

Transacción heredada:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction` es una interfaz que requiere tener los siguientes datos:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
La interfaz `Instruction` tiene varias implementaciones como `TransferInstruction`, `SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction` y `BaseInstruction` (la que se usa para enviar transacciones arbitrarias).

`AccountMeta` es una clase que te permite especificar metadatos para las cuentas utilizadas en una instrucción. Requiere una clave pública y dos valores booleanos: `signer` y `writable`.

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
También tiene tres funciones de conveniencia para construir objetos con diferentes combinaciones de propiedades.

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

Aquí hay un ejemplo de cómo combinar todo: crear una transacción que envía información a un programa de juego.

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

He aquí otro ejemplo: crear una cuenta de token asociada para la billetera de un usuario.

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

Puedes encontrar más ejemplos [en las pruebas del proyecto](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt).

## Configuración de desarrollo

Para compilar y ejecutar sol4k localmente, realiza los siguientes pasos:
- Instala JDK 11 o superior como versión predeterminada de JDK (al ejecutar `java --version` debería mostrar 11 o superior).
- Instala JDK 8, pero no lo selecciones como predeterminado. Si usas macOS, puedes buscar Java 8 en la lista ejecutando `/usr/libexec/java_home -V`.
- Ejecuta `./gradlew publishToMavenLocal`.

Esto instalaría sol4k en tu repositorio local de Maven, y podrías importarlo en otros proyectos (asegúrate de que `mavenLocal` esté entre [tus repositorios de Maven](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14)). Ajusta `currentVersion` en `gradle.properties` para asegurarte de importar la versión que has compilado.

Para ejecutar pruebas de extremo a extremo, necesitas configurar dos variables de entorno: una URL RPC y una clave secreta codificada en Base-58 de una cuenta que usarías para las pruebas.

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

Ejecuta las pruebas:

```shell
./gradlew integrationTest
```

La cuenta necesita tener algo de SOL de Devnet así como USDC de Devnet para ejecutar las pruebas. Puedes hacerles airdrop a ambos usando [este grifo](https://spl-token-faucet.com/?token-name=USDC-Dev).

Si quieres generar una nueva cuenta para las pruebas, puedes hacerlo así:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

Si no se configuran variables de entorno, las pruebas de extremo a extremo usarían `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF` para interactuar con la blockchain. Su clave secreta está disponible públicamente en el código fuente, por lo que asegúrate de que tenga USDC y SOL de Devnet si quieres confiar en ella.

## Soporte

Si te gusta sol4k y quieres que el proyecto siga adelante, considera patrocinarlo [a través de GitHub Sponsors](https://github.com/sponsors/Shpota) o directamente a la dirección de la billetera:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
