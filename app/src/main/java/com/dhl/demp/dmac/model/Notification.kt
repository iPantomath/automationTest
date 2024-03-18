package com.dhl.demp.dmac.model

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

private val FORMATTER_DAY = SimpleDateFormat("d MMM")
private val FORMATTER_TIME = SimpleDateFormat("HH:mm")

data class Notification(
        val date: Date,
        val title: String,
        val message: String
) {
    val displayDate: String
        get() =
            if (DateUtils.isToday(date.time)) {
                FORMATTER_TIME.format(date)
            } else {
                FORMATTER_DAY.format(date)
            }
}