package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.DateFormatStyle
import com.example.model.TemperatureUnit
import com.example.model.ThemeMode
import com.example.model.UkCity
import com.example.model.VirtualAppId
import com.example.model.VirtualPhoneSettings
import com.example.model.WallpaperStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "uk_virtual_phone_prefs")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val VIRTUAL_DEVICE_NAME = stringPreferencesKey("virtual_device_name")
        val REGION_LABEL = stringPreferencesKey("region_label")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val LANGUAGE_LABEL = stringPreferencesKey("language_label")
        val SELECTED_CITY = stringPreferencesKey("selected_city")
        val TIME_ZONE_ID = stringPreferencesKey("time_zone_id")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val TEMP_UNIT = stringPreferencesKey("temp_unit")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val WALLPAPER_STYLE = stringPreferencesKey("wallpaper_style")
        val SHOW_HOME_WIDGET = booleanPreferencesKey("show_home_widget")
        val SHOW_QS_CITY_CHIP = booleanPreferencesKey("show_qs_city_chip")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val SOUND_ON_TOUCH = booleanPreferencesKey("sound_on_touch")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val PIN_LOCK_ENABLED = booleanPreferencesKey("pin_lock_enabled")
        val PINNED_HOME_APPS = stringSetPreferencesKey("pinned_home_apps")
        val DISABLED_APPS = stringSetPreferencesKey("disabled_apps")
    }

    val settingsFlow: Flow<VirtualPhoneSettings> = context.settingsDataStore.data.map { prefs ->
        val defaultSettings = VirtualPhoneSettings()
        val pinnedSet = prefs[Keys.PINNED_HOME_APPS]?.mapNotNull { name ->
            runCatching { VirtualAppId.valueOf(name) }.getOrNull()
        }?.toSet() ?: defaultSettings.pinnedHomeApps

        val disabledSet = prefs[Keys.DISABLED_APPS]?.mapNotNull { name ->
            runCatching { VirtualAppId.valueOf(name) }.getOrNull()
        }?.toSet() ?: emptySet()

        VirtualPhoneSettings(
            virtualDeviceName = prefs[Keys.VIRTUAL_DEVICE_NAME] ?: defaultSettings.virtualDeviceName,
            regionLabel = prefs[Keys.REGION_LABEL] ?: defaultSettings.regionLabel,
            languageCode = prefs[Keys.LANGUAGE_CODE] ?: defaultSettings.languageCode,
            languageLabel = prefs[Keys.LANGUAGE_LABEL] ?: defaultSettings.languageLabel,
            selectedCity = prefs[Keys.SELECTED_CITY]?.let {
                runCatching { UkCity.valueOf(it) }.getOrNull()
            } ?: defaultSettings.selectedCity,
            timeZoneId = prefs[Keys.TIME_ZONE_ID] ?: defaultSettings.timeZoneId,
            use24HourClock = prefs[Keys.USE_24_HOUR] ?: defaultSettings.use24HourClock,
            dateFormatStyle = prefs[Keys.DATE_FORMAT]?.let {
                runCatching { DateFormatStyle.valueOf(it) }.getOrNull()
            } ?: defaultSettings.dateFormatStyle,
            temperatureUnit = prefs[Keys.TEMP_UNIT]?.let {
                runCatching { TemperatureUnit.valueOf(it) }.getOrNull()
            } ?: defaultSettings.temperatureUnit,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: defaultSettings.themeMode,
            wallpaperStyle = prefs[Keys.WALLPAPER_STYLE]?.let {
                runCatching { WallpaperStyle.valueOf(it) }.getOrNull()
            } ?: defaultSettings.wallpaperStyle,
            showHomeDashboardWidget = prefs[Keys.SHOW_HOME_WIDGET] ?: defaultSettings.showHomeDashboardWidget,
            showQuickSettingsCityChip = prefs[Keys.SHOW_QS_CITY_CHIP] ?: defaultSettings.showQuickSettingsCityChip,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: defaultSettings.notificationsEnabled,
            soundOnTouch = prefs[Keys.SOUND_ON_TOUCH] ?: defaultSettings.soundOnTouch,
            biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: defaultSettings.biometricEnabled,
            pinLockEnabled = prefs[Keys.PIN_LOCK_ENABLED] ?: defaultSettings.pinLockEnabled,
            pinnedHomeApps = pinnedSet,
            disabledApps = disabledSet
        )
    }

    suspend fun updateSettings(transform: (VirtualPhoneSettings) -> VirtualPhoneSettings) {
        context.settingsDataStore.edit { prefs ->
            val current = VirtualPhoneSettings(
                virtualDeviceName = prefs[Keys.VIRTUAL_DEVICE_NAME] ?: VirtualPhoneSettings().virtualDeviceName,
                regionLabel = prefs[Keys.REGION_LABEL] ?: VirtualPhoneSettings().regionLabel,
                languageCode = prefs[Keys.LANGUAGE_CODE] ?: VirtualPhoneSettings().languageCode,
                languageLabel = prefs[Keys.LANGUAGE_LABEL] ?: VirtualPhoneSettings().languageLabel,
                selectedCity = prefs[Keys.SELECTED_CITY]?.let {
                    runCatching { UkCity.valueOf(it) }.getOrNull()
                } ?: UkCity.LONDON,
                timeZoneId = prefs[Keys.TIME_ZONE_ID] ?: "Europe/London",
                use24HourClock = prefs[Keys.USE_24_HOUR] ?: true,
                dateFormatStyle = prefs[Keys.DATE_FORMAT]?.let {
                    runCatching { DateFormatStyle.valueOf(it) }.getOrNull()
                } ?: DateFormatStyle.UK_NUMERIC,
                temperatureUnit = prefs[Keys.TEMP_UNIT]?.let {
                    runCatching { TemperatureUnit.valueOf(it) }.getOrNull()
                } ?: TemperatureUnit.CELSIUS,
                themeMode = prefs[Keys.THEME_MODE]?.let {
                    runCatching { ThemeMode.valueOf(it) }.getOrNull()
                } ?: ThemeMode.SYSTEM,
                wallpaperStyle = prefs[Keys.WALLPAPER_STYLE]?.let {
                    runCatching { WallpaperStyle.valueOf(it) }.getOrNull()
                } ?: WallpaperStyle.LONDON_TWILIGHT,
                showHomeDashboardWidget = prefs[Keys.SHOW_HOME_WIDGET] ?: true,
                showQuickSettingsCityChip = prefs[Keys.SHOW_QS_CITY_CHIP] ?: true,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                soundOnTouch = prefs[Keys.SOUND_ON_TOUCH] ?: true,
                biometricEnabled = prefs[Keys.BIOMETRIC_ENABLED] ?: false,
                pinLockEnabled = prefs[Keys.PIN_LOCK_ENABLED] ?: false,
                pinnedHomeApps = prefs[Keys.PINNED_HOME_APPS]?.mapNotNull {
                    runCatching { VirtualAppId.valueOf(it) }.getOrNull()
                }?.toSet() ?: VirtualPhoneSettings().pinnedHomeApps,
                disabledApps = prefs[Keys.DISABLED_APPS]?.mapNotNull {
                    runCatching { VirtualAppId.valueOf(it) }.getOrNull()
                }?.toSet() ?: emptySet()
            )
            val updated = transform(current)
            prefs[Keys.VIRTUAL_DEVICE_NAME] = updated.virtualDeviceName
            prefs[Keys.REGION_LABEL] = updated.regionLabel
            prefs[Keys.LANGUAGE_CODE] = updated.languageCode
            prefs[Keys.LANGUAGE_LABEL] = updated.languageLabel
            prefs[Keys.SELECTED_CITY] = updated.selectedCity.name
            prefs[Keys.TIME_ZONE_ID] = updated.timeZoneId
            prefs[Keys.USE_24_HOUR] = updated.use24HourClock
            prefs[Keys.DATE_FORMAT] = updated.dateFormatStyle.name
            prefs[Keys.TEMP_UNIT] = updated.temperatureUnit.name
            prefs[Keys.THEME_MODE] = updated.themeMode.name
            prefs[Keys.WALLPAPER_STYLE] = updated.wallpaperStyle.name
            prefs[Keys.SHOW_HOME_WIDGET] = updated.showHomeDashboardWidget
            prefs[Keys.SHOW_QS_CITY_CHIP] = updated.showQuickSettingsCityChip
            prefs[Keys.NOTIFICATIONS_ENABLED] = updated.notificationsEnabled
            prefs[Keys.SOUND_ON_TOUCH] = updated.soundOnTouch
            prefs[Keys.BIOMETRIC_ENABLED] = updated.biometricEnabled
            prefs[Keys.PIN_LOCK_ENABLED] = updated.pinLockEnabled
            prefs[Keys.PINNED_HOME_APPS] = updated.pinnedHomeApps.map { it.name }.toSet()
            prefs[Keys.DISABLED_APPS] = updated.disabledApps.map { it.name }.toSet()
        }
    }

    suspend fun resetToUkDefaults() {
        context.settingsDataStore.edit { it.clear() }
    }
}
