package org.rascmatt.z3

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Locale

class Z3Plugin : Plugin<Project> {
    override fun apply(project: Project) {
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)
        val arch = System.getProperty("os.arch").lowercase(Locale.ROOT)

        project.logger.lifecycle(">>> Z3Plugin initialized")
        project.logger.lifecycle(">>> Detected OS: $os")
        project.logger.lifecycle(">>> Detected Arch: $arch")
    }
}
