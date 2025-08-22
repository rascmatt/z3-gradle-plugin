package io.github.rascmatt.z3

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

object Z3Bootstrap {

    @Volatile
    private var initialized = false

    fun init() {

        if (initialized) {
            return
        }

        synchronized(this) {

            if (initialized) {
                return
            }

            initInternal()

            // Tell Z3 com.microsoft.z3.Native to load the lib again
            System.setProperty("z3.skipLibraryLoad", "true")

            initialized = true
        }
    }

    private fun initInternal() {

        val (z3, z3Java) = locateLibs()

        System.load(z3)
        System.load(z3Java)
    }

    private fun locateLibs(): Pair<String, String> {

        // Prefer the Z3_HOME environment variable if available
        System.getenv("Z3_HOME")?.let {
            getLibs(it)?.let { libs ->
                return libs
            }
        }

        // Try to resolve directory from where the code runs
        runCatching {
            val url = Z3Bootstrap::class.java.protectionDomain.codeSource?.location ?: return@runCatching null
            val p = Paths.get(url.toURI())
            if (Files.isRegularFile(p)) p.parent else p
        }.getOrNull()?.let {
            getLibs(it.absolutePathString())?.let { libs ->
                return libs
            }
        }

        // Fall back to the working directory
        System.getProperty("user.dir")?.let {
            getLibs(it)?.let { libs ->
                return libs
            }
        }

        throw IllegalArgumentException("Could not locate Z3 library. Consider setting the Z3_HOME environment variable.")
    }

    private fun getLibs(path: String): Pair<String, String>? {

        val p = File(path)
        if (!p.exists() || !p.isDirectory) {
            return null
        }

        return getFiles(p)
    }

    private fun getFiles(libDir: File): Pair<String, String>? {

        val os = System.getProperty("os.name").lowercase()

        val fileExt = when {
            os.contains("win") -> "dll"
            os.contains("mac") || os.contains("darwin") -> "dylib"
            else -> "so"
        }

        val libz3 = File(libDir, "libz3.$fileExt")
        if (!libz3.exists()) {
            return null
        }

        val libz3java = File(libDir, "libz3java.$fileExt")
        if (!libz3java.exists()) {
            return null
        }

        return Pair(libz3.absolutePath, libz3java.absolutePath)
    }
}