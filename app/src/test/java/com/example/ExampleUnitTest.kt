package com.example

import com.example.model.UkCity
import com.example.model.VirtualPhoneSettings
import com.example.util.UkFormatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class ExampleUnitTest {

    @Test
    fun ukPhoneFormatter_formatsMobileAndLandlineWithPlus44() {
        assertEquals("+44 7700 900123", UkFormatters.formatUkPhoneNumber("07700900123"))
        assertEquals("+44 20 7946 0000", UkFormatters.formatUkPhoneNumber("+44 20 7946 0000"))
        assertEquals("111", UkFormatters.formatUkPhoneNumber("111"))
        assertTrue(UkFormatters.isValidUkNumber("07700900123"))
    }

    @Test
    fun ukBankHolidays_includesChristmasAndEarlyMay() {
        val holidays = UkFormatters.ukBankHolidaysForYear(2026)
        assertTrue(holidays.values.any { it.contains("Christmas Day") })
        assertTrue(holidays.values.any { it.contains("Early May") })
        assertEquals("Christmas Day (UK)", holidays[LocalDate.of(2026, Month.DECEMBER, 25)])
    }

    @Test
    fun haversineDistance_londonToManchesterIsRealistic() {
        val km = UkFormatters.haversineKm(
            UkCity.LONDON.latitude,
            UkCity.LONDON.longitude,
            UkCity.MANCHESTER.latitude,
            UkCity.MANCHESTER.longitude
        )
        assertTrue(km in 250.0..280.0)
    }

    @Test
    fun virtualPhoneSettings_defaultsMatchUkProfile() {
        val defaults = VirtualPhoneSettings()
        assertEquals("United Kingdom", defaults.countryName)
        assertEquals("Europe/London", defaults.timeZoneId)
        assertEquals("GBP (£)", defaults.currencyLabel)
        assertEquals("+44", defaults.dialPrefix)
        assertEquals("14°C", defaults.formatTemperature(14))
    }
}
