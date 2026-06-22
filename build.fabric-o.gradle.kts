plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("me.modmuss50.mod-publish-plugin")
}

val mcVersion = project.name.substringBeforeLast("-")
fun dep(key: String): String = property("${key}_$mcVersion") as String

stonecutter {
    // Minecraft 1.21.11 renamed ResourceLocation -> Identifier (and location() -> identifier()).
    replacements.string(current.parsed >= "1.21.11") {
        replace("ResourceLocation", "Identifier")
        replace("location()", "identifier()")
    }
}

version = "${property("version")}+1.21.9-1.21.11"
group = property("mod_group")!!
base { archivesName = "${property("mod_id")}-fabric" }

java {
    toolchain.languageVersion = JavaLanguageVersion.of((property("java_version") as String).toInt())
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.parchmentmc.org/")
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("src/fabric/resources"))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        findProperty("parchment_$mcVersion")?.let {
            parchment("org.parchmentmc.data:parchment-$mcVersion:$it@zip")
        }
    })
    modImplementation("net.fabricmc:fabric-loader:${dep("fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${dep("fabric_api")}")
    modImplementation("com.terraformersmc:modmenu:${dep("modmenu")}")
    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${dep("cloth")}")

    // Pure-core unit tests (no Minecraft types).
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

loom {
    mixin {
        useLegacyMixinAp = true
    }
}

val resourceProps = mapOf(
    "version" to property("version").toString(),
    "fabric_loader_version" to dep("fabric_loader"),
    "fabric_minecraft_version_range" to ">=1.21.9 <1.22",
)
tasks.processResources {
    inputs.properties(resourceProps)
    filesMatching("fabric.mod.json") {
        expand(resourceProps)
    }
    filesMatching("cleansingpotions.mixins.json") {
        expand(mapOf("mixin_compat_level" to "JAVA_21"))
    }
}

publishMods {
    file = tasks.named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("remapJar").flatMap { it.archiveFile }
    type = me.modmuss50.mpp.ReleaseType.STABLE
    modLoaders.add("fabric")
    displayName = "CleansingPotions ${property("version")} (Fabric 1.21.9-1.21.11)"
    version = project.version.toString()
    changelog = providers.environmentVariable("RELEASE_CHANGELOG")
        .orElse("See https://github.com/alazso/cleansingpotions/blob/main/CHANGELOG.md")
    // Publishes for real only when tokens are present (CI); a local run is a dry run.
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = property("modrinth_id").toString()
        minecraftVersions.addAll("1.21.9", "1.21.10", "1.21.11")
        requires("fabric-api")
        optional("cloth-config")
        optional("modmenu")
    }
    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = property("curseforge_id").toString()
        minecraftVersions.addAll("1.21.9", "1.21.10", "1.21.11")
        requires { slug = "fabric-api" }
        optional { slug = "cloth-config" }
        optional { slug = "modmenu" }
    }
}
