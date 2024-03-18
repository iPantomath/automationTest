package com.dhl.demp.dmac.di.module

import android.content.Context
import androidx.room.Room
import com.dhl.demp.dmac.data.db.DB_NAME
import com.dhl.demp.dmac.data.db.DmacDatabase
import com.dhl.demp.dmac.data.db.dao.AppDao
import com.dhl.demp.dmac.data.db.dao.AppWriteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DBModule {
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext appContext: Context): DmacDatabase =
        Room.databaseBuilder(appContext, DmacDatabase::class.java, DB_NAME)
            .build()

    @Singleton
    @Provides
    fun provideAppDao(db: DmacDatabase): AppDao = db.getAppDao()

    @Singleton
    @Provides
    fun provideAppWriteDao(db: DmacDatabase): AppWriteDao = db.getAppWriteDao()
}