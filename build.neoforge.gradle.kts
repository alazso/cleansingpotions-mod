plugins {
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

val mcVersion = project.name.substringBeforeLast("-")
fun dep(key: String): String = property("${key}_$mcVersion") as String

// 26.x breaks ABI between drops and NeoForge has no intermediary cushion, so each patch is its
// own jar: scope the dependency range and the published game versions to this node's exact patch.
val nextMc = mcVersion.split(".").let { "${it[0]}.${it[1].toInt() + 1}" }
val neoMcRange = "[$mcVersion,$nextMc)"
val neoGameVersions = listOf(mcVersion)

// The 1.21.x-era replacement rules are gone with those nodes; see build.fabric-m.gradle.kts.

version = "${property("version")}+$mcVersion"
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
    // FastStats usage metrics.
    // gson is provided by Minecraft; drop FastStats's strict gson pin so it does not clash with MC's.
    implementation("dev.faststats.metrics:neoforge:0.27.0") {
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

// FastStats ships as a plain library (no mods.toml), so merge its classes into the jar for
// runtime (gson is excluded above, Minecraft provides it).
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

neoForge {
    // 26.x is unobfuscated, so there are no mappings and no parchment block.
    version = dep("neoforge")
    runs {
        register("client") { client() }
        register("server") { server() }
    }
    mods {
        register(property("mod_id") as String) {
            sourceSet(sourceSets["main"])
        }
    }
}

// 26.3 made brewing data driven and deleted PotionBrewing; see build.fabric-m.gradle.kts.
val brewingIsDataDriven = mcVersion.split(".")
    .let { it[0].toInt() > 26 || (it[0].toInt() == 26 && it[1].toInt() >= 3) }

val resourceProps = mapOf(
    "version" to property("version").toString(),
    "minecraft_version_range" to neoMcRange,
    "neoforge_loader_version_range" to "[4,)",
    "pack_format" to dep("pack_format"),
    "pack_format_minor" to dep("pack_format_minor"),
)
tasks.processResources {
    inputs.properties(resourceProps)
    if (!brewingIsDataDriven) {
        // 26.2: the minecraft:brewing recipe type does not exist yet; the mixin covers brewing.
        exclude("data/cleansingpotions/recipe/brewing/**")
    }
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(resourceProps)
    }
    filesMatching("cleansingpotions.mixins.json") {
        expand(mapOf("mixin_compat_level" to "JAVA_25"))
    }
}

publishMods {
    file = tasks.named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("jar").flatMap { it.archiveFile }
    type = me.modmuss50.mpp.ReleaseType.STABLE
    modLoaders.add("neoforge")
    displayName = "CleansingPotions ${property("version")} (NeoForge $mcVersion)"
    version = project.version.toString()
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
        optional { slug = "cloth-config" }
    }
}
