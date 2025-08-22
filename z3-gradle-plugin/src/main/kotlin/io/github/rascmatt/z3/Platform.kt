package io.github.rascmatt.z3

enum class Platform(val z3Name: String) {
    LINUX("glibc"),
    MAC("osx"),
    WINDOWS("win");

    companion object {
        fun resolve(name: String): Platform {
            val lowerName = name.lowercase()
            return when {
                lowerName.contains("win") -> WINDOWS
                lowerName.contains("mac") || lowerName.contains("darwin") -> MAC
                else -> LINUX
            }
        }
    }
}
