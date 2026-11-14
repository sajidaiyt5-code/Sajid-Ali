package com.example.ui.apps

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.CallLogEntity
import com.example.data.db.ContactEntity
import com.example.data.system.DeviceTelemetryManager
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.UkFormatters
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PhoneDialerScreen(
    settings: VirtualPhoneSettings,
    contacts: List<ContactEntity>,
    callLogs: List<CallLogEntity>,
    pendingNumber: String?,
    telemetryManager: DeviceTelemetryManager,
    onConsumePendingNumber: () -> Unit,
    onLogCall: (String, String, String) -> Unit,
    onClearCallLogs: () -> Unit,
    onOpenSms: (String, String) -> Unit,
    onPostToast: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var dialInput by remember { mutableStateOf("+44 ") }

    LaunchedEffect(pendingNumber) {
        if (!pendingNumber.isNullOrBlank()) {
            dialInput = pendingNumber
            selectedTab = 0
            onConsumePendingNumber()
        }
    }

    val matchedContact = remember(dialInput, contacts) {
        val cleanInput = dialInput.filter { it.isDigit() }
        if (cleanInput.length < 3) null
        else contacts.firstOrNull { c ->
            c.phoneNumber.filter { it.isDigit() }.endsWith(cleanInput.takeLast(6)) ||
                c.phoneNumber.filter { it.isDigit() } == cleanInput
        }
    }

    VirtualAppScaffold(
        title = "UK Phone",
        subtitle = "Default Prefix ${settings.dialPrefix} • ${settings.selectedCity.displayName} (${settings.selectedCity.areaCode})",
        onBack = onBack,
        actions = {
            if (selectedTab == 1 && callLogs.isNotEmpty()) {
                IconButton(onClick = onClearCallLogs) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear call history")
                }
            }
        }
    ) {
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Keypad") },
                icon = { Icon(Icons.Rounded.Dialpad, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Recents (${callLogs.size})") },
                icon = { Icon(Icons.Rounded.History, contentDescription = null) }
            )
        }

        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DisclosureNoticeCard(
                    text = "Calls use your device's real carrier dialer (ACTION_DIAL) or log a local sandbox entry. No fake SIM or virtual carrier line is simulated."
                )

                // Number Display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dialInput.ifBlank { "+44" },
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.testTag("dialer_number_display")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = matchedContact?.name ?: if (UkFormatters.isValidUkNumber(dialInput)) {
                            "Formatted UK: ${UkFormatters.formatUkPhoneNumber(dialInput)}"
                        } else {
                            "Enter UK number (e.g. +44 20 7946 0000)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Dial Pad
                val padRows = listOf(
                    listOf("1" to "", "2" to "ABC", "3" to "DEF"),
                    listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
                    listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
                    listOf("*" to "", "0" to "+", "#" to "")
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    padRows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            row.forEach { (digit, sub) ->
                                Surface(
                                    onClick = {
                                        if (settings.soundOnTouch) telemetryManager.playDtmfTone(digit[0])
                                        dialInput += digit
                                    },
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .testTag("dial_key_$digit")
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = digit,
                                            fontFamily = JetBrainsMonoFamily,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (sub.isNotEmpty()) {
                                            Text(
                                                text = sub,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = {
                            val formatted = UkFormatters.formatUkPhoneNumber(dialInput)
                            if (formatted.isNotBlank()) {
                                onLogCall(matchedContact?.name ?: "UK Number", formatted, "LOCAL_TEST")
                                onPostToast("Logged local sandbox call to $formatted")
                            }
                        }
                    ) {
                        Text("Log Test")
                    }

                    Button(
                        onClick = {
                            val formatted = UkFormatters.formatUkPhoneNumber(dialInput)
                            if (formatted.isBlank()) {
                                onPostToast("Enter a UK phone number first")
                            } else {
                                onLogCall(matchedContact?.name ?: "UK Dial", formatted, "OUTGOING_CARRIER")
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$formatted"))
                                runCatching { context.startActivity(dialIntent) }
                                    .onFailure { onPostToast("No carrier dialer app found on this device") }
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                        modifier = Modifier.testTag("dial_call_button")
                    ) {
                        Icon(Icons.Rounded.Call, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Carrier Dial", color = Color.White)
                    }

                    IconButton(
                        onClick = {
                            if (dialInput.isNotEmpty()) dialInput = dialInput.dropLast(1)
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Delete digit")
                    }
                }
            }
        } else {
            if (callLogs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No call history yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(callLogs, key = { it.id }) { log ->
                        val timeStr = Instant.ofEpochMilli(log.timestamp)
                            .atZone(ZoneId.of(settings.timeZoneId))
                            .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        dialInput = log.phoneNumber
                                        selectedTab = 0
                                    }
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.CallMade,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = log.contactName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = log.phoneNumber,
                                        fontFamily = JetBrainsMonoFamily,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${log.callType} • $timeStr",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onOpenSms(log.phoneNumber, log.contactName) }) {
                                    Icon(Icons.AutoMirrored.Rounded.Message, contentDescription = "Message")
                                }
                                IconButton(onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${log.phoneNumber}"))
                                        )
                                    }
                                }) {
                                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Open carrier dialer")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
