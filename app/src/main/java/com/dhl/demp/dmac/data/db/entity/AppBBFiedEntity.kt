package com.dhl.demp.dmac.data.db.entity

import androidx.room.*
import com.dhl.demp.dmac.data.db.typeconverter.BBfiedApproveStateConverter
import com.dhl.demp.dmac.model.BBfiedApproveState

@Entity(
    tableName = "app_bbfied",
    foreignKeys = [
        ForeignKey(
            entity = AppInfoEntity::class,
            parentColumns = ["app_id"],
            childColumns = ["app_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(BBfiedApproveStateConverter::class)
data class AppBBFiedEntity(
    @[PrimaryKey ColumnInfo(name = "app_id")] val appId: String,
    @ColumnInfo(name = "is_bbfied") val isBBfied: Boolean,
    @ColumnInfo(name = "bbfied_approve_state") val bbfiedApproveState: BBfiedApproveState?,
)