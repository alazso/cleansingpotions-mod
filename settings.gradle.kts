pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.8"
}

rootProject.name = "CleansingPotions"

stonecutter {
    create(rootProject) {
        // 26.x breaks ABI between drops, so each drop compiles against its own Minecraft.
        version("26.2-fabric", "26.2").buildscript = "build.fabric.gradle.kts"
        version("26.2-neoforge", "26.2").buildscript = "build.neoforge.gradle.kts"
        version("26.3-fabric", "26.3").buildscript = "build.fabric.gradle.kts"
        version("26.3-neoforge", "26.3").buildscript = "build.neoforge.gradle.kts"

        vcsVersion = "26.2-fabric"
    }
}
