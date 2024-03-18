package com.dhl.demp.dmac.ext

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.dhl.demp.mydmac.obj.ReleaseType
import com.dhl.demp.mydmac.utils.Utils
import mydmac.R

fun TextView.adjustReleaseTypeLabel(releaseType: String?) {
    this.visibility = View.VISIBLE
    when (releaseType) {
        ReleaseType.TEST -> {
            setText(R.string.release_type_test)
            setBackgroundResource(R.color.type_test)
        }
        ReleaseType.BETA -> {
            setText(R.string.release_type_beta)
            setBackgroundResource(R.color.type_beta)
        }
        ReleaseType.DEV -> {
            setText(R.string.release_type_dev)
            setBackgroundResource(R.color.type_dev)
        }
        else -> this.visibility = View.GONE
    }
}

fun ImageView.loadAppIcon(appIconUrl: String, iconSize: Int) {
    Utils.createIconRequest(context, appIconUrl)
        .placeholder(R.drawable.apps_list_default_icon)
        .override(iconSize, iconSize)
        .into(this)
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.show() {
    this.visibility = View.VISIBLE
}