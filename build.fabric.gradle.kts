plugins {
    id("net.fabricmc.fabric-loom")
    id("me.modmuss50.mod-publish-plugin")
}

val mcVersion = project.name.substringBeforeLast("-")
fun dep(key: String): String = property("${key}_$mcVersion") as String

// 26.x breaks ABI between drops, so a jar declares only its own drop. The trailing hyphen also
// excludes the next drop's prereleases.
val nextMc = mcVersion.split(".").let { "${it[0]}.${it[1].toInt() + 1}" }
val fabricMcRange = ">=$mcVersion <$nextMc-"

val modVersion = property("version").toString()
version = "$modVersion+$mcVersion"
group = property("mod_group")!!
base { archivesName = "${property("mod_id")}-fabric" }

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
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
    implementation("net.fabricmc:fabric-loader:${dep("fabric_loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${dep("fabric_api")}")
    implementation("com.terraformersmc:modmenu:${dep("modmenu")}")
    implementation("me.shedaniel.cloth:cloth-config-fabric:${dep("cloth")}")
    // The FastStats artifact is a mod (id "faststats") and ships jar-in-jar. Minecraft provides gson,
    // so FastStats's pin is excluded.
    implementation("dev.faststats.metrics:fabric:${dep("faststats_fabric")}") {
        exclude(group = "com.google.code.gson", module = "gson")
    }
    include("dev.faststats.metrics:fabric:${dep("faststats_fabric")}")

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.3")
    testImplementation("org.assertj:assertj-core:3.27.7")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// Run tasks disable FastStats unless -Pfaststats is passed.
loom {
    runs {
        configureEach {
            if (!providers.gradleProperty("faststats").isPresent) {
                systemProperties.put("faststats.enabled", "false")
            }
        }
    }
}

// Adds the gametest source set and the gameTest run, and makes check depend on that run.
fabricApi {
    configureTests {
        createSourceSet = true
        modId = "cleansingpotions-gametest"
        enableClientGameTests = false
    }
}

loom {
    runs {
        named("gameTest") {
            systemProperties.put("fabric-api.gametest.report-file",
                layout.buildDirectory.file("reports/gametest/TEST-gametest.xml").map { it.asFile.absolutePath })
            providers.gradleProperty("gameTestFilter").orNull?.let {
                systemProperties.put("fabric-api.gametest.filter", it)
            }
        }
    }
}

// Loom clears only the client test run directory. The server run would keep its config and world.
val cleanGameTestRun = tasks.register<Delete>("cleanGameTestRun") {
    delete(layout.buildDirectory.dir("run/gameTest"))
}
tasks.named("runGameTest") {
    dependsOn(cleanGameTestRun)
}

// PotionBrewing does not exist from 26.3. Those nodes ship JSON brewing recipes and no mixin.
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
        exclude("cleansingpotions.mixins.json")
    } else {
        // 26.2 has no minecraft:brewing recipe type, and loading these files logs errors.
        exclude("data/cleansingpotions/recipe/brewing/**")
    }
    filesMatching(listOf("fabric.mod.json", "pack.mcmeta")) {
        expand(resourceProps)
    }
    if (brewingIsDataDriven) {
        // fabric.mod.json must stay valid JSON in source for Fabric Loader's dev-time scan, so the
        // entry is filtered out.
        filesMatching("fabric.mod.json") {
            filter { line -> if (line.contains("cleansingpotions.mixins.json")) null else line }
        }
    }
}

publishMods {
    // Loom does not remap on 26.x, so the jar task output is the final artifact.
    file = tasks.named<org.gradle.api.tasks.bundling.AbstractArchiveTask>("jar").flatMap { it.archiveFile }
    type = me.modmuss50.mpp.ReleaseType.STABLE
    modLoaders.add("fabric")
    displayName = "CleansingPotions $modVersion (Fabric $mcVersion)"
    // A Fabric and a NeoForge jar share a game version, so the version number names the loader.
    version = "$modVersion+fabric-$mcVersion"
    changelog = providers.environmentVariable("RELEASE_CHANGELOG")
        .filter { it.isNotBlank() }
        .orElse("See https://github.com/alazso/cleansingpotions-mod/blob/main/CHANGELOG.md")
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").getOrNull() == null

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = property("modrinth_id").toString()
        minecraftVersions.add(mcVersion)
        requires("fabric-api")
        optional("cloth-config")
        optional("modmenu")
    }
    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_API_KEY")
        projectId = property("curseforge_id").toString()
        minecraftVersions.add(mcVersion)
        client = true
        server = true
        requires { slug = "fabric-api" }
        optional { slug = "cloth-config" }
        optional { slug = "modmenu" }
    }
}
