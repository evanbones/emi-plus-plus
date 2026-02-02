import me.modmuss50.mpp.ReleaseType

plugins {
    id("com.gradleup.shadow")
    id("me.modmuss50.mod-publish-plugin")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentFabric: Configuration by configurations.getting
@Suppress("UnstableApiUsage") configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentFabric.extendsFrom(common)
}

val fabricLoaderVersion: String by rootProject
val forgeConfigApiPortVersion: String by project
val forgeVersion: String by project
dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation(libs.fabric.kotlin)
    modImplementation("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:$forgeConfigApiPortVersion")
    modImplementation(libs.emi.fabric)

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionFabric")) { isTransitive = false }
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
            )
        )
    }

    val common = project(":common")
    from(common.sourceSets.main.get().resources) {
        include("emixx-common.accesswidener")
    }
}

tasks.shadowJar {
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    inputFile.set(tasks.shadowJar.get().archiveFile)
}

publishMods {
    file.set(tasks.remapJar.flatMap { it.archiveFile })
    changelog = rootProject.file("CHANGELOG-LATEST.md").readText()
    type = ReleaseType.STABLE
    displayName = "Emi++ Fabric - ${project.version}"
    modLoaders.add("fabric")

    curseforge {
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        projectId = "1411826"
        minecraftVersions.add("1.20.1")

        requires { slug = "fabric-api" }
        requires { slug = "fabric-language-kotlin" }
        requires { slug = "emi" }
    }

    modrinth {
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        projectId = "N9WucjHL"
        minecraftVersions.add("1.20.1")

        requires { slug = "fabric-api" }
        requires { slug = "fabric-language-kotlin" }
        requires { slug = "emi" }
    }
}