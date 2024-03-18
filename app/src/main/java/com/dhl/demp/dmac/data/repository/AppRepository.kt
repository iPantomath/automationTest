package com.dhl.demp.dmac.data.repository

import com.dhl.demp.dmac.data.model.FetchAppsResult
import com.dhl.demp.dmac.model.AppActionInfo
import com.dhl.demp.dmac.model.AppExtendedFullInfo
import com.dhl.demp.dmac.model.AppFullInfo
import com.dhl.demp.dmac.model.AppInstallationInfo
import com.dhl.demp.mydmac.obj.InstallationAppItem
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    val needSelfUpdate: Flow<Boolean>
    fun getAppsList(): Flow<List<AppFullInfo>>
    fun getAppExtendedInfo(appId: String): Flow<AppExtendedFullInfo>
    suspend fun getAppInstallationInfo(appId: String): AppInstallationInfo?
    suspend fun fetchAndSaveAppsList(): FetchAppsResult
    suspend fun fetchAndSaveAppDetails(appId: String): FetchAppsResult
    suspend fun getAppWebLink(appId: String): String?
    suspend fun getAppPackageName(appId: String): String?
    suspend fun getAppIdByPackageName(packageName: String): String?
    suspend fun getAppActionDetails(appId: String, actionId: String): AppActionInfo?
    suspend fun getAppDependenciesStatus(appId: String): AppDependenciesStatus
    suspend fun getInstallationAppItem(appId: String): InstallationAppItem
    suspend fun hasApp(appId: String): Boolean
}

sealed class AppDependenciesStatus {
    object Resolved : AppDependenciesStatus()
    object Unresolvable : AppDependenciesStatus()
    class Unresolved(val unresolvedDependencies: List<InstallationAppItem>) : AppDependenciesStatus()
}