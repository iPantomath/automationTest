package com.dhl.demp.dmac.data.db.typeconverter

import androidx.room.TypeConverter
import com.dhl.demp.dmac.model.BBfiedApproveState

class BBfiedApproveStateConverter {
    @TypeConverter
    fun fromString(value: String?): BBfiedApproveState? =
        value?.let { BBfiedApproveState.from(value) }

    @TypeConverter
    fun toString(state: BBfiedApproveState?): String? = state?.value
}