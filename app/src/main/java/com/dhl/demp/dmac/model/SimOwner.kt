package com.dhl.demp.dmac.model

import java.lang.IllegalArgumentException

enum class SimOwner(val value: String) {
    PRIVATE("private"),
    CORPORATE("corporate"),
    BOTH("both");

    companion object {
        @JvmStatic
        fun from(value: String): SimOwner = SimOwner.values().firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown SimOwner $value")
    }
}