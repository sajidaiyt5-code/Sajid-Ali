package com.example.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object UkFormatters {

    fun formatUkPhoneNumber(rawInput: String): String {
        val digitsOnly = rawInput.filter { it.isDigit() || it == '+' }
        if (digitsOnly.isBlank()) return ""
        if (digitsOnly == "999" || digitsOnly == "111" || digitsOnly == "112" || digitsOnly == "123") {
            return digitsOnly
        }
        val normalized = when {
            digitsOnly.startsWith("+44") -> "+44" + digitsOnly.removePrefix("+44").removePrefix("0")
            digitsOnly.startsWith("0044") -> "+44" + digitsOnly.removePrefix("0044").removePrefix("0")
            digitsOnly.startsWith("0") && digitsOnly.length >= 4 -> "+44" + digitsOnly.drop(1)
            digitsOnly.startsWith("+") -> digitsOnly
            else -> "+44$digitsOnly"
        }
        val national = normalized.removePrefix("+44")
        if (!normalized.startsWith("+44") || national.isEmpty()) return normalized

        return when {
            national.startsWith("7") && national.length >= 10 -> {
                "+44 ${national.substring(0, 4)} ${national.substring(4, 10)}"
            }
            national.startsWith("20") && national.length >= 10 -> {
                "+44 20 ${national.substring(2, 6)} ${national.substring(6, 10)}"
            }
            national.length >= 9 -> {
                "+44 ${national.substring(0, 3)} ${national.substring(3, 6)} ${national.substring(6)}"
            }
            else -> "+44 $national"
        }
    }

    fun isValidUkNumber(rawInput: String): Boolean {
        val clean = rawInput.filter { it.isDigit() }
        return clean in setOf("111", "112", "123", "999") ||
            (clean.length in 10..13)
    }

    fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun ukBankHolidaysForYear(year: Int): Map<LocalDate, String> {
        val holidays = linkedMapOf<LocalDate, String>()

        fun observed(date: LocalDate): LocalDate = when (date.dayOfWeek) {
            DayOfWeek.SATURDAY -> date.plusDays(2)
            DayOfWeek.SUNDAY -> date.plusDays(1)
            else -> date
        }

        // New Year's Day
        holidays[observed(LocalDate.of(year, Month.JANUARY, 1))] = "New Year's Day (UK)"

        // Easter (Anonymous Gregorian algorithm)
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        val easterSunday = LocalDate.of(year, month, day)
        holidays[easterSunday.minusDays(2)] = "Good Friday (UK)"
        holidays[easterSunday.plusDays(1)] = "Easter Monday (Eng/Wal/NI)"

        // Early May (First Monday in May)
        var earlyMay = LocalDate.of(year, Month.MAY, 1)
        while (earlyMay.dayOfWeek != DayOfWeek.MONDAY) earlyMay = earlyMay.plusDays(1)
        holidays[earlyMay] = "Early May Bank Holiday"

        // Spring Bank Holiday (Last Monday in May)
        var spring = LocalDate.of(year, Month.MAY, 31)
        while (spring.dayOfWeek != DayOfWeek.MONDAY) spring = spring.minusDays(1)
        holidays[spring] = "Spring Bank Holiday"

        // Summer Bank Holiday (Last Monday in August - Eng/Wal/NI)
        var summer = LocalDate.of(year, Month.AUGUST, 31)
        while (summer.dayOfWeek != DayOfWeek.MONDAY) summer = summer.minusDays(1)
        holidays[summer] = "Summer Bank Holiday"

        // St Andrew's Day (Scotland - 30 Nov observed)
        holidays[observed(LocalDate.of(year, Month.NOVEMBER, 30))] = "St Andrew's Day (Scotland)"

        // Christmas & Boxing Day
        val xmas = LocalDate.of(year, Month.DECEMBER, 25)
        val boxing = LocalDate.of(year, Month.DECEMBER, 26)
        when (xmas.dayOfWeek) {
            DayOfWeek.FRIDAY -> {
                holidays[xmas] = "Christmas Day (UK)"
                holidays[boxing.plusDays(2)] = "Boxing Day (Substitute)"
            }
            DayOfWeek.SATURDAY -> {
                holidays[xmas.plusDays(2)] = "Christmas Day (Substitute)"
                holidays[boxing.plusDays(2)] = "Boxing Day (Substitute)"
            }
            DayOfWeek.SUNDAY -> {
                holidays[xmas.plusDays(1)] = "Christmas Day (Substitute)"
                holidays[boxing.plusDays(1)] = "Boxing Day (Substitute)"
            }
            else -> {
                holidays[xmas] = "Christmas Day (UK)"
                holidays[boxing] = "Boxing Day (UK)"
            }
        }
        return holidays
    }
}
