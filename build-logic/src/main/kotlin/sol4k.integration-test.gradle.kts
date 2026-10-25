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
