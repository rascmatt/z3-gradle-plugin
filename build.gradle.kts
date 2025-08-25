plugins {
    kotlin("jvm") version "2.0.21"
}

group = "io.github.rascmatt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

subprojects {

    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension>("publishing") {

            repositories {

                maven {
                    url = uri(
                        if (version.toString().endsWith("SNAPSHOT"))
                            "https://central.sonatype.com/repository/maven-snapshots/"
                        else
                            "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
                    )
                    credentials {
                        username = providers.gradleProperty("ossrhUsername").get()
                        password = providers.gradleProperty("ossrhPassword").get()
                    }
                }

                mavenLocal()
            }

        }
    }
}