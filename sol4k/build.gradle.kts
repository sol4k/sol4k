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
