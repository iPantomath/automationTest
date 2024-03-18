package com.dhl.demp.dmac.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.dhl.demp.dmac.model.AppInstallationInfo
import com.dhl.demp.dmac.model.InstallationRoutineType
import com.dhl.demp.dmac.utils.localBroadcastToFlow
import com.dhl.demp.mydmac.installer.AppInstallerService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import timber.log.Timber
import javax.inject.Inject

class AppInstallManagerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : AppInstallManager {
    override fun installApp(installationInfo: AppInstallationInfo, launchAfterInstallation: Boolean) {
        if (installationInfo.installationType != InstallationRoutineType.INSTALL) {
            Timber.e("Can not install app ${installationInfo.appId} with installation type ${installationInfo.installationType}")
            return
        }

        AppInstallerService.installApp(
            appContext,
            installationInfo.appId,
            installationInfo.name,
            installationInfo.version,
            installationInfo.source,
            installationInfo.md5,
            null,
            launchAfterInstallation
        )
    }

    override fun isAppInstalling(appId: String): Boolean =
        AppInstallerService.isAppInstalling(appId)

    override fun getAppsInstallEventFlow(): Flow<AppInstallEvent> = createAppsInstallEventFlow()

    override fun getAppInstallEventFlow(appId: String): Flow<AppInstallEvent> =
        createAppsInstallEventFlow().filter { it.appId == appId }

    private fun createAppsInstallEventFlow(): Flow<AppInstallEvent> {
        val intentFilter = IntentFilter(AppInstallerService.ACTION_APP_INSTALL_EVENT)

        val mapper: (Intent) -> AppInstallEvent = { intent: Intent ->
            val event = AppInstallerService.Event(intent.extras)

            when (event.type) {
                AppInstallerService.Event.TYPE_STARTING_DOWNLOAD ->
                    AppInstallEvent.StartDownloading(event.appId)
                AppInstallerService.Event.TYPE_FILE_CORRUPTED -> AppInstallEvent.FileCorrupted(event.appId)
                AppInstallerService.Event.TYPE_MD5_DONOT_MATCH -> AppInstallEvent.Md5NotMatch(event.appId)
                AppInstallerService.Event.TYPE_INSTALL_FINISHED -> AppInstallEvent.InstallationFinished(
                    event.appId
                )
                else -> error("Unknown install event type ${event.type}")
            }
        }

        return localBroadcastToFlow(appContext, intentFilter, mapper)
    }
}