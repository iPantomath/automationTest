package com.dhl.demp.dmac.domain.utils

import android.content.Context
import com.dhl.demp.mydmac.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DependencyUtilsImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : DependencyUtils {
    override fun isDependencyResolved(packageId: String, version: String): Boolean {
        val appVersion = Utils.getInstalledAppVersion(appContext, packageId)

        if (appVersion == null) {
            //app is not installed at all
            return false
        }

        if (version.isEmpty()) {
            //any version of the dependency app is enough
            return true
        }

        return Utils.compareVersions(appVersion, version) >= 0
    }
}