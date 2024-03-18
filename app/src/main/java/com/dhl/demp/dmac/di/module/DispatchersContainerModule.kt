package com.dhl.demp.dmac.di.module

import com.dhl.demp.dmac.utils.DispatchersContainer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatchersContainerModule {
    @Singleton
    @Provides
    fun provideDispatchersContainer() = DispatchersContainer()
}