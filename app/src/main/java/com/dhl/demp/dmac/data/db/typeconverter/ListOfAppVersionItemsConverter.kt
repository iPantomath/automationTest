package com.dhl.demp.dmac.data.db.typeconverter

import androidx.room.TypeConverter
import com.dhl.demp.dmac.model.AppVersionItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ListOfAppVersionItemsConverter {
    private val gson = Gson()
    private val listType = object : TypeToken<List<AppVersionItem>>() {}.type

    @TypeConverter
    fun fromString(value: String): List<AppVersionItem> {
        return try {
            gson.fromJson(value, listType)
        } catch (th: Throwable) {
            emptyList()
        }
    }

    @TypeConverter
    fun toString(list: List<AppVersionItem>): String {
        return gson.toJson(list)
    }
}