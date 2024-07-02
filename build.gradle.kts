import groovy.util.Node
import groovy.util.NodeList

plugins {
    id("java")
    id("dev.yumi.gradle.licenser") version "1.+"
    id("me.champeau.jmh") version "0.7.2"
    `maven-publish`
}

group = "dev.silverandro"
version = "0.1.4"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.fabricmc.net/")
        name = "fabricmc maven"
        content {
            includeGroup("net.fabricmc")
        }
    }
}

dependencies {
    implementation("net.fabricmc:tiny-remapper:0.8.9")
    jmh("net.fabricmc:mapping-io:0.4.2")

    implementation("org.ow2.asm:asm:9.7")
    implementation("org.ow2.asm:asm-util:9.7")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

license {
    rule(file("codeformat/HEADER"))

    include("**/*.kt", "**/*.java")
}

java {
    withSourcesJar()
}

jmh {
    warmupIterations.set(3)
    iterations.set(7)
    fork.set(2)
    resultFormat.set("csv")
    resultsFile.set(project.file("${project.rootDir}/run/benchmark_results.json"))
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("broadsword") {
            from(components["java"])

            pom {
                name.set("Broadsword")
                description.set("A java class file remapper with a focus on speed")
                inceptionYear.set("2023")
                licenses {
                    license {
                        name.set("ARR")
                        distribution.set("repo")
                        comments.set("Contact Silver for licensing specifics")
                    }
                }
                scm {
                    url.set("https://github.com/SilverAndro/broadsword")
                }
                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/SilverAndro/broadsword/issues")
                }
                developers {
                    developer {
                        name.set("Silver")
                        email.set("me@silverando.dev")
                        url.set("silverandro.dev")
                    }
                }
            }
            pom.withXml {
                val n = asNode()
                (n.get("dependencies") as NodeList).forEach {
                    n.remove(it as Node)
                }
            }
        }
    }

    repositories {
        maven {
            name = "SilverMaven"
            url = uri("https://maven.silverandro.dev")
            credentials {
                username = System.getProperty("silverMavenUsername")
                password = System.getProperty("silverMavenPassword")
            }
        }
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}