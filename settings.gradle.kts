pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.2"
}

rootProject.name = "CleansingPotions"

stonecutter {
    create(rootProject) {
        // Minecraft 26.x is unobfuscated (Java 25) and has no intermediary cushion, so a patch
        // with breaking ABI changes cannot share a jar with its neighbours: each compiles against
        // its own Minecraft. 1.21.x and 26.1.2 were retired in 0.3.0.
        version("26.2-fabric", "26.2").buildscript = "build.fabric-m.gradle.kts"
        version("26.2-neoforge", "26.2").buildscript = "build.neoforge.gradle.kts"
        // NeoForge 26.3 is still 26.3.0.1-beta, so only Fabric ships for 26.3 for now.
        version("26.3-fabric", "26.3").buildscript = "build.fabric-m.gradle.kts"

        vcsVersion = "26.2-fabric"
    }
}
