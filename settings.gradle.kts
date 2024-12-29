pluginManagement {
    plugins {
        kotlin("jvm") version "1.9.22"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "sustc"

include(
    "sustc-api",
    "sustc-runner",
)

dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        mavenCentral()
        gradlePluginPortal()
    }
}
