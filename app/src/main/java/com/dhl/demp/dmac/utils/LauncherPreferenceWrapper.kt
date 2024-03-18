package com.dhl.demp.dmac.utils

import android.content.Context
import com.dhl.demp.dmac.model.DeviceOwner
import com.dhl.demp.dmac.model.SimOwner
import com.dhl.demp.mydmac.LauncherPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LauncherPreferenceWrapper @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val dispatchersContainer: DispatchersContainer,
) {
    suspend fun getClientEmail(): String {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.getClientEmail(appContext)
        }
    }

    suspend fun getId(): String {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.getId(appContext)
        }
    }

    suspend fun getToken(): String {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.getToken(appContext)
        }
    }

    suspend fun getRefToken(): String {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.getRefToken(appContext)
        }
    }

    suspend fun setChangePasscode(value: Boolean) {
        withContext(dispatchersContainer.IO) {
            LauncherPreference.setChangePasscode(appContext, value)
        }
    }

    suspend fun isChangePasscode(): Boolean {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.isChangePasscode(appContext)
        }
    }

    suspend fun getLastPasscodeForAppId(appId: String, sharedPin: Boolean): Long {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.getLastPasscodeForAppId(appContext, appId, sharedPin)
        }
    }

    suspend fun setLocked(value: Boolean) {
        withContext(dispatchersContainer.IO) {
            LauncherPreference.setLocked(appContext, value)
        }
    }

    suspend fun getLocked(): Boolean {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.getLocked(appContext)
        }
    }

    suspend fun loadDeviceOwner(): DeviceOwner {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.loadDeviceOwner(appContext)
        }
    }

    suspend fun saveDeviceOwner(deviceOwner: DeviceOwner) {
        withContext(dispatchersContainer.IO) {
            LauncherPreference.saveDeviceOwner(appContext, deviceOwner)
        }
    }

    suspend fun loadSimOwner(): SimOwner {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.loadSimOwner(appContext)
        }
    }

    suspend fun saveSimOwner(simOwner: SimOwner) {
        withContext(dispatchersContainer.IO) {
            LauncherPreference.saveSimOwner(appContext, simOwner)
        }
    }

    suspend fun getUpgradePostponedTo(): Long {
        return withContext(dispatchersContainer.IO) {
            LauncherPreference.getUpgradePostponedTo(appContext)
        }
    }

    suspend fun setUpgradePostponedTo(value: Long) {
        withContext(dispatchersContainer.IO) {
            LauncherPreference.setUpgradePostponedTo(appContext, value)
        }
    }
}