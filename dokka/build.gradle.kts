plugins {
    id("io.github.smyrgeorge.readline4k.dokka")
}

dependencies {
    dokka(project(":readline4k"))
}

dokka {
    moduleName.set(rootProject.name)
}
