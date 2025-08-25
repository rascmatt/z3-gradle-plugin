plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

group = "io.github.rascmatt"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {

    publications {
        create<MavenPublication>("mavenKotlin") {
            from(components["kotlin"])
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))

            pom {
                name.set("z3-bootstrap")
                description.set("Runtime components to load the Z3 JNI library")
                url.set("https://github.com/rascmatt/z3-gradle-plugin")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("rascmatt")
                        name.set("Matthias Raschhofer")
                        email.set("matthias.raschhofer@gmail.com")
                    }
                }
                scm {
                    url.set("https://github.com/rascmatt/z3-gradle-plugin")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}