plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "z3"
include("z3-bundle")
include("z3-gradle-plugin")
