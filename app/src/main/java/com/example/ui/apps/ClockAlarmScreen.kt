package com.example.ui.apps

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AlarmEntity
import com.example.data.system.DeviceTelemetryManager
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ClockAlarmScreen(
    settings: VirtualPhoneSettings,
    nowLondon: ZonedDateTime,
    alarms: List<AlarmEntity>,
    telemetryManager: DeviceTelemetryManager,
    onSaveAlarm: (AlarmEntity) -> Unit,
    onDeleteAlarm: (Int) -> Unit,
    onPostToast: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddAlarm by remember { mutableStateOf(false) }

    // Stopwatch state
    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchMillis by remember { mutableLongStateOf(0L) }
    var laps by remember { mutableStateOf(listOf<Long>()) }

    LaunchedEffect(stopwatchRunning) {
        while (stopwatchRunning) {
            delay(50L)
            stopwatchMillis += 50L
        }
    }

    // Countdown timer state
    var timerRunning by remember { mutableStateOf(false) }
    var timerRemainingSec by remember { mutableIntStateOf(60) }

    LaunchedEffect(timerRunning) {
        while (timerRunning && timerRemainingSec > 0) {
            delay(1_000L)
            timerRemainingSec -= 1
            if (timerRemainingSec == 0) {
                timerRunning = false
                telemetryManager.triggerAlertChimeAndVibrate()
                onPostToast("UK Timer Finished!")
            }
        }
    }

    VirtualAppScaffold(
        title = "UK Clock & Alarm",
        subtitle = "${settings.timeZoneId} (${nowLondon.format(DateTimeFormatter.ofPattern("z"))})",
        onBack = onBack,
        floatingActionButton = if (selectedTab == 1) {
            {
                FloatingActionButton(onClick = { showAddAlarm = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Alarm")
                }
            }
        } else null
    ) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Clock") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Alarms") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Stopwatch") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Timer") })
        }

        when (selectedTab) {
            0 -> {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.tertiary
                val surfaceOutline = MaterialTheme.colorScheme.outlineVariant
                val deviceLocal = remember(nowLondon) { ZonedDateTime.now(ZoneId.systemDefault()) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Analog Canvas Clock for Europe/London
                    Canvas(modifier = Modifier.size(180.dp)) {
                        val radius = size.minDimension / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        drawCircle(
                            color = surfaceOutline,
                            radius = radius - 4f,
                            center = center,
                            style = Stroke(width = 4f)
                        )
                        for (i in 0 until 12) {
                            val angle = Math.toRadians((i * 30 - 90).toDouble())
                            val start = Offset(
                                center.x + ((radius - 16f) * cos(angle)).toFloat(),
                                center.y + ((radius - 16f) * sin(angle)).toFloat()
                            )
                            val end = Offset(
                                center.x + ((radius - 6f) * cos(angle)).toFloat(),
                                center.y + ((radius - 6f) * sin(angle)).toFloat()
                            )
                            drawLine(surfaceOutline, start, end, strokeWidth = 3f)
                        }

                        val secAngle = Math.toRadians((nowLondon.second * 6 - 90).toDouble())
                        val minAngle = Math.toRadians((nowLondon.minute * 6 + nowLondon.second * 0.1 - 90))
                        val hrAngle = Math.toRadians((((nowLondon.hour % 12) * 30) + nowLondon.minute * 0.5 - 90))

                        drawLine(
                            color = primaryColor,
                            start = center,
                            end = Offset(
                                center.x + (radius * 0.5f * cos(hrAngle)).toFloat(),
                                center.y + (radius * 0.5f * sin(hrAngle)).toFloat()
                            ),
                            strokeWidth = 9f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = primaryColor,
                            start = center,
                            end = Offset(
                                center.x + (radius * 0.72f * cos(minAngle)).toFloat(),
                                center.y + (radius * 0.72f * sin(minAngle)).toFloat()
                            ),
                            strokeWidth = 5f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = secondaryColor,
                            start = center,
                            end = Offset(
                                center.x + (radius * 0.8f * cos(secAngle)).toFloat(),
                                center.y + (radius * 0.8f * sin(secAngle)).toFloat()
                            ),
                            strokeWidth = 2.5f,
                            cap = StrokeCap.Round
                        )
                    }

                    Text(
                        text = nowLondon.format(
                            DateTimeFormatter.ofPattern(
                                if (settings.use24HourClock) "HH:mm:ss" else "hh:mm:ss a",
                                Locale.UK
                            )
                        ),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${settings.selectedCity.displayName}, UK • ${settings.formatCurrentDate(nowLondon)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Public, contentDescription = null)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Physical Host Device Time", style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        ZoneId.systemDefault().id,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = deviceLocal.format(DateTimeFormatter.ofPattern("HH:mm")),
                                fontFamily = JetBrainsMonoFamily,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = String.format(Locale.UK, "%02d:%02d", alarm.hour, alarm.minute),
                                        fontFamily = JetBrainsMonoFamily,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${alarm.label} • ${alarm.daysSummary}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        telemetryManager.triggerAlertChimeAndVibrate()
                                        onPostToast("Testing alarm chime: ${alarm.label}")
                                    }
                                ) {
                                    Icon(Icons.Rounded.NotificationsActive, contentDescription = "Test chime")
                                }
                                Switch(
                                    checked = alarm.isEnabled,
                                    onCheckedChange = { enabled ->
                                        onSaveAlarm(alarm.copy(isEnabled = enabled))
                                    }
                                )
                                IconButton(onClick = { onDeleteAlarm(alarm.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete alarm")
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                val mins = (stopwatchMillis / 60000)
                val secs = (stopwatchMillis / 1000) % 60
                val centis = (stopwatchMillis % 1000) / 10
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = String.format(Locale.UK, "%02d:%02d.%02d", mins, secs, centis),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { stopwatchRunning = !stopwatchRunning }) {
                            Text(if (stopwatchRunning) "Pause" else "Start")
                        }
                        FilledTonalButton(
                            onClick = {
                                if (stopwatchRunning) {
                                    laps = listOf(stopwatchMillis) + laps
                                } else {
                                    stopwatchMillis = 0L
                                    laps = emptyList()
                                }
                            }
                        ) {
                            Text(if (stopwatchRunning) "Lap" else "Reset")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(laps.size) { index ->
                            val lapVal = laps[index]
                            val m = lapVal / 60000
                            val s = (lapVal / 1000) % 60
                            val c = (lapVal % 1000) / 10
                            Text(
                                text = "Lap ${laps.size - index}   •   " + String.format(Locale.UK, "%02d:%02d.%02d", m, s, c),
                                fontFamily = JetBrainsMonoFamily,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            3 -> {
                val m = timerRemainingSec / 60
                val s = timerRemainingSec % 60
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = String.format(Locale.UK, "%02d:%02d", m, s),
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30 to "30s", 60 to "1m", 300 to "5m", 600 to "10m").forEach { (sec, label) ->
                            OutlinedButton(
                                onClick = {
                                    timerRunning = false
                                    timerRemainingSec = sec
                                }
                            ) {
                                Text(label)
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                if (timerRemainingSec > 0) timerRunning = !timerRunning
                            }
                        ) {
                            Text(if (timerRunning) "Pause Timer" else "Start Timer")
                        }
                        FilledTonalButton(
                            onClick = {
                                timerRunning = false
                                timerRemainingSec = 60
                            }
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }
        }
    }

    if (showAddAlarm) {
        var hourStr by remember { mutableStateOf("08") }
        var minStr by remember { mutableStateOf("00") }
        var label by remember { mutableStateOf("UK Alarm") }
        AlertDialog(
            onDismissRequest = { showAddAlarm = false },
            title = { Text("New Alarm (24h)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hourStr,
                            onValueChange = { if (it.length <= 2) hourStr = it.filter(Char::isDigit) },
                            label = { Text("Hour (00-23)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minStr,
                            onValueChange = { if (it.length <= 2) minStr = it.filter(Char::isDigit) },
                            label = { Text("Min (00-59)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val h = (hourStr.toIntOrNull() ?: 8).coerceIn(0, 23)
                        val m = (minStr.toIntOrNull() ?: 0).coerceIn(0, 59)
                        onSaveAlarm(
                            AlarmEntity(
                                hour = h,
                                minute = m,
                                label = label.trim().ifBlank { "UK Alarm" },
                                isEnabled = true
                            )
                        )
                        showAddAlarm = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAlarm = false }) { Text("Cancel") }
            }
        )
    }
}
