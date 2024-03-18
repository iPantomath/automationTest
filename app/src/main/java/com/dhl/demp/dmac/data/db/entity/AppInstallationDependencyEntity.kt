package com.dhl.demp.dmac.data.db.entity

import androidx.room.*
import com.dhl.demp.dmac.data.db.typeconverter.InstallationRoutineTypeConverter
import com.dhl.demp.dmac.model.InstallationRoutineType

@Entity(
    tableName = "app_install_dependency",
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
@TypeConverters(InstallationRoutineTypeConverter::class)
data class AppInstallationDependencyEntity(
    @[PrimaryKey(autoGenerate = true) ColumnInfo(name = "id")] val id: Long,
    @ColumnInfo(name = "app_id") val appId: String,//id of app to which this dependency belongs
    @ColumnInfo(name = "dependency_app_id") val dependencyAppId: String,//id of the dependency app
    @ColumnInfo(name = "dependency_package_id") val dependencyPackageId: String,
    @ColumnInfo(name = "app_name") val appName: String,
    @ColumnInfo(name = "app_icon") val appIcon: String,
    @ColumnInfo(name = "version") val version: String,
    @ColumnInfo(name = "type") val type: InstallationRoutineType,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "size") val size: Long,
    @ColumnInfo(name = "md5") val md5: String,
    @ColumnInfo(name = "release_type") val releaseType: String?,
    @ColumnInfo(name = "install_before") val installBefore: Boolean,
    @ColumnInfo(name = "available") val available: Boolean,
)