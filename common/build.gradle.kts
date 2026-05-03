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
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")!!)

    modCompileOnly("dev.emi:emi-xplat-intermediary:$emiVersion")

    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-common-neoforgeapi:$forgeConfigApiPortVersion")

    modCompileOnly(libs.kubejs.neoforge)
}

tasks.remapJar {
    addNestedDependencies = false
}