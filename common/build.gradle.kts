val enabledPlatforms: String by rootProject
architectury {
    common(enabledPlatforms.split(","))
}

loom {
    accessWidenerPath = file("src/main/resources/emixx-common.accesswidener")
}

val fabricLoaderVersion: String by rootProject
val mixinExtrasVersion: String by rootProject
val emiVersion: String by rootProject
val forgeConfigApiPortVersion: String by rootProject

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:$mixinExtrasVersion")!!)!!)

    modCompileOnly("dev.emi:emi-xplat-intermediary:$emiVersion")

    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-common:$forgeConfigApiPortVersion")

    modCompileOnly(libs.kubejs.forge)
}

tasks.remapJar {
    addNestedDependencies = false
}
