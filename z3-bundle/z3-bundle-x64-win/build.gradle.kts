plugins {
    kotlin("jvm")
}

group = "io.github.rascmatt"
version = parent!!.version

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}
