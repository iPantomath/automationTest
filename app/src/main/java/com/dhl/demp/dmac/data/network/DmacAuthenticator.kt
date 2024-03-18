package com.dhl.demp.dmac.data.network

import android.content.Context
import com.dhl.demp.dmac.data.repository.LogoutDataCleaner
import com.dhl.demp.mydmac.LauncherPreference
import com.dhl.demp.mydmac.api.ApiFactory
import com.dhl.demp.mydmac.api.request.RefreshTokenRequest
import com.dhl.demp.mydmac.utils.Utils
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val ERROR_AUTHORIZATION_REQUIRED = "authorization_required"

@Singleton
class DmacAuthenticator @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val logoutDataCleaner: Lazy<LogoutDataCleaner>
) : Authenticator {
    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        try {
            val isRefreshed = refreshToken()

            if (isRefreshed) {
                val accessToken = LauncherPreference.getToken(appContext)

                //repeat request with new access token
                return response.request().newBuilder()
                    .removeHeader(AUTH_HEADER)
                    .addHeader(AUTH_HEADER, "Bearer $accessToken")
                    .build()
            }
        } catch (e: Exception) {
            Timber.d("TOKEN Refresh failed")
        }

        return null
    }

    private fun refreshToken(): Boolean {
        //TODO inject api
        val refreshToken = LauncherPreference.getRefToken(appContext)
        val scope = ApiFactory.getScope()
        val deviceInfo = Utils.buildDeviceInfo(appContext)
        val refreshTokenRequest = RefreshTokenRequest(refreshToken, scope, deviceInfo)
        val apiKey = ApiFactory.getApiKey()

        val api = ApiFactory.buildUnifiedApi()

        val response = api.refreshToken(refreshTokenRequest, apiKey).execute()

        if (response.isSuccessful) {
            Utils.updateTokens(appContext, response.body())
            return true
        }

        if (response.code() == Utils.HTTP_CODE_SERVER_UNAVAILABLE) {
            return false
        }

        if (Utils.canAutoRelogin(appContext, response.code())) {
            val wasReloginned = Utils.autoReloginBlocking(appContext)

            if (wasReloginned) {
                return true
            }
        }

        //This is legacy approach, user should be logged out only if backend sends "error":"authorization_required"
        val needLogout = response.errorBody()?.string()?.contains(ERROR_AUTHORIZATION_REQUIRED, ignoreCase = true)
        if (needLogout == true) {
            logoutDataCleaner.get().cleanData(deleteFCMToken = false)
            Utils.showLoginScreen(appContext, null)
        }

        return false
    }
}