package com.dhl.demp.dmac.domain.appfilter

import com.dhl.demp.dmac.model.AppFullInfo
import com.dhl.demp.mydmac.LauncherApplication
import com.dhl.demp.mydmac.utils.Utils

class InstalledAppsFilter : Filter<AppFullInfo> {
    private val context = LauncherApplication.get()

    override fun match(item: AppFullInfo): Boolean {
        return Utils.isAppInstalled(context, item.packageId)
    }
}