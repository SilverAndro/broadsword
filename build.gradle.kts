plugins {
    id("java")
    id("org.quiltmc.gradle.licenser") version "1.+"
    id("me.champeau.jmh") version "0.7.1"
}

group = "io.github.silverandro"
version = "0.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

repositories {
    maven {
        url = uri("https://maven.fabricmc.net/")
        name = "fabricmc maven"
        content {
            includeGroup("net.fabricmc")
        }
    }
}

dependencies {
    implementation("it.unimi.dsi:fastutil-core:8.5.12")

    jmh("net.fabricmc:mapping-io:0.4.2")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

license {
    rule(file("codeformat/HEADER"))

    include("**/*.kt", "**/*.java")
}

jmh {
    warmupIterations.set(4)
    iterations.set(12)
    fork.set(2)
    resultFormat.set("csv")
    resultsFile.set(project.file("${project.rootDir}/run/benchmark_results.json"))
}

tasks.test {
    useJUnitPlatform()
}