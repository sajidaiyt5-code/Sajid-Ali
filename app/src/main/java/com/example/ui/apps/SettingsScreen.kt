package com.example.ui.apps

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.security.BiometricCapability
import com.example.data.system.DeviceTelemetryManager
import com.example.model.DateFormatStyle
import com.example.model.TemperatureUnit
import com.example.model.ThemeMode
import com.example.model.UkCity
import com.example.model.VirtualAppId
import com.example.model.VirtualPhoneSettings
import com.example.model.WallpaperStyle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: VirtualPhoneSettings,
    telemetryManager: DeviceTelemetryManager,
    biometricCapability: BiometricCapability,
    isKeystoreBacked: Boolean,
    onUpdateDeviceName: (String) -> Unit,
    onSelectCity: (UkCity) -> Unit,
    onSetRegionProfile: (String, String) -> Unit,
    onSetUse24Hour: (Boolean) -> Unit,
    onSetDateFormat: (DateFormatStyle) -> Unit,
    onSetTemperatureUnit: (TemperatureUnit) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetWallpaperStyle: (WallpaperStyle) -> Unit,
    onUpdateSetting: ((VirtualPhoneSettings) -> VirtualPhoneSettings) -> Unit,
    onToggleAppEnabled: (VirtualAppId) -> Unit,
    onSetPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onLockNow: () -> Unit,
    onResetSettingsOnly: () -> Unit,
    onClearAllUserData: () -> Unit,
    onPostToast: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var deviceNameDraft by remember(settings.virtualDeviceName) { mutableStateOf(settings.virtualDeviceName) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var permissionRefreshTick by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissionRefreshTick++
    }

    val hasCameraPerm = remember(permissionRefreshTick) {
        telemetryManager.hasPermission(Manifest.permission.CAMERA)
    }
    val hasLocationPerm = remember(permissionRefreshTick) {
        telemetryManager.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    val hasNotifPerm = remember(permissionRefreshTick) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            telemetryManager.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else true
    }

    VirtualAppScaffold(
        title = "Settings & Privacy",
        subtitle = "${settings.virtualDeviceName} • United Kingdom",
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Virtual Device Identity & Admin
            item {
                SettingsSectionCard(title = "Virtual Device & Admin", icon = Icons.Rounded.PhoneAndroid) {
                    OutlinedTextField(
                        value = deviceNameDraft,
                        onValueChange = { deviceNameDraft = it },
                        label = { Text("Virtual Phone Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onUpdateDeviceName(deviceNameDraft)
                            onPostToast("Virtual device renamed")
                        }
                    ) {
                        Text("Save Device Name")
                    }
                }
            }

            // 2. United Kingdom Regional Settings
            item {
                SettingsSectionCard(title = "United Kingdom Regional Profile", icon = Icons.Rounded.Language) {
                    Text(
                        text = "Country: ${settings.countryName} • Time Zone: ${settings.timeZoneId} • Currency: ${settings.currencyLabel} • Dial Prefix: ${settings.dialPrefix} • Keyboard: ${settings.keyboardLayout}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("UK Constituent Region", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            "UK (Great Britain)" to "English (United Kingdom)",
                            "England" to "English (United Kingdom)",
                            "Scotland" to "English (UK - Scotland)",
                            "Wales" to "English / Cymraeg (UK)"
                        ).forEach { (reg, lang) ->
                            FilterChip(
                                selected = settings.regionLabel == reg,
                                onClick = { onSetRegionProfile(reg, lang) },
                                label = { Text(reg, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text("Selected UK City", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        UkCity.entries.forEach { city ->
                            FilterChip(
                                selected = settings.selectedCity == city,
                                onClick = { onSelectCity(city) },
                                label = { Text(city.displayName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    SettingsToggleRow(
                        title = "24-Hour UK Time Format",
                        subtitle = if (settings.use24HourClock) "24-hour (e.g. 16:30)" else "12-hour (e.g. 04:30 PM)",
                        checked = settings.use24HourClock,
                        onCheckedChange = onSetUse24Hour
                    )

                    Spacer(Modifier.height(6.dp))
                    Text("Date Format", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DateFormatStyle.entries.forEach { fmt ->
                            FilterChip(
                                selected = settings.dateFormatStyle == fmt,
                                onClick = { onSetDateFormat(fmt) },
                                label = { Text(fmt.label) }
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text("Temperature Unit", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TemperatureUnit.entries.forEach { unit ->
                            FilterChip(
                                selected = settings.temperatureUnit == unit,
                                onClick = { onSetTemperatureUnit(unit) },
                                label = { Text(unit.label) }
                            )
                        }
                    }
                }
            }

            // 3. Appearance & Home Customization
            item {
                SettingsSectionCard(title = "Appearance & Home Screen", icon = Icons.Rounded.Palette) {
                    Text("Theme Mode", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(mode.label) },
                                modifier = Modifier.testTag("theme_chip_${mode.name.lowercase()}")
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Wallpaper", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WallpaperStyle.entries.forEach { wp ->
                            FilterChip(
                                selected = settings.wallpaperStyle == wp,
                                onClick = { onSetWallpaperStyle(wp) },
                                label = { Text(wp.label) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingsToggleRow(
                        title = "Home Telemetry Widget",
                        subtitle = "Show battery, storage, and network summary on Home",
                        checked = settings.showHomeDashboardWidget,
                        onCheckedChange = { enabled ->
                            onUpdateSetting { it.copy(showHomeDashboardWidget = enabled) }
                        }
                    )
                    SettingsToggleRow(
                        title = "Virtual Notifications",
                        subtitle = "Receive local alerts in Notification Shade",
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { enabled ->
                            onUpdateSetting { it.copy(notificationsEnabled = enabled) }
                        }
                    )
                    SettingsToggleRow(
                        title = "Dialer DTMF Keypad Tones",
                        subtitle = "Play audible tones when pressing dialer keys",
                        checked = settings.soundOnTouch,
                        onCheckedChange = { enabled ->
                            onUpdateSetting { it.copy(soundOnTouch = enabled) }
                        }
                    )
                }
            }

            // 4. Security & Privacy Dashboard
            item {
                SettingsSectionCard(title = "Security & Privacy", icon = Icons.Rounded.Security) {
                    Text(
                        text = if (isKeystoreBacked) {
                            "Secure Storage: Android Keystore AES-256-GCM active"
                        } else {
                            "Secure Storage: Local software vault"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { showPinDialog = true },
                            modifier = Modifier.testTag("configure_pin_button")
                        ) {
                            Icon(Icons.Rounded.Lock, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (settings.pinLockEnabled) "Change PIN" else "Set 4-Digit PIN")
                        }
                        if (settings.pinLockEnabled) {
                            OutlinedButton(onClick = onLockNow) {
                                Text("Lock Now")
                            }
                            TextButton(onClick = onDisablePin) {
                                Text("Remove")
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingsToggleRow(
                        title = "Biometric Authentication",
                        subtitle = biometricCapability.label,
                        checked = settings.biometricEnabled,
                        enabled = settings.pinLockEnabled && biometricCapability == BiometricCapability.AVAILABLE,
                        onCheckedChange = { enabled ->
                            onUpdateSetting { it.copy(biometricEnabled = enabled) }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Android Permission Dashboard", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    PermissionAuditRow("Camera (CameraX)", hasCameraPerm)
                    PermissionAuditRow("Coarse Location (GPS Check)", hasLocationPerm)
                    PermissionAuditRow("Notifications (Android 13+)", hasNotifPerm)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val perms = buildList {
                                    add(Manifest.permission.CAMERA)
                                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        add(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }.toTypedArray()
                                permissionLauncher.launch(perms)
                            }
                        ) {
                            Text("Request Permissions")
                        }
                        TextButton(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                runCatching { context.startActivity(intent) }
                            }
                        ) {
                            Text("System App Info")
                        }
                    }
                }
            }

            // 5. Installed App Modules Manager
            item {
                SettingsSectionCard(title = "Manage App Modules", icon = Icons.Rounded.Apps) {
                    Text(
                        text = "Enable or hide built-in virtual phone applications (Settings stays active).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        VirtualAppId.entries.filter { it != VirtualAppId.SETTINGS }.forEach { app ->
                            val enabled = app !in settings.disabledApps
                            FilterChip(
                                selected = enabled,
                                onClick = { onToggleAppEnabled(app) },
                                label = { Text(app.title, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            // 6. Reset & Clear User Data
            item {
                SettingsSectionCard(title = "Reset & Storage Wipe", icon = Icons.Rounded.RestartAlt) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = onResetSettingsOnly,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset UK Defaults")
                        }
                        Button(
                            onClick = { showClearDataConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Clear User Data")
                        }
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configure 4-Digit Device PIN") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            error = null
                            if (it.length <= 4) newPin = it.filter(Char::isDigit)
                        },
                        label = { Text("Enter 4-digit PIN") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            error = null
                            if (it.length <= 4) confirmPin = it.filter(Char::isDigit)
                        },
                        label = { Text("Confirm 4-digit PIN") },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        when {
                            newPin.length != 4 -> error = "PIN must be exactly 4 digits"
                            newPin != confirmPin -> error = "PINs do not match"
                            else -> {
                                onSetPin(newPin)
                                showPinDialog = false
                            }
                        }
                    }
                ) {
                    Text("Save PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Clear All User Data?") },
            text = {
                Text("This erases local contacts, SMS threads, notes, calendar events, alarms, sandbox files, gallery photos, and resets UK settings.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllUserData()
                        showClearDataConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Erase All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Composable
private fun PermissionAuditRow(label: String, granted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(
            text = if (granted) "Granted" else "Not Granted",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (granted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
