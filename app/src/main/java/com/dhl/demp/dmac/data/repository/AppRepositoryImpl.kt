package com.dhl.demp.dmac.data.repository

import android.content.Context
import com.dhl.demp.dmac.data.db.dao.AppDao
import com.dhl.demp.dmac.data.db.dao.AppWriteDao
import com.dhl.demp.dmac.data.mapper.*
import com.dhl.demp.dmac.data.model.FetchAppsResult
import com.dhl.demp.dmac.data.network.AppService
import com.dhl.demp.dmac.data.network.dto.AppInfoDTO
import com.dhl.demp.dmac.data.network.dto.AppInfoExtendedDTO
import com.dhl.demp.dmac.data.network.dto.BaseAppInfoDTO
import com.dhl.demp.dmac.domain.utils.DependencyUtils
import com.dhl.demp.dmac.model.AppActionInfo
import com.dhl.demp.dmac.model.AppExtendedFullInfo
import com.dhl.demp.dmac.model.AppFullInfo
import com.dhl.demp.dmac.model.AppInstallationInfo
import com.dhl.demp.dmac.utils.DispatchersContainer
import com.dhl.demp.dmac.utils.LauncherPreferenceWrapper
import com.dhl.demp.mydmac.LauncherPreference
import com.dhl.demp.mydmac.obj.InstallationAppItem
import com.dhl.demp.mydmac.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import mydmac.BuildConfig
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appService: AppService,
    private val appDao: AppDao,
    private val appWriteDao: AppWriteDao,
    private val dependencyUtils: DependencyUtils,
    private val launcherPreferenceWrapper: LauncherPreferenceWrapper,
    private val dispatchersContainer: DispatchersContainer
) : AppRepository {
    private val _needSelfUpdate = MutableSharedFlow<Boolean>(replay = 0)
    override val needSelfUpdate: Flow<Boolean>
        get() = _needSelfUpdate

    override fun getAppsList(): Flow<List<AppFullInfo>> {
        return appDao.listenAllAppsFullInfo()
            .map { apps ->
                apps.map { fullAppInfo ->
                    fullAppInfo.toAppInfo()
                }
            }
            .flowOn(dispatchersContainer.Default)
    }

    override fun getAppExtendedInfo(appId: String): Flow<AppExtendedFullInfo> {
        return appDao.listenAppExtendedFullInfo(appId)
            .filterNotNull()
            .map { extendedFullAppInfo ->
                extendedFullAppInfo.toAppExtendedInfo()
            }
            .flowOn(dispatchersContainer.Default)
    }

    override suspend fun getAppInstallationInfo(appId: String): AppInstallationInfo? {
        val appInfo = appDao.getAppInfo(appId) ?: return null
        val appInstallationRoutine = appDao.getAppInstallationRoutine(appId) ?: return null

        return AppInstallationInfo(
            appId = appInfo.appId,
            name = appInfo.name,
            version = appInfo.version,
            installationType = appInstallationRoutine.type,
            source = appInstallationRoutine.source,
            md5 = appInstallationRoutine.md5.orEmpty()
        )
    }

    override suspend fun fetchAndSaveAppsList(): FetchAppsResult {
        val response = runCatching {
            appService.getAppList()
            //save RefreshTime here
        }.getOrNull()

        if (response?.isSuccessful == true) {
            response.body()?.apps?.let { appsList ->
                saveAppsList(appsList)
                checkSelfUpdate(appsList)
                sendAppListForLauncher(appsList)
            }
            LauncherPreference.setLatestApplistRefreshTime(appContext, Calendar.getInstance().timeInMillis)
            return FetchAppsResult.SUCCESS
        } else {
            return if (response?.code() == Utils.HTTP_CODE_SERVER_UNAVAILABLE) FetchAppsResult.SERVER_UNAVAILABLE else FetchAppsResult.FAILED
        }
    }

    private fun sendAppListForLauncher(appsList: List<AppInfoDTO>) {

        var packageId = ArrayList<String>()
        var appNames = ArrayList<String>()
        var marketplaceIcons = ArrayList<String>()
        for(app in appsList ){
            var appInstalledOrNot : Int = Utils.appInstalledOrNot(appContext, app.packageId, app.name)
            if(app.launcher==true && appInstalledOrNot!=0) {
                packageId.add(app.packageId)
                appNames.add(app.name)
                marketplaceIcons.add(app.iconUrl)
            }
        }
        LauncherPreference.setDMACPackageIds(appContext, packageId)
        LauncherPreference.setDMACAppNames(appContext, appNames)
        LauncherPreference.setDMACAppMarketplaceIcon(appContext, marketplaceIcons)
    }

    private suspend fun checkSelfUpdate(appsList: List<AppInfoDTO>) {
        val selfPackageName = appContext.packageName

        val latestVersion = withContext(dispatchersContainer.Default) {
            appsList
                .firstOrNull { it.packageId == selfPackageName }
                ?.version
        } ?: return

        if (BuildConfig.VERSION_NAME != latestVersion) {
            val nextUpgradeTime = launcherPreferenceWrapper.getUpgradePostponedTo()
            if (System.currentTimeMillis() > nextUpgradeTime) {
                _needSelfUpdate.emit(true)
            }
        }
    }

    private suspend fun saveAppsList(appInfoList: List<AppInfoDTO>) {
        appWriteDao.inTransaction {
            deleteAllAppInfos()

            appInfoList.forEach { appInfoDTO ->
                saveAppBaseInfo(appInfoDTO)
            }
        }
    }

    override suspend fun fetchAndSaveAppDetails(appId: String): FetchAppsResult {
        val response = runCatching {
            appService.getAppDetails(appId)
        }.getOrNull()

        if (response?.isSuccessful == true) {
            response.body()?.let { appDetailsInfo ->
                saveAppDetailsInfo(appDetailsInfo)
            }
            return FetchAppsResult.SUCCESS
        } else {
            return if (response?.code() == Utils.HTTP_CODE_SERVER_UNAVAILABLE) FetchAppsResult.SERVER_UNAVAILABLE else FetchAppsResult.FAILED
        }
    }

    private suspend fun saveAppDetailsInfo(appDetailsInfo: AppInfoExtendedDTO) {
        appWriteDao.inTransaction {
            saveAppBaseInfo(appDetailsInfo)

            insertAppExtraInfo(appDetailsInfo.toAppExtraInfoEntity())
        }
    }

    private suspend fun AppWriteDao.saveAppBaseInfo(appBaseInfo: BaseAppInfoDTO) {
        //base app info
        insertAppInfo(appBaseInfo.toAppInfoEntity())

        val actions = appBaseInfo.actions
        if (actions != null) {
            val actionEntities = actions.map {
                it.toAppActionEntity(appBaseInfo.appId)
            }
            insertAppActionList(actionEntities)
        }


        if (!appBaseInfo.categories.isNullOrEmpty()) {
            val categories = appBaseInfo.categories
                .orEmpty()
                .map {
                    it.toAppCategoryEntity(appBaseInfo.appId)
                }
            insertAppCategories(categories)
        }

        insertAppBBFied(appBaseInfo.toAppBBFiedEntity())

        //installation routine
        val installationRoutine = appBaseInfo.installationRoutines.first()
        val installationRoutineEntity =
            installationRoutine.toAppInstallationRoutineEntity(appBaseInfo.appId)
        insertAppInstallationRoutine(installationRoutineEntity)

        //installation dependencies
        if (installationRoutine.dependencies != null) {
            val installationDependencies = installationRoutine.dependencies.map {
                it.toAppInstallationDependencyEntity(appBaseInfo.appId)
            }
            insertAppInstallationDependencyList(installationDependencies)
        }

        insertAppCalculatedInfoEntity(appBaseInfo.toAppCalculatedInfoEntity())
    }

    override suspend fun getAppWebLink(appId: String): String? = appDao.getAppWebLink(appId)

    override suspend fun getAppPackageName(appId: String): String? = appDao.getAppPackageName(appId)

    override suspend fun getAppIdByPackageName(packageName: String): String? =
        appDao.getAppIdByPackageName(packageName)

    override suspend fun getAppActionDetails(appId: String, actionId: String): AppActionInfo? {
        val actionEntity = appDao.getAppAction(appId, actionId) ?: return null

        return actionEntity.toAppActionInfo()
    }

    override suspend fun getAppDependenciesStatus(appId: String): AppDependenciesStatus {
        val dependencies = appDao.getAppDependencies(appId)
        val unresolvedDependencies = withContext(dispatchersContainer.Default) {
            dependencies.filterNot {
                dependencyUtils.isDependencyResolved(it.dependencyPackageId, it.version)
            }
        }

        if (unresolvedDependencies.isEmpty()) {
            return AppDependenciesStatus.Resolved
        }

        if (unresolvedDependencies.any { !it.available }) {
            return AppDependenciesStatus.Unresolvable
        }

        val unresolvedDependencyItems = withContext(dispatchersContainer.Default) {
            unresolvedDependencies.map { dependency ->
                InstallationAppItem(
                    dependency.dependencyAppId,
                    dependency.appName,
                    dependency.version,
                    dependency.source,
                    dependency.md5
                )
            }
        }

        return AppDependenciesStatus.Unresolved(unresolvedDependencyItems)
    }

    override suspend fun getInstallationAppItem(appId: String): InstallationAppItem {
        val appInfo = checkNotNull(appDao.getAppInfo(appId))
        val appInstallationRoutine = checkNotNull(appDao.getAppInstallationRoutine(appId))

        return InstallationAppItem(
            appId,
            appInfo.name,
            appInfo.version,
            appInstallationRoutine.source,
            appInstallationRoutine.md5.orEmpty()
        )
    }

    override suspend fun hasApp(appId: String): Boolean {
        return appDao.getAppPackageName(appId) != null
    }
}