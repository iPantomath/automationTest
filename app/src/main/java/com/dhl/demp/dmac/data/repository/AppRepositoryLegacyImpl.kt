package com.dhl.demp.dmac.data.repository

import android.content.Context
import com.dhl.demp.dmac.data.db.dao.AppDao
import com.dhl.demp.dmac.data.db.dao.AppWriteDao
import com.dhl.demp.dmac.data.model.FetchAppsResult
import com.dhl.demp.dmac.data.network.AppService
import com.dhl.demp.dmac.utils.DispatchersContainer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class AppRepositoryLegacyImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appWriteDao: AppWriteDao,
    private val appDao: AppDao,
    private val appService: AppService,
    private val dispatchersContainer: DispatchersContainer,
    private val appRepository: AppRepository
) : AppRepositoryLegacy {
    override fun deleteAllApps() {
        runBlocking {
            appWriteDao.deleteAllAppInfos()
            appWriteDao.deleteAllAppExtraInfos()
        }
    }

    override fun hasApp(appId: String): Boolean {
        return runBlocking {
            appDao.getAppInfo(appId) != null
        }
    }

    override fun getPackageName(appId: String): String? {
        return runBlocking {
            appDao.getAppPackageName(appId)
        }
    }

    override fun getAppLatestMD5(appId: String, callback: GetAppLatestMD5callback) {
        GlobalScope.launch(dispatchersContainer.Main) {
            val response = runCatching {
                appService.getAppDetails(appId)
            }.getOrNull()

            val md5 = if (response?.isSuccessful == true) {
                response.body()
                    ?.installationRoutines
                    ?.firstOrNull()
                    ?.md5
            } else {
                null
            }

            callback.onLatestMD5(md5)
        }
    }

    override fun fetchAndSaveAppsList(): FetchAppsResult{

        return runBlocking {
            appRepository.fetchAndSaveAppsList()
        }
    }
}