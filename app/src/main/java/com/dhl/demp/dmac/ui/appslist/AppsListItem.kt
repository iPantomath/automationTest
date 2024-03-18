package com.dhl.demp.dmac.ui.appslist

import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.AppMainAction

data class AppsListItem(
    val appId: String,
    val packageId: String,
    val name: String,
    val version : String,
    val iconUrl: String,
    val releaseType: String?,
    val annotation: String?,
    val mainAction: AppMainAction,
    val extraActions: List<AppExtraAction>
)