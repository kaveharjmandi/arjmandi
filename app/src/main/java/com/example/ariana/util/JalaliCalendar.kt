package com.example.ariana.util

import java.text.DecimalFormat
import java.util.Calendar

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    override fun toString(): String {
        return "%04d/%02d/%02d".format(year, month, day)
    }

    fun toPersianString(): String {
        return JalaliCalendar.toPersianDigits(toString())
    }

    fun toLongReadable(): String {
        val mName = if (month in 1..12) JalaliCalendar.MONTH_NAMES[month - 1] else ""
        return JalaliCalendar.toPersianDigits("$day $mName $year")
    }
}

object JalaliCalendar {

    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder()
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(text: String): String {
        val sb = StringBuilder()
        for (ch in text) {
            when (ch) {
                '۰' -> sb.append('0')
                '۱' -> sb.append('1')
                '۲' -> sb.append('2')
                '۳' -> sb.append('3')
                '۴' -> sb.append('4')
                '۵' -> sb.append('5')
                '۶' -> sb.append('6')
                '۷' -> sb.append('7')
                '۸' -> sb.append('8')
                '۹' -> sb.append('9')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatToman(amount: Long): String {
        val formatter = DecimalFormat("#,###")
        val formatted = formatter.format(amount)
        return toPersianDigits(formatted)
    }

    fun formatToman(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            formatToman(amount.toLong())
        } else {
            val formatter = DecimalFormat("#,##0.00")
            toPersianDigits(formatter.format(amount))
        }
    }

    fun todayIso(): String {
        val c = Calendar.getInstance()
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val d = c.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }

    fun today(): JalaliDate {
        val c = Calendar.getInstance()
        return gregorianToJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    fun todayString(): String {
        return today().toLongReadable()
    }

    fun isoToJalali(iso: String): JalaliDate {
        val clean = iso.trim()
        val parts = clean.split("-", "/")
        if (parts.size >= 3) {
            val p0 = parts[0].toIntOrNull() ?: 2026
            val p1 = parts[1].toIntOrNull() ?: 1
            val p2 = parts[2].substringBefore("T").substringBefore(" ").toIntOrNull() ?: 1

            // If it's already Jalali year (e.g. 1403, 1404, 1405)
            if (p0 in 1300..1500) {
                return JalaliDate(p0, p1, p2)
            }
            return gregorianToJalali(p0, p1, p2)
        }
        return today()
    }

    fun formatJalaliShort(iso: String): String {
        val jd = isoToJalali(iso)
        val mName = if (jd.month in 1..12) MONTH_NAMES[jd.month - 1] else ""
        return toPersianDigits("${jd.day} $mName")
    }

    fun formatJalaliLong(iso: String): String {
        val jd = isoToJalali(iso)
        return jd.toLongReadable()
    }

    fun formatJalaliNumeric(iso: String): String {
        val jd = isoToJalali(iso)
        return toPersianDigits("%04d/%02d/%02d".format(jd.year, jd.month, jd.day))
    }

    fun jalaliToIso(jy: Int, jm: Int, jd: Int): String {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        return "%04d-%02d-%02d".format(gy, gm, gd)
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDays = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        var gy2 = gy
        val jy0 = if (gy2 <= 1600) 0 else 979
        gy2 -= if (gy2 <= 1600) 621 else 1600
        val gy3 = if (gm > 2) (gy2 + 1) else gy2
        var days = (365 * gy2) + (gy3 + 3) / 4 - (gy3 + 99) / 100 + (gy3 + 399) / 400 - 80 + gd + gDays[gm - 1]
        var jy = jy0 + 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + (if (days < 186) (days % 31) else ((days - 186) % 30))
        return JalaliDate(jy, jm, jd)
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        var days = -355668 + (365 * jy2) + ((jy2 / 33) * 8) + (((jy2 % 33) + 3) / 4) + jd +
                (if (jm < 7) ((jm - 1) * 31) else (((jm - 7) * 30) + 186))
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            gy += 100 * (--days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val isLeapG = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
        val salA = intArrayOf(0, 31, if (isLeapG) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        var gd = days + 1
        for (m in 1..12) {
            if (gd <= salA[m]) {
                gm = m
                break
            }
            gd -= salA[m]
        }
        return Triple(gy, gm, gd)
    }

    fun getDaysInJalaliMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            month == 12 -> if (isJalaliLeapYear(year)) 30 else 29
            else -> 30
        }
    }

    private fun isJalaliLeapYear(jy: Int): Boolean {
        val (gy, gm, gd) = jalaliToGregorian(jy, 12, 30)
        val j = gregorianToJalali(gy, gm, gd)
        return j.year == jy && j.month == 12 && j.day == 30
    }
}
