package com.dhl.demp.dmac.data.network

import android.content.Context
import com.dhl.demp.mydmac.LauncherPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

const val AUTH_HEADER = "Authorization"
private const val CONNECTION_HEADER = "Connection"
private const val ACCEPT_HEADER = "Accept"

class HeadersInterceptor @Inject constructor(
    @ApplicationContext private val appContext: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val accessToken: String? = LauncherPreference.getToken(appContext)

        if (accessToken.isNullOrEmpty() || hasAuthorizationHeader(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        val authorisedRequest = originalRequest.newBuilder()
            .addHeader(AUTH_HEADER, "Bearer $accessToken")
            .addHeader(CONNECTION_HEADER, "close")
            .addHeader(ACCEPT_HEADER, "application/json")
            .build()

        return chain.proceed(authorisedRequest)
    }

    private fun hasAuthorizationHeader(originalRequest: Request): Boolean {
        return originalRequest.header(AUTH_HEADER) != null
    }
}