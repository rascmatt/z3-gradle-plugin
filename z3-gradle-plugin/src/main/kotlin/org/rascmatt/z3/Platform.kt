package org.rascmatt.z3

enum class Platform(val values: Set<String>, val z3Name: String) {
    LINUX(setOf("linux"), "glibc"),
    MAC(setOf("mac", "macos", "osx"), "osx"),
    WINDOWS(setOf("windows", "win"), "win");

    companion object {
        fun resolve(name: String): Platform {
            val lowerName = name.lowercase()
            return when {
                MAC.values.any { lowerName.contains(it) } -> return MAC
                WINDOWS.values.any { lowerName.contains(it) } -> return WINDOWS
                else -> LINUX
            }
        }
    }
}