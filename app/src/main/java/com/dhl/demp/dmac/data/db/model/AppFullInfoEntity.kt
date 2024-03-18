package com.dhl.demp.dmac.data.db.model

import androidx.room.Embedded
import androidx.room.Relation
import com.dhl.demp.dmac.data.db.entity.AppActionEntity
import com.dhl.demp.dmac.data.db.entity.AppBBFiedEntity
import com.dhl.demp.dmac.data.db.entity.AppCalculatedInfoEntity
import com.dhl.demp.dmac.data.db.entity.AppInfoEntity

class AppFullInfoEntity(
    @Embedded
    val appInfo: AppInfoEntity,
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
    val actions: List<AppActionEntity>
)