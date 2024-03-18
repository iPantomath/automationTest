package com.dhl.demp.dmac.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.dhl.demp.dmac.data.db.entity.AppActionEntity
import com.dhl.demp.dmac.data.db.entity.AppInfoEntity
import com.dhl.demp.dmac.data.db.entity.AppInstallationDependencyEntity
import com.dhl.demp.dmac.data.db.entity.AppInstallationRoutineEntity
import com.dhl.demp.dmac.data.db.model.AppExtendedFullInfoEntity
import com.dhl.demp.dmac.data.db.model.AppFullInfoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Transaction
    @Query("SELECT * FROM app_info ORDER BY name, app_id")
    fun listenAllAppsFullInfo(): Flow<List<AppFullInfoEntity>>

    @Transaction
    @Query("SELECT * FROM app_info WHERE app_id = :appId")
    fun listenAppExtendedFullInfo(appId: String): Flow<AppExtendedFullInfoEntity?>

    @Query("SELECT package_id FROM app_info WHERE app_id = :appId")
    suspend fun getAppPackageName(appId: String): String?

    @Query("SELECT app_id from app_info WHERE package_id = :packageName")
    suspend fun getAppIdByPackageName(packageName: String): String?

    @Query("SELECT source FROM app_install_routine WHERE app_id = :appId")
    suspend fun getAppWebLink(appId: String): String?

    @Query("SELECT * FROM app_info WHERE app_id = :appId")
    suspend fun getAppInfo(appId: String): AppInfoEntity?

    @Query("SELECT * FROM app_install_routine WHERE app_id = :appId")
    suspend fun getAppInstallationRoutine(appId: String): AppInstallationRoutineEntity?

    @Query("SELECT * FROM app_action WHERE app_id = :appId AND id = :actionId")
    suspend fun getAppAction(appId: String, actionId: String): AppActionEntity?

    @Query("SELECT * FROM app_install_dependency WHERE app_id = :appId")
    suspend fun getAppDependencies(appId: String): List<AppInstallationDependencyEntity>
}