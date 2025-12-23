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
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.

    compileOnly(project(":stub"))

    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    include(implementation(annotationProcessor("io.github.llamalad7:mixinextras-fabric:$mixinExtrasVersion")!!)!!)

//    implementation("dev.emi:emi-fabric:${emiVersion}")
//    modCompileOnly("dev.emi:emi-xplat-intermediary:$emiVersion")
    modCompileOnly("dev.emi:emi-xplat-intermediary:$emiVersion")

    modApi("fuzs.forgeconfigapiport:forgeconfigapiport-common:$forgeConfigApiPortVersion")

    modCompileOnly(libs.kubejs.neoforge)
}

tasks.remapJar {
    addNestedDependencies = false
}
