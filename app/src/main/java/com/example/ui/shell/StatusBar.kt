package com.example.ui.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryFull
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.system.RealBatteryInfo
import com.example.data.system.RealNetworkInfo
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import java.time.ZonedDateTime

@Composable
fun VirtualStatusBar(
    settings: VirtualPhoneSettings,
    nowLondon: ZonedDateTime,
    batteryInfo: RealBatteryInfo,
    networkInfo: RealNetworkInfo,
    isOnHomeScreen: Boolean,
    onOpenShade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = if (isOnHomeScreen) Color.White else MaterialTheme.colorScheme.onSurface
    val barBg = if (isOnHomeScreen) {
        Color.Black.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(barBg)
            .statusBarsPadding()
            .height(36.dp)
            .clickable(onClick = onOpenShade)
            .padding(horizontal = 16.dp)
            .testTag("virtual_status_bar"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = settings.formatCurrentTime(nowLondon),
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(contentColor.copy(alpha = 0.14f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "UK • ${settings.selectedCity.shortCode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    fontSize = 10.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Open notification shade",
                tint = contentColor.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = when {
                    !networkInfo.isConnected -> Icons.Rounded.WifiOff
                    networkInfo.transportType == "Cellular" -> Icons.Rounded.SignalCellularAlt
                    else -> Icons.Rounded.Wifi
                },
                contentDescription = "Network: ${networkInfo.transportType}",
                tint = contentColor,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (batteryInfo.isCharging) {
                    Icons.Rounded.BatteryChargingFull
                } else {
                    Icons.Rounded.BatteryFull
                },
                contentDescription = "Battery ${batteryInfo.levelPercent}%",
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "${batteryInfo.levelPercent}%",
                fontFamily = JetBrainsMonoFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
