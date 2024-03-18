package com.dhl.demp.dmac.data.db.model

import androidx.room.Embedded
import androidx.room.Relation
import com.dhl.demp.dmac.data.db.entity.*

class AppExtendedFullInfoEntity(
    @Embedded
    val appInfo: AppInfoEntity,
    @Relation(
        parentColumn = "app_id",
        entityColumn = "app_id"
    )
    val extraInfo: AppExtraInfoEntity?,
    @Relation(
        parentColumn = "app_id",
        entityColumn = "app_id"
    )
    val calculatedInfo: AppCalculatedInfoEntity,
    @Relation(
        parentColumn = "app_id",
        entityColumn = "app_id"
    )
    val bbFiedInfo: AppBBFiedEntity,
    @Relation(
        parentColumn = "app_id",
        entityColumn = "app_id"
    )
    val actions: List<AppActionEntity>,
    @Relation(
        parentColumn = "app_id",
        entityColumn = "app_id"
    )
    val categories: List<AppCategoryEntity>,
    @Relation(
        parentColumn = "app_id",
        entityColumn = "app_id"
    )
    val installationDependencies: List<AppInstallationDependencyEntity>
)