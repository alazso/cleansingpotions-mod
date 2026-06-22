pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "Fabric" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.2"
}

rootProject.name = "CleansingPotions"

stonecutter {
    create(rootProject) {
        // Fabric obfuscated (1.21.x): one jar covers 1.21.9-1.21.11; intermediary mappings make
        // the 1.21.11 ResourceLocation->Identifier rename transparent at runtime.
        version("1.21.9-fabric", "1.21.9").buildscript = "build.fabric-o.gradle.kts"
        // NeoForge runs against Mojmap with no intermediary cushion, so the rename forces a split:
        // one jar for 1.21.9-1.21.10, one for 1.21.11.
        version("1.21.9-neoforge", "1.21.9").buildscript = "build.neoforge.gradle.kts"
        version("1.21.11-neoforge", "1.21.11").buildscript = "build.neoforge.gradle.kts"
        // Minecraft 26.x is unobfuscated (Java 25). Build against the era's lowest version, 26.1.2, so
        // one jar runs across 26.1.2-26.2 (compiling against the lowest keeps it forward-compatible).
        version("26.1.2-fabric", "26.1.2").buildscript = "build.fabric-m.gradle.kts"
        version("26.1.2-neoforge", "26.1.2").buildscript = "build.neoforge.gradle.kts"

        vcsVersion = "1.21.9-fabric"
    }
}
