plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "io.github.rascmatt"
version = parent!!.version

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

kotlin {
    jvmToolchain(21)
}
