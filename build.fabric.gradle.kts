plugins {
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

val mcVersion = project.name.substringBeforeLast("-")
fun dep(key: String): String = property("${key}_$mcVersion") as String

// 26.x breaks ABI between drops and has no intermediary cushion, so each patch is its own jar:
// scope the dependency range and the published game versions to this node's exact patch.
val nextMc = mcVersion.split(".").let { "${it[0]}.${it[1].toInt() + 1}" }
val fabricMcRange = ">=$mcVersion <$nextMc-"
val publishGameVersions = listOf(mcVersion)

// The 1.21.x-era replacement rules (ResourceLocation -> Identifier, the throwableitemprojectile
// package move, getToastManager -> gui.toastManager) are gone: src/ now holds the 26.x names
// directly, and every remaining node shares them. Re-adding one would be actively wrong, since
// "projectile.ThrowableItemProjectile" is a substring of the already-replaced package path.

val modVersion = property("version").toString()
version = "$modVersion+$mcVersion"
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
    // FastStats metrics and error tracking. Since SDK 0.29 the modded artifact is a mod in its own
    // right (id "faststats") and ships jar-in-jar instead of being merged into our classes.
    // gson is provided by Minecraft; drop FastStats's gson pin so it never shadows Minecraft's.
    implementation("dev.faststats.metrics:fabric:${dep("faststats_fabric")}") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    include("dev.faststats.metrics:fabric:${dep("faststats_fabric")}")

    // Pure-core unit tests (no Minecraft types).
    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Dev runs never report to FastStats, so testing does not show up as real servers on the dashboard.
// Pass -Pfaststats to a run task to exercise the integration anyway.
loom {
    runs {
        configureEach {
            if (!providers.gradleProperty("faststats").isPresent) {
                systemProperties.put("faststats.enabled", "false")
            }
        }
    }
}

// 26.3 made brewing data driven and deleted PotionBrewing, so the one mixin the mod has is
// 26.2-only. From 26.3 on, the brewing recipes ship as JSON and the jar carries no mixin at all.
val brewingIsDataDriven = stonecutter.current.parsed >= "26.3"

val resourceProps = mapOf(
    "version" to project.version.toString(),
    "fabric_loader_version" to property("fabric_loader_min").toString(),
    "fabric_minecraft_version_range" to fabricMcRange,
    "pack_format" to dep("pack_format"),
    "pack_format_minor" to dep("pack_format_minor"),
)
tasks.processResources {
    inputs.properties(resourceProps)
    if (brewingIsDataDriven) {
        // 26.3+: brewing is JSON, so the mixin config has nothing to apply.
        exclude("cleansingpotions.mixins.json")
    } else {
        // 26.2: the minecraft:brewing recipe type does not exist yet, so shipping these would
        // only log unknown-recipe-type errors. The mixin covers brewing there instead.
        exclude("data/cleansingpotions/recipe/brewing/**")
    }
    filesMatching(listOf("fabric.mod.json", "pack.mcmeta")) {
        expand(resourceProps)
    }
    if (brewingIsDataDriven) {
        // Drop the mixin config reference, leaving an empty "mixins" array. Done by dropping the
        // line rather than templating the whole array, so the source file stays valid JSON: an
        // unquoted ${...} there breaks Fabric Loader's dev-time scan and every JSON tool.
        filesMatching("fabric.mod.json") {
            filter { line -> if (line.contains("cleansingpotions.mixins.json")) null else line }
        }
    }
}

publishMods {
    // No-remap loom: the final artifact is the jar task (no remapJar), with FastStats nested inside.
    file = tasks.named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("jar").flatMap { it.archiveFile }
    type = me.modmuss50.mpp.ReleaseType.STABLE
    modLoaders.add("fabric")
    displayName = "CleansingPotions $modVersion (Fabric $mcVersion)"
    // Distinct version number from the NeoForge 26.x jar (both share the 26.2 game version).
    version = "$modVersion+fabric-$mcVersion"
    changelog = providers.environmentVariable("RELEASE_CHANGELOG")
        .orElse("See https://github.com/alazso/cleansingpotions-mod/blob/main/CHANGELOG.md")
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = property("modrinth_id").toString()
        minecraftVersions.addAll(publishGameVersions)
        requires("fabric-api")
        optional("cloth-config")
        optional("modmenu")
    }
    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = property("curseforge_id").toString()
        minecraftVersions.addAll(publishGameVersions)
        client = true
        server = true
        requires { slug = "fabric-api" }
        optional { slug = "cloth-config" }
        optional { slug = "modmenu" }
    }
}
