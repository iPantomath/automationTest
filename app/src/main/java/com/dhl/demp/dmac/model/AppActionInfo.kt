package com.dhl.demp.dmac.model

data class AppActionInfo(
    val appId: String,
    val actionId: String,
    val name: String,
    val url: String,
    val targetPackageId: String,
    val message: String?,
)