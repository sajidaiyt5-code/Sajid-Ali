package com.example.ui.shell

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.system.RealBatteryInfo
import com.example.data.system.RealNetworkInfo
import com.example.model.ThemeMode
import com.example.model.UkCity
import com.example.model.VirtualAppId
import com.example.model.VirtualNotification
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun NotificationQuickSettingsShade(
    settings: VirtualPhoneSettings,
    nowLondon: ZonedDateTime,
    batteryInfo: RealBatteryInfo,
    networkInfo: RealNetworkInfo,
    notifications: List<VirtualNotification>,
    onClose: () -> Unit,
    onCycleTheme: () -> Unit,
    onSelectNextCity: () -> Unit,
    onToggle24Hour: () -> Unit,
    onLockNow: () -> Unit,
    onOpenApp: (VirtualAppId) -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearAllNotifications: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notification_shade"),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.97f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Shade Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${settings.formatCurrentTime(nowLondon)}  •  ${settings.formatCurrentDate(nowLondon)}",
                        fontFamily = JetBrainsMonoFamily,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${settings.virtualDeviceName} • ${batteryInfo.levelPercent}% (${batteryInfo.statusLabel})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row {
                    IconButton(onClick = { onOpenApp(VirtualAppId.SETTINGS) }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Open Settings")
                    }
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_shade_button")) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close Shade")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Settings Tiles (2 rows x 2 columns)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickSettingTile(
                        title = if (networkInfo.isConnected) networkInfo.transportType else "Offline",
                        subtitle = if (networkInfo.isValidated) "Internet Active" else "Tap to diagnose",
                        icon = if (networkInfo.isConnected) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                        isActive = networkInfo.isConnected,
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                runCatching {
                                    context.startActivity(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
                                }.onFailure { onOpenApp(VirtualAppId.NETWORK) }
                            } else {
                                onOpenApp(VirtualAppId.NETWORK)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickSettingTile(
                        title = "Theme: ${settings.themeMode.label}",
                        subtitle = "Tap to cycle mode",
                        icon = if (settings.themeMode == ThemeMode.DARK) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                        isActive = settings.themeMode != ThemeMode.SYSTEM,
                        onClick = onCycleTheme,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickSettingTile(
                        title = "UK: ${settings.selectedCity.displayName}",
                        subtitle = "${settings.selectedCity.postcodePrefix} • Cycle city",
                        icon = Icons.Rounded.LocationOn,
                        isActive = true,
                        onClick = onSelectNextCity,
                        modifier = Modifier.weight(1f)
                    )
                    QuickSettingTile(
                        title = if (settings.use24HourClock) "24-Hour Clock" else "12-Hour Clock",
                        subtitle = "Europe/London",
                        icon = Icons.Rounded.AccessTime,
                        isActive = settings.use24HourClock,
                        onClick = onToggle24Hour,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickSettingTile(
                        title = "Lock Phone",
                        subtitle = if (settings.pinLockEnabled) "PIN Active" else "Set PIN in Security",
                        icon = Icons.Rounded.Lock,
                        isActive = settings.pinLockEnabled,
                        onClick = onLockNow,
                        modifier = Modifier.weight(1f)
                    )
                    QuickSettingTile(
                        title = "Network Info",
                        subtitle = "Diagnostics & DNS",
                        icon = Icons.Rounded.Wifi,
                        isActive = false,
                        onClick = { onOpenApp(VirtualAppId.NETWORK) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Notifications Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications (${notifications.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = onClearAllNotifications) {
                        Text("Clear all")
                    }
                }
            }

            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No notifications",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notifications, key = { it.id }) { item ->
                        val timeText = Instant.ofEpochMilli(item.timestampMillis)
                            .atZone(ZoneId.of(settings.timeZoneId))
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                        Card(
                            onClick = { onOpenApp(item.sourceApp) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(item.sourceApp.accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.sourceApp.iconVector(),
                                        contentDescription = null,
                                        tint = androidx.compose.ui.graphics.Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.sourceApp.title} • $timeText",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = item.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { onDismissNotification(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Dismiss notification",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickSettingTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val fg = if (isActive) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = fg.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
