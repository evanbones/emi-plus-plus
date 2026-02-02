import me.modmuss50.mpp.ReleaseType

plugins {
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath

    forge {
        mixinConfig("emixx-common.mixins.json")
        mixinConfig("emixx.mixins.json")
    }
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentForge: Configuration by configurations.getting
@Suppress("UnstableApiUsage") configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentForge.extendsFrom(common)
}

repositories {
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://modmaven.dev/")
    maven("https://jitpack.io")
    maven("https://api.modrinth.com/maven")
}

val forgeVersion: String by project
val kotlinForForgeVersion: String by project
val emiVersion: String by project
val mixinExtrasVersion: String by project
dependencies {
    forge("net.minecraftforge:forge:$forgeVersion")
    implementation("thedarkcolour:kotlinforforge:$kotlinForForgeVersion")

    modCompileOnly("maven.modrinth:mekanism:uxe1WQp4")
    modImplementation("dev.emi:emi-forge:$emiVersion")
    modImplementation(libs.kubejs.forge)

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")!!)
    implementation(include("io.github.llamalad7:mixinextras-forge:$mixinExtrasVersion")!!)

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionForge")) { isTransitive = false }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("META-INF/mods.toml") {
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
    displayName = "EMI++ Forge - ${project.version}"
    modLoaders.add("forge")

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = "1411826"
        minecraftVersions.add("1.20.1")

        requires { slug = "kotlin-for-forge" }
        requires { slug = "emi" }
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "N9WucjHL"
        minecraftVersions.add("1.20.1")

        requires { slug = "kotlin-for-forge" }
        requires { slug = "emi" }
    }
}