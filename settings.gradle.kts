plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "z3"

include("z3-gradle-plugin")
include("z3-bootstrap")

include("z3-bundle")

include("z3-bundle:z3-bundle-arm64-osx")
include("z3-bundle:z3-bundle-arm64-glibc")
include("z3-bundle:z3-bundle-x64-glibc")
include("z3-bundle:z3-bundle-x64-win")
