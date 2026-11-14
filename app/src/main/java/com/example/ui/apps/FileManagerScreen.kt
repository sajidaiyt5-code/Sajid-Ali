package com.example.ui.apps

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.SandboxFileItem
import com.example.data.repository.VirtualPhoneRepository
import com.example.model.VirtualPhoneSettings
import com.example.ui.theme.JetBrainsMonoFamily
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun FileManagerScreen(
    settings: VirtualPhoneSettings,
    currentDir: File,
    files: List<SandboxFileItem>,
    repository: VirtualPhoneRepository,
    onOpenDir: (File) -> Unit,
    onNavigateUp: () -> Boolean,
    onCreateFolder: (String) -> Unit,
    onWriteTextFile: (String, String) -> Unit,
    onDeleteFile: (File) -> Unit,
    onImportDocument: (android.net.Uri, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var editingFileState by remember { mutableStateOf<Pair<String, String>?>(null) }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            var name = "imported_file_${System.currentTimeMillis()}"
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) name = c.getString(idx) ?: name
                }
            }
            onImportDocument(uri, name)
        }
    }

    val isAtRoot = currentDir.canonicalPath == repository.sandboxRootDir.canonicalPath
    val relativePath = "/" + currentDir.relativeToOrSelf(repository.sandboxRootDir).path.trimStart('.')

    VirtualAppScaffold(
        title = "File Manager",
        subtitle = "Sandbox: $relativePath",
        onBack = {
            if (!onNavigateUp()) onBack()
        },
        actions = {
            if (!isAtRoot) {
                IconButton(onClick = { onNavigateUp() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Up folder")
                }
            }
            IconButton(onClick = { showNewFolderDialog = true }) {
                Icon(Icons.Rounded.CreateNewFolder, contentDescription = "New Folder")
            }
            IconButton(onClick = { editingFileState = "note_${System.currentTimeMillis() % 1000}.txt" to "" }) {
                Icon(Icons.AutoMirrored.Rounded.NoteAdd, contentDescription = "New Text File")
            }
            IconButton(onClick = { openDocLauncher.launch(arrayOf("*/*")) }) {
                Icon(Icons.Rounded.FileUpload, contentDescription = "Import Document")
            }
        }
    ) {
        if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Empty folder. Create a file or import a document.",
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
                items(files, key = { it.path }) { item ->
                    val dateStr = Instant.ofEpochMilli(item.lastModified)
                        .atZone(ZoneId.of(settings.timeZoneId))
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    val sizeText = if (item.isDirectory) {
                        "${item.sizeBytes} items"
                    } else if (item.sizeBytes >= 1024) {
                        "${item.sizeBytes / 1024} KB"
                    } else {
                        "${item.sizeBytes} B"
                    }

                    Card(
                        onClick = {
                            val file = File(item.path)
                            if (item.isDirectory) {
                                onOpenDir(file)
                            } else if (item.name.endsWith(".txt") || item.name.endsWith(".md") || item.name.endsWith(".json")) {
                                scope.launch {
                                    val text = repository.readSandboxTextFile(file)
                                    editingFileState = item.name to text
                                }
                            }
                        },
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
                            Icon(
                                imageVector = if (item.isDirectory) Icons.Rounded.Folder else Icons.Rounded.Description,
                                contentDescription = null,
                                tint = if (item.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$sizeText • $dateStr",
                                    fontFamily = JetBrainsMonoFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteFile(File(item.path)) }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete ${item.name}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("Create Folder") },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            onCreateFolder(folderName)
                            showNewFolderDialog = false
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    editingFileState?.let { (initialName, initialContent) ->
        var fileName by remember(initialName) { mutableStateOf(initialName) }
        var fileContent by remember(initialContent) { mutableStateOf(initialContent) }
        AlertDialog(
            onDismissRequest = { editingFileState = null },
            title = { Text("Text File Editor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("File Name (.txt)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fileContent,
                        onValueChange = { fileContent = it },
                        label = { Text("Content") },
                        minLines = 5
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fileName.isNotBlank()) {
                            onWriteTextFile(fileName, fileContent)
                            editingFileState = null
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingFileState = null }) { Text("Cancel") }
            }
        )
    }
}
