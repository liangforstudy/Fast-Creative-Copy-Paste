// Forge, only for 1.20.1 (NeoForge barely existed on that version). ModDevGradle's legacy variant.
plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
    id("neoforge-mutex")
}

version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-forge"

val requiredJava = JavaVersion.VERSION_17
val modId = property("mod.id") as String
val refmap = "$modId.refmap.json"

legacyForge {
    version = property("deps.forge") as String

    mods {
        register(modId) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = file("../../run/")
            client()
        }
    }
}

// Forge 1.20.1 runs obfuscated, so mixins need a refmap
mixin {
    add(sourceSets.main.get(), refmap)
    config("$modId.mixins.json")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("minecraft", sc.properties["mod.mc_compat"])
        }

        filesMatching("META-INF/mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") {
            expand("java" to mixinJava)
            filter { it.replace("\"package\":", "\"refmap\": \"$refmap\",\n  \"package\":") }
        }

        exclude("fabric.mod.json", "META-INF/neoforge.mods.toml")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    // MIT requires the license text to travel with copies of the mod
    withType<Jar> {
        inputs.property("mod_id", modId)
        from("../../LICENSE") { rename { "$it-$modId" } }
    }

    // Forge only finds mixin configs listed in the manifest
    jar { manifest.attributes("MixinConfigs" to "$modId.mixins.json") }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies them to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(named("reobfJar")) // The jar Forge can actually load, with SRG names
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
