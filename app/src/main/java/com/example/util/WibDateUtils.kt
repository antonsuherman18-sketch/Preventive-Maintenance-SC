package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utilitas penanggalan dan waktu khusus Zona WIB (Waktu Indonesia Barat / UTC+7 / Asia/Jakarta).
 */
object WibDateUtils {
    val WIB_TIME_ZONE: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")
    val ID_LOCALE: Locale = Locale("id", "ID")

    fun getCalendar(): Calendar {
        return Calendar.getInstance(WIB_TIME_ZONE, ID_LOCALE)
    }

    fun getCalendar(timestamp: Long): Calendar {
        val cal = Calendar.getInstance(WIB_TIME_ZONE, ID_LOCALE)
        cal.timeInMillis = timestamp
        return cal
    }

    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val cal = getCalendar(timestamp)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun createFormatter(pattern: String, locale: Locale = ID_LOCALE): SimpleDateFormat {
        return SimpleDateFormat(pattern, locale).apply {
            timeZone = WIB_TIME_ZONE
        }
    }

    fun format(pattern: String, date: Date = Date(), locale: Locale = ID_LOCALE): String {
        return createFormatter(pattern, locale).format(date)
    }

    fun format(pattern: String, timestamp: Long, locale: Locale = ID_LOCALE): String {
        return createFormatter(pattern, locale).format(Date(timestamp))
    }
}
