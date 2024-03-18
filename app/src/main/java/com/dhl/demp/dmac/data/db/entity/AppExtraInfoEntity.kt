package com.dhl.demp.dmac.data.db.entity

import androidx.room.*
import com.dhl.demp.dmac.data.db.typeconverter.ListOfAppVersionItemsConverter
import com.dhl.demp.dmac.data.db.typeconverter.ListOfStringsConverter
import com.dhl.demp.dmac.model.AppVersionItem

@Entity(tableName = "app_extra_info")
@TypeConverters(value = [ListOfStringsConverter::class, ListOfAppVersionItemsConverter::class])
data class AppExtraInfoEntity(
    @[PrimaryKey ColumnInfo(name = "app_id")] val appId: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "release_note") val releaseNote: String,
    @ColumnInfo(name = "screenshots") val screenshots: List<String>,
    @ColumnInfo(name = "versions") val versions: List<AppVersionItem>,
    @ColumnInfo(name = "developed_by") val developedBy: String,
    @ColumnInfo(name = "support_contact") val supportContact: String?
)
