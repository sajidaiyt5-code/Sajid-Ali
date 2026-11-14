package com.example.model

import androidx.compose.ui.graphics.Color
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class UkCity(
    val displayName: String,
    val shortCode: String,
    val regionName: String,
    val constituentCountry: String,
    val postcodePrefix: String,
    val areaCode: String,
    val latitude: Double,
    val longitude: Double,
    val typicalTempCelsius: Int,
    val description: String
) {
    LONDON(
        displayName = "London",
        shortCode = "LDN",
        regionName = "Greater London",
        constituentCountry = "England",
        postcodePrefix = "SW1A",
        areaCode = "020",
        latitude = 51.5074,
        longitude = -0.1278,
        typicalTempCelsius = 14,
        description = "Capital of the United Kingdom along the River Thames"
    ),
    MANCHESTER(
        displayName = "Manchester",
        shortCode = "MAN",
        regionName = "North West England",
        constituentCountry = "England",
        postcodePrefix = "M1",
        areaCode = "0161",
        latitude = 53.4808,
        longitude = -2.2426,
        typicalTempCelsius = 12,
        description = "Major northern hub for technology, media, and industry"
    ),
    BIRMINGHAM(
        displayName = "Birmingham",
        shortCode = "BHX",
        regionName = "West Midlands",
        constituentCountry = "England",
        postcodePrefix = "B1",
        areaCode = "0121",
        latitude = 52.4862,
        longitude = -1.8904,
        typicalTempCelsius = 13,
        description = "Heart of the West Midlands and UK's second-largest city"
    ),
    LIVERPOOL(
        displayName = "Liverpool",
        shortCode = "LPL",
        regionName = "North West England",
        constituentCountry = "England",
        postcodePrefix = "L1",
        areaCode = "0151",
        latitude = 53.4084,
        longitude = -2.9916,
        typicalTempCelsius = 12,
        description = "Historic maritime port city on the Mersey Estuary"
    ),
    LEEDS(
        displayName = "Leeds",
        shortCode = "LDS",
        regionName = "Yorkshire and the Humber",
        constituentCountry = "England",
        postcodePrefix = "LS1",
        areaCode = "0113",
        latitude = 53.8008,
        longitude = -1.5491,
        typicalTempCelsius = 11,
        description = "Financial and cultural centre of West Yorkshire"
    ),
    GLASGOW(
        displayName = "Glasgow",
        shortCode = "GLA",
        regionName = "West Central Scotland",
        constituentCountry = "Scotland",
        postcodePrefix = "G1",
        areaCode = "0141",
        latitude = 55.8642,
        longitude = -4.2518,
        typicalTempCelsius = 10,
        description = "Scotland's largest city on the River Clyde"
    ),
    EDINBURGH(
        displayName = "Edinburgh",
        shortCode = "EDI",
        regionName = "Lothian",
        constituentCountry = "Scotland",
        postcodePrefix = "EH1",
        areaCode = "0131",
        latitude = 55.9533,
        longitude = -3.1883,
        typicalTempCelsius = 10,
        description = "Capital city of Scotland overlooking the Firth of Forth"
    ),
    BRISTOL(
        displayName = "Bristol",
        shortCode = "BRS",
        regionName = "South West England",
        constituentCountry = "England",
        postcodePrefix = "BS1",
        areaCode = "0117",
        latitude = 51.4545,
        longitude = -2.5879,
        typicalTempCelsius = 14,
        description = "Aerospace, creative, and harbourside city in the South West"
    ),
    CARDIFF(
        displayName = "Cardiff",
        shortCode = "CWL",
        regionName = "South Wales",
        constituentCountry = "Wales",
        postcodePrefix = "CF10",
        areaCode = "029",
        latitude = 51.4816,
        longitude = -3.1791,
        typicalTempCelsius = 13,
        description = "Capital city of Wales on the Bristol Channel"
    )
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}

enum class WallpaperStyle(val label: String, val subtitle: String) {
    LONDON_TWILIGHT("London Twilight", "Thames deep indigo silk"),
    HIGHLANDS_MIST("Highlands Mist", "Scottish morning light"),
    ROYAL_GEOMETRIC("Royal Geometric", "Procedural slate & cyan")
}

enum class DateFormatStyle(val pattern: String, val label: String) {
    UK_NUMERIC("dd/MM/yyyy", "DD/MM/YYYY"),
    UK_TEXT("dd MMM yyyy", "DD MMM YYYY")
}

enum class TemperatureUnit(val symbol: String, val label: String) {
    CELSIUS("°C", "Celsius (°C)"),
    FAHRENHEIT("°F", "Fahrenheit (°F)")
}

enum class VirtualAppId(
    val title: String,
    val category: String,
    val accentHex: Long,
    val isCoreDockDefault: Boolean = false
) {
    PHONE("Phone", "Communication", 0xFF16A34A, isCoreDockDefault = true),
    CONTACTS("Contacts", "Communication", 0xFF2563EB),
    MESSAGES("Messages", "Communication", 0xFF0EA5E9, isCoreDockDefault = true),
    BROWSER("Browser", "Internet", 0xFF4F46E5, isCoreDockDefault = true),
    CAMERA("Camera", "Media", 0xFF334155, isCoreDockDefault = true),
    GALLERY("Gallery", "Media", 0xFFEC4899),
    MAPS("UK Maps", "Navigation", 0xFF0D9488),
    CLOCK("Clock", "Utilities", 0xFF7C3AED),
    CALENDAR("Calendar", "Productivity", 0xFFDC2626),
    NOTES("Notes", "Productivity", 0xFFD97706),
    CALCULATOR("Calculator", "Utilities", 0xFF059669),
    FILES("Files", "Utilities", 0xFFEA580C),
    DASHBOARD("Device", "System", 0xFF143673),
    NETWORK("Network", "System", 0xFF0284C7),
    SETTINGS("Settings", "System", 0xFF475569, isCoreDockDefault = true);

    val accentColor: Color get() = Color(accentHex)
}

data class VirtualPhoneSettings(
    val virtualDeviceName: String = "Britannia Pixel Virtual",
    val deviceModelLabel: String = "UK-VPhone 15 Pro (Virtual)",
    val countryName: String = "United Kingdom",
    val countryCode: String = "GB",
    val regionLabel: String = "UK (Great Britain)",
    val languageCode: String = "en-GB",
    val languageLabel: String = "English (United Kingdom)",
    val keyboardLayout: String = "English (UK) QWERTY",
    val currencyLabel: String = "GBP (£)",
    val dialPrefix: String = "+44",
    val timeZoneId: String = "Europe/London",
    val selectedCity: UkCity = UkCity.LONDON,
    val use24HourClock: Boolean = true,
    val dateFormatStyle: DateFormatStyle = DateFormatStyle.UK_NUMERIC,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val wallpaperStyle: WallpaperStyle = WallpaperStyle.LONDON_TWILIGHT,
    val showHomeDashboardWidget: Boolean = true,
    val showQuickSettingsCityChip: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val soundOnTouch: Boolean = true,
    val biometricEnabled: Boolean = false,
    val pinLockEnabled: Boolean = false,
    val pinnedHomeApps: Set<VirtualAppId> = setOf(
        VirtualAppId.DASHBOARD,
        VirtualAppId.MAPS,
        VirtualAppId.CLOCK,
        VirtualAppId.CALENDAR,
        VirtualAppId.NOTES,
        VirtualAppId.CALCULATOR,
        VirtualAppId.FILES,
        VirtualAppId.GALLERY,
        VirtualAppId.CONTACTS,
        VirtualAppId.NETWORK
    ),
    val disabledApps: Set<VirtualAppId> = emptySet()
) {
    fun formatTemperature(celsius: Int): String {
        return when (temperatureUnit) {
            TemperatureUnit.CELSIUS -> "$celsius°C"
            TemperatureUnit.FAHRENHEIT -> "${(celsius * 9 / 5) + 32}°F"
        }
    }

    fun formatCurrentTime(now: ZonedDateTime = ZonedDateTime.now(ZoneId.of(timeZoneId))): String {
        val pattern = if (use24HourClock) "HH:mm" else "hh:mm a"
        return now.format(DateTimeFormatter.ofPattern(pattern, Locale.UK))
    }

    fun formatCurrentDate(now: ZonedDateTime = ZonedDateTime.now(ZoneId.of(timeZoneId))): String {
        return now.format(DateTimeFormatter.ofPattern(dateFormatStyle.pattern, Locale.UK))
    }
}

data class VirtualNotification(
    val id: String,
    val title: String,
    val body: String,
    val sourceApp: VirtualAppId,
    val timestampMillis: Long = System.currentTimeMillis()
)
