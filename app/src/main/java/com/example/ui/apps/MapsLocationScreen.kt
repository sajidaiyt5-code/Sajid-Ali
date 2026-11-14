package com.example.ui.apps

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.system.RealDeviceLocationInfo
import com.example.model.UkCity
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.UkFormatters
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapsLocationScreen(
    settings: VirtualPhoneSettings,
    deviceLocation: RealDeviceLocationInfo,
    onSelectVirtualCity: (UkCity) -> Unit,
    onRefreshDeviceLocation: () -> Unit,
    onPostToast: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var inspectedCity by remember(settings.selectedCity) { mutableStateOf(settings.selectedCity) }
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onRefreshDeviceLocation()
        if (!granted) {
            onPostToast("Coarse location permission denied. Virtual UK location remains active.")
        }
    }

    VirtualAppScaffold(
        title = "UK Maps & Location",
        subtitle = "Active Region: ${settings.selectedCity.displayName} (${settings.selectedCity.postcodePrefix})",
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                DisclosureNoticeCard(
                    text = "Virtual UK Location selects your regional city profile (London, Manchester, etc.). Actual Device Location is read separately from Android LocationManager and is never falsified as physical GPS."
                )
            }

            // Interactive UK Geographic Canvas Map
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Interactive United Kingdom Map",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Pinch/drag to zoom",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        UkInteractiveMapCanvas(
                            selectedVirtualCity = settings.selectedCity,
                            inspectedMapCity = inspectedCity,
                            zoomScale = zoomScale,
                            panOffset = panOffset,
                            onTransform = { pan, zoom ->
                                zoomScale = (zoomScale * zoom).coerceIn(1f, 3.5f)
                                panOffset = Offset(
                                    (panOffset.x + pan.x).coerceIn(-250f, 250f),
                                    (panOffset.y + pan.y).coerceIn(-300f, 300f)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    zoomScale = 1f
                                    panOffset = Offset.Zero
                                }
                            ) {
                                Text("Reset View")
                            }
                            Button(
                                onClick = {
                                    val geoUri = Uri.parse(
                                        "geo:${inspectedCity.latitude},${inspectedCity.longitude}?q=${inspectedCity.latitude},${inspectedCity.longitude}(${inspectedCity.displayName}, UK)"
                                    )
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                    runCatching { context.startActivity(mapIntent) }
                                        .onFailure { onPostToast("No external map viewer installed") }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("External Map")
                            }
                        }
                    }
                }
            }

            // Virtual UK City Selector (All 9 Required Cities)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Virtual UK City",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap a UK city to set it as your virtual regional location and inspect it on the map.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            UkCity.entries.forEach { city ->
                                val isActive = city == settings.selectedCity
                                FilterChip(
                                    selected = isActive,
                                    onClick = {
                                        onSelectVirtualCity(city)
                                        inspectedCity = city
                                    },
                                    leadingIcon = if (isActive) {
                                        { Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    label = { Text(city.displayName) },
                                    modifier = Modifier.testTag("city_chip_${city.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // 1. Selected Regional Location Card
            item {
                val active = settings.selectedCity
                LocationComparisonCard(
                    title = "1. Selected Virtual UK Location",
                    badge = "Regional Profile",
                    icon = Icons.Rounded.LocationOn,
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                    lines = listOf(
                        "City & Region" to "${active.displayName}, ${active.regionName} (${active.constituentCountry})",
                        "Coordinates" to String.format(Locale.UK, "%.4f° N, %.4f° W", active.latitude, kotlin.math.abs(active.longitude)),
                        "Postcode & Area Code" to "${active.postcodePrefix} • UK ${active.areaCode}",
                        "Regional Climate Est." to settings.formatTemperature(active.typicalTempCelsius)
                    )
                )
            }

            // 2. Map Pin Location Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Map, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "2. Map Inspection Pin",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            UkCity.entries.forEach { city ->
                                FilterChip(
                                    selected = city == inspectedCity,
                                    onClick = { inspectedCity = city },
                                    label = { Text(city.shortCode, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        val distKm = UkFormatters.haversineKm(
                            settings.selectedCity.latitude,
                            settings.selectedCity.longitude,
                            inspectedCity.latitude,
                            inspectedCity.longitude
                        )
                        val distMiles = distKm * 0.621371
                        Text(
                            text = "${inspectedCity.displayName} (${inspectedCity.constituentCountry}) — ${inspectedCity.description}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = String.format(
                                Locale.UK,
                                "Distance from %s: %.1f miles (%.1f km)",
                                settings.selectedCity.displayName,
                                distMiles,
                                distKm
                            ),
                            fontFamily = JetBrainsMonoFamily,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 3. Actual Device GPS / LocationManager Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "3. Actual Device Location (Hardware)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = deviceLocation.statusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (deviceLocation.latitude != null && deviceLocation.longitude != null) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = String.format(
                                    Locale.UK,
                                    "Fix: %.5f, %.5f (±%.0fm)",
                                    deviceLocation.latitude,
                                    deviceLocation.longitude,
                                    deviceLocation.accuracyMeters ?: 0f
                                ),
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (!deviceLocation.hasPermission) {
                                Button(
                                    onClick = {
                                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                                    }
                                ) {
                                    Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Grant Coarse Location")
                                }
                            } else {
                                OutlinedButton(onClick = onRefreshDeviceLocation) {
                                    Text("Refresh Hardware Fix")
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
private fun LocationComparisonCard(
    title: String,
    badge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    lines: List<Pair<String, String>>
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Text(
                        badge,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            lines.forEach { (k, v) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(k, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(v, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun UkInteractiveMapCanvas(
    selectedVirtualCity: UkCity,
    inspectedMapCity: UkCity,
    zoomScale: Float,
    panOffset: Offset,
    onTransform: (Offset, Float) -> Unit
) {
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val seaColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    val landColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
    val landBorder = MaterialTheme.colorScheme.secondary
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    val activeColor = MaterialTheme.colorScheme.tertiary
    val inspectColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface

    // Approximate Lat/Lon bounding box for UK: Lon [-6.5, 2.0], Lat [49.8, 59.0]
    fun project(lat: Double, lon: Double, w: Float, h: Float): Offset {
        val xNorm = ((lon - (-6.5)) / (2.0 - (-6.5))).toFloat()
        val yNorm = (1.0 - (lat - 49.8) / (59.0 - 49.8)).toFloat()
        return Offset(xNorm * w, yNorm * h)
    }

    // Simplified Great Britain coastline coordinates
    val gbOutline = remember {
        listOf(
            50.07 to -5.71, // Land's End
            51.20 to -3.50, // Bristol Channel
            51.45 to -5.20, // Pembrokeshire
            53.25 to -4.60, // Anglesey
            53.40 to -3.05, // Liverpool Bay
            54.85 to -3.60, // Solway Firth
            55.50 to -5.70, // Kintyre
            57.80 to -5.70, // NW Highlands
            58.64 to -3.07, // John o' Groats
            57.65 to -1.80, // Aberdeenshire
            56.05 to -2.70, // Firth of Forth
            55.00 to -1.42, // Tynemouth
            53.55 to 0.10,  // Humber
            52.75 to 1.75,  // Norfolk
            51.45 to 1.40,  // Kent
            50.75 to 0.30,  // Sussex
            50.60 to -2.45, // Dorset
            50.07 to -5.71
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp)
            .clip(RoundedCornerShape(16.dp))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    onTransform(pan, zoom)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = seaColor)
            withTransform({
                translate(left = panOffset.x, top = panOffset.y)
                scale(scaleX = zoomScale, scaleY = zoomScale, pivot = Offset(size.width / 2f, size.height / 2f))
            }) {
                // Lat/Lon grid lines
                for (lon in -6..2 step 2) {
                    val topPt = project(59.0, lon.toDouble(), size.width, size.height)
                    val botPt = project(49.8, lon.toDouble(), size.width, size.height)
                    drawLine(gridColor, topPt, botPt, strokeWidth = 1.2f)
                }
                for (lat in 50..58 step 2) {
                    val leftPt = project(lat.toDouble(), -6.5, size.width, size.height)
                    val rightPt = project(lat.toDouble(), 2.0, size.width, size.height)
                    drawLine(gridColor, leftPt, rightPt, strokeWidth = 1.2f)
                }

                // Great Britain polygon
                val path = Path()
                gbOutline.forEachIndexed { idx, (lat, lon) ->
                    val pt = project(lat, lon, size.width, size.height)
                    if (idx == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }
                path.close()
                drawPath(path, color = landColor)
                drawPath(path, color = landBorder, style = Stroke(width = 2.5f / zoomScale))

                // Connection line between active virtual city and inspected city
                if (selectedVirtualCity != inspectedMapCity) {
                    val p1 = project(selectedVirtualCity.latitude, selectedVirtualCity.longitude, size.width, size.height)
                    val p2 = project(inspectedMapCity.latitude, inspectedMapCity.longitude, size.width, size.height)
                    drawLine(
                        color = activeColor.copy(alpha = 0.7f),
                        start = p1,
                        end = p2,
                        strokeWidth = 3f / zoomScale
                    )
                }

                // Plot all 9 UK cities
                UkCity.entries.forEach { city ->
                    val pt = project(city.latitude, city.longitude, size.width, size.height)
                    val isVirtual = city == selectedVirtualCity
                    val isInspected = city == inspectedMapCity
                    val dotRadius = when {
                        isVirtual -> 9f / zoomScale
                        isInspected -> 8f / zoomScale
                        else -> 5f / zoomScale
                    }
                    val dotColor = when {
                        isVirtual -> activeColor
                        isInspected -> inspectColor
                        else -> landBorder
                    }
                    drawCircle(color = dotColor, radius = dotRadius, center = pt)
                    if (isVirtual || isInspected) {
                        drawCircle(
                            color = Color.White,
                            radius = dotRadius * 0.45f,
                            center = pt
                        )
                    }
                    val labelLayout = textMeasurer.measure(
                        text = city.shortCode,
                        style = TextStyle(
                            color = labelColor,
                            fontSize = 10.sp,
                            fontWeight = if (isVirtual) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                    drawText(
                        textLayoutResult = labelLayout,
                        topLeft = Offset(pt.x + 8f, pt.y - 8f)
                    )
                }
            }
        }
    }
}
