plugins {
    id("net.neoforged.moddev")
    id("me.modmuss50.mod-publish-plugin")
}

val mcVersion = project.name.substringBeforeLast("-")
fun dep(key: String): String = property("${key}_$mcVersion") as String

// The nested FastStats mod requires this exact Minecraft version and FML treats a missing required
// dependency as fatal, so a wider range here could never load.
val neoMcRange = "[$mcVersion]"

val modVersion = property("version").toString()
version = "$modVersion+$mcVersion"
group = property("mod_group")!!
base { archivesName = "${property("mod_id")}-neoforge" }

java {
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
    implementation("me.shedaniel.cloth:cloth-config-neoforge:${dep("cloth")}")
    // The FastStats artifact is a mod (id "faststats") and ships jar-in-jar. Minecraft provides gson,
    // so FastStats's pin is excluded.
    implementation("dev.faststats.metrics:neoforge:${dep("faststats_neoforge")}") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    jarJar("dev.faststats.metrics:neoforge:${dep("faststats_neoforge")}")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("org.assertj:assertj-core:3.27.7")
    // This test classpath has no Minecraft, so it does not get Minecraft's gson or slf4j.
    testImplementation("com.google.code.gson:gson:2.14.0")
    testRuntimeOnly("org.slf4j:slf4j-api:2.0.17")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

neoForge {
    version = dep("neoforge")
    runs {
        register("client") { client() }
        register("server") { server() }
        // Run tasks disable FastStats unless -Pfaststats is passed.
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

// PotionBrewing does not exist from 26.3. Those nodes ship JSON brewing recipes and no mixin.
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
        exclude("cleansingpotions.mixins.json")
    } else {
        // 26.2 has no minecraft:brewing recipe type, and loading these files logs errors.
        exclude("data/cleansingpotions/recipe/brewing/**")
        // The BrewingStand mixin classes exist only on Fabric.
        filesMatching("cleansingpotions.mixins.json") {
            filter { line -> if (line.contains("BrewingStand")) null else line }
        }
    }
    filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
        expand(resourceProps)
    }
    if (brewingIsDataDriven) {
        // Removes the [[mixins]] table, because these jars ship no mixin config.
        filesMatching("META-INF/neoforge.mods.toml") {
            filter { line ->
                if (line == "[[mixins]]" || line.contains("cleansingpotions.mixins.json")) null else line
            }
        }
    }
}

publishMods {
    file = tasks.named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("jar").flatMap { it.archiveFile }
    type = me.modmuss50.mpp.ReleaseType.STABLE
    modLoaders.add("neoforge")
    displayName = "CleansingPotions $modVersion (NeoForge $mcVersion)"
    version = "$modVersion+neoforge-$mcVersion"
    changelog = providers.environmentVariable("RELEASE_CHANGELOG")
        .filter { it.isNotBlank() }
        .orElse("See https://github.com/alazso/cleansingpotions-mod/blob/main/CHANGELOG.md")
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = property("modrinth_id").toString()
        minecraftVersions.add(mcVersion)
        optional("cloth-config")
    }
    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = property("curseforge_id").toString()
        minecraftVersions.add(mcVersion)
        client = true
        server = true
        optional { slug = "cloth-config" }
    }
}
