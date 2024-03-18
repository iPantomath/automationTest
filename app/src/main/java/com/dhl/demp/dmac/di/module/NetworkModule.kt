package com.dhl.demp.dmac.di.module

import com.dhl.demp.dmac.data.network.AppService
import com.dhl.demp.dmac.data.network.DmacAuthenticator
import com.dhl.demp.dmac.data.network.HeadersInterceptor
import com.dhl.demp.dmac.data.network.TipInterceptor
import com.dhl.demp.dmac.data.network.adapter.BBfiedApproveStateAdapter
import com.dhl.demp.dmac.data.network.adapter.InstallationRoutineTypeAdapter
import com.dhl.demp.dmac.model.BBfiedApproveState
import com.dhl.demp.dmac.model.InstallationRoutineType
import com.dhl.demp.mydmac.api.ApiFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mydmac.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Modifier
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideAppService(retrofit: Retrofit): AppService = retrofit.create(AppService::class.java)

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
                .baseUrl(ApiFactory.baseUrl())
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
            tipInterceptor: TipInterceptor,
            headersInterceptor: HeadersInterceptor,
            authenticator: DmacAuthenticator
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .addInterceptor(tipInterceptor)
                .addInterceptor(headersInterceptor)
                .authenticator(authenticator)

        if (BuildConfig.DEBUG) {
            val loggerInterceptor = HttpLoggingInterceptor()
            loggerInterceptor.level = HttpLoggingInterceptor.Level.BODY
            builder.addNetworkInterceptor(loggerInterceptor)
        }

        return builder.build()
    }

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
            .registerTypeAdapter(InstallationRoutineType::class.java, InstallationRoutineTypeAdapter())
            .registerTypeAdapter(BBfiedApproveState::class.java, BBfiedApproveStateAdapter())
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
            .create()
}