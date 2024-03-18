package com.dhl.demp.dmac.model

data class AppFullInfo(
    val appId: String,
    val packageId: String,
    val name: String,
    val version: String,
    val iconUrl: String,
    val releaseType: String?,
    val annotation: String?,
    val isAvailable: Boolean,
    val hasDependencies: Boolean,
    val isWebLink: Boolean,
    val isBBfied: Boolean,
    val bbfiedApproveState: BBfiedApproveState?,
    val actions: List<AppExtraAction>,
)