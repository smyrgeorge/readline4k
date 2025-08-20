plugins {
    id("io.github.smyrgeorge.readline4k.multiplatform.lib")
    id("io.github.smyrgeorge.readline4k.publish")
}

kotlin {
    @Suppress("unused")
    sourceSets {
        configureEach {
            languageSettings.progressiveMode = true
        }
        val nativeMain by getting {
            dependencies {
                implementation(libs.kotlinx.io.core)
            }
        }
        val nativeTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.assertk)
            }
        }
    }
}
