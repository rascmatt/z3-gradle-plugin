package io.github.rascmatt.z3

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.distribution.DistributionContainer
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Tar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.testing.Test
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.process.JavaForkOptions
import java.io.File
import java.util.*


class Z3Plugin : Plugin<Project> {

    private val defaultBundleVersion = "4.15.3"

    override fun apply(project: Project) {

        // Resolve the z3-bundle dependency
        val z3Ext = project.extensions.create("z3", Z3Extension::class.java)

        val z3BundleCfg = project.configurations.create("z3Version") {
            it.isCanBeResolved = true
            it.isCanBeConsumed = false
            it.isVisible = false
        }

        val (os, arch) = resolvePlatform(project)

        val bundleCoords = project.providers.provider {
            when {
                z3Ext.bundleCoordinates.isPresent -> z3Ext.bundleCoordinates.get()
                z3Ext.version.isPresent -> "io.github.rascmatt:z3-bundle-${arch.z3Name}-${os.z3Name}:${z3Ext.version.get()}"
                else -> "io.github.rascmatt:z3-bundle-${arch.z3Name}-${os.z3Name}:$defaultBundleVersion"
            }
        }


        z3BundleCfg.dependencies.addLater(
            project.providers.provider {
                project.logger.debug("Adding dependency to ${bundleCoords.get()}")
                project.dependencies.create(bundleCoords.get())
            }
        )

        // Prepare bundle for extraction

        val bundleJar: Provider<File> = resolveBundleJar(z3BundleCfg, project)

        val outDir = project.layout.buildDirectory.dir("generated/z3/resources").get().asFile

        val extractTask = project.tasks.register("z3Extract", ExtractZipFileTask::class.java) {
            it.group = "z3"
            it.description = "Extracts the Z3 binaries into build-generated resources."
            it.outputDir.set(outDir)
            it.bundleJar.set(bundleJar.get())
            it.filter = { name ->
                name.endsWith(".dylib") || name.endsWith(".so") || name.endsWith(".dll") || name.endsWith(".jar")
            }
        }

        project.tasks.named("processResources", ProcessResources::class.java).configure {
            it.dependsOn(extractTask)
            it.from(project.layout.buildDirectory.dir("generated/z3/resources"))
        }

        // After nativeCompile, copy the z3 binaries next to the executable
        project.pluginManager.withPlugin("org.graalvm.buildtools.native") {

            val nativeCompile = kotlin.runCatching {
                project.tasks.named("nativeCompile")
            }.getOrNull()

            if (nativeCompile == null) {
                return@withPlugin
            }

            val placeNatives = project.tasks.register("z3PlaceNativesNextToImage", Copy::class.java) {
                // make sure natives exist and the image is compiled
                it.dependsOn(extractTask, nativeCompile)

                it.from(outDir)
                it.include("**/libz3.*", "**/libz3java.*")

                it.into(project.layout.buildDirectory.dir("native/nativeCompile"))
            }

            // run it after native image is built
            nativeCompile.configure { it.finalizedBy(placeNatives) }
        }

        // Configure environment for gradle run & tests

        fun setEnv(task: JavaForkOptions) {
            task.environment("Z3_HOME", outDir.absolutePath)

            when (os) {
                Platform.LINUX -> task.environment("LD_LIBRARY_PATH", outDir.absolutePath)
                Platform.MAC -> task.environment("DYLD_LIBRARY_PATH", outDir.absolutePath)
                Platform.WINDOWS -> task.environment(
                    "PATH",
                    outDir.absolutePath + File.pathSeparator + (System.getenv("PATH") ?: "")
                )
            }
        }

        project.tasks.withType(JavaExec::class.java).configureEach(::setEnv)
        project.tasks.withType(Test::class.java).configureEach(::setEnv)

        // Add the jar file as a dependency

        val jarFile = File(outDir, "com.microsoft.z3.jar")

        fun addFileDepOn(configName: String) {
            val files = project.files(jarFile).builtBy(extractTask)
            project.dependencies.add(configName, files)
        }

        project.pluginManager.withPlugin("java") {
            addFileDepOn("implementation")
        }
        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            addFileDepOn("implementation")
        }

        // Avoid duplicate issues by skipping the dependency if it already exists

        val skipDupes: (CopySpec) -> Unit = {
            it.eachFile { file ->
                if (file.name == jarFile.name) {
                    file.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                }
            }
        }

        project.tasks.withType(Tar::class.java).configureEach(skipDupes)
        project.tasks.withType(Zip::class.java).configureEach(skipDupes)
        project.tasks.withType(Copy::class.java).configureEach(skipDupes)

        // Add binaries to the distribution (installDist)

        project.pluginManager.withPlugin("application") {

            // Copy the natives to the lib dir
            project.extensions.configure(DistributionContainer::class.java) {

                it.named("main").configure { dist ->
                    dist.contents { c ->
                        skipDupes(c)

                        c.from(outDir) { f ->
                            f.into("lib")
                            f.include("**/libz3.*", "**/libz3java.*")
                        }
                    }
                }
            }
        }

        // Patch the start script (if available)
        kotlin.runCatching {
            project.tasks.named("startScripts", CreateStartScripts::class.java)
        }.getOrNull()?.configure { t ->

            t.doLast {

                val unixContent = t.unixScript.readText()
                    .replace(
                        Regex("(?m)^exec \"${'\\'}${'$'}JAVACMD\".*$"),
                        """
                        # Provide Z3 lib location
                        export Z3_HOME="${'\\'}${'$'}APP_HOME/lib"
                        
                        $0
                    """.trimIndent()
                    )

                t.unixScript.writeText(unixContent)

                val windowsContent = t.windowsScript.readText()
                    .replace(
                        Regex("(?m)^set JAVA_EXE=.*$"),
                        """
                        $0
                        
                        # Provide Z3 lib location
                        set Z3_HOME=%APP_HOME%/lib
                    """.trimIndent()
                    )

                t.windowsScript.writeText(windowsContent)
            }
        }

    }

    private fun resolvePlatform(project: Project): Pair<Platform, Architecture> {

        val os = System.getProperty("os.name").lowercase(Locale.US)
        val arch = System.getProperty("os.arch").lowercase(Locale.US)

        val platform = Platform.resolve(os)
        val architecture = Architecture.resolve(arch)

        project.logger.info("Found os '$os', mapped to '${platform.z3Name}'")
        project.logger.info("Found arch '$arch', mapped to '${architecture.z3Name}'")

        return (platform to architecture)
    }

    private fun resolveBundleJar(z3BundleConfig: Configuration, project: Project): Provider<File> =
        project.providers.provider {
            val files = z3BundleConfig.resolve()
                .filter { it.name.contains("z3-bundle") }
                .filter { it.name.endsWith(".jar") }
            require(files.size == 1) { "Expected exactly one z3-bundle JAR, got: $files" }
            files.single()
        }

}

