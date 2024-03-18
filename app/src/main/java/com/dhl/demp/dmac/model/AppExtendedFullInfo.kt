package com.dhl.demp.dmac.model

data class AppExtendedFullInfo(
    val appId: String,
    val packageId: String,
    val name: String,
    val version: String,
    val versionDate: Long,
    val iconUrl: String,
    val releaseType: String?,
    val annotation: String?,
    val screenshots: List<String>?,
    val versions: List<AppVersionItem>?,
    val isAvailable: Boolean,
    val hasDependencies: Boolean,
    val isWebLink: Boolean,
    val isBBfied: Boolean,
    val bbfiedApproveState: BBfiedApproveState?,
    val actions: List<AppExtraAction>,
    val categories: Map<String, List<String>>,
    val description: String?,
    val releaseNote: String?,
    val website: String?,
    val developedBy: String?,
    val supportContact: String?,
    val installationDependencies: List<InstallationDependencyInfo>
)