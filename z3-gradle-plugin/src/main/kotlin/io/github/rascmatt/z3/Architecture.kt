package io.github.rascmatt.z3

enum class Architecture(val values: Set<String>, val z3Name: String) {
    INTEL_86(setOf("x86"), "x86"),
    INTEL_64(setOf("x64", "amd", "amd64"), "x64"),
    ARM(setOf("arm", "arm64", "aarch", "aarch64"), "arm64");

    companion object {
        fun resolve(name: String): Architecture {
            return when (name.lowercase()) {
                in INTEL_64.values -> return INTEL_64
                in ARM.values -> return ARM
                else -> INTEL_86
            }
        }
    }
}
