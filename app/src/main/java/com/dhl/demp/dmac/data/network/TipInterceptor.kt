package com.dhl.demp.dmac.data.network

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TipInterceptor @Inject constructor(): Interceptor{

    var connected:Boolean = true


    override fun intercept(chain: Interceptor.Chain): Response {
        if(connected==false){
            val originalRequest = chain.request()
            val tipRequest = originalRequest.newBuilder()
                .url("https://magtw1-off.dhl.com")
                .build()
            return chain.proceed(tipRequest)

        }else{
            return chain.proceed(chain.request())
        }
    }
}