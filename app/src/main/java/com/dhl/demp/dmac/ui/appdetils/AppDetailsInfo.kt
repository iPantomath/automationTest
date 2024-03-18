package com.dhl.demp.dmac.ui.appdetils

import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.AppMainAction
import com.dhl.demp.dmac.model.AppVersionItem
import com.dhl.demp.dmac.model.InstallationDependencyInfo

data class AppDetailsInfo(
    val appId: String,
    val name: String,
    val version : String,
    val iconUrl: String,
    val releaseType: String?,
    val annotation: String?,
    val screenshots: List<String>?,
    val versionDate: Long,
    val description: String?,
    val releaseNote: String?,
    val website: String?,
    val developedBy: String?,
    val supportContact: String?,
    val versions: List<AppVersionItem>?,
    val tags: Map<String, String>,
    val installationDependencies: List<InstallationDependencyInfo>,
    val resolvedDependencies: Set<String>,
    val mainAction: AppMainAction,
    val extraActions: List<AppExtraAction>
)