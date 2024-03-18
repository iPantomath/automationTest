package com.dhl.demp.dmac.data.repository

import kotlinx.coroutines.flow.Flow

interface AppStatusRepository {
    fun getAppsPresenceEventFlow(): Flow<AppPresenceEvent>
    fun getAppPresenceEventFlow(packageName: String): Flow<AppPresenceEvent>
    fun isAppDependencyResolved(packageId: String, version: String): Boolean
}

sealed class AppPresenceEvent(val packageName: String) {
    class Installed(packageName: String): AppPresenceEvent(packageName)
    class Uninstalled(packageName: String): AppPresenceEvent(packageName)
}