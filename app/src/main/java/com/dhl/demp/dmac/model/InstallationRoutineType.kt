package com.dhl.demp.dmac.model

enum class InstallationRoutineType(val value: String) {
    INSTALL("apkinstall"),
    EXTERNAL_LINK("external_link");

    companion object {
        fun from(value: String): InstallationRoutineType =
            values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown InstallationRoutineType $value")
    }
}