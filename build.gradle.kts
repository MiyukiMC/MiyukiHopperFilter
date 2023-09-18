plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "app.miyuki.miyukihopperfilter"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    implementation("org.bstats:bstats-bukkit:3.0.2")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
    shadowJar {
        archiveFileName.set("MiyukiHopperFilter-${project.version}.jar")
        relocate("org.bstats", "app.miyuki.miyukihopperfilter.bstats")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}