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
