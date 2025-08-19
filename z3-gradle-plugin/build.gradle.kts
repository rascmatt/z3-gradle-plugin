plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

group = "org.rascmatt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("z3Plugin") {
            id = "org.rascmatt.z3"
            implementationClass = "org.rascmatt.z3.Z3Plugin"
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