plugins {
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

val mcVersion = project.name.substringBeforeLast("-")
fun dep(key: String): String = property("${key}_$mcVersion") as String

// The nested FastStats mod requires this exact Minecraft version and FML treats a missing required
// dependency as fatal, so a wider range here could never load.
val neoMcRange = "[$mcVersion]"
val neoGameVersions = listOf(mcVersion)

// The 1.21.x-era replacement rules are gone with those nodes; see build.fabric.gradle.kts.

val modVersion = property("version").toString()
version = "$modVersion+$mcVersion"
group = property("mod_group")!!
base { archivesName = "${property("mod_id")}-neoforge" }

java {
    // 26.x runs on Java 25.
    toolchain.languageVersion = JavaLanguageVersion.of(25)
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.shedaniel.me/")
    maven("https://repo.faststats.dev/releases")
}

sourceSets {
    main {
        resources.srcDir(rootProject.file("src/neoforge/resources"))
    }
}

dependencies {
    // Cloth Config powers the in-game config screen, shared with Fabric.
    implementation("me.shedaniel.cloth:cloth-config-neoforge:${dep("cloth")}")
    // FastStats metrics and error tracking. Since SDK 0.29 the modded artifact is a mod in its own
    // right (id "faststats") and ships jar-in-jar instead of being merged into our classes.
    // gson is provided by Minecraft; drop FastStats's gson pin so it never shadows Minecraft's.
    implementation("dev.faststats.metrics:neoforge:${dep("faststats_neoforge")}") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    jarJar("dev.faststats.metrics:neoforge:${dep("faststats_neoforge")}")

    // Pure-core unit tests (no Minecraft types).
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

neoForge {
    // 26.x is unobfuscated, so there are no mappings and no parchment block.
    version = dep("neoforge")
    runs {
        register("client") { client() }
        register("server") { server() }
        // Dev runs never report to FastStats, so testing does not show up as real servers on the
        // dashboard. Pass -Pfaststats to a run task to exercise the integration anyway.
        configureEach {
            if (!providers.gradleProperty("faststats").isPresent) {
                systemProperty("faststats.enabled", "false")
            }
        }
    }
    mods {
        register(property("mod_id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

// 26.3 made brewing data driven and deleted PotionBrewing; see build.fabric.gradle.kts.
val brewingIsDataDriven = stonecutter.current.parsed >= "26.3"

val resourceProps = mapOf(
    "version" to project.version.toString(),
    "minecraft_version_range" to neoMcRange,
    "neoforge_loader_version_range" to "[4,)",
    "pack_format" to dep("pack_format"),
    "pack_format_minor" to dep("pack_format_minor"),
)
tasks.processResources {
    inputs.properties(resourceProps)
    if (brewingIsDataDriven) {
        // 26.3+: brewing is JSON and the mixin class is compiled out, so the config must not ship.
        exclude("cleansingpotions.mixins.json")
    } else {
        // 26.2: the minecraft:brewing recipe type does not exist yet; the mixin covers brewing.
        exclude("data/cleansingpotions/recipe/brewing/**")
    }
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(resourceProps)
    }
    if (brewingIsDataDriven) {
        // Drop the [[mixins]] table: NeoForge would otherwise look for a config that is not in the jar.
        filesMatching("META-INF/neoforge.mods.toml") {
            filter { line ->
                if (line == "[[mixins]]" || line.contains("cleansingpotions.mixins.json")) null else line
            }
        }
    }
    filesMatching("cleansingpotions.mixins.json") {
        expand(mapOf("mixin_compat_level" to "JAVA_25"))
    }
}

publishMods {
    file = tasks.named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("jar").flatMap { it.archiveFile }
    type = me.modmuss50.mpp.ReleaseType.STABLE
    modLoaders.add("neoforge")
    displayName = "CleansingPotions $modVersion (NeoForge $mcVersion)"
    version = "$modVersion+neoforge-$mcVersion"
    changelog = providers.environmentVariable("RELEASE_CHANGELOG")
        .orElse("See https://github.com/alazso/cleansingpotions-mod/blob/main/CHANGELOG.md")
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = property("modrinth_id").toString()
        minecraftVersions.addAll(neoGameVersions)
        optional("cloth-config")
    }
    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = property("curseforge_id").toString()
        minecraftVersions.addAll(neoGameVersions)
        client = true
        server = true
        optional { slug = "cloth-config" }
    }
}
