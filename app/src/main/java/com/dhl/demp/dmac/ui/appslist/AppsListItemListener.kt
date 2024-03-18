package com.dhl.demp.dmac.ui.appslist

import android.view.View
import androidx.core.util.Pair
import com.dhl.demp.dmac.model.AppExtraAction
import com.dhl.demp.dmac.model.AppMainAction

interface AppsListItemListener {
    fun onAppSelected(appId: String, sharedElements: Array<Pair<View, String>>)
    fun onAppMainActionSelected(appId: String, action: AppMainAction)
    fun onExtraActionSelected(appId: String, extraAction: AppExtraAction)
}