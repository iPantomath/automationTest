package com.dhl.demp.dmac.model

enum class BBfiedApproveState(val value: String) {
    VISIBLE("visible"),//smartphone shows application icon and [Request] button is visible and enabled
    REQUESTED("requested"),//smartphone shows application icon and [Request] button is visible but not enabled
    APPROVED("approved"),//smartphone shows application icon and [Request] button is visible but not enabled
    REG_REQUEST("regRequest"),//smartphone shows application icon and [Request] button is visible but not enabled
    INSTALL_ALLOWED("installAllowed"),//application is ready to be installed or installed
    UNKNOWN("unknown");

    companion object {
        fun from(value: String): BBfiedApproveState =
            values().firstOrNull { it.value == value } ?: UNKNOWN
    }
}