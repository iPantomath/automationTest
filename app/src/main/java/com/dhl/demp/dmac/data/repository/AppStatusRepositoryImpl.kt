package com.dhl.demp.dmac.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.dhl.demp.dmac.domain.utils.DependencyUtils
import com.dhl.demp.dmac.utils.broadcastToFlow
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

private const val PACKAGE_SCHEME = "package"

class AppStatusRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val dependencyUtils: DependencyUtils
) : AppStatusRepository {
    override fun getAppsPresenceEventFlow(): Flow<AppPresenceEvent> = createAppsPresenceEventFlow()

    override fun getAppPresenceEventFlow(packageName: String): Flow<AppPresenceEvent> =
        createAppsPresenceEventFlow().filter { it.packageName == packageName }

    private fun createAppsPresenceEventFlow(): Flow<AppPresenceEvent> {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addDataScheme(PACKAGE_SCHEME)

        val mapper: (Intent) -> AppPresenceEvent = { intent: Intent ->
            val packageName = intent.data?.schemeSpecificPart.orEmpty()

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> AppPresenceEvent.Installed(packageName)
                Intent.ACTION_PACKAGE_REMOVED -> AppPresenceEvent.Uninstalled(packageName)
                else -> error("Unknown action ${intent.action}")
            }
        }

        return broadcastToFlow(appContext, intentFilter, mapper)
    }

    override fun isAppDependencyResolved(packageId: String, version: String): Boolean {
        return dependencyUtils.isDependencyResolved(packageId, version)
    }
}