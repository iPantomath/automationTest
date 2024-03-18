package com.dhl.demp.mydmac.api

import android.content.Context
import com.dhl.demp.mydmac.LauncherApplication
import com.dhl.demp.mydmac.LauncherPreference
import com.dhl.demp.mydmac.api.request.RefreshTokenRequest
import com.dhl.demp.mydmac.utils.Utils
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val AUTH_HEADER = "Authorization"
private const val TAG = "Authenticator"

class Authenticator : Authenticator {
    private val context: Context
        get() = LauncherApplication.get()

    @Synchronized
    override fun authenticate(route: Route?, response: Response?): Request? {
        if (response == null) {
            return null
        }

        try {
            if (hasBearerAuthorizationToken(response)) {
                val isTokenRefreshed = refreshToken()

                if (isTokenRefreshed) {
                    //repeat request
                    return rewriteRequest(response.request())
                }
            }
        } catch (e: Throwable) {
            Utils.logE(TAG, "TOKEN Refresh failed: ${e.message}")
        }

        return null
    }

    private fun refreshToken(): Boolean {
        val refreshToken = LauncherPreference.getRefToken(context)
        val deviceInfo = Utils.buildDeviceInfo(context)
        Utils.logI(TAG, "refreshToken: refresh_token: " + refreshToken +
                " scope " + ApiFactory.getScope() + " ClientID: " + LauncherPreference.getId(context))

        val api = ApiFactory.buildUnifiedApi()

        val requestTokenBody = RefreshTokenRequest(refreshToken, ApiFactory.getScope(), deviceInfo)
        val apiKey = ApiFactory.getApiKey()
        val refreshTokenRequest = api.refreshToken(requestTokenBody, apiKey)

        val response = refreshTokenRequest.execute()
        if (response.isSuccessful) {
            //save new tokens
            Utils.updateTokens(context, response.body())

            return true
        }

        return false
    }

    private fun hasBearerAuthorizationToken(response: Response): Boolean {
        val authorizationHeader = response.request().header(AUTH_HEADER)
        return authorizationHeader?.startsWith("bearer", ignoreCase = true) ?: false
    }

    private fun rewriteRequest(originalRequest: Request?): Request? {
        val authToken = LauncherPreference.getToken(context)

        return originalRequest?.newBuilder()
                ?.header(AUTH_HEADER, "Bearer $authToken")
                ?.build()
    }
}