plugins {
    id("io.github.smyrgeorge.readline4k.multiplatform.binaries")
}

kotlin {
    @Suppress("unused")
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(project(":readline4k"))
            }
        }
    }
}
