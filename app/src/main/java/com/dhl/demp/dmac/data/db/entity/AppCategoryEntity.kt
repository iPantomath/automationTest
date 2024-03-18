package com.dhl.demp.dmac.data.db.entity

import androidx.room.*
import com.dhl.demp.dmac.data.db.typeconverter.ListOfStringsConverter

@Entity(
    tableName = "app_category",
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
@TypeConverters(value = [ListOfStringsConverter::class])
data class AppCategoryEntity(
    @[PrimaryKey(autoGenerate = true) ColumnInfo(name = "id")] val id: Long,
    @ColumnInfo(name = "app_id") val appId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "tags") val tags: List<String>,
)