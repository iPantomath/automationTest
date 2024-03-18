package com.dhl.demp.dmac.model

import java.lang.IllegalArgumentException

enum class DeviceOwner(val value: String) {
    PRIVATE("private"),
    CORPORATE("corporate");

    companion object {
        @JvmStatic
        fun from(value: String): DeviceOwner = values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown DeviceOwner $value")
    }
}