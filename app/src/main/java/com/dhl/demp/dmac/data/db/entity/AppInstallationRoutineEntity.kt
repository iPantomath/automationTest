package com.dhl.demp.dmac.data.db.entity

import androidx.room.*
import com.dhl.demp.dmac.data.db.typeconverter.InstallationRoutineTypeConverter
import com.dhl.demp.dmac.model.InstallationRoutineType

@Entity(
    tableName = "app_install_routine",
    foreignKeys = [
        ForeignKey(
            entity = AppInfoEntity::class,
            parentColumns = ["app_id"],
            childColumns = ["app_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
@TypeConverters(InstallationRoutineTypeConverter::class)
data class AppInstallationRoutineEntity(
    @[PrimaryKey ColumnInfo(name = "app_id")] val appId: String,
    @ColumnInfo(name = "type") val type: InstallationRoutineType,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "size") val size: Long?,
    @ColumnInfo(name = "md5") val md5: String?
)