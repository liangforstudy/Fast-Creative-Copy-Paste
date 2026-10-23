plugins {
    // Applies the correct Loom variant based on the Minecraft version
    id("dev.kikugie.loom-back-compat")
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-fabric"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

repositories {
    // Restricts dependency search of the given groups to one maven, which speeds up setup
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Mojang mappings on obfuscated versions; 26.1+ ships unobfuscated
    loomx.applyMojangMappings()

    // `mod{dependency type}` works on 26.1+ too: loom-back-compat converts them
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    // All of Fabric API, like a real install: with only some modules the dev client can
    // behave differently (missing language files, for one)
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
    // Dev client only, never shipped: MCPFabric lets Claude drive the game over MCP (see README)
    findProperty("deps.mcpfabric")?.let { modLocalRuntime("maven.modrinth:mcpfabric:$it") }
    // Optional: Mod Menu's settings button (ModMenuIntegration). Compiled against, not shipped;
    // also in the dev client to try the button
    modCompileOnly("maven.modrinth:modmenu:${property("deps.modmenu")}")
    modLocalRuntime("maven.modrinth:modmenu:${property("deps.modmenu")}")
    // Dev client only: Ctrl-Z (undo/redo), to test alongside it; needs Vanilib
    findProperty("deps.ctrlz")?.let { modLocalRuntime("maven.modrinth:kFgIgZ37:$it") }
    findProperty("deps.vanilib")?.let { modLocalRuntime("maven.modrinth:TpZlEkF6:$it") }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Names lambdas, useful for mixins
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Shared between versions
        jvmArguments.add("-Dmixin.debug.export=true")
        jvmArguments.add("-Dcreativeblueprint.devtools=true") // #cbtest chat hook (DevTools.java)
    }
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
            register("loader", Regex("\\d+\\.\\d+").find(sc.properties.get<String>("deps.fabric_loader"))!!.value)
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml", "pack.mcmeta")
    }

    // MIT requires the license text to travel with copies of the mod
    withType<Jar> {
        val name = project.property("mod.id")
        inputs.property("mod_id", name)
        from("../../LICENSE") { rename { "$it-$name" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies them to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        // loomx.modJar is the jar task of whichever Loom variant is applied
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
