import sun.jvmstat.monitor.MonitoredVmUtil.jvmArgs

plugins {
	id("net.fabricmc.fabric-loom-remap")
	`maven-publish`
}

repositories {
	mavenCentral()
	maven {
		url = uri("https://maven.caffeinemc.net/releases")
	}
	maven {
		url = uri("https://api.modrinth.com/maven")
		content {
			includeGroup("maven.modrinth")
		}
	}
}

loom {
	splitEnvironmentSourceSets()

	mods {
		register("trees-of-wynn-vanish") {
			sourceSet(sourceSets.main.get())
			sourceSet(sourceSets.getByName("client"))
		}

		runs {
			configureEach {
				jvmArguments.add("-XX:+AllowEnhancedClassRedefinition")
			}
		}
	}

}

afterEvaluate {
	loom.runs.configureEach {
		jvmArguments.add(
			"-javaagent:${configurations.compileClasspath.get().resolve().find { it.name.contains("sponge-mixin") }}"
		)
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
    mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")

	modCompileOnly("maven.modrinth:voxy:${providers.gradleProperty("voxy_version").get()}")
	modCompileOnly("net.caffeinemc:sodium-fabric:${providers.gradleProperty("sodium_version").get()}")
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 21
}

tasks.register<Exec>("run + RenderDoc") {
	val javaExecTask = tasks.withType<JavaExec>().named("runClient").get()
	val javaHome = javaExecTask.javaLauncher.get().metadata.installationPath.asFile.absolutePath

	commandLine = listOf(
		"C:\\Program Files\\RenderDoc\\renderdoccmd.exe",
		"capture",
		"--opt-hook-children",
		"--wait-for-exit",
		"--working-dir",
		".",
		"$javaHome/bin/java.exe",
		"-Xmx64m",
		"-Xms64m",
		"-Dorg.gradle.appname=gradlew",
		"-Dorg.gradle.java.home=$javaHome",
		"-classpath",
		"gradle/wrapper/gradle-wrapper.jar",
		"org.gradle.wrapper.GradleWrapperMain",
		"runClient"
	)
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
