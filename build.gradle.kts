import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension
import org.apache.tools.ant.taskdefs.condition.Os.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.nio.file.Paths
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorPlugin
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Style

val kotlinVersion = "1.2.70"
val log4jVersion = "2.17.2"

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.vanniktech:gradle-dependency-graph-generator-plugin:0.5.0")
    }
}

plugins {
    kotlin("jvm").version("1.2.70")
    id("com.atlassian.performance.tools.gradle-release").version("0.7.1")
    `java-library`
}

plugins.apply("com.vanniktech.dependency.graph.generator")

configurations.all {
    resolutionStrategy {
        activateDependencyLocking()
        failOnVersionConflict()
        eachDependency {
            when (requested.module.toString()) {
                "commons-codec:commons-codec" -> useVersion("1.10")
                "com.google.guava:guava" -> useVersion("23.6-jre")
                "org.apache.commons:commons-csv" -> useVersion("1.4")
                "commons-logging:commons-logging" -> useVersion("1.2")
                "commons-io:commons-io" -> useVersion("2.5")
                "org.apache.httpcomponents:httpclient" -> useVersion("4.5.5")
                "org.apache.httpcomponents:httpcore" -> useVersion("4.4.9")
                "org.codehaus.plexus:plexus-utils" -> useVersion("3.1.0")
                "org.slf4j:slf4j-api" -> useVersion("1.8.0-alpha2")
                "com.jcraft:jzlib" -> useVersion("1.1.3")
                "com.google.code.gson:gson" -> useVersion("2.8.2")
                "org.jsoup:jsoup" -> useVersion("1.10.2")
                "com.fasterxml.jackson.core:jackson-core" -> useVersion("2.9.4")
                "javax.xml.bind:jaxb-api" -> useVersion("2.2.11")
            }
            when (requested.group) {
                "org.apache.logging.log4j" -> useVersion(log4jVersion)
                "org.jetbrains.kotlin" -> useVersion(kotlinVersion)
            }
        }
    }
}

val jptDependenciesGenerator = Generator(
    "jptLibraries",
    { dependency -> dependency.moduleGroup.startsWith("com.atlassian.performance.tools") },
    { true },
    { node, _ -> node.add(Style.FILLED, Color.rgb("#ffcb2b")) }
)

configure<DependencyGraphGeneratorExtension> {
    generators = listOf(jptDependenciesGenerator)
}

dependencies {
    listOf(
        "com.atlassian.performance.tools:workspace:[2.0.0,3.0.0)",
        "com.atlassian.performance.tools:report:[3.13.0,4.0.0)",
        "com.atlassian.performance.tools:aws-infrastructure:[2.8.0,3.0.0)",
        "com.atlassian.performance.tools:aws-resources:[1.9.0,2.0.0)",
        "com.atlassian.performance.tools:virtual-users:[3.11.0,3.12.0)",
        "com.atlassian.performance.tools:infrastructure:[4.22.0,5.0.0)"
    ).forEach { api(it) }

    listOf(
        "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion",
        "com.atlassian.performance.tools:jira-software-actions:[1.0.0,2.0.0)",
        "com.atlassian.performance.tools:concurrency:[1.0.0,2.0.0)"
    ).plus(
        log4jCore()
    ).forEach { implementation(it) }
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    listOf(
        "junit:junit:4.12",
        "org.assertj:assertj-core:3.11.0",
        "org.codehaus.plexus:plexus-utils:3.1.0",
        "org.zeroturnaround:zt-exec:1.10"
    ).forEach { testCompile(it) }
}

fun log4jCore(): List<String> = log4j(
    "api",
    "core",
    "slf4j-impl"
)

fun log4j(
    vararg modules: String
): List<String> = modules.map { module ->
    "org.apache.logging.log4j:log4j-$module:$log4jVersion"
}

tasks.getByName("test", Test::class).apply {
    useJUnit {
        excludeCategories("com.atlassian.performance.tools.jiraperformancetests.AcceptanceCategory")
    }
}

val testAcceptance = task<Test>("testAcceptance") {
    dependsOn("publishToMavenLocal")
    systemProperty("jpt.version", version)
    maxParallelForks = 3
}

tasks.withType(Test::class.java) {
    val shadowJarTask = tasks.getByPath(":reference-virtual-users:shadowJar")
    dependsOn(shadowJarTask)
    systemProperty("jpt.virtual-users.shadow-jar", shadowJarTask.outputs.files.files.first())
    failFast = true
}

tasks.wrapper {
    gradleVersion = "5.1.1"
    distributionType = Wrapper.DistributionType.ALL
}
