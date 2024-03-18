package com.dhl.demp.dmac.data.repository

import android.content.Context
import com.dhl.demp.dmac.data.db.dao.AppWriteDao
import com.dhl.demp.dmac.data.network.AppService
import com.dhl.demp.dmac.data.network.dto.ApproveRequestDTO
import com.dhl.demp.dmac.utils.LauncherPreferenceWrapper
import com.dhl.demp.mydmac.utils.Utils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BBfiedManagerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val appService: AppService,
    private val appWriteDao: AppWriteDao,
    private val launcherPreference: LauncherPreferenceWrapper
) : BBfiedManager {
    override suspend fun requestApprove(appId: String): Boolean {
        val email = launcherPreference.getClientEmail()
        val clientId = launcherPreference.getId()
        val requestId = Utils.generateUniqueId(appContext)
        val requestBody = ApproveRequestDTO(email, appId, requestId, clientId)

        val response = runCatching {
            appService.requestApprove(requestBody)
        }.getOrNull()

        if (response?.isSuccessful == true) {
            val newApproveState = response.body()?.approveState
            appWriteDao.updateApproveState(appId, newApproveState)

            return true
        } else {
            return false
        }
    }
}