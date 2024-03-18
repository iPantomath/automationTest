package com.dhl.demp.dmac.data.repository

import com.dhl.demp.dmac.data.model.FetchAppsResult

/*
This repository implements methods, needed for legacy code
 */
interface AppRepositoryLegacy {
    fun deleteAllApps()
    fun hasApp(appId: String): Boolean
    fun getPackageName(appId: String): String?
    fun getAppLatestMD5(appId: String, callback: GetAppLatestMD5callback)
    fun fetchAndSaveAppsList(): FetchAppsResult
}

interface GetAppLatestMD5callback {
    fun onLatestMD5(latestMD5: String?)
}