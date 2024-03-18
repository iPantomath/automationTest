package com.dhl.demp.dmac.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_calculated_info",
    foreignKeys = [
        ForeignKey(
            entity = AppInfoEntity::class,
            parentColumns = ["app_id"],
            childColumns = ["app_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AppCalculatedInfoEntity(
    @[PrimaryKey ColumnInfo(name = "app_id")] val appId: String,
    @ColumnInfo(name = "has_dependencies") val hasDependencies: Boolean,
    @ColumnInfo(name = "is_weblink") val isWebLink: Boolean,
)