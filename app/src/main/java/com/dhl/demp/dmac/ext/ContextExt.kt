package com.dhl.demp.dmac.ext

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

fun Context.openLink(url: String) {
    val webpage = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, webpage)
    if (intent.resolveActivity(packageManager) != null) {
        startActivity(intent)
    }
}

fun Context.launchApp(packageName: String) {
    val launchIntent = this.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(launchIntent)
    }
}

fun Context.showAppNotificationSettings() {
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            }
            else -> {
                action = "android.settings.APP_NOTIFICATION_SETTINGS"
                putExtra("app_package", packageName)
                putExtra("app_uid", applicationInfo.uid)
            }
        }
    }

    startActivity(intent)
}