package com.dhl.demp.dmac.ui.shared

import com.dhl.demp.dmac.model.AppMainAction
import mydmac.R

fun getMainActionTextRes(action: AppMainAction): Int {
    return when (action) {
        AppMainAction.OpenLink -> R.string.open_weblink
        is AppMainAction.Install -> R.string.market_action_install
        AppMainAction.Installed -> R.string.market_action_installed
        is AppMainAction.Update -> R.string.market_action_update
        AppMainAction.Request -> R.string.market_action_request
        AppMainAction.Requested -> R.string.market_action_requested
        AppMainAction.Progress, AppMainAction.Absent -> R.string.empty
    }
}

fun getMainActionIconRes(action: AppMainAction): Int {
    return when (action) {
        is AppMainAction.Install -> {
            if (action.hasDependencies) {
                R.drawable.ic_install_sequence_marker
            } else {
                0
            }
        }
        is AppMainAction.Update -> {
            if (action.hasDependencies) {
                R.drawable.ic_install_sequence_marker
            } else {
                0
            }
        }
        else -> 0
    }
}