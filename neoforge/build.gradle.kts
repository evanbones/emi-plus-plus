import me.modmuss50.mpp.ReleaseType

plugins {
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentNeoForge: Configuration by configurations.getting
@Suppress("UnstableApiUsage") configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentNeoForge.extendsFrom(common)
}

repositories {
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://modmaven.dev/")
    maven("https://jitpack.io")
    maven("https://api.modrinth.com/maven")
}

val neoForgeVersion: String by rootProject
val kotlinForForgeVersion: String by rootProject
val emiVersion: String by rootProject
val mixinExtrasVersion: String by rootProject
dependencies {
    neoForge("net.neoforged:neoforge:$neoForgeVersion")
    implementation("thedarkcolour:kotlinforforge-neoforge:$kotlinForForgeVersion") {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }
    modImplementation("mekanism:Mekanism:1.21.1-10.7.0.55")
    modImplementation("dev.emi:emi-neoforge:$emiVersion")

    implementation("io.github.llamalad7:mixinextras-neoforge:$mixinExtrasVersion")
    annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionNeoForge")) { isTransitive = false }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(
            mapOf(
                "version" to project.version,
            )
        )
    }
}

tasks.shadowJar {
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
    atAccessWideners.add("emixx-common.accesswidener")
}

tasks.withType<net.fabricmc.loom.task.RemapSourcesJarTask> {
    enabled = false
}

publishMods {
    file.set(tasks.remapJar.flatMap { it.archiveFile })
    changelog = rootProject.file("CHANGELOG-LATEST.md").readText()
    type = ReleaseType.STABLE
    displayName = "EMI++ NeoForge - ${project.version}"
    modLoaders.add("neoforge")

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = "1411826"
        minecraftVersions.add("1.21.1")

        requires { slug = "kotlin-for-forge" }
        requires { slug = "emi" }
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "N9WucjHL"
        minecraftVersions.add("1.21.1")

        requires { slug = "kotlin-for-forge" }
        requires { slug = "emi" }
    }
}