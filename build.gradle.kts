plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.16.0"
}

group = "com.bracketbow"

// Versiyonu en son git tag'inden oku (v0.2.1 → 0.2.1).
// Tag yoksa veya git mevcut değilse "0.0.0-dev" kullan.
version = providers.exec {
    commandLine("git", "describe", "--tags", "--abbrev=0")
}.standardOutput.asText
    .map { it.trim().removePrefix("v") }
    .getOrElse("0.0.0-dev")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Hedeflenen IDE sürümü - istersen değiştir
        intellijIdeaCommunity("2024.2.4")

        // Opsiyonel dependency'lerin runIde sırasında yüklenmesi için
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")

        pluginVerifier()
        zipSigner()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "252.*"
        }
    }
    publishing {
        token = providers.environmentVariable("JETBRAINS_MARKETPLACE_TOKEN")
    }
}

kotlin {
    jvmToolchain(17)
}
