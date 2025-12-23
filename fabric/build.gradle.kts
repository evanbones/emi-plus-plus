plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

loom {
    accessWidenerPath = file("src/main/resources/emixx-common.accesswidener")
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

tasks.register<Copy>("copyAccessWidener") {
    from(project(":common").loom.accessWidenerPath)
    into("src/main/resources/")
}

tasks.named("validateAccessWidener") {
    dependsOn("copyAccessWidener")
}

tasks.processResources {
    dependsOn("copyAccessWidener")
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
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
}
