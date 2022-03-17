import org.gradle.jvm.toolchain.JavaLanguageVersion.of

plugins {
  application
  kotlin("jvm") version "1.6.10"
  id("com.github.johnrengelman.shadow") version "7.1.2"
}

application {
  mainClass.set("net.horizonsend.limbo.LimboKt")
}

repositories {
  mavenCentral()
  maven("https://repo.aikar.co/content/groups/aikar/")
  maven("https://repo.spongepowered.org/maven")
  maven("https://jitpack.io")
}

dependencies {
  implementation("net.kyori:adventure-text-minimessage:4.10.0")
  implementation("com.github.Minestom:Minestom:-SNAPSHOT")
}

tasks {
  compileKotlin { kotlinOptions { jvmTarget = "17" } }
  shadowJar {
    archiveFileName.set("../Limbo.jar")
  }
}

java.toolchain.languageVersion.set(of(17))