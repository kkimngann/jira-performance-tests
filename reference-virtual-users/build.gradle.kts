import java.net.URI
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val kotlinVersion = "1.2.70"
val log4jVersion = "2.17.2"

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow").version("2.0.4")
}

dependencies {
    runtime("com.atlassian.performance.tools:jira-software-actions:[1.0.0,2.0.0)")
    runtime("com.atlassian.performance.tools:virtual-users:[3.11.0,3.12.0)")
}

tasks.getByName("shadowJar", ShadowJar::class).apply {
    manifest.attributes["Main-Class"] = "com.atlassian.performance.tools.virtualusers.api.EntryPointKt"
}

configurations.all {
    resolutionStrategy {
        activateDependencyLocking()
        failOnVersionConflict()
        eachDependency {
            when (requested.module.toString()) {
                "commons-codec:commons-codec" -> useVersion("1.10")
                "com.google.code.gson:gson" -> useVersion("2.8.2")
                "org.slf4j:slf4j-api" -> useVersion("1.8.0-alpha2")
            }
            when (requested.group) {
                "org.apache.logging.log4j" -> useVersion(log4jVersion)
                "org.jetbrains.kotlin" -> useVersion(kotlinVersion)
            }
        }
    }
}

repositories {
    maven(url = URI("https://packages.atlassian.com/maven-external/"))
}
