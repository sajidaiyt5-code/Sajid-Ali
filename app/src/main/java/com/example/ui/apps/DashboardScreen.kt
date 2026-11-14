package com.example.ui.apps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.system.DeviceTelemetryManager
import com.example.data.system.RealBatteryInfo
import com.example.data.system.RealNetworkInfo
import com.example.data.system.RealStorageInfo
import com.example.model.VirtualAppId
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import java.time.ZonedDateTime
import java.util.Locale

@Composable
fun DashboardScreen(
    settings: VirtualPhoneSettings,
    nowLondon: ZonedDateTime,
    batteryInfo: RealBatteryInfo,
    storageInfo: RealStorageInfo,
    networkInfo: RealNetworkInfo,
    telemetryManager: DeviceTelemetryManager,
    onRefresh: () -> Unit,
    onOpenApp: (VirtualAppId) -> Unit,
    onBack: () -> Unit
) {
    val deviceSummary = telemetryManager.getAndroidDeviceSummary()

    VirtualAppScaffold(
        title = "Virtual Phone Dashboard",
        subtitle = settings.virtualDeviceName,
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = settings.virtualDeviceName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = settings.deviceModelLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            FilledTonalButton(onClick = onRefresh) {
                                Text("Refresh")
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "${settings.formatCurrentTime(nowLondon)}  •  ${settings.formatCurrentDate(nowLondon)} (${settings.timeZoneId})",
                            fontFamily = JetBrainsMonoFamily,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                DashboardSectionCard(
                    title = "United Kingdom Environment",
                    icon = Icons.Rounded.Language,
                    rows = listOf(
                        "Country" to "${settings.countryName} (${settings.countryCode})",
                        "Region" to settings.regionLabel,
                        "Selected City" to "${settings.selectedCity.displayName} (${settings.selectedCity.postcodePrefix})",
                        "Language" to settings.languageLabel,
                        "Keyboard" to settings.keyboardLayout,
                        "Time Zone" to settings.timeZoneId,
                        "Currency & Dial Code" to "${settings.currencyLabel} • ${settings.dialPrefix}",
                        "Temperature Scale" to settings.temperatureUnit.label
                    )
                )
            }

            item {
                DashboardSectionCard(
                    title = "Host Android Hardware & OS",
                    icon = Icons.Rounded.PhoneAndroid,
                    rows = deviceSummary.entries.map { it.key to it.value }
                )
            }

            item {
                val batteryTemp = batteryInfo.temperatureCelsius?.let {
                    settings.formatTemperature(it.toInt())
                } ?: "N/A"
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Battery & Power (Live)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { batteryInfo.levelPercent / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        DashboardRow("Charge Level", "${batteryInfo.levelPercent}% (${batteryInfo.statusLabel})")
                        DashboardRow("Battery Temperature", batteryTemp)
                    }
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Internal Storage (StatFs)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { storageInfo.usedFraction },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        DashboardRow(
                            "Used / Total",
                            String.format(Locale.UK, "%.1f GB / %.1f GB", storageInfo.usedGb, storageInfo.totalGb)
                        )
                        DashboardRow(
                            "Available Free",
                            String.format(Locale.UK, "%.1f GB", storageInfo.freeGb)
                        )
                    }
                }
            }

            item {
                DashboardSectionCard(
                    title = "Connectivity Summary",
                    icon = Icons.Rounded.Wifi,
                    rows = listOf(
                        "Status" to if (networkInfo.isConnected) "Connected (${networkInfo.transportType})" else "Offline",
                        "Validated Internet" to if (networkInfo.isValidated) "Yes" else "No",
                        "Interface" to networkInfo.interfaceName
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onOpenApp(VirtualAppId.MAPS) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.LocationOn, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("UK Location")
                    }
                    FilledTonalButton(
                        onClick = { onOpenApp(VirtualAppId.SETTINGS) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.Settings, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Settings")
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardSectionCard(
    title: String,
    icon: ImageVector,
    rows: List<Pair<String, String>>
) {
    Card(
        shape = RoundedCornerShape(18.dp),
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
            rows.forEach { (k, v) -> DashboardRow(k, v) }
        }
    }
}

@Composable
private fun DashboardRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
