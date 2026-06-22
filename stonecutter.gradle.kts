plugins {
    id("dev.kikugie.stonecutter")
    // Loom 1.17 splits into two plugin ids (both on maven.fabricmc.net) so obfuscated (1.21.x)
    // and unobfuscated (26.x) Minecraft can coexist in one build:
    //   net.fabricmc.fabric-loom-remap -> obfuscated, Mojmap remapping  (build.fabric-o)
    //   net.fabricmc.fabric-loom       -> unobfuscated, no remapping    (build.fabric-m)
    id("net.fabricmc.fabric-loom-remap") version "1.17.11" apply false
    id("net.fabricmc.fabric-loom") version "1.17.11" apply false
    id("net.neoforged.moddev") version "2.0.141" apply false
    id("me.modmuss50.mod-publish-plugin") version "0.7.4" apply false
}

stonecutter active file(".sc_active_version")

stonecutter parameters {
    // Expose the active loader as a Stitcher constant so code can use //? if fabric { ... }
    constants.match(current.project.substringAfterLast('-'), "fabric", "neoforge")
}

// Aggregate tasks across every version/loader node (used by CI).
tasks.register("chiseledBuild") {
    group = "build"
    description = "Builds every version and loader node."
    dependsOn(stonecutter.tasks.named("build"))
}
tasks.register("chiseledPublish") {
    group = "publishing"
    description = "Publishes every version and loader node to Modrinth and CurseForge."
    dependsOn(stonecutter.tasks.named("publishMods"))
}
