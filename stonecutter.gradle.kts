plugins {
    id("dev.kikugie.stonecutter")
    // Minecraft 26.x is unobfuscated, so the plain loom plugin applies with no remapping step.
    // (The obfuscated variant, net.fabricmc.fabric-loom-remap, went away with 1.21.x in 0.3.0.)
    id("net.fabricmc.fabric-loom") version "1.19.3" apply false
    id("net.neoforged.moddev") version "2.0.148" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply false
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
