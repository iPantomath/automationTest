package com.dhl.demp.dmac.domain.appfilter

import com.dhl.demp.dmac.model.AppFullInfo
import com.dhl.demp.dmac.ui.appslist.AppsListType

fun Filter.Companion.forListType(listType: AppsListType): Filter<AppFullInfo> {
    return when (listType) {
        AppsListType.PUBLIC_APPS -> PublicAppsFilter()
        AppsListType.INTERNAL_APPS -> InternalAppsFilter()
        AppsListType.INSTALLED_APPS -> InstalledAppsFilter()
    }
}