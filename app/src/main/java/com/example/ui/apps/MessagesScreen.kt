package com.example.ui.apps

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.SmsMessageEntity
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.UkFormatters
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MessagesScreen(
    settings: VirtualPhoneSettings,
    messages: List<SmsMessageEntity>,
    pendingPeer: Pair<String, String>?,
    onConsumePendingPeer: () -> Unit,
    onSendLocalSms: (String, String, String, String) -> Unit,
    onDeleteThread: (String) -> Unit,
    onPostToast: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var activePeer by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showComposeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pendingPeer) {
        if (pendingPeer != null) {
            activePeer = pendingPeer
            onConsumePendingPeer()
        }
    }

    val threads = remember(messages) {
        messages.groupBy { it.peerAddress }
            .map { (addr, list) -> addr to list.sortedBy { it.timestamp } }
            .sortedByDescending { it.second.lastOrNull()?.timestamp ?: 0L }
    }

    if (activePeer == null) {
        VirtualAppScaffold(
            title = "UK Messages",
            subtitle = "Local SMS Sandbox & Carrier Handoff",
            onBack = onBack,
            floatingActionButton = {
                FloatingActionButton(onClick = { showComposeDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = "New SMS")
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DisclosureNoticeCard(
                    text = "Receiving live inbound SMS requires a physical SIM or VoIP service. Conversations here are saved locally, or can be sent via your device's Carrier SMS app.",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 84.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(threads, key = { it.first }) { (address, threadMsgs) ->
                        val last = threadMsgs.last()
                        val timeText = Instant.ofEpochMilli(last.timestamp)
                            .atZone(ZoneId.of(settings.timeZoneId))
                            .format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))
                        Card(
                            onClick = { activePeer = address to last.peerName },
                            shape = RoundedCornerShape(18.dp),
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
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = last.peerName.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = last.peerName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = timeText,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = address,
                                        fontFamily = JetBrainsMonoFamily,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = last.body,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { onDeleteThread(address) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Delete conversation")
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        val (peerAddress, peerName) = activePeer!!
        val threadMsgs = remember(messages, peerAddress) {
            messages.filter { it.peerAddress == peerAddress }.sortedBy { it.timestamp }
        }
        var draftText by remember { mutableStateOf("") }

        VirtualAppScaffold(
            title = peerName,
            subtitle = peerAddress,
            onBack = { activePeer = null },
            actions = {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$peerAddress")).apply {
                            putExtra("sms_body", draftText)
                        }
                        runCatching { context.startActivity(intent) }
                            .onFailure { onPostToast("No carrier SMS app available") }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Send via Carrier SMS App")
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(threadMsgs, key = { it.id }) { msg ->
                        val align = if (msg.isOutgoing) Alignment.End else Alignment.Start
                        val bubbleColor = if (msg.isOutgoing) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = align
                        ) {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = bubbleColor,
                                modifier = Modifier.widthIn(max = 300.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.body,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.deliveryNote,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = draftText,
                        onValueChange = { draftText = it },
                        placeholder = { Text("Write UK SMS message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = draftText.trim()
                            if (trimmed.isNotEmpty()) {
                                onSendLocalSms(peerAddress, peerName, trimmed, "Saved Locally (Sandbox)")
                                draftText = ""
                            }
                        },
                        shape = CircleShape,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Save SMS")
                    }
                }
            }
        }
    }

    if (showComposeDialog) {
        var newNumber by remember { mutableStateOf("+44 ") }
        var newName by remember { mutableStateOf("") }
        var newBody by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showComposeDialog = false },
            title = { Text("New UK Message") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newNumber,
                        onValueChange = { newNumber = it },
                        label = { Text("UK Number (+44)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Recipient Name (optional)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newBody,
                        onValueChange = { newBody = it },
                        label = { Text("Message") }
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val formatted = UkFormatters.formatUkPhoneNumber(newNumber)
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$formatted")).apply {
                                putExtra("sms_body", newBody)
                            }
                            runCatching { context.startActivity(intent) }
                                .onFailure { onPostToast("No carrier SMS app available") }
                        }
                    ) {
                        Text("Carrier SMS")
                    }
                    Button(
                        onClick = {
                            val formatted = UkFormatters.formatUkPhoneNumber(newNumber)
                            if (formatted.isNotBlank() && newBody.isNotBlank()) {
                                val label = newName.trim().ifBlank { formatted }
                                onSendLocalSms(formatted, label, newBody.trim(), "Saved Locally (Sandbox)")
                                activePeer = formatted to label
                                showComposeDialog = false
                            }
                        }
                    ) {
                        Text("Save Local")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showComposeDialog = false }) { Text("Cancel") }
            }
        )
    }
}
