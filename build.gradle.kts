plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.serialization") version "2.1.10"
    id("com.diffplug.spotless") version "7.0.3"
    id("maven-publish")
}

group = "dicedb-kotlin"

version = "1.0.0"

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    testImplementation(kotlin("test"))

    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.9.0")

    // Testcontainers
    testImplementation("org.testcontainers:testcontainers:1.19.1")
    testImplementation("org.testcontainers:junit-jupiter:1.19.1")

    testImplementation("org.slf4j:slf4j-simple:2.0.9")
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(21) }

sourceSets { create("examples") }

kotlin {
    target.compilations.getByName("examples").associateWith(target.compilations.getByName("main"))
}

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

publishing {
    publications {
        create<MavenPublication>("jitpack") {
            from(components["java"])
            artifactId = "client"
        }
    }
}
