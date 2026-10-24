# Implementation plan: WebSocket support as `sol4k-websocket`

This plan is for the agent or contributor who implements WebSocket support in
sol4k. Read all of it before starting.

Every build file below was compiled and tested on a copy of this repository at
0.9.0 (Gradle 9.8.2, Kotlin 2.4.21, gradle-maven-publish-plugin 0.37.0). The
proposed public API was compiled from Kotlin and from a Java 8 caller. If
`main` has moved on, keep the versions that are on `main` when you start.

## Goal

Add Solana WebSocket (PubSub) subscriptions without adding dependencies for
people who don't use them:

- `org.sol4k:sol4k` stays as it is: same coordinates, packages, public API
  and dependencies.
- A new `org.sol4k:sol4k-websocket` artifact (package `org.sol4k.websocket`)
  depends on `sol4k` and OkHttp 5.
- A new `org.sol4k:sol4k-bom` keeps the versions of all sol4k modules aligned.
- All modules live in this repository, share one version (`currentVersion` in
  `gradle.properties`) and are released together.

## Decisions already made

Don't revisit these without asking the maintainer.

| Topic | Decision |
| --- | --- |
| Artifacts | `org.sol4k:sol4k` (unchanged), `org.sol4k:sol4k-websocket` (new), `org.sol4k:sol4k-bom` (new) |
| Core/common module | Not now. Don't extract one. |
| Packages | Existing packages unchanged. New code goes in `org.sol4k.websocket` only, so no package is split across jars. |
| Gradle layout | Directories named like the artifacts (`sol4k/`, `sol4k-websocket/`, `sol4k-bom/`), shared logic in `build-logic/` convention plugins, versions in `gradle/libs.versions.toml`, Kotlin DSL |
| Versioning | One version for all modules, one tag, one GitHub release, one Maven Central deployment |
| WebSocket client | OkHttp 5 as `com.squareup.okhttp3:okhttp`, declared as `api`. Never depend on `okhttp-jvm` directly. |
| API style | Listener-based and Java-friendly (no kotlinx-coroutines), `AutoCloseable` handles, `@JvmOverloads`/`@JvmStatic` like the rest of sol4k |
| Stability | The new public API carries `@ExperimentalSol4kWebSocketApi` (warning-level opt-in) |
| Sharing internals | Public declarations marked `@InternalSol4kApi` (error-level opt-in), excluded from ABI dumps |

## Rules

- Don't change the public API of `sol4k`. The only allowed ABI change is the
  new `InternalSol4kApi` annotation class in Phase 2. `./gradlew build`
  enforces this through `checkKotlinAbi`.
- Don't bump `currentVersion`, create tags or publish releases. The maintainer
  releases with the `release` skill.
- Don't add dependencies other than OkHttp (main) and `mockwebserver3` (tests).
- Don't edit `.github/workflows/*`. They work unchanged from the root project.
- Don't put anything in `docs/` except the README changes in Phase 3: it is the
  source of the sol4k.org website.
- Match the existing style: ktlint with the IntelliJ IDEA code style,
  `@JvmOverloads`/`@JvmStatic` for Java callers, comment density like the
  surrounding code.
- One PR per phase, in order. Each phase ends with its checklist passing. Ask
  the maintainer before deviating from this plan.

## Before you start (maintainer)

- Two open PRs edit files that Phase 1 moves:
  [sol4k/sol4k#206](https://github.com/sol4k/sol4k/pull/206) (RPC error
  handling and transport timeouts) and
  [sol4k/sol4k#214](https://github.com/sol4k/sol4k/pull/214) (sendTransaction
  parameters). Merge them first, or expect to update them after Phase 1. Git's
  rename detection carries most source changes over; `build.gradle` changes
  have to be redone in the new files.
- Dependabot PRs [sol4k/sol4k#194](https://github.com/sol4k/sol4k/pull/194),
  [sol4k/sol4k#218](https://github.com/sol4k/sol4k/pull/218),
  [sol4k/sol4k#219](https://github.com/sol4k/sol4k/pull/219) and
  [sol4k/sol4k#220](https://github.com/sol4k/sol4k/pull/220) target
  `build.gradle` or the wrapper and are already superseded by `main`. Close
  them. Dependabot then tracks `gradle/libs.versions.toml`.

## Environment

- The library compiles with a Java 8 toolchain (`jvmToolchain(8)`), while
  Gradle itself runs on JDK 17 or newer. If `ls /usr/lib/jvm` shows no JDK 8,
  install one with `apt-get install -y openjdk-8-jdk-headless` (prefix `sudo`
  if you aren't root); Gradle detects it automatically. Run Gradle with `JAVA_HOME` pointing at JDK 17 or
  21. GitHub's Ubuntu runners already have JDK 8.
- Maven Central sometimes answers HTTP 429 to cloud machines. Retry first. If
  it persists, use the local-only mirror in Appendix A. Never commit it.
- Only `integrationTest` needs Devnet. Unit tests need no network beyond
  downloading dependencies.

## Phase 1: split the build into modules (PR 1)

Result: the same `org.sol4k:sol4k` artifact, now built from `sol4k/`, plus a
new `sol4k-bom`. No source file changes.

### 1.1 Capture the baseline before moving anything

```bash
V=$(sed -n 's/^currentVersion=//p' gradle.properties)
# Snapshot the public API with the old build
printf '\nkotlin {\n    abiValidation()\n}\n' >> build.gradle
./gradlew updateKotlinAbi                       # writes api/sol4k.api
./gradlew jar generatePomFileForMavenPublication
mkdir -p /tmp/sol4k-baseline
cp "build/libs/sol4k-$V.jar" build/publications/maven/pom-default.xml /tmp/sol4k-baseline/
git checkout -- build.gradle                    # drop the temporary change, keep api/sol4k.api
```

### 1.2 Move the sources

```bash
mkdir sol4k
git mv src sol4k/src                            # keeps file history
mv api sol4k/api && git add sol4k/api
git rm build.gradle settings.gradle
```

### 1.3 Add the build files

`settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "sol4k-parent"

include(
    ":sol4k",
    ":sol4k-bom",
)
```

`gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.4.21"
kotlinx-serialization = "1.12.0"
dokka = "2.2.0"
maven-publish = "0.37.0"
ktlint-gradle = "14.2.0"
ktlint = "1.8.0"

[libraries]
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
sol4k-tweetnacl = { module = "org.sol4k:tweetnacl", version = "0.1.6" }
sol4k-utilities = { module = "org.sol4k:utilities", version = "0.1.0" }

# Gradle plugins, consumed by build-logic as regular dependencies
kotlin-gradle-plugin = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
kotlin-serialization-gradle-plugin = { module = "org.jetbrains.kotlin:kotlin-serialization", version.ref = "kotlin" }
dokka-gradle-plugin = { module = "org.jetbrains.dokka:dokka-gradle-plugin", version.ref = "dokka" }
maven-publish-gradle-plugin = { module = "com.vanniktech:gradle-maven-publish-plugin", version.ref = "maven-publish" }
ktlint-gradle-plugin = { module = "org.jlleitschuh.gradle:ktlint-gradle", version.ref = "ktlint-gradle" }
```

`build-logic/settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
```

`build-logic/build.gradle.kts`:

```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
    implementation(libs.dokka.gradle.plugin)
    implementation(libs.maven.publish.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
}
```

`build-logic/src/main/kotlin/sol4k.publishing.gradle.kts` (today's
`mavenPublishing` block, made generic):

```kotlin
// Shared Maven Central publishing setup for every published module (libraries and the BOM).
plugins {
    id("com.vanniktech.maven.publish")
}

group = "org.sol4k"
version = providers.gradleProperty("currentVersion").get()

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)
    if (gradle.startParameter.taskNames.none { it.contains("publishToMavenLocal") }) {
        signAllPublications()
    }

    coordinates("org.sol4k", project.name, version.toString())

    pom {
        name.set(project.name)
        description.set(provider { project.description })
        inceptionYear.set("2023")
        url.set("https://sol4k.org")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("shpota")
                name.set("Sasha Shpota")
                url.set("https://github.com/shpota/")
            }
        }
        scm {
            url.set("https://github.com/sol4k/sol4k/")
            connection.set("scm:git:git://github.com/sol4k/sol4k.git")
            developerConnection.set("scm:git:ssh://git@github.com/sol4k/sol4k.git")
        }
    }
}
```

`build-logic/src/main/kotlin/sol4k.kotlin-library.gradle.kts`:

```kotlin
// Shared setup for every published Kotlin/JVM library module.
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
    id("sol4k.publishing")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(8)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        languageVersion = KotlinVersion.KOTLIN_2_0
        apiVersion = KotlinVersion.KOTLIN_2_0
    }
    // Public API is snapshotted in <module>/api/<module>.api; `check` fails if it changes unexpectedly.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

dependencies {
    // Every module depends on the BOM, so Gradle keeps all sol4k artifacts on the same version.
    "api"(platform(project(":sol4k-bom")))
    "testImplementation"(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
}

tasks.named<Jar>("jar") {
    manifest {
        // Stable JPMS name, e.g. org.sol4k / org.sol4k.websocket
        attributes("Automatic-Module-Name" to "org." + project.name.replace("-", "."))
    }
}

ktlint {
    version.set(libs.findVersion("ktlint").get().requiredVersion)
    additionalEditorconfig.set(mapOf("ktlint_code_style" to "intellij_idea"))
}
```

`build-logic/src/main/kotlin/sol4k.integration-test.gradle.kts`:

```kotlin
// Integration tests that hit a real cluster (Devnet by default): ./gradlew integrationTest
plugins {
    id("org.jetbrains.kotlin.jvm")
}

val integrationTest = sourceSets.create("integrationTest") {
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    resources.srcDir("src/integration-test/resources")
}
kotlin.sourceSets.named("integrationTest") {
    kotlin.srcDir("src/integration-test/kotlin")
}
configurations.named(integrationTest.implementationConfigurationName) {
    extendsFrom(configurations.testImplementation.get())
}
configurations.named(integrationTest.runtimeOnlyConfigurationName) {
    extendsFrom(configurations.testRuntimeOnly.get())
}

tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    mustRunAfter(tasks.test)
    listOf("E2E_RPC_URL", "E2E_SECRET_KEY").forEach { name ->
        providers.environmentVariable(name).orNull?.let { systemProperty(name, it) }
    }
}
```

`sol4k/build.gradle.kts`:

```kotlin
plugins {
    id("sol4k.kotlin-library")
    id("sol4k.integration-test")
}

description = "Kotlin, JVM, and Android client for Solana"

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.sol4k.tweetnacl)
    implementation(libs.sol4k.utilities)
}
```

`sol4k-bom/build.gradle.kts`:

```kotlin
plugins {
    `java-platform`
    id("sol4k.publishing")
}

description = "Bill of materials that aligns the versions of all sol4k modules"

dependencies {
    constraints {
        api(project(":sol4k"))
    }
}
```

`gradle.properties` stays as it is.

### 1.4 Fix `.gitignore`

Replace `/build/` with `build/` and add `.kotlin/`. Otherwise every module's
build output shows up as untracked files.

### 1.5 Update CLAUDE.md

- Running one test: `./gradlew :sol4k:test --tests "org.sol4k.TransactionTest"`.
  The root-level form fails once a module has no matching test.
- Public API checks: `./gradlew build` runs `checkKotlinAbi`. After an
  intended public API change, run `./gradlew :<module>:updateKotlinAbi` and
  commit the updated `<module>/api/<module>.api`.
- Integration tests now live in `sol4k/src/integration-test/kotlin`.
- Architecture: describe the module layout (`sol4k`, `sol4k-bom`,
  `build-logic`, the version catalog) and that `publishToMavenLocal` publishes
  every module.

### 1.6 Checklist

- [ ] `./gradlew build` passes: tests, ktlint and `checkKotlinAbi` against the
      baseline dump.
- [ ] `./gradlew compileIntegrationTestKotlin` passes.
- [ ] `./gradlew :sol4k:jar :sol4k:generatePomFileForMavenPublication`, then
      `diff /tmp/sol4k-baseline/pom-default.xml sol4k/build/publications/maven/pom-default.xml`
      shows only a new `<dependencyManagement>` block importing
      `org.sol4k:sol4k-bom`.
- [ ] `diff <(unzip -Z1 /tmp/sol4k-baseline/sol4k-$V.jar | sort) <(unzip -Z1 sol4k/build/libs/sol4k-$V.jar | sort)`
      prints nothing, and `META-INF/MANIFEST.MF` now contains
      `Automatic-Module-Name: org.sol4k`.
- [ ] `./gradlew publishAllPublicationsToMavenCentral --dry-run` lists sign
      and publish tasks for `:sol4k` and `:sol4k-bom`. This is what
      `release.yml` runs.
- [ ] `./gradlew ktlintCheck` passes from the root (the lint workflow).
- [ ] `git log --follow --oneline sol4k/src/main/kotlin/org/sol4k/Connection.kt`
      shows the history from before the move.

PR title: "Split the build into Gradle modules (no API change)". Say in the
description that the published `sol4k` jar and API are unchanged and that
`sol4k-bom` is new.

## Phase 2: `sol4k-websocket` core with account and slot subscriptions (PR 2)

### 2.1 Add the internal API marker to `sol4k`

`sol4k/src/main/kotlin/org/sol4k/InternalSol4kApi.kt`:

```kotlin
package org.sol4k

/**
 * Marks declarations that are public only so that other sol4k modules (e.g. sol4k-websocket) can use them.
 * They are not part of the supported API and can change in any release.
 */
@RequiresOptIn(
    message = "This is an internal sol4k API shared between sol4k modules. It can change in any release.",
    level = RequiresOptIn.Level.ERROR,
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.CONSTRUCTOR,
)
annotation class InternalSol4kApi
```

In `sol4k.kotlin-library.gradle.kts`, opt every sol4k module in and keep the
marked declarations out of the ABI dumps:

```kotlin
kotlin {
    compilerOptions {
        // ...existing settings...
        // sol4k modules may use each other's @InternalSol4kApi declarations; library users must not.
        optIn.add("org.sol4k.InternalSol4kApi")
    }
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        filters {
            exclude {
                annotatedWith.add("org.sol4k.InternalSol4kApi")
            }
        }
    }
}
```

Run `./gradlew :sol4k:updateKotlinAbi`. The only change in
`sol4k/api/sol4k.api` must be the new annotation class.

### 2.2 Share account decoding

The WebSocket module needs to decode the same account payload as
`getAccountInfo`. Kotlin `internal` doesn't cross module boundaries, so share
a function marked `@InternalSol4kApi`.

`sol4k/src/main/kotlin/org/sol4k/rpc/AccountInfoDecoding.kt`:

```kotlin
package org.sol4k.rpc

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.sol4k.InternalSol4kApi
import org.sol4k.PublicKey
import org.sol4k.api.AccountInfo
import java.util.Base64

private val accountJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/** Decodes a base64-encoded account `value` as returned by getAccountInfo and accountNotification. */
@InternalSol4kApi
fun decodeAccountInfo(value: JsonElement): AccountInfo = accountJson.decodeFromJsonElement(GetAccountInfoValue.serializer(), value).toAccountInfo()

internal fun GetAccountInfoValue.toAccountInfo(): AccountInfo {
    val bytes = Base64.getDecoder().decode(data[0])
    return AccountInfo(
        bytes,
        executable = executable,
        lamports = lamports,
        owner = PublicKey(owner),
        rentEpoch = rentEpoch,
        space = space ?: bytes.size,
    )
}
```

- First add a unit test for `Connection.getAccountInfo` and
  `getMultipleAccounts` with a fake `RpcTransport` (follow
  `ConnectionTransportTest`). Then replace the duplicated mapping in both
  methods with `toAccountInfo()`.
- Share functions, never `@Serializable` classes. A public serializable class
  puts its generated `$serializer` and `Companion` into the ABI even when it
  is annotated.
- Keep `isLenient = true`, as `Connection`'s parser does: `rentEpoch` arrives
  as a bare u64 number such as 18446744073709551615, which
  `BigIntegerSerializer` reads as a string.

### 2.3 Add the module

- `settings.gradle.kts`: add `":sol4k-websocket"` to `include(...)`.
- `sol4k-bom/build.gradle.kts`: add `api(project(":sol4k-websocket"))` to the
  constraints.
- `gradle/libs.versions.toml`: add

  ```toml
  [versions]
  okhttp = "5.5.1"

  [libraries]
  okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
  okhttp-mockwebserver3 = { module = "com.squareup.okhttp3:mockwebserver3", version.ref = "okhttp" }
  ```

- `sol4k-websocket/build.gradle.kts`:

  ```kotlin
  plugins {
      id("sol4k.kotlin-library")
      id("sol4k.integration-test")
  }

  description = "WebSocket subscriptions for sol4k"

  dependencies {
      // Public API uses sol4k types (PublicKey, Commitment, AccountInfo) and accepts an OkHttpClient.
      api(project(":sol4k"))
      api(libs.okhttp)
      implementation(libs.kotlinx.serialization.json)

      testImplementation(libs.okhttp.mockwebserver3)
  }
  ```

- Generate `sol4k-websocket/api/sol4k-websocket.api` with
  `./gradlew :sol4k-websocket:updateKotlinAbi` and commit it.

### 2.4 Public API for this phase

Add the declarations from "API specification" below that this phase needs:
`ExperimentalSol4kWebSocketApi`, `NotificationListener`, `Subscription`,
`AccountNotification`, `SlotNotification`, `WebSocketConnectionListener`, the
transport types, `OkHttpWebSocketTransport`, and `WebSocketConnection` with
`accountSubscribe`, `slotSubscribe` and `close`. The other subscribe methods
come in Phase 3. Don't add declarations that throw `NotImplementedError`.

### 2.5 How `WebSocketConnection` must behave

Lifecycle:

- Creating a `WebSocketConnection` doesn't open a socket. The first
  `*Subscribe` call opens it, and all subscriptions share that one socket.
- `close()` is idempotent. It marks the connection closed, closes the socket
  with code 1000, marks every subscription inactive and calls
  `WebSocketConnectionListener.onClosed()` once. A subscribe call after
  `close()` throws `IllegalStateException`.

Requests:

- Request ids come from an `AtomicLong` starting at 1. Don't reuse
  `RpcRequest`'s `System.currentTimeMillis()` ids: several requests can go out
  in the same millisecond.
- Always send the commitment explicitly, and `"encoding": "base64"` for
  account data, which is what `decodeAccountInfo` expects.

Subscription registry:

- Keep all state behind one lock, and never call user listeners while
  holding it.
- Each subscription knows its subscribe method and params, its unsubscribe
  method, how to decode its notification, its listener, its state (pending,
  active or closed) and its current server id.
- When the socket opens, send the subscribe request of every subscription
  that isn't closed. The same code path later handles reconnects.
- On a subscribe response, record the server id. If the handle was closed in
  the meantime, send the unsubscribe request right away.
- On an error response, close the handle and call
  `WebSocketConnectionListener.onSubscriptionError(subscription, RpcException(code, message, rawMessage))`.
  `RpcException` already exists in `org.sol4k.exception`.
- On a notification, look up `params.subscription`. Ignore unknown ids.
  Decode `params.result` and call the listener. A decoding error goes to
  `onFailure` and doesn't close the socket.
- `unsubscribe()` is idempotent. For an active handle it sends
  `<method>Unsubscribe` with the server id. It marks the handle inactive and
  removes it from the registry.
- Messages that contain `"method"` are notifications. Messages that contain
  `"id"` are responses, with either `result` or `error`. Parse frames with
  `Json.parseToJsonElement` and ignore unknown keys.
- Internal wire DTOs must not reuse public type names such as
  `AccountNotification` (prefix them, for example `RpcAccountNotification`).

Threading:

- Listeners run on the transport's thread (for OkHttp, the socket's reader
  thread). Document that listeners must return quickly.
- `subscribe`, `unsubscribe` and `close` are safe to call from any thread,
  including from inside a listener.

### 2.6 Unit tests

Use OkHttp's `mockwebserver3`: enqueue
`MockResponse.Builder().webSocketUpgrade(serverListener).build()`, capture the
frames the client sends, and push notifications through the server-side
`WebSocket`. Wait with `CountDownLatch` or `LinkedBlockingQueue` and
timeouts, never `Thread.sleep`.

- [ ] `accountSubscribe` sends
      `{"jsonrpc":"2.0","id":<n>,"method":"accountSubscribe","params":["<pubkey>",{"encoding":"base64","commitment":"<commitment>"}]}`.
      `slotSubscribe` sends `"params":[]`.
- [ ] An `accountNotification` (use the payload in "Protocol reference") is
      decoded into `AccountNotification(slot, account)` and reaches the right
      listener.
- [ ] Two subscriptions on one socket each get only their own notifications.
- [ ] `unsubscribe()` sends `accountUnsubscribe` with the server id, later
      notifications for it are ignored, and `isActive` is false.
- [ ] Unsubscribing before the subscribe response arrives sends the
      unsubscribe request once the id is known.
- [ ] An error response calls `onSubscriptionError` with the code and message
      and leaves the handle inactive.
- [ ] `close()` closes the socket with code 1000, calls `onClosed` once, and a
      later subscribe throws `IllegalStateException`.
- [ ] Creating a connection opens no socket (the server sees no request).
- [ ] Request ids are unique when subscribing several times in a row.
- [ ] `webSocketUrl` turns `https://api.devnet.solana.com` into
      `wss://api.devnet.solana.com` and `http://localhost:8899` into
      `ws://localhost:8900`, keeping the path and query.
- [ ] A compile-only Java test (`src/test/java`) calls the API the way the
      Java example in "API specification" does.

### 2.7 Checklist

- [ ] Everything from the Phase 1 checklist still passes.
- [ ] `sol4k/api/sol4k.api` differs from Phase 1 only by `InternalSol4kApi`.
- [ ] `sol4k-websocket/api/sol4k-websocket.api` is committed and contains no
      `@InternalSol4kApi` declarations.
- [ ] `./gradlew :sol4k-websocket:dependencies --configuration runtimeClasspath`
      shows `com.squareup.okhttp3:okhttp` resolving to `okhttp-jvm`.
- [ ] The generated POM of `sol4k-websocket` lists `sol4k`, `okhttp-jvm`
      (Gradle writes the JVM artifact for Maven users), `kotlin-stdlib` and
      `kotlinx-serialization-json-jvm`.
- [ ] A project depending only on `org.sol4k:sol4k` still gets no OkHttp.

## Phase 3: remaining subscriptions, reconnect, integration tests and docs (PR 3)

### 3.1 Remaining subscriptions

- `programSubscribe(programId, commitment, listener)` with
  `"encoding": "base64"`, delivering `ProgramNotification(slot, pubkey, account)`.
- `signatureSubscribe(signature, commitment, listener)` delivering
  `SignatureNotification(slot, err)`. The server ends this subscription after
  its terminal notification: mark the handle inactive after delivering it and
  don't send `signatureUnsubscribe`. Don't send `enableReceivedNotification`
  in this version.
- `logsSubscribe(filter, commitment, listener)` delivering
  `LogsNotification(slot, signature, err, logs)`. Filters map to `"all"`,
  `"allWithVotes"` and `{"mentions":["<address>"]}`; Solana accepts exactly
  one address there.
- `rootSubscribe(listener)` delivering the root slot as a `Long`.
- A transaction error (`err`) is a JSON value. Turn it into a string the way
  `Connection.simulateTransaction` does: the content of a JSON primitive,
  otherwise `toString()` of the element.

### 3.2 Reconnect and keepalive

- After an unexpected close or failure (anything except `close()`), reconnect
  with exponential backoff: 500 ms doubling per attempt, capped at 30 s, with
  up to 20% jitter, reset after a successful open. Call
  `WebSocketConnectionListener.onReconnecting(attempt, delayMillis)` before
  each attempt.
- Reconnect only while at least one subscription is active. Otherwise stay
  disconnected until the next subscribe call.
- On reopen, resend the subscribe request of every subscription that isn't
  closed. Server ids change, so the registry maps the new ids to the existing
  handles. Notifications with stale ids are ignored.
- Schedule reconnects on one daemon `ScheduledExecutorService` per
  connection, shut down in `close()`. Make the delays injectable through an
  `internal` constructor parameter so tests run fast.
- `OkHttpWebSocketTransport` sets OkHttp's `pingInterval` (default 30 s). A
  missed pong surfaces as a failure and triggers a reconnect.

### 3.3 More unit tests

- [ ] Request shape and notification decoding for `programSubscribe`,
      `signatureSubscribe`, `logsSubscribe` (all three filters) and
      `rootSubscribe`.
- [ ] A `signatureNotification` with a non-null `err` gives `isError == true`
      and the error as a string.
- [ ] After the terminal `signatureNotification` the handle is inactive and
      no `signatureUnsubscribe` is sent.
- [ ] When the server drops the socket, the client reconnects (enqueue a
      second upgrade), resubscribes every active subscription, and
      notifications under the new ids reach the same listeners.
      `onReconnecting` is called; closed handles are not resubscribed.
- [ ] No reconnect happens after `close()`.

### 3.4 Integration tests on Devnet

Put them in
`sol4k-websocket/src/integration-test/kotlin/org/sol4k/websocket/`. Use only
public API: the integration-test source set can't see `internal`
declarations, including `webSocketUrl`. Read `E2E_RPC_URL` the way
`ConnectionTest` does and turn it into a WebSocket URL with a small helper in
the test (`https://` to `wss://`, `http://` to `ws://`). Space tests out like
`ConnectionTest` does to avoid HTTP 429.

- [ ] `slotSubscribe` receives a notification within 30 s.
- [ ] Send a transfer the way `ConnectionTest.shouldSendTransaction` does,
      then `signatureSubscribe` with `CONFIRMED` gets `err == null` within
      60 s.
- [ ] `accountSubscribe` on the receiver, opened before sending, sees its
      balance change.

### 3.5 Docs

- `docs/README.md`, `README_JP.md`, `README_KR.md`, `README_ZH.md`: next to
  the existing Gradle and Maven snippets, add `sol4k-websocket` and the BOM
  with the same version string. Add a "WebSocket subscriptions
  (experimental)" section with a Kotlin and a Java example. Translate the
  prose if you can do it accurately; otherwise keep it in English and say so
  in the PR.
- `.claude/skills/release/SKILL.md`: the new version strings must be bumped
  too. Update the file list and the "exactly 9 lines changed" check to the new
  count.
- `CLAUDE.md`: describe `sol4k-websocket` (purpose, dependencies, where the
  integration tests live).

### 3.6 Checklist

- [ ] Everything from the Phase 2 checklist still passes.
- [ ] `./gradlew integrationTest` passes against Devnet, for both modules.
- [ ] The README examples compile: copy them into a test or the Java usage
      test.
- [ ] `./gradlew publishAllPublicationsToMavenCentral --dry-run` lists all
      three modules.

After PR 3 is merged, the maintainer releases 0.10.0 with the `release` skill.

## API specification

The names and shapes below compiled from Kotlin and from Java 8 in the
prototype. Keep them unless you hit a concrete problem, and explain any change
in the PR.

```kotlin
package org.sol4k.websocket

@RequiresOptIn(
    message = "sol4k-websocket is experimental. Its API may change in minor releases.",
    level = RequiresOptIn.Level.WARNING,
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CONSTRUCTOR)
annotation class ExperimentalSol4kWebSocketApi

fun interface NotificationListener<T> {
    fun onNotification(notification: T)
}

interface Subscription : AutoCloseable {
    val isActive: Boolean

    fun unsubscribe()

    override fun close() = unsubscribe()
}

data class AccountNotification(val slot: Long, val account: AccountInfo)

data class ProgramNotification(val slot: Long, val pubkey: PublicKey, val account: AccountInfo)

data class SignatureNotification(val slot: Long, val err: String?) {
    val isError: Boolean get() = err != null
}

data class LogsNotification(val slot: Long, val signature: String, val err: String?, val logs: List<String>)

data class SlotNotification(val slot: Long, val parent: Long, val root: Long)

sealed class LogsFilter {
    data object All : LogsFilter()

    data object AllWithVotes : LogsFilter()

    data class Mentions(val address: PublicKey) : LogsFilter()

    companion object {
        @JvmStatic
        fun all(): LogsFilter = All

        @JvmStatic
        fun allWithVotes(): LogsFilter = AllWithVotes

        @JvmStatic
        fun mentions(address: PublicKey): LogsFilter = Mentions(address)
    }
}

// Connection-level events. An abstract class with no-op methods, like OkHttp's WebSocketListener,
// so Java callers override only what they need.
abstract class WebSocketConnectionListener {
    open fun onOpen() {}

    open fun onReconnecting(attempt: Int, delayMillis: Long) {}

    open fun onSubscriptionError(subscription: Subscription, error: RpcException) {}

    open fun onFailure(error: Throwable) {}

    open fun onClosed() {}
}

// Transport seam, mirroring org.sol4k.transport.RpcTransport. OkHttp is the default;
// a Ktor or JDK adapter can be added later without breaking anyone.
interface WebSocketTransport {
    fun connect(url: String, headers: Map<String, String>, listener: WebSocketTransportListener): WebSocketSession
}

interface WebSocketSession {
    fun send(text: String): Boolean

    fun close(code: Int, reason: String?)
}

interface WebSocketTransportListener {
    fun onOpen()

    fun onMessage(text: String)

    fun onClosed(code: Int, reason: String)

    fun onFailure(error: Throwable)
}

// Derives a client from the caller's OkHttpClient, so the connection pool and interceptors are shared.
class OkHttpWebSocketTransport @JvmOverloads constructor(
    client: OkHttpClient = OkHttpClient(),
    pingIntervalMillis: Long = 30_000,
) : WebSocketTransport

// Each default transport creates its own OkHttpClient. Apps that already have one, or that open
// several connections, should pass OkHttpWebSocketTransport(theirClient).
@ExperimentalSol4kWebSocketApi
class WebSocketConnection @JvmOverloads constructor(
    private val url: String, // wss://...
    private val commitment: Commitment = Commitment.FINALIZED,
    private val transport: WebSocketTransport = OkHttpWebSocketTransport(),
    private val headers: Map<String, String> = emptyMap(),
    private val listener: WebSocketConnectionListener? = null,
) : AutoCloseable {
    // RpcUrl.DEVNET -> wss://api.devnet.solana.com (see webSocketUrl below)
    @JvmOverloads
    constructor(
        rpcUrl: RpcUrl,
        commitment: Commitment = Commitment.FINALIZED,
        transport: WebSocketTransport = OkHttpWebSocketTransport(),
        headers: Map<String, String> = emptyMap(),
        listener: WebSocketConnectionListener? = null,
    )

    @JvmOverloads
    fun accountSubscribe(account: PublicKey, commitment: Commitment = this.commitment, listener: NotificationListener<AccountNotification>): Subscription

    @JvmOverloads
    fun programSubscribe(programId: PublicKey, commitment: Commitment = this.commitment, listener: NotificationListener<ProgramNotification>): Subscription

    @JvmOverloads
    fun signatureSubscribe(signature: String, commitment: Commitment = this.commitment, listener: NotificationListener<SignatureNotification>): Subscription

    @JvmOverloads
    fun logsSubscribe(filter: LogsFilter, commitment: Commitment = this.commitment, listener: NotificationListener<LogsNotification>): Subscription

    fun slotSubscribe(listener: NotificationListener<SlotNotification>): Subscription

    fun rootSubscribe(listener: NotificationListener<Long>): Subscription

    override fun close()
}

// https://host -> wss://host; an explicit port moves up by one (solana-test-validator: 8899 -> 8900).
internal fun webSocketUrl(httpUrl: String): String {
    val match = Regex("^(https?)://([^/:]+)(?::(\\d+))?(.*)$").matchEntire(httpUrl)
        ?: throw IllegalArgumentException("Not an http(s) URL: $httpUrl")
    val (scheme, host, port, rest) = match.destructured
    val wsScheme = if (scheme == "https") "wss" else "ws"
    val wsPort = if (port.isEmpty()) "" else ":${port.toInt() + 1}"
    return "$wsScheme://$host$wsPort$rest"
}
```

Kotlin callers:

```kotlin
@OptIn(ExperimentalSol4kWebSocketApi::class)
fun watch() {
    WebSocketConnection(RpcUrl.DEVNET).use { ws ->
        val subscription = ws.accountSubscribe(PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx")) {
            println(it.account.lamports)
        }
        ws.logsSubscribe(LogsFilter.All, Commitment.CONFIRMED) { println(it.logs) }
        ws.slotSubscribe { println(it.slot) }
        subscription.unsubscribe()
    }
}
```

Java callers (keep a compile-only version of this as a test):

```java
PublicKey account = new PublicKey("DxPv2QMA5cWR5Xfg7tXr5YtJ1EEStg5Kiag9HhkY1mSx");
try (WebSocketConnection ws = new WebSocketConnection(RpcUrl.DEVNET)) {
    Subscription a = ws.accountSubscribe(account, n -> System.out.println(n.getAccount().getLamports()));
    Subscription b = ws.accountSubscribe(account, Commitment.CONFIRMED, n -> System.out.println(n.getSlot()));
    Subscription logs = ws.logsSubscribe(LogsFilter.mentions(account), n -> System.out.println(n.getLogs()));
    Subscription sig = ws.signatureSubscribe("<signature>", Commitment.CONFIRMED, n -> System.out.println(n.isError()));
    Subscription root = ws.rootSubscribe(slot -> System.out.println(slot + 1));
    a.unsubscribe();
    b.close();
}
WebSocketConnection custom = new WebSocketConnection(
    "wss://api.devnet.solana.com",
    Commitment.CONFIRMED,
    new OkHttpWebSocketTransport(new OkHttpClient(), 15_000L),
    Collections.singletonMap("x-api-key", "<key>"),
    new WebSocketConnectionListener() {
        @Override
        public void onReconnecting(int attempt, long delayMillis) {
            System.out.println("Reconnecting, attempt " + attempt);
        }
    }
);
```

## Protocol reference

Source: [Solana RPC WebSocket Methods](https://solana.com/docs/rpc/websocket).
Messages are JSON-RPC 2.0 over one WebSocket. The endpoint only serves
subscriptions; everything else stays on HTTP.

Endpoints: `wss://api.devnet.solana.com`, `wss://api.testnet.solana.com`,
`wss://api.mainnet-beta.solana.com`, and `ws://localhost:8900` for
`solana-test-validator`.

Subscribe request and response:

```json
{"jsonrpc":"2.0","id":1,"method":"accountSubscribe","params":["CM78CPUeXjn8o3yroDHxUtKsZZgoy4GPkPPXfouKNH12",{"encoding":"base64","commitment":"finalized"}]}
{"jsonrpc":"2.0","result":23784,"id":1}
```

Error response:

```json
{"jsonrpc":"2.0","error":{"code":-32602,"message":"Invalid params"},"id":1}
```

Unsubscribe request and response:

```json
{"jsonrpc":"2.0","id":2,"method":"accountUnsubscribe","params":[23784]}
{"jsonrpc":"2.0","result":true,"id":2}
```

| Subscribe method | Params | Notification method | `params.result` | Unsubscribe method |
| --- | --- | --- | --- | --- |
| `accountSubscribe` | `[pubkey, {encoding, commitment}]` | `accountNotification` | `{context:{slot}, value: account}` | `accountUnsubscribe` |
| `programSubscribe` | `[programId, {encoding, commitment}]` | `programNotification` | `{context:{slot}, value:{pubkey, account}}` | `programUnsubscribe` |
| `signatureSubscribe` | `[signature, {commitment}]` | `signatureNotification` | `{context:{slot}, value:{err}}` | `signatureUnsubscribe` |
| `logsSubscribe` | `["all" \| "allWithVotes" \| {mentions:[pubkey]}, {commitment}]` | `logsNotification` | `{context:{slot}, value:{signature, err, logs}}` | `logsUnsubscribe` |
| `slotSubscribe` | `[]` | `slotNotification` | `{parent, root, slot}` | `slotUnsubscribe` |
| `rootSubscribe` | `[]` | `rootNotification` | root slot number | `rootUnsubscribe` |

Example notifications:

```json
{"jsonrpc":"2.0","method":"accountNotification","params":{"result":{"context":{"slot":5199307},"value":{"data":["AAAA","base64"],"executable":false,"lamports":33594,"owner":"11111111111111111111111111111111","rentEpoch":18446744073709551615,"space":3}},"subscription":23784}}
{"jsonrpc":"2.0","method":"programNotification","params":{"result":{"context":{"slot":583},"value":{"pubkey":"BpdYYo2Vw1NVbzE2DqJxCX2xEfr42xvMYFU2dKd1CW57","account":{"lamports":499997095000,"data":["","base64"],"owner":"11111111111111111111111111111111","executable":false,"rentEpoch":18446744073709551615,"space":0}}},"subscription":11}}
{"jsonrpc":"2.0","method":"signatureNotification","params":{"result":{"context":{"slot":5207624},"value":{"err":null}},"subscription":24006}}
{"jsonrpc":"2.0","method":"logsNotification","params":{"result":{"context":{"slot":3557},"value":{"signature":"67Viive4HpLJRPuYggAu3cbxZeQra5Y58oh4AmGc1YRXgQHevSwFbyxhBeDXw6rTCmUtHyCY7BjeHiHvhC9tud9U","err":null,"logs":["Program 11111111111111111111111111111111 invoke [1]","Program 11111111111111111111111111111111 success"]}},"subscription":14}}
{"jsonrpc":"2.0","method":"slotNotification","params":{"result":{"slot":373,"parent":372,"root":341},"subscription":5}}
{"jsonrpc":"2.0","method":"rootNotification","params":{"result":324,"subscription":3}}
```

Notes:

- When `commitment` is omitted the server uses `finalized`. sol4k always sends
  it, using the connection's default.
- `signatureSubscribe` ends on the server after its terminal notification.
- `blockSubscribe`, `voteSubscribe` and `slotsUpdatesSubscribe` are out of
  scope: Solana marks them unstable, and the first two need extra validator
  flags.

## Gotchas found while prototyping

- Apply third-party plugins only inside the convention plugins. A module that
  requests `org.jetbrains.kotlin.plugin.serialization` by ID without a version
  fails to resolve.
- KGP's ABI validation DSL still needs
  `@OptIn(ExperimentalAbiValidation::class)` from Kotlin code (2.4.21), and the
  filter block is `exclude { }`; `excluded { }` is deprecated. The Groovy
  `kotlin { abiValidation() }` used for the baseline needs no opt-in.
- `val x by sourceSets.creating { }` is deprecated in Gradle 9.8. Use
  `sourceSets.create("x") { }`.
- List `mavenCentral()` before `gradlePluginPortal()`. The portal redirects to
  Maven Central for many artifacts, and a failed redirect shows up as "Could
  not find ...jar".
- `./gradlew test --tests X` from the root fails in modules without a
  matching test. Use `./gradlew :sol4k:test --tests X`.
- The integration-test source set isn't associated with `main`, so it can't
  see `internal` declarations. Test public API there.
- A public type and an internal wire DTO with the same name in one package
  is a redeclaration error. Prefix the DTOs.
- Don't depend on `okhttp-jvm`: an Android app that also uses `okhttp` would
  get both `okhttp-jvm` and `okhttp-android` and fail with duplicate classes.
  Depending on `okhttp` makes Gradle pick the right one, and Gradle writes
  `okhttp-jvm` into the POM for Maven users automatically.
- Never edit `.api` files by hand; regenerate them with `updateKotlinAbi`.

## Out of scope

Possible follow-ups once the API has settled:

- `blockSubscribe`, `voteSubscribe`, `slotsUpdatesSubscribe`
- `programSubscribe` filters (`dataSize`, `memcmp`) and `jsonParsed` encoding
- `enableReceivedNotification` for `signatureSubscribe`
- Coroutines/Flow adapters, closing idle sockets, Ktor or JDK transports
- Extracting a `sol4k-core` module (only if the RPC layer gains a third-party
  dependency, users ask for an offline-only artifact, or a second RPC client
  is added)

## Appendix A: local-only Maven Central mirror

Use this only if Maven Central keeps answering HTTP 429. Save it outside the
repository and pass it with `./gradlew -I /path/to/mirror.init.gradle ...`.
Don't commit it.

```groovy
// Routes Maven Central through Google's read-only mirror for this machine only.
def mirror = 'https://maven-central.storage-download.googleapis.com/maven2/'

def rewrite = { RepositoryHandler repos ->
    repos.configureEach { r ->
        if (r instanceof MavenArtifactRepository && r.url.toString().startsWith('https://repo.maven.apache.org/maven2')) {
            r.url = mirror
        }
    }
}

beforeSettings { settings ->
    settings.pluginManagement.repositories {
        maven { url = mirror }
        gradlePluginPortal()
    }
    rewrite(settings.pluginManagement.repositories)
    rewrite(settings.dependencyResolutionManagement.repositories)
}

allprojects {
    rewrite(repositories)
    buildscript { rewrite(repositories) }
}
```
