@file:Suppress("PropertyName")

import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.compiler
import org.jetbrains.gradle.ext.runConfigurations
import org.jetbrains.gradle.ext.settings

plugins {
    id("java")
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("eclipse")
    id("com.gtnewhorizons.retrofuturagradle") version "1.3.27"
}

val mod_version: String by project
val maven_group: String by project

version = mod_version
group = maven_group

val archives_base_name: String by project

val use_access_transformer: String by project

val use_mixins: String by project
val use_coremod: String by project
val use_assetmover: String by project

val include_mod: String by project
val coremod_plugin_class_name: String by project

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
        // Azul covers the most platforms for Java 8 toolchains, crucially including MacOS arm64
        vendor.set(JvmVendorSpec.AZUL)
    }
    // Generate sources and javadocs jars when building and publishing
    withSourcesJar()
    // withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

configurations {
    val embed = create("embed")
    implementation.configure {
        extendsFrom(embed)
    }
}

minecraft {
    mcVersion.set("1.12.2")

    // MCP Mappings
    mcpMappingChannel.set("stable")
    mcpMappingVersion.set("39")

    // Set username here, the UUID will be looked up automatically
    username.set("Developer")

    // Add any additional tweaker classes here
    // extraTweakClasses.add("org.spongepowered.asm.launch.MixinTweaker")

    // Add various JVM arguments here for runtime
    val args = mutableListOf("-ea:${group}")
    if (use_coremod.toBoolean()) {
        args += "-Dfml.coreMods.load=$coremod_plugin_class_name"
    }
    if (use_mixins.toBoolean()) {
        args += "-Dmixin.hotSwap=true"
        args += "-Dmixin.checks.interfaces=true"
        args += "-Dmixin.debug.export=true"
    }
    extraRunJvmArguments.addAll(args)

    // Include and use dependencies' Access Transformer files
    useDependencyAccessTransformers.set(true)

    // Add any properties you want to swap out for a dynamic value at build time here
    // Any properties here will be added to a class at build time, the name can be configured below
    injectedTags.put("VERSION", mod_version)
}

// Generate a group.archives_base_name.Tags class
tasks.injectTags.configure {
    // Change Tags class' name here:
    outputClassName.set("${maven_group}.Tags")
}

repositories {
    maven {
        name = "CleanroomMC Maven"
        url = uri("https://maven.cleanroommc.com")
    }
    maven {
        name = "SpongePowered Maven"
        url = uri("https://repo.spongepowered.org/maven")
    }
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
    maven { url = uri("https://dvs1.progwml6.com/files/maven") }
    maven { url = uri("https://maven.tterrag.com") }
    maven { url = uri("https://repo.elytradev.com/") }
    maven { url = uri("https://maven.mcmoddev.com") }
    maven { url = uri("https://maven.blamejared.com/") }
    maven { url = uri("https://maven.covers1624.net/") }
    mavenLocal() // Must be last for caching to work
}

dependencies {

    // OreLib
    // https://www.curseforge.com/minecraft/mc-mods/orelib/files
    implementation(rfg.deobf("curse.maven:OreLib-307806:2820815"))

    // Animania
    compileOnly("curse.maven:CraftStudioAPI-268704:2661859")
    compileOnly("com.animania:animania-1.12.2-base:2.0.3.28")
    compileOnly("com.animania:animania-1.12.2-farm:1.0.2.28")
    compileOnly("com.animania:animania-1.12.2-extra:1.0.2.28")

    // CosmeticArmorReworked
    compileOnly("curse.maven:CosmeticArmorReworked-237307:2660068")

    // Iron Chest
    compileOnly("curse.maven:IronChest-228756:2747935")

    // TheOneProbe
    compileOnly("curse.maven:TheOneProbe-245211:2667280")

    // JEI
    compileOnly("mezz.jei:jei_1.12.2:4.16.1.301")

    // MultiPart
    compileOnly("curse.maven:codechicken-lib-1-8-242818:2779849")
    compileOnly("curse.maven:ForgeMultipart-258426:2755791")

    compileOnly("curse.maven:chisel-235279:2915375")
    compileOnly("curse.maven:BiomesOPlenty-220318:2715506")
    compileOnly("curse.maven:CreativeCore-257814:3467576")
    compileOnly("curse.maven:LittleTiles-257818:3483878")
    compileOnly("curse.maven:SereneSeasons-291874:2799213")
    compileOnly("curse.maven:CTM-267602:2915363")

    if (use_assetmover.toBoolean()) {
        implementation("com.cleanroommc:assetmover:2.5")
    }
    if (use_mixins.toBoolean()) {
        implementation("zone.rong:mixinbooter:8.1")
    }

    if (use_mixins.toBoolean()) {
        val mixin = modUtils.enableMixins(
            /* mixinSpec = */ "org.spongepowered:mixin:0.8.3",
            /* refMapName = */ "mixins.dsurround.refmap.json"
        ) as String
        api(mixin) {
            isTransitive = true
        }
        annotationProcessor("org.ow2.asm:asm-debug-all:5.2")
        annotationProcessor("com.google.guava:guava:24.1.1-jre")
        annotationProcessor("com.google.code.gson:gson:2.8.6")
        annotationProcessor(mixin) {
            isTransitive = false
        }
    }
}

// Adds Access Transformer files to tasks
if (use_access_transformer.toBoolean()) {
    for (at in sourceSets.getByName("main").resources.files) {
        if (at.name.lowercase().endsWith("_at.cfg")) {
            tasks.deobfuscateMergedJarToSrg.get().accessTransformerFiles.from(at)
            tasks.srgifyBinpatchedJar.get().accessTransformerFiles.from(at)
        }
    }
}

@Suppress("UnstableApiUsage")
tasks.withType<ProcessResources> {
    // This will ensure that this task is redone when the versions change
    inputs.property("version", mod_version)
    inputs.property("mcversion", minecraft.mcVersion)

    // Replace various properties in mcmod.info and pack.mcmeta if applicable
    filesMatching(arrayListOf("mcmod.info", "pack.mcmeta")) {
        expand(
            "version" to mod_version,
            "mcversion" to minecraft.mcVersion
        )
    }

    if (use_access_transformer.toBoolean()) {
        rename("(.+_at.cfg)", "META-INF/$1") // Make sure Access Transformer files are in META-INF folder
    }
}

tasks.withType<Jar> {
    archiveBaseName.set("$archives_base_name-${minecraft.mcVersion.get()}")

    manifest {
        val attributeMap = mutableMapOf<String, String>()
        if (use_coremod.toBoolean()) {
            attributeMap["FMLCorePlugin"] = coremod_plugin_class_name
            if (include_mod.toBoolean()) {
                attributeMap["FMLCorePluginContainsFMLMod"] = true.toString()
                attributeMap["ForceLoadAsMod"] =
                    (project.gradle.startParameter.taskNames.getOrNull(0) == "build").toString()
            }
        }
        if (use_access_transformer.toBoolean()) {
            attributeMap["FMLAT"] = archives_base_name.lowercase() + "_at.cfg"
        }
        attributes(attributeMap)
    }
    // Add all embedded dependencies into the jar
    from(provider {
        configurations.getByName("embed").map {
            if (it.isDirectory()) it else zipTree(it)
        }
    })
}

idea {
    module {
        inheritOutputDirs = true
    }
    project {
        settings {
            runConfigurations {
                add(Gradle("1. Run Client").apply {
                    setProperty("taskNames", listOf("runClient"))
                })
                add(Gradle("2. Run Server").apply {
                    setProperty("taskNames", listOf("runServer"))
                })
                add(Gradle("3. Run Obfuscated Client").apply {
                    setProperty("taskNames", listOf("runObfClient"))
                })
                add(Gradle("4. Run Obfuscated Server").apply {
                    setProperty("taskNames", listOf("runObfServer"))
                })
            }
            compiler.javac {
                afterEvaluate {
                    javacAdditionalOptions = "-encoding utf8"
                    moduleJavacAdditionalOptions = mutableMapOf(
                        (project.name + ".main") to tasks.compileJava.get().options.compilerArgs.joinToString(" ") { "\"$it\"" }
                    )
                }
            }
        }
    }
}

tasks.named("processIdeaSettings").configure {
    dependsOn("injectTags")
}