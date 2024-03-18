package com.dhl.demp.dmac.data.db.dao

import androidx.room.*
import com.dhl.demp.dmac.data.db.entity.*
import com.dhl.demp.dmac.model.BBfiedApproveState

@Dao
abstract class AppWriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAppInfo(appInfo: AppInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAppExtraInfo(appExtraInfo: AppExtraInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAppActionList(actions: List<AppActionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAppBBFied(bbFied: AppBBFiedEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAppCalculatedInfoEntity(calculatedInfo: AppCalculatedInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAppInstallationRoutine(installationRoutine: AppInstallationRoutineEntity)

    @Insert
    abstract suspend fun insertAppInstallationDependencyList(dependencies: List<AppInstallationDependencyEntity>)

    @Insert
    abstract suspend fun insertAppCategories(categories: List<AppCategoryEntity>)

    @Query("DELETE FROM app_info")
    abstract suspend fun deleteAllAppInfos()

    @Query("DELETE FROM app_extra_info")
    abstract suspend fun deleteAllAppExtraInfos()

    @Query("UPDATE app_bbfied SET bbfied_approve_state=:newApproveState WHERE app_id = :appId")
    abstract suspend fun updateApproveState(appId: String, newApproveState: BBfiedApproveState?)

    @Transaction
    open suspend fun inTransaction(action: suspend AppWriteDao.() -> Unit) = action()
}