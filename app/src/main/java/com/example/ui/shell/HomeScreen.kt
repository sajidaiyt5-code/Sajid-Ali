package com.example.ui.shell

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.system.RealBatteryInfo
import com.example.data.system.RealNetworkInfo
import com.example.data.system.RealStorageInfo
import com.example.model.VirtualAppId
import com.example.model.VirtualPhoneSettings
import com.example.model.WallpaperStyle
import com.example.ui.theme.JetBrainsMonoFamily
import java.time.ZonedDateTime
import java.util.Locale

@Composable
fun WallpaperBackdrop(
    wallpaperStyle: WallpaperStyle,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (wallpaperStyle) {
            WallpaperStyle.LONDON_TWILIGHT -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_london_1794611294601),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            WallpaperStyle.HIGHLANDS_MIST -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_highlands_1794611296037),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            WallpaperStyle.ROYAL_GEOMETRIC -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0A1633),
                                Color(0xFF143673),
                                Color(0xFF0E4B5A)
                            )
                        )
                    )
                    drawCircle(
                        color = Color(0xFF5AD7C0).copy(alpha = 0.22f),
                        radius = size.width * 0.6f,
                        center = Offset(size.width * 0.85f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = Color(0xFFC81E3A).copy(alpha = 0.16f),
                        radius = size.width * 0.5f,
                        center = Offset(size.width * 0.12f, size.height * 0.72f)
                    )
                }
            }
        }
        // Subtle scrim for crisp icon label contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.12f),
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmartphoneHomeScreen(
    settings: VirtualPhoneSettings,
    nowLondon: ZonedDateTime,
    batteryInfo: RealBatteryInfo,
    storageInfo: RealStorageInfo,
    networkInfo: RealNetworkInfo,
    isCustomizeMode: Boolean,
    onToggleCustomizeMode: (Boolean) -> Unit,
    onOpenApp: (VirtualAppId) -> Unit,
    onOpenAppDrawer: () -> Unit,
    onTogglePinApp: (VirtualAppId) -> Unit,
    onCycleWallpaper: () -> Unit
) {
    val homeApps = remember(settings.pinnedHomeApps, settings.disabledApps) {
        settings.pinnedHomeApps
            .filter { it !in settings.disabledApps && !it.isCoreDockDefault }
            .sortedBy { it.ordinal }
    }
    val dockApps = remember(settings.disabledApps) {
        listOf(
            VirtualAppId.PHONE,
            VirtualAppId.MESSAGES,
            VirtualAppId.BROWSER,
            VirtualAppId.CAMERA,
            VirtualAppId.SETTINGS
        ).filter { it !in settings.disabledApps }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Scrollable Home Body (At a Glance + Optional Dashboard Widget + Customizable Grid)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("home_scroll_list"),
            contentPadding = PaddingValues(top = 8.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // At A Glance UK Widget
            item(key = "widget_at_a_glance") {
                Card(
                    onClick = { onOpenApp(VirtualAppId.CLOCK) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F1B33).copy(alpha = 0.72f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = settings.formatCurrentTime(nowLondon),
                                fontFamily = JetBrainsMonoFamily,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${settings.formatCurrentDate(nowLondon)} • ${settings.timeZoneId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF5AD7C0),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${settings.selectedCity.displayName}, UK • ${settings.formatTemperature(settings.selectedCity.typicalTempCelsius)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF9BB8FF)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "GBP £  •  +44",
                                    fontFamily = JetBrainsMonoFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            FilledTonalButton(
                                onClick = { onToggleCustomizeMode(!isCustomizeMode) },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("customize_home_button")
                            ) {
                                Icon(
                                    imageVector = if (isCustomizeMode) Icons.Rounded.Close else Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isCustomizeMode) "Done" else "Customize",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }

            // Customization Helper Bar
            if (isCustomizeMode) {
                item(key = "home_customize_panel") {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Customize Home Screen",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Button(
                                    onClick = onCycleWallpaper,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Wallpaper: ${settings.wallpaperStyle.label}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap apps below to pin or unpin them from your Home Screen:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                VirtualAppId.entries
                                    .filter { !it.isCoreDockDefault && it !in settings.disabledApps }
                                    .forEach { app ->
                                        val pinned = app in settings.pinnedHomeApps
                                        FilterChip(
                                            selected = pinned,
                                            onClick = { onTogglePinApp(app) },
                                            label = { Text(app.title, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                            }
                        }
                    }
                }
            }

            // Optional Virtual Phone Telemetry Widget
            if (settings.showHomeDashboardWidget) {
                item(key = "widget_telemetry") {
                    Card(
                        onClick = { onOpenApp(VirtualAppId.DASHBOARD) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F1B33).copy(alpha = 0.65f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WidgetMetricItem(
                                icon = Icons.Rounded.BatteryChargingFull,
                                label = "Battery",
                                value = "${batteryInfo.levelPercent}%",
                                tint = Color(0xFF5AD7C0)
                            )
                            WidgetMetricItem(
                                icon = Icons.Rounded.Storage,
                                label = "Free Space",
                                value = String.format(Locale.UK, "%.1f GB", storageInfo.freeGb),
                                tint = Color(0xFF9BB8FF)
                            )
                            WidgetMetricItem(
                                icon = Icons.Rounded.Wifi,
                                label = "Network",
                                value = if (networkInfo.isConnected) networkInfo.transportType else "Offline",
                                tint = if (networkInfo.isConnected) Color(0xFF5AD7C0) else Color(0xFFFF8FA3)
                            )
                        }
                    }
                }
            }

            // Home App Icons Grid (4 columns, each row as a LazyColumn item)
            val rows = homeApps.chunked(4)
            items(rows.size, key = { idx -> "home_row_$idx" }) { rowIdx ->
                val rowApps = rows[rowIdx]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowApps.forEach { app ->
                        VirtualAppIconTile(
                            appId = app,
                            onClick = { onOpenApp(app) },
                            showRemoveBadge = isCustomizeMode,
                            onRemoveClick = { onTogglePinApp(app) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(4 - rowApps.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // App Drawer Handle + Persistent Dock
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                onClick = onOpenAppDrawer,
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.42f),
                modifier = Modifier.testTag("open_app_drawer_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Rounded.Apps,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "All Apps",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dock Container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color(0xFF0D172B).copy(alpha = 0.72f))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dockApps.forEach { app ->
                    VirtualAppIconTile(
                        appId = app,
                        onClick = { onOpenApp(app) },
                        iconSize = 50.dp,
                        showLabel = false,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AppDrawerOverlay(
    settings: VirtualPhoneSettings,
    onSelectApp: (VirtualAppId) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val enabledApps = remember(settings.disabledApps, searchQuery) {
        VirtualAppId.entries
            .filter { it !in settings.disabledApps }
            .filter {
                searchQuery.isBlank() ||
                    it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.title }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_drawer_overlay"),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.97f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "App Drawer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose, modifier = Modifier.testTag("close_drawer_button")) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close App Drawer")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps & UK tools...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_drawer_search")
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 82.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(enabledApps, key = { it.name }) { app ->
                    VirtualAppIconTile(
                        appId = app,
                        onClick = { onSelectApp(app) },
                        labelColor = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
