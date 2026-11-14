package com.example.ui.apps

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.db.ContactEntity
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.util.UkFormatters

@Composable
fun ContactsScreen(
    settings: VirtualPhoneSettings,
    contacts: List<ContactEntity>,
    onSaveContact: (ContactEntity) -> Unit,
    onDeleteContact: (Int) -> Unit,
    onDialContact: (String) -> Unit,
    onMessageContact: (String, String) -> Unit,
    onPostToast: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var editingContact by remember { mutableStateOf<ContactEntity?>(null) }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val name = if (nameIdx >= 0) cursor.getString(nameIdx) else "Imported Contact"
                        editingContact = ContactEntity(
                            name = name ?: "Imported Contact",
                            phoneNumber = "+44 ",
                            ukCity = settings.selectedCity.displayName,
                            notes = "Imported via Android Contact Picker"
                        )
                    }
                }
            }.onFailure {
                onPostToast("Unable to read selected contact")
            }
        }
    }

    val filtered = remember(contacts, searchQuery) {
        contacts.filter {
            searchQuery.isBlank() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phoneNumber.contains(searchQuery, ignoreCase = true) ||
                it.ukCity.contains(searchQuery, ignoreCase = true)
        }
    }

    VirtualAppScaffold(
        title = "UK Contacts",
        subtitle = "${contacts.size} saved • +44 Directory",
        onBack = onBack,
        actions = {
            IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                Icon(Icons.Rounded.Download, contentDescription = "Import from Android Contacts")
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingContact = ContactEntity(
                        name = "",
                        phoneNumber = "+44 ",
                        ukCity = settings.selectedCity.displayName
                    )
                },
                modifier = Modifier.testTag("add_contact_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Contact")
            }
        }
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search name, +44 number, or UK city") },
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
                    text = "No matching contacts",
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
                items(filtered, key = { it.id }) { contact ->
                    Card(
                        onClick = { editingContact = contact },
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
                                    text = contact.name.take(1).uppercase().ifBlank { "U" },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = contact.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = contact.phoneNumber,
                                    fontFamily = JetBrainsMonoFamily,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${contact.ukCity}, UK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    onSaveContact(contact.copy(isFavorite = !contact.isFavorite))
                                }
                            ) {
                                Icon(
                                    imageVector = if (contact.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = "Favorite",
                                    tint = if (contact.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDialContact(contact.phoneNumber) }) {
                                Icon(Icons.Rounded.Call, contentDescription = "Dial ${contact.name}")
                            }
                            IconButton(onClick = { onMessageContact(contact.phoneNumber, contact.name) }) {
                                Icon(Icons.AutoMirrored.Rounded.Message, contentDescription = "SMS ${contact.name}")
                            }
                        }
                    }
                }
            }
        }
    }

    editingContact?.let { contact ->
        var name by remember(contact) { mutableStateOf(contact.name) }
        var phone by remember(contact) { mutableStateOf(contact.phoneNumber) }
        var city by remember(contact) { mutableStateOf(contact.ukCity) }
        var notes by remember(contact) { mutableStateOf(contact.notes) }

        AlertDialog(
            onDismissRequest = { editingContact = null },
            title = { Text(if (contact.id == 0) "New UK Contact" else "Edit Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone (+44)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("UK City") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            onSaveContact(
                                contact.copy(
                                    name = name.trim(),
                                    phoneNumber = UkFormatters.formatUkPhoneNumber(phone),
                                    ukCity = city.trim().ifBlank { settings.selectedCity.displayName },
                                    notes = notes.trim()
                                )
                            )
                            editingContact = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Row {
                    if (contact.id != 0) {
                        TextButton(
                            onClick = {
                                onDeleteContact(contact.id)
                                editingContact = null
                            }
                        ) {
                            Icon(Icons.Rounded.Delete, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                    TextButton(onClick = { editingContact = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
