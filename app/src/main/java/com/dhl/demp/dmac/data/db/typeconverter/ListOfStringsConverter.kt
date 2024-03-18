package com.dhl.demp.dmac.data.db.typeconverter

import androidx.room.TypeConverter

private const val LIST_DELIMITER = "\n"

class ListOfStringsConverter {
    @TypeConverter
    fun fromString(value: String): List<String> =
        if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(LIST_DELIMITER)
        }

    @TypeConverter
    fun toString(list: List<String>): String = list.joinToString(separator = LIST_DELIMITER)
}