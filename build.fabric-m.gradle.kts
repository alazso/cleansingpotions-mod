plugins {
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

val mcVersion = project.name.substringBeforeLast("-")
fun dep(key: String): String = property("${key}_$mcVersion") as String

stonecutter {
    // 1.21.11+ (including unobfuscated 26.x) renamed ResourceLocation -> Identifier and moved the
    // throwable-item projectile classes into a .throwableitemprojectile subpackage.
    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
        replace("location()", "identifier()")
        replace("projectile.ThrowableItemProjectile", "projectile.throwableitemprojectile.ThrowableItemProjectile")
    }
    // Minecraft 26.x (unobfuscated) API renames are added here as the compiler flags them.
    // This is server content (items/effects/brewing/entities), so duraping's client-only
    // renames (HUD/keybind/displayClientMessage) do not apply.
}

version = "${property("version")}+$mcVersion"
group = property("mod_group")!!
base { archivesName = "${property("mod_id")}-fabric" }

java {
    // 26.x runs on Java 25.
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://repo.faststats.dev/releases")
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("src/fabric/resources"))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    // Unobfuscated Minecraft: no mappings() block (officialMojangMappings does not exist for 26.x),
    // and dependencies are used as-is (plain implementation, no remap).
    implementation("net.fabricmc:fabric-loader:${dep("fabric_loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${dep("fabric_api")}")
    implementation("com.terraformersmc:modmenu:${dep("modmenu")}")
    implementation("me.shedaniel.cloth:cloth-config-fabric:${dep("cloth")}")
    // FastStats usage metrics (requires Java 25; this modern buildscript is 26.x only).
    // gson is provided by Minecraft; drop FastStats's strict gson pin so it does not clash with MC's.
    implementation("dev.faststats.metrics:fabric:0.27.0") {
        exclude(group = "com.google.code.gson", module = "gson")
    }

    // Pure-core unit tests (no Minecraft types).
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// FastStats ships as a plain library (no fabric.mod.json), so merge its classes into the jar
// for runtime (gson is excluded above; Minecraft provides it).
tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from({
        configurations.runtimeClasspath.get()
                .filter { it.path.contains("dev.faststats") }
                .map { zipTree(it) }
    }) {
        exclude("module-info.class", "META-INF/MANIFEST.MF")
    }
}

val resourceProps = mapOf(
    "version" to property("version").toString(),
    "fabric_loader_version" to dep("fabric_loader"),
    "fabric_minecraft_version_range" to ">=26.1.2 <26.3",
)
tasks.processResources {
    inputs.properties(resourceProps)
    filesMatching("fabric.mod.json") {
        expand(resourceProps)
    }
    filesMatching("cleansingpotions.mixins.json") {
        expand(mapOf("mixin_compat_level" to "JAVA_25"))
    }
}

publishMods {
    // No-remap loom: the final artifact is the jar task (no remapJar), with FastStats bundled in.
    file = tasks.named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("jar").flatMap { it.archiveFile }
    type = me.modmuss50.mpp.ReleaseType.STABLE
    modLoaders.add("fabric")
    displayName = "CleansingPotions ${property("version")} (Fabric $mcVersion)"
    // Distinct version number from the NeoForge 26.x jar (both share the 26.2 game version).
    version = "${property("version")}+fabric-$mcVersion"
    changelog = providers.environmentVariable("RELEASE_CHANGELOG")
        .orElse("See https://github.com/alazso/cleansingpotions-mod/blob/main/CHANGELOG.md")
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = property("modrinth_id").toString()
        minecraftVersions.addAll("26.1.2", "26.2")
        requires("fabric-api")
        optional("cloth-config")
        optional("modmenu")
    }
    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = property("curseforge_id").toString()
        minecraftVersions.addAll("26.1.2", "26.2")
        requires { slug = "fabric-api" }
        optional { slug = "cloth-config" }
        optional { slug = "modmenu" }
    }
}
