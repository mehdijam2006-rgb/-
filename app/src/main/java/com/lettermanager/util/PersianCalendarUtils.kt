package com.lettermanager.util

import java.util.Calendar
import java.util.TimeZone

/**
 * Pure-Kotlin Persian (Jalali/Shamsi) calendar utilities.
 * Converts between Gregorian and Jalali dates.
 */
object PersianCalendarUtils {

    data class PersianDate(val year: Int, val month: Int, val day: Int) {
        override fun toString(): String = "%04d/%02d/%02d".format(year, month, day)
        fun toDisplayString(): String = "%04d/%02d/%02d".format(year, month, day)
        fun toMonthDisplay(): String = "$year/${persianMonthName(month)}"
    }

    private val persianMonthNames = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    fun persianMonthName(month: Int): String = persianMonthNames.getOrElse(month - 1) { "" }

    fun persianDayOfWeekName(dayOfWeek: Int): String = when (dayOfWeek) {
        Calendar.SATURDAY -> "شنبه"
        Calendar.SUNDAY -> "یکشنبه"
        Calendar.MONDAY -> "دوشنبه"
        Calendar.TUESDAY -> "سه‌شنبه"
        Calendar.WEDNESDAY -> "چهارشنبه"
        Calendar.THURSDAY -> "پنجشنبه"
        Calendar.FRIDAY -> "جمعه"
        else -> ""
    }

    /**
     * Convert Gregorian date to Jalali date.
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): PersianDate {
        val g_d_no: Int
        val j_d_no: Int
        val i: Int
        var j: Int

        val g_days_in_month = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val j_days_in_month = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1

        g_d_no = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400

        var gd3 = g_d_no
        for (idx in 0 until gm2) gd3 += g_days_in_month[idx]
        if (gm2 > 1 && ((gy2 + 1600) % 4 == 0 && (gy2 + 1600) % 100 != 0 || (gy2 + 1600) % 400 == 0)) gd3++
        gd3 += gd2

        j_d_no = gd3 - 79

        val j_np = j_d_no / 12053
        var jd2 = j_d_no % 12053

        var jy = 979 + 33 * j_np + 4 * (jd2 / 1461)
        jd2 %= 1461

        if (jd2 >= 366) {
            jy += (jd2 - 1) / 365
            jd2 = (jd2 - 1) % 365
        }

        var jd3 = jd2
        j = 0
        while (j < 11 && jd3 >= j_days_in_month[j]) {
            jd3 -= j_days_in_month[j]
            j++
        }

        val jm = j + 1
        val jd4 = jd3 + 1

        return PersianDate(jy, jm, jd4)
    }

    /**
     * Convert Jalali date to Gregorian date.
     * Returns Triple(year, month, day)
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        var jy2 = jy - 979
        val jm2 = jm - 1
        var jd2 = jd - 1

        var j_day_no = 365 * jy2 + (jy2 / 33) * 8 + (jy2 % 33 + 3) / 4

        val j_days_in_month = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
        for (i in 0 until jm2) j_day_no += j_days_in_month[i]
        j_day_no += jd2

        var g_day_no = j_day_no + 79

        var gy = 1600 + 400 * (g_day_no / 146097)
        g_day_no %= 146097

        var leap = true
        if (g_day_no >= 36525) {
            g_day_no--
            gy += 100 * (g_day_no / 36524)
            g_day_no %= 36524
            if (g_day_no >= 365) g_day_no++ else leap = false
        }

        gy += 4 * (g_day_no / 1461)
        g_day_no %= 1461

        if (g_day_no >= 366) {
            leap = false
            g_day_no--
            gy += g_day_no / 365
            g_day_no %= 365
        }

        val g_days_in_month = intArrayOf(31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < g_days_in_month.size && g_day_no >= g_days_in_month[gm]) {
            g_day_no -= g_days_in_month[gm]
            gm++
        }

        return Triple(gy, gm + 1, g_day_no + 1)
    }

    fun timestampToJalali(timestamp: Long): PersianDate {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.timeInMillis = timestamp
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun jalaliToTimestamp(year: Int, month: Int, day: Int): Long {
        val (gy, gm, gd) = jalaliToGregorian(year, month, day)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.set(gy, gm - 1, gd, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun parseJalaliString(dateStr: String): PersianDate? {
        return try {
            val parts = dateStr.split("/")
            if (parts.size == 3) {
                PersianDate(parts[0].trim().toInt(), parts[1].trim().toInt(), parts[2].trim().toInt())
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun currentJalaliDate(): PersianDate {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun currentJalaliYear(): Int = currentJalaliDate().year

    fun isLeapJalaliYear(year: Int): Boolean {
        val remainders = intArrayOf(1, 5, 9, 13, 17, 22, 26, 30)
        return (year - 474) % 2820 + 474 + 38 in remainders.map { ((year - 474) % 2820 + 474 + 38).let { y -> y } }
            .let { remainders.contains((year % 2820 + 474 + 38) % 2820) }
    }

    fun daysInJalaliMonth(year: Int, month: Int): Int = when {
        month <= 6 -> 31
        month <= 11 -> 30
        isLeapJalaliYear(year) -> 30
        else -> 29
    }
}
