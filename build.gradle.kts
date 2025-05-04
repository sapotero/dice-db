plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("com.diffplug.spotless") version "7.0.3"
}

group = "dicedb-kotlin"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }

spotless {
    kotlin {
        targetExclude("build/**")
        ktfmt().kotlinlangStyle().configure { it.setRemoveUnusedImports(true) }
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktfmt().kotlinlangStyle().configure { it.setRemoveUnusedImports(true) }
    }

    format("misc") {
        target("*.gradle", "*.md", ".gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
