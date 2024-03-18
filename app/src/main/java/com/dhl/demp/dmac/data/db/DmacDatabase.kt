package com.dhl.demp.dmac.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dhl.demp.dmac.data.db.dao.AppDao
import com.dhl.demp.dmac.data.db.dao.AppWriteDao
import com.dhl.demp.dmac.data.db.entity.*

private const val DB_VERSION = 1
const val DB_NAME = "dmac.db"

@Database(
    entities = [
        AppInfoEntity::class,
        AppInstallationRoutineEntity::class,
        AppCalculatedInfoEntity::class,
        AppBBFiedEntity::class,
        AppActionEntity::class,
        AppInstallationDependencyEntity::class,
        AppExtraInfoEntity::class,
        AppCategoryEntity::class,
    ],
    version = DB_VERSION
)
abstract class DmacDatabase : RoomDatabase() {
    abstract fun getAppDao(): AppDao
    abstract fun getAppWriteDao(): AppWriteDao
}