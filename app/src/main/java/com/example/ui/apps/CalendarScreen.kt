package com.example.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.db.CalendarEventEntity
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.UkFormatters
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    settings: VirtualPhoneSettings,
    events: List<CalendarEventEntity>,
    onSaveEvent: (CalendarEventEntity) -> Unit,
    onDeleteEvent: (Int) -> Unit,
    onBack: () -> Unit
) {
    val ukToday = remember { LocalDate.now(ZoneId.of("Europe/London")) }
    var visibleMonth by remember { mutableStateOf(YearMonth.from(ukToday)) }
    var selectedDate by remember { mutableStateOf(ukToday) }
    var showAddDialog by remember { mutableStateOf(false) }

    val bankHolidays = remember(visibleMonth.year) {
        UkFormatters.ukBankHolidaysForYear(visibleMonth.year)
    }

    val eventsByDate = remember(events) {
        events.groupBy { it.dateIso }
    }

    VirtualAppScaffold(
        title = "UK Calendar",
        subtitle = "Monday-first UK Grid • Bank Holidays Included",
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Event")
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Month Navigation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { visibleMonth = visibleMonth.minusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                }
                Text(
                    text = visibleMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.UK)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { visibleMonth = visibleMonth.plusMonths(1) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                }
            }

            // UK Monday-first Weekday Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            // Month Days Grid
            val firstOfMonth = visibleMonth.atDay(1)
            val leadingEmpty = (firstOfMonth.dayOfWeek.value - 1) // Monday = 1 -> 0 offset
            val daysInMonth = visibleMonth.lengthOfMonth()
            val totalCells = leadingEmpty + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (r in 0 until rows) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (c in 0..6) {
                            val index = r * 7 + c
                            val dayNum = index - leadingEmpty + 1
                            if (dayNum in 1..daysInMonth) {
                                val cellDate = visibleMonth.atDay(dayNum)
                                val isSelected = cellDate == selectedDate
                                val isToday = cellDate == ukToday
                                val isHoliday = bankHolidays.containsKey(cellDate)
                                val hasEvents = eventsByDate.containsKey(cellDate.toString())

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            when {
                                                isSelected -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                                isHoliday -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .clickable { selectedDate = cellDate },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontFamily = JetBrainsMonoFamily,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                        if (hasEvents || isHoliday) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else if (isHoliday) MaterialTheme.colorScheme.tertiary
                                                        else MaterialTheme.colorScheme.secondary
                                                    )
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selected Date Agenda
            val selectedIso = selectedDate.toString()
            val dayHoliday = bankHolidays[selectedDate]
            val dayEvents = eventsByDate[selectedIso].orEmpty()

            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern(settings.dateFormatStyle.pattern, Locale.UK)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 84.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (dayHoliday != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = dayHoliday,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }

                if (dayEvents.isEmpty() && dayHoliday == null) {
                    item {
                        Text(
                            text = "No events scheduled for this date. Tap + to add one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    items(dayEvents, key = { it.id }) { ev ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${ev.timeLabel} • ${ev.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = ev.locationLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (ev.description.isNotBlank()) {
                                        Text(
                                            text = ev.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = { onDeleteEvent(ev.id) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete event")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var time by remember { mutableStateOf("10:00") }
        var location by remember { mutableStateOf("${settings.selectedCity.displayName}, UK") }
        var desc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Event (${selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Event Title") })
                    OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (24h, e.g. 14:30)") })
                    OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Notes") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSaveEvent(
                                CalendarEventEntity(
                                    title = title.trim(),
                                    dateIso = selectedDate.toString(),
                                    timeLabel = time.trim().ifBlank { "09:00" },
                                    locationLabel = location.trim(),
                                    description = desc.trim()
                                )
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
