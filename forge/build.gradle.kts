plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    forge()
}

loom {
    accessWidenerPath = project(":common").loom.accessWidenerPath
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
}

val neoForgeVersion: String by project
val kotlinForForgeVersion: String by project
val emiVersion: String by project
dependencies {
    forge("net.neoforged:forge:$neoForgeVersion")
    implementation("thedarkcolour:kotlinforforge:$kotlinForForgeVersion") {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }
    modImplementation("dev.emi:emi-forge:$emiVersion")
    modImplementation("mekanism:Mekanism:1.21.1-10.7.0.55")

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionForge")) { isTransitive = false }

//    modImplementation("dev.latvian.apps:tiny-java-server:1.0.0-build.26")
//    modImplementation(libs.rhino)
//    modImplementation(libs.kubejs.forge)
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