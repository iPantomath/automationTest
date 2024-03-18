package com.dhl.demp.dmac.di.module

import android.content.Context
import com.dhl.demp.dmac.di.qualifier.DeepLinkScheme
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mydmac.R

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    @DeepLinkScheme
    fun provideDeepLinkScheme(@ApplicationContext appContext: Context): String =
        appContext.getString(R.string.deeplink_scheme)
}