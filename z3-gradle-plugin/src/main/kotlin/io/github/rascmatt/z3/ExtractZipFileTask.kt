package io.github.rascmatt.z3

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.jar.JarFile
import java.util.zip.ZipInputStream

abstract class ExtractZipFileTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:InputFile
    abstract val bundleJar: RegularFileProperty

    @get:Input
    abstract val zipFileName: Property<String>

    @get:Input
    var filter: (String) -> Boolean = { true }

    @get:Input
    var flatten = true

    @TaskAction
    fun execute() {

        JarFile(bundleJar.asFile.get()).use { jar ->
            val entry = jar.getJarEntry(zipFileName.get())
            requireNotNull(entry) {
                "Resource to unzip not found at: $zipFileName."
            }

            ZipInputStream(jar.getInputStream(entry)).use { zis ->

                var e = zis.nextEntry
                while (e != null) {

                    if (!filter(e.name)) {
                        zis.closeEntry()
                        e = zis.nextEntry
                        continue
                    }

                    val targetName = if (flatten) {
                        e.name.substringAfterLast('/')
                    } else {
                        e.name
                    }

                    project.logger.debug("Extracting $targetName")

                    val target = File(outputDir.get().asFile, targetName)
                    if (e.isDirectory) {
                        target.mkdirs()
                    } else {
                        target.parentFile.mkdirs()
                        target.outputStream().use { zis.copyTo(it) }
                    }

                    zis.closeEntry()
                    e = zis.nextEntry
                }
            }

        }
    }

}
