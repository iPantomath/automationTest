package com.dhl.demp.dmac.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "app_action",
    primaryKeys = ["id", "app_id"],
    foreignKeys = [
        ForeignKey(
            entity = AppInfoEntity::class,
            parentColumns = ["app_id"],
            childColumns = ["app_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["app_id"])]
)
data class AppActionEntity(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "app_id") val appId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "target_package_id") val targetPackageId: String,
    @ColumnInfo(name = "message") val message: String?,
)