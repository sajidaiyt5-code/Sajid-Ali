package com.example.ui.apps

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.db.NoteEntity
import com.example.model.VirtualPhoneSettings
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotesScreen(
    settings: VirtualPhoneSettings,
    notes: List<NoteEntity>,
    onVerifyPin: (String) -> Boolean,
    onSaveNote: (NoteEntity) -> Unit,
    onDeleteNote: (Int) -> Unit,
    onBack: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var pinChallengeNote by remember { mutableStateOf<NoteEntity?>(null) }

    val filtered = remember(notes, search) {
        notes.filter {
            search.isBlank() ||
                it.title.contains(search, ignoreCase = true) ||
                (!it.isLocked && it.content.contains(search, ignoreCase = true))
        }
    }

    VirtualAppScaffold(
        title = "UK Notes",
        subtitle = "${notes.size} notes • Local Room Storage",
        onBack = onBack,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingNote = NoteEntity(title = "", content = "") }
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "New Note")
            }
        }
    ) {
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            placeholder = { Text("Search notes...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No notes found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 84.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { note ->
                    val updated = Instant.ofEpochMilli(note.updatedAt)
                        .atZone(ZoneId.of(settings.timeZoneId))
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    Card(
                        onClick = {
                            if (note.isLocked && settings.pinLockEnabled) {
                                pinChallengeNote = note
                            } else {
                                editingNote = note
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title.ifBlank { "Untitled Note" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (note.isLocked) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Locked note",
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (note.isLocked && settings.pinLockEnabled) {
                                    "Locked with device PIN. Tap to unlock."
                                } else {
                                    note.content
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${note.colorTag} • $updated",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    editingNote?.let { note ->
        var title by remember(note) { mutableStateOf(note.title) }
        var content by remember(note) { mutableStateOf(note.content) }
        var tag by remember(note) { mutableStateOf(note.colorTag) }
        var locked by remember(note) { mutableStateOf(note.isLocked) }

        AlertDialog(
            onDismissRequest = { editingNote = null },
            title = { Text(if (note.id == 0) "New Note" else "Edit Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content") },
                        minLines = 4
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("NAVY", "TEAL", "AMBER").forEach { t ->
                            FilterChip(selected = tag == t, onClick = { tag = t }, label = { Text(t) })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = locked, onCheckedChange = { locked = it })
                        Text("Require PIN to open (if PIN enabled)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank() || content.isNotBlank()) {
                            onSaveNote(
                                note.copy(
                                    title = title.trim().ifBlank { "Untitled Note" },
                                    content = content.trim(),
                                    colorTag = tag,
                                    isLocked = locked
                                )
                            )
                            editingNote = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    if (note.id != 0) {
                        TextButton(
                            onClick = {
                                onDeleteNote(note.id)
                                editingNote = null
                            }
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                    TextButton(onClick = { editingNote = null }) { Text("Cancel") }
                }
            }
        )
    }

    pinChallengeNote?.let { lockedNote ->
        var pinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { pinChallengeNote = null },
            title = { Text("Unlock Private Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinError = false
                            if (it.length <= 4) pinInput = it.filter(Char::isDigit)
                        },
                        label = { Text("Enter 4-digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError
                    )
                    if (pinError) {
                        Text("Incorrect PIN", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onVerifyPin(pinInput)) {
                            pinChallengeNote = null
                            editingNote = lockedNote
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Unlock")
                }
            },
            dismissButton = {
                TextButton(onClick = { pinChallengeNote = null }) { Text("Cancel") }
            }
        )
    }
}
