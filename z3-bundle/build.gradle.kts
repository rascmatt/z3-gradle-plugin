plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "io.github.rascmatt"
version = "4.15.3"

repositories {
    mavenCentral()
}

publishing {

    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])
        }
    }

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
