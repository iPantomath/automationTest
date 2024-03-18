package com.dhl.demp.dmac.data.db.typeconverter

import androidx.room.TypeConverter
import com.dhl.demp.dmac.model.InstallationRoutineType

class InstallationRoutineTypeConverter {
    @TypeConverter
    fun fromString(value: String): InstallationRoutineType = InstallationRoutineType.from(value)

    @TypeConverter
    fun toString(type: InstallationRoutineType): String = type.value
}