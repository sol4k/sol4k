# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Sol4k is a Kotlin client for Solana usable from Java or any JVM language, and on
Android. It talks to a Solana RPC node over JSON-RPC to query the blockchain and
submit transactions, and provides local primitives for key pairs, public keys,
encoding, and transaction construction/signing. It is published to Maven Central
as `org.sol4k:sol4k`.

## Commands

Build all modules (compile, unit tests, ktlint and the public API check):
```shell
./gradlew build
```

Run unit tests:
```shell
./gradlew test
```

Run a single unit test class / method. Prefix the task with the module: the
root-level `./gradlew test --tests ...` fails in every module that has no
matching test.
```shell
./gradlew :sol4k:test --tests "org.sol4k.TransactionTest"
./gradlew :sol4k:test --tests "org.sol4k.TransactionTest.someMethod"
```

Lint (ktlint, code style = IntelliJ IDEA). The lint job is enforced in CI:
```shell
./gradlew ktlintCheck     # check
./gradlew ktlintFormat    # auto-fix
```

Public API check: `./gradlew build` runs `checkKotlinAbi`, which compares each
library module's public API with the dump committed in
`<module>/api/<module>.api`. After an intended public API change, regenerate
the dump and commit it (never edit `.api` files by hand):
```shell
./gradlew :sol4k:updateKotlinAbi
```

Install every module (`sol4k` and `sol4k-bom`) to the local Maven repo (for
importing into other projects; bump `currentVersion` in `gradle.properties`
first):
```shell
./gradlew publishToMavenLocal
```

### Integration / end-to-end tests

`integrationTest` is a separate source set under
`sol4k/src/integration-test/kotlin` that hits a real Solana cluster (Devnet by
default). It is configured via two environment variables; without them it
falls back to a hardcoded shared Devnet key in the source.
```shell
export E2E_RPC_URL="https://api.devnet.solana.com"
export E2E_SECRET_KEY="base-58-encoded-secret-key..."
./gradlew integrationTest
```
The test account needs Devnet SOL and Devnet USDC.

## Toolchain note

The library targets JVM 1.8 (`jvmToolchain(8)`), so a JDK 8 must be installed
(but not the default). The Gradle build itself runs on JDK 17+. On macOS,
`/usr/libexec/java_home -V` lists installed JDKs.

## Architecture

The repository is a multi-module Gradle build (Kotlin DSL). All modules share
one version and are released together:

- `sol4k/` — the `org.sol4k:sol4k` library described below.
- `sol4k-bom/` — `org.sol4k:sol4k-bom`, a Java platform (BOM) with version
  constraints for every published sol4k module. Each library module depends on
  it, so Gradle keeps all sol4k artifacts on the same version.
- `build-logic/` — an included build with the convention plugins that hold the
  shared configuration: `sol4k.kotlin-library` (JVM 1.8 toolchain, ABI
  validation, ktlint, `Automatic-Module-Name`), `sol4k.publishing` (Maven
  Central coordinates, POM, signing) and `sol4k.integration-test`. Apply
  third-party plugins in these convention plugins, not in module build files.
- `gradle/libs.versions.toml` — the version catalog with every dependency and
  plugin version, shared by the main build and `build-logic`.

The public API of `sol4k` lives in `org.sol4k`
(`sol4k/src/main/kotlin/org/sol4k`). Three layers:

- **Network layer — `Connection`** is the single entry point for all RPC calls.
  Each public method (e.g. `getBalance`, `getLatestBlockhash`, `sendTransaction`,
  `simulateTransaction`) maps to a JSON-RPC method via the private generic
  `rpcCall`, which uses `java.net.HttpURLConnection` directly (no HTTP client
  dependency) and kotlinx.serialization. A `Connection` carries a default
  `Commitment` that individual calls can override. On a serialization failure
  `rpcCall` re-parses the body as an error and throws `RpcException`.

- **Wire vs. public types.** `org.sol4k.rpc` holds `@Serializable` DTOs that
  mirror the raw JSON-RPC request/response shapes (`RpcRequest`, `RpcResponse`,
  `RpcError*`, and per-method `*Response`/`*Result` types). `Connection` maps
  these internal DTOs into the cleaner public domain types in `org.sol4k.api`
  (`AccountInfo`, `Blockhash`, `Commitment`, `EpochInfo`, etc.). Keep this
  separation: RPC-shaped serialization types in `rpc`, user-facing types in
  `api`.

- **Transactions & instructions (local construction).**
  - `Instruction` (`org.sol4k.instruction`) is the core interface: `data`,
    `keys: List<AccountMeta>`, `programId`. Implementations include
    `TransferInstruction`, `SplTransferInstruction`,
    `CreateAssociatedTokenAccountInstruction`, the compute-budget instructions,
    and `BaseInstruction` (for arbitrary/custom program calls).
  - `AccountMeta` pairs a `PublicKey` with `signer`/`writable` flags
    (helpers: `AccountMeta.signer/writable/signerAndWritable`).
  - `TransactionMessage` is the compiled message (header, accounts,
    instructions); it serializes/deserializes the on-chain message format.
  - `VersionedTransaction` (preferred) wraps a `TransactionMessage`;
    `Transaction` is the legacy form. Both build, `sign`, `serialize`,
    deserialize (`from`), and are sent via `Connection.sendTransaction`.

- **Primitives.** `Keypair` (generate / `fromSecretKey`, sign), `PublicKey`
  (base58, `verify`, `findProgramDerivedAddress` for associated token
  accounts), `Base58`, `Binary`/`Convert` (compact-length & lamport/SOL
  helpers), `Constants`, `RpcUrl`, `ComputeBudget`. Cryptography (ed25519) comes
  from the external `org.sol4k:tweetnacl` dependency.

## Conventions

- Public API methods use `@JvmStatic` / `@JvmOverloads` so Java callers get
  static methods and overloads instead of Kotlin default arguments — preserve
  these when adding or changing public functions.
- The README that ships with the project is `docs/README.md` (the GitHub Pages
  site source), not a root `README.md`. Update it when the public API changes;
  translated copies (`docs/README_*.md`) also exist.
- `currentVersion` in `gradle.properties` is the version of every module.
