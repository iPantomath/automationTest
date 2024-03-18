package com.dhl.demp.dmac.model

enum class AppCategory(val value: String) {
    ALL("ALL"),
    DPDHL_GROUP("DPDHL_GROUP"),
    DGFF("GLOBAL_FORWARDING_FREIGHT"),
    DSC("SUPPLY_CHAIN"),
    ECS("ECS"),
    EXP("EXPRESS"),
    GBS("GBS"),
    CSI("GBS_ITS"),
    CC("GBS_CSI_CC"),
    PNP("PNP");

    companion object {
        @JvmStatic
        fun from(value: String): AppCategory? = values().firstOrNull { it.value == value }
    }
}