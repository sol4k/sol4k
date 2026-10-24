# sol4k [![Maven Central](https://img.shields.io/maven-central/v/org.sol4k/sol4k?color=green)](https://central.sonatype.com/artifact/org.sol4k/sol4k) [![Build](https://github.com/sol4k/sol4k/actions/workflows/build.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/build.yml) [![Style](https://github.com/sol4k/sol4k/actions/workflows/lint.yml/badge.svg)](https://github.com/sol4k/sol4k/actions/workflows/lint.yml) [![License](https://img.shields.io/badge/License-Apache_2.0-green.svg)](https://github.com/sol4k/sol4k/blob/main/LICENSE)

<a href="https://github.com/sol4k/sol4k/blob/main/docs/README.md#sol4k----">English</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_KR.md#sol4k----">한국어</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ZH.md#sol4k----">中文</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_JP.md#sol4k----">日本語</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_HI.md#sol4k----">हिंदी</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_UR.md#sol4k----">اردو</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_AR.md#sol4k----">العربية</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ID.md#sol4k----">Indonesian</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_TR.md#sol4k----">Türkçe</a> │ <a href="https://github.com/sol4k/sol4k/blob/main/docs/README_ES.md#sol4k----">Español</a> │ Português

Sol4k é um cliente Kotlin para Solana que pode ser usado com Java ou qualquer outra linguagem JVM, bem como no Android. Ele permite a comunicação com um nó RPC, possibilitando aos usuários consultar informações da blockchain, criar contas, ler dados delas, enviar diferentes tipos de transações e trabalhar com pares de chaves e chaves públicas. O cliente também expõe APIs convenientes para tornar a experiência do desenvolvedor fluida e direta.

## Como importar

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

## Como usar

Crie uma conexão, solicite o blockhash mais recente e envie uma transação de transferência de SOL de uma conta para outra
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

Consulte o repositório [sol4k-examples](https://github.com/sol4k/sol4k-examples) para encontrar exemplos Java prontos para uso das APIs do sol4k.

## APIs

### Trabalhando com pares de chaves e chaves públicas.

Gerando um par de chaves.
```kotlin
val generatedKeypair = Keypair.generate()
```

Criando um par de chaves a partir de um segredo existente.
```kotlin
val keypairFromSecretKey = Keypair.fromSecretKey(secretKeyByteArray)
```

Criando uma chave pública a partir de uma string.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

Criando uma chave pública a partir de um array de bytes.

```kotlin
val publicKey = PublicKey(publicKeyByteArray)
```

Obtendo um endereço de conta de token associada para um token SPL.

```kotlin
val programDerivedAddress = PublicKey.findProgramDerivedAddress(holderAddress, tokenMintAddress)
```

Convertendo uma chave pública em uma string.

```kotlin
val publicKey = PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
publicKey.toBase58() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
publicKey.toString() // DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx
```

### Codificação Base 58

Decodificando dados.

```kotlin
val decodedBytes: ByteArray = Base58.decode("DxPv2QMA5cWR5Xfg7tXr5YtJEEStg5Kiag9HhkY1mSx")
```

Codificando dados.
```kotlin
val encodedData: String = Base58.encode(inputByteArray)
```

### Trabalhando com assinaturas

Assinando uma mensagem.

```kotlin
val signature: ByteArray = keypair.sign(messageByteArray)
```

Verificando.

```kotlin
val result: Boolean = publicKey.verify(signature, message)
```

### Funções RPC

As chamadas RPC são realizadas por meio de uma classe `Connection` que expõe funções que espelham [os métodos JSON RPC](https://docs.solana.com/api/http). Uma conexão pode ser criada com uma URL HTTP de um nó RPC e um commitment.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.PROCESSED)
```

Se o commitment não for especificado, `FINALIZED` é usado por padrão.

```kotlin
val connection = Connection(RpcUrl.DEVNET)
```

Você também pode passar a URL RPC como uma string.

```kotlin
val connection = Connection("https://api.devnet.solana.com")
```

Métodos RPC que exigem commitment usarão o especificado durante a criação da conexão ou podem ser substituídos passando commitment como um argumento adicional.

```kotlin
val connection = Connection(RpcUrl.DEVNET, Commitment.CONFIRMED)
// um blockhash com o commitment 'confirmed'
val blockhash = connection.getLatestBlockhash() 
// o commitment é substituído por 'finalized'
val finalizedBlockhash = connection.getLatestBlockhash(Commitment.FINALIZED)
```

APIs compatíveis:
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

### Transações

O Sol4k suporta transações versionadas e transações legadas com as classes `VersionedTransaction` e `Transaction`, respectivamente. Ambas as classes suportam APIs semelhantes e podem ser usadas para construir, assinar, serializar, desserializar e enviar transações Solana. Recomenda-se usar `VersionedTransaction` em códigos novos. Uma transação pode ser criada especificando o blockhash mais recente, uma ou várias instruções e um pagador de taxas.

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instruction)
val transaction = VersionedTransaction(message)
```

Uma transação versionada com múltiplas instruções:

```kotlin
val message = TransactionMessage.newMessage(feePayer, blockhash, instructions)
val transaction = VersionedTransaction(message)
```

Transação legada:
```kotlin
val transaction = Transaction(blockhash, instructions, feePayer)
```

`Instruction` é uma interface que requer ter os seguintes dados:
```kotlin
interface Instruction {
    val data: ByteArray
    val keys: List<AccountMeta>
    val programId: PublicKey
}
```
A interface `Instruction` tem várias implementações, como `TransferInstruction`, `SplTransferInstruction`, `CreateAssociatedTokenAccountInstruction` e `BaseInstruction` (a usada para enviar transações arbitrárias).

`AccountMeta` é uma classe que permite especificar metadados para as contas usadas em uma instrução. Ela requer uma chave pública e dois valores booleanos: `signer` e `writable`.

```kotlin
val accountMeta = AccountMeta(publicKey, signer = false, writable = true)
```
Ela também possui três funções de conveniência para construir objetos com diferentes combinações de propriedades.

```kotlin
val signerAndWritable: AccountMeta = AccountMeta.signerAndWritable(publicKey)
val signer: AccountMeta = AccountMeta.signer(publicKey)
val writable: AccountMeta = AccountMeta.writable(publicKey)
```

Aqui está um exemplo de como combinar tudo: criar uma transação que envia informações para um programa de jogo.

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

Aqui está outro exemplo: criar uma conta de token associada para a carteira de um usuário.

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

Você pode encontrar mais exemplos [nos testes do projeto](https://github.com/sol4k/sol4k/blob/main/src/integration-test/kotlin/org/sol4k/ConnectionTest.kt).

## Configuração de desenvolvimento

Para compilar e executar o sol4k localmente, execute as etapas a seguir:
- Instale o JDK 11 ou mais recente como a versão padrão do JDK (ao executar `java --version` deve ser exibido 11 ou mais recente).
- Instale o JDK 8, mas não o selecione como padrão. Se você usa macOS, pode verificar o Java 8 na lista executando `/usr/libexec/java_home -V`.
- Execute `./gradlew publishToMavenLocal`.

Isso instalaria o sol4k no seu repositório local do Maven, e você poderia importá-lo em outros projetos (certifique-se de que `mavenLocal` esteja entre [seus repositórios Maven](https://github.com/sol4k/sol4k-examples/blob/main/build.gradle#L11-L14)). Ajuste `currentVersion` em `gradle.properties` para garantir que você está importando a versão que compilou.

Para executar testes de ponta a ponta, você precisa definir duas variáveis de ambiente: uma URL RPC e uma chave secreta codificada em Base-58 de uma conta que você usaria para testes.

```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encode-secret-key..."
```

Execute os testes:

```shell
./gradlew integrationTest
```

A conta precisa ter algum SOL de Devnet, bem como USDC de Devnet, para executar os testes. Você pode fazer airdrop de ambos usando [este faucet](https://spl-token-faucet.com/?token-name=USDC-Dev).

Se você quiser gerar uma nova conta para testes, pode fazer da seguinte forma:

```shell
val keypair = Keypair.generate()
val secret = Base58.encode(keypair.secret)
println("Secret: $secret")
println("Public Key: ${keypair.publicKey}")
```

Se nenhuma variável de ambiente for definida, os testes de ponta a ponta usarão `EwtJVgZQGHe9MXmrNWmujwcc6JoVESU2pmq7wTDBvReF` para interagir com a blockchain. Sua chave secreta está publicamente disponível no código-fonte; por isso, certifique-se de que ela tenha USDC e SOL de Devnet se você quiser dependER dela.

## Suporte

Se você gosta do sol4k e quer que o projeto continue, considere patrociná-lo [via GitHub Sponsors](https://github.com/sponsors/Shpota) ou diretamente para o endereço da carteira:

```shell
HNFoca4s9e9XG6KBpaQurVj4Yr6k3GQKhnubRxAGwAZs
```
