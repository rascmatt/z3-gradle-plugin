package io.github.rascmatt.z3

enum class Architecture(val values: Set<String>, val z3Name: String) {
    INTEL_86(setOf("x86"), "x86"),
    INTEL_64(setOf("x64", "x86_64", "amd"), "x64"),
    ARM(setOf("arm", "aarch"), "arm64");

    companion object {
        fun resolve(name: String): Architecture {

            val lowerName = name.lowercase()
            return when {
                INTEL_64.values.any { lowerName.contains(it) } -> return INTEL_64
                ARM.values.any { lowerName.contains(it) } -> return ARM
                else -> INTEL_86
            }
        }
    }
}
