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
