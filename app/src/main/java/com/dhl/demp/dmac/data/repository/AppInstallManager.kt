package com.dhl.demp.dmac.data.repository

import com.dhl.demp.dmac.model.AppInstallationInfo
import kotlinx.coroutines.flow.Flow

interface AppInstallManager {
    fun installApp(installationInfo: AppInstallationInfo, launchAfterInstallation: Boolean)
    fun isAppInstalling(appId: String): Boolean
    fun getAppsInstallEventFlow(): Flow<AppInstallEvent>
    fun getAppInstallEventFlow(appId: String): Flow<AppInstallEvent>
}

sealed class AppInstallEvent(val appId: String) {
    class StartDownloading(appId: String) : AppInstallEvent(appId)
    class FileCorrupted(appId: String) : AppInstallEvent(appId)
    class Md5NotMatch(appId: String) : AppInstallEvent(appId)
    class InstallationFinished(appId: String) : AppInstallEvent(appId)
}