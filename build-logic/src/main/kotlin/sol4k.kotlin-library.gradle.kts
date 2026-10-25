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
