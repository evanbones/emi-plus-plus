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
    maven("https://jitpack.io")
}

val forgeVersion: String by project
val kotlinForForgeVersion: String by project
val emiVersion: String by project
val mixinExtrasVersion: String by project
dependencies {
    forge("net.neoforged:forge:$forgeVersion")
    implementation("thedarkcolour:kotlinforforge:$kotlinForForgeVersion") {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }
    modImplementation("dev.emi:emi-forge:$emiVersion")
    modImplementation("mekanism:Mekanism:1.20.1-10.4.9.61")

    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")!!)
    implementation(include("io.github.llamalad7:mixinextras-forge:$mixinExtrasVersion")!!)

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