import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    kotlin("jvm") version "2.2.0"
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.9-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "8.3.6" apply false
}

val minecraftVersion: String by project
architectury {
    minecraft = minecraftVersion
}

val mavenGroup: String by project
val modVersion: String by project
allprojects {
    group = mavenGroup
    version = modVersion
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "architectury-plugin")
    apply(plugin = "dev.architectury.loom")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xjdk-release=17")
        }
    }

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom").apply {
        silentMojangMappingsLicense()
    }

    val archivesName: String by project
    base {
        this.archivesName = "$archivesName-${project.name}"
    }

    repositories {
        maven("https://maven.parchmentmc.org") // ParchmentMC
        maven("https://maven.minecraftforge.net/releases/") // Forge
        maven("https://maven.terraformersmc.com/") // EMI
        maven("https://jitpack.io/") // Animated GIF
        maven("https://maven.latvian.dev/releases/") // KubeJS, Rhino
        maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // Forge Config API Port
    }

    dependencies {
        val minecraftVersion: String by project
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
        @Suppress("UnstableApiUsage") "mappings"(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-1.20.1:2023.09.03@zip")
        })
    }

}