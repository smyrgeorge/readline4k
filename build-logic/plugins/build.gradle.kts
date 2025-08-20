plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("multiplatform.lib") {
            id = "io.github.smyrgeorge.readline4k.multiplatform.lib"
            implementationClass = "io.github.smyrgeorge.readline4k.multiplatform.MultiplatformLibConventions"
        }
        create("multiplatform.binaries") {
            id = "io.github.smyrgeorge.readline4k.multiplatform.binaries"
            implementationClass = "io.github.smyrgeorge.readline4k.multiplatform.MultiplatformBinariesConventions"
        }
        create("publish") {
            id = "io.github.smyrgeorge.readline4k.publish"
            implementationClass = "io.github.smyrgeorge.readline4k.publish.PublishConventions"
        }
    }
}

dependencies {
    compileOnly(libs.gradle.kotlin.plugin)
    compileOnly(libs.gradle.publish.plugin)
}
