rootProject.name = "readline4k"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }

    includeBuild("build-logic")
}

include("readline4k")

include("dokka")
include("examples")
