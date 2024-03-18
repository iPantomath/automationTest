package com.dhl.demp.dmac.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_info")
data class AppInfoEntity(
    @[PrimaryKey ColumnInfo(name = "app_id")] val appId: String,
    @ColumnInfo(name = "package_id") val packageId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "icon_url") val iconUrl: String,
    @ColumnInfo(name = "website") val website: String?,
    @ColumnInfo(name = "version") val version: String,
    @ColumnInfo(name = "version_date_millis") val versionDateMillis: Long,
    @ColumnInfo(name = "release_type") val releaseType : String?,
    @ColumnInfo(name = "annotation") val annotation: String?,
    @ColumnInfo(name = "available") val available: Boolean,
    @ColumnInfo(name = "launcher") val launcher: Boolean,
    @ColumnInfo(name = "hidden") val hidden: Boolean
)