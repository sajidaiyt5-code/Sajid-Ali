package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.AlarmEntity
import com.example.data.db.AppDatabase
import com.example.data.db.CalendarEventEntity
import com.example.data.db.CallLogEntity
import com.example.data.db.ContactEntity
import com.example.data.db.GalleryItemEntity
import com.example.data.db.NoteEntity
import com.example.data.db.SmsMessageEntity
import com.example.data.preferences.SettingsDataStore
import com.example.data.security.SecurityManager
import com.example.model.VirtualPhoneSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

data class SandboxFileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long
)

class VirtualPhoneRepository(
    private val context: Context,
    private val db: AppDatabase,
    val settingsStore: SettingsDataStore,
    val securityManager: SecurityManager
) {
    val settingsFlow: Flow<VirtualPhoneSettings> = settingsStore.settingsFlow

    // Room Observable Streams
    val contactsFlow: Flow<List<ContactEntity>> = db.contactDao().observeAll()
    val callLogsFlow: Flow<List<CallLogEntity>> = db.callLogDao().observeAll()
    val smsMessagesFlow: Flow<List<SmsMessageEntity>> = db.smsMessageDao().observeAll()
    val notesFlow: Flow<List<NoteEntity>> = db.noteDao().observeAll()
    val calendarEventsFlow: Flow<List<CalendarEventEntity>> = db.calendarEventDao().observeAll()
    val alarmsFlow: Flow<List<AlarmEntity>> = db.alarmDao().observeAll()
    val galleryItemsFlow: Flow<List<GalleryItemEntity>> = db.galleryItemDao().observeAll()

    // Sandbox File Manager Root
    val sandboxRootDir: File by lazy {
        File(context.filesDir, "virtual_phone").apply {
            if (!exists()) mkdirs()
            File(this, "Documents").apply { if (!exists()) mkdirs() }
            File(this, "DCIM").apply { if (!exists()) mkdirs() }
            File(this, "Downloads").apply { if (!exists()) mkdirs() }
            val readme = File(this, "Documents/UK_Device_Readme.txt")
            if (!readme.exists()) {
                readme.writeText(
                    "UK Virtual Phone Sandbox Storage\n" +
                        "Region: United Kingdom (Europe/London)\n" +
                        "Currency: GBP (£)\n" +
                        "All files created or imported here are stored privately in app internal storage."
                )
            }
        }
    }

    val cameraPhotosDir: File by lazy {
        File(sandboxRootDir, "DCIM").apply { if (!exists()) mkdirs() }
    }

    suspend fun listSandboxFiles(directory: File): List<SandboxFileItem> = withContext(Dispatchers.IO) {
        val safeDir = if (directory.canonicalPath.startsWith(sandboxRootDir.canonicalPath)) {
            directory
        } else {
            sandboxRootDir
        }
        safeDir.listFiles()?.map { file ->
            SandboxFileItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                sizeBytes = if (file.isDirectory) (file.listFiles()?.size?.toLong() ?: 0L) else file.length(),
                lastModified = file.lastModified()
            )
        }?.sortedWith(compareByDescending<SandboxFileItem> { it.isDirectory }.thenBy { it.name.lowercase() })
            ?: emptyList()
    }

    suspend fun createSandboxFolder(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        val clean = name.trim().replace("/", "_")
        if (clean.isEmpty()) return@withContext false
        File(parent, clean).mkdirs()
    }

    suspend fun writeSandboxTextFile(parent: File, fileName: String, content: String): Boolean =
        withContext(Dispatchers.IO) {
            val clean = fileName.trim().replace("/", "_")
            if (clean.isEmpty()) return@withContext false
            File(parent, clean).writeText(content)
            true
        }

    suspend fun readSandboxTextFile(file: File): String = withContext(Dispatchers.IO) {
        if (file.exists() && file.isFile) file.readText() else ""
    }

    suspend fun deleteSandboxFile(file: File): Boolean = withContext(Dispatchers.IO) {
        if (file.canonicalPath == sandboxRootDir.canonicalPath) return@withContext false
        file.deleteRecursively()
    }

    suspend fun importDocumentIntoSandbox(parent: File, uri: Uri, displayName: String): Boolean =
        withContext(Dispatchers.IO) {
            runCatching {
                val target = File(parent, displayName.replace("/", "_"))
                context.contentResolver.openInputStream(uri)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                true
            }.getOrDefault(false)
        }

    // Contacts
    suspend fun saveContact(contact: ContactEntity) {
        if (contact.id == 0) db.contactDao().insert(contact) else db.contactDao().update(contact)
    }

    suspend fun deleteContact(id: Int) = db.contactDao().deleteById(id)

    // Call Logs
    suspend fun logCall(contactName: String, phoneNumber: String, callType: String, durationSeconds: Int = 0) {
        db.callLogDao().insert(
            CallLogEntity(
                contactName = contactName,
                phoneNumber = phoneNumber,
                callType = callType,
                durationSeconds = durationSeconds
            )
        )
    }

    suspend fun deleteCallLog(id: Int) = db.callLogDao().deleteById(id)
    suspend fun clearCallLogs() = db.callLogDao().clearAll()

    // SMS
    suspend fun sendLocalSms(peerAddress: String, peerName: String, body: String, deliveryNote: String) {
        db.smsMessageDao().insert(
            SmsMessageEntity(
                peerAddress = peerAddress,
                peerName = peerName,
                body = body,
                isOutgoing = true,
                deliveryNote = deliveryNote
            )
        )
    }

    suspend fun deleteSmsThread(peerAddress: String) = db.smsMessageDao().deleteThread(peerAddress)

    // Notes
    suspend fun saveNote(note: NoteEntity) {
        val stamped = note.copy(updatedAt = System.currentTimeMillis())
        if (stamped.id == 0) db.noteDao().insert(stamped) else db.noteDao().update(stamped)
    }

    suspend fun deleteNote(id: Int) = db.noteDao().deleteById(id)

    // Calendar
    suspend fun saveCalendarEvent(event: CalendarEventEntity) {
        db.calendarEventDao().insert(event)
    }

    suspend fun deleteCalendarEvent(id: Int) = db.calendarEventDao().deleteById(id)

    // Alarms
    suspend fun saveAlarm(alarm: AlarmEntity) {
        if (alarm.id == 0) db.alarmDao().insert(alarm) else db.alarmDao().update(alarm)
    }

    suspend fun deleteAlarm(id: Int) = db.alarmDao().deleteById(id)

    // Gallery
    suspend fun addGalleryItem(item: GalleryItemEntity) {
        db.galleryItemDao().insert(item)
    }

    suspend fun importPhotoToSandbox(uri: Uri, title: String): GalleryItemEntity? =
        withContext(Dispatchers.IO) {
            runCatching {
                val fileName = "IMG_${System.currentTimeMillis()}.jpg"
                val dest = File(cameraPhotosDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                val item = GalleryItemEntity(
                    title = title,
                    uriString = Uri.fromFile(dest).toString(),
                    sourceType = "IMPORTED"
                )
                val insertedId = db.galleryItemDao().insert(item)
                item.copy(id = insertedId.toInt())
            }.getOrNull()
        }

    suspend fun deleteGalleryItem(item: GalleryItemEntity) = withContext(Dispatchers.IO) {
        db.galleryItemDao().deleteById(item.id)
        if (item.uriString.startsWith("file://")) {
            runCatching { File(Uri.parse(item.uriString).path ?: "").delete() }
        }
    }

    // Settings & Data Wipe
    suspend fun updateSettings(transform: (VirtualPhoneSettings) -> VirtualPhoneSettings) {
        settingsStore.updateSettings(transform)
    }

    suspend fun resetSettingsOnly() {
        settingsStore.resetToUkDefaults()
        securityManager.clearPinAndVault()
    }

    suspend fun clearAllUserData() = withContext(Dispatchers.IO) {
        db.resetAndSeedDefaults()
        settingsStore.resetToUkDefaults()
        securityManager.clearPinAndVault()
        sandboxRootDir.deleteRecursively()
        sandboxRootDir
    }
}
