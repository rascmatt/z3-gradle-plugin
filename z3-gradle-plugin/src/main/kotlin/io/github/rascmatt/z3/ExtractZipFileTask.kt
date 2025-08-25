package io.github.rascmatt.z3

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.jar.JarFile

abstract class ExtractZipFileTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputFile
    abstract val bundleJar: RegularFileProperty

    @get:Input
    var filter: (String) -> Boolean = { true }

    @TaskAction
    fun execute() {

        JarFile(bundleJar.asFile.get()).use { jar ->

            jar.entries().asSequence().forEach { jarEntry ->

                if (!filter(jarEntry.name)) {
                    return@forEach
                }

                jar.getInputStream(jarEntry).use { input ->

                    project.logger.debug("Extracting ${jarEntry.name}")

                    val target = File(outputDir.get().asFile, jarEntry.name)
                    target.parentFile.mkdirs()
                    target.outputStream().use { input.copyTo(it) }

                }
            }

        }
    }

}
