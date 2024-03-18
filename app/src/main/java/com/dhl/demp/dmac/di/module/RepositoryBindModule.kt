package com.dhl.demp.dmac.di.module

import com.dhl.demp.dmac.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryBindModule {
    @Singleton
    @Binds
    fun bindNotificationsRepository(repo: NotificationsRepositoryImpl): NotificationsRepository

    @Singleton
    @Binds
    fun bindAppRepository(repo: AppRepositoryImpl): AppRepository

    @Singleton
    @Binds
    fun bindAppRepositoryLegacy(repo: AppRepositoryLegacyImpl): AppRepositoryLegacy

    @Singleton
    @Binds
    fun bindInstalledAppRepository(repo: AppStatusRepositoryImpl): AppStatusRepository

    @Singleton
    @Binds
    fun bindAppInstallManager(manager: AppInstallManagerImpl): AppInstallManager

    @Singleton
    @Binds
    fun bindBBfiedManager(manager: BBfiedManagerImpl): BBfiedManager

    @Binds
    fun bindLogoutDataCleaner(logoutDataCleaner: LogoutDataCleanerImpl): LogoutDataCleaner
}