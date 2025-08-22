plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

group = "io.github.rascmatt"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.github.rascmatt:z3-bundle:$version")

    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("z3Plugin") {
            id = "io.github.rascmatt.z3"
            implementationClass = "io.github.rascmatt.z3.Z3Plugin"
            displayName = "Z3 Gradle Plugin"
        }
    }
}


publishing {
    repositories {
        mavenLocal()
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
