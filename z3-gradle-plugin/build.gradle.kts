plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "1.3.1"
}

group = "io.github.rascmatt"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

gradlePlugin {
    website.set("https://github.com/rascmatt/z3-gradle-plugin")
    vcsUrl.set("https://github.com/rascmatt/z3-gradle-plugin.git")

    plugins {
        create("z3Plugin") {
            id = "io.github.rascmatt.z3"
            implementationClass = "io.github.rascmatt.z3.Z3Plugin"
            displayName = "Z3 Gradle Plugin"
            description = "Gradle plugin which provides platform dependent Z3 dependencies at build time"
            tags = listOf("z3", "smt", "solver", "jni")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
