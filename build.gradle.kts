plugins {
    idea
    java
    id("gg.essential.loom") version "1.3.12"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.22"
}

val modName: String by project
val modID: String by project
val modVersion: String by project

version = modVersion
group = modID

sourceSets.main {
    output.setResourcesDir(file("${layout.buildDirectory.asFile.get()}/classes/kotlin/main"))
}

loom {
    silentMojangMappingsLicense()
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.${modID}.json")
    }
}

repositories {
    maven("https://repo.spongepowered.org/maven/")
    maven("https://repo.polyfrost.cc/releases")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")
    compileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.0-alpha+")
    shadowImpl("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+")
}

tasks {
    processResources {
        inputs.property("modname", modName)
        inputs.property("modid", modID)
        inputs.property("version", version)
        inputs.property("mcversion", "1.8.9")

        filesMatching(listOf("mcmod.info", "mixins.${modID}.json")) {
            expand(
                mapOf(
                    "modname" to modName,
                    "modid" to modID,
                    "version" to version,
                    "mcversion" to "1.8.9"
                )
            )
        }
        dependsOn(compileJava)
    }
    jar {
        manifest.attributes(
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "MixinConfigs" to "mixins.${modID}.json",
            "ModSide" to "CLIENT",
            "TweakOrder" to "0",
            "TweakClass" to "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker"
        )
        dependsOn(shadowJar)
        enabled = false
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
    }
    shadowJar {
        configurations = listOf(shadowImpl)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        exclude(
            "**/LICENSE.md",
            "**/LICENSE.txt",
            "**/LICENSE",
            "**/NOTICE",
            "**/NOTICE.txt",
            "pack.mcmeta",
            "dummyThing",
            "**/module-info.class",
            "META-INF/proguard/**",
            "META-INF/maven/**",
            "META-INF/versions/**",
            "META-INF/Neion.kotlin_module",
            "META-INF/com.android.tools/**",
            "fabric.mod.json"
        )
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
tasks.assemble.get().dependsOn(tasks.remapJar)