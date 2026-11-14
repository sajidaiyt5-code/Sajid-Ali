package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val ukCity: String = "London",
    val email: String = "",
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val contactName: String,
    val phoneNumber: String,
    val callType: String, // "OUTGOING_CARRIER", "LOCAL_TEST", "MISSED"
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sms_messages")
data class SmsMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val peerAddress: String,
    val peerName: String,
    val body: String,
    val isOutgoing: Boolean,
    val deliveryNote: String = "Local Sandbox",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val colorTag: String = "NAVY", // NAVY, TEAL, AMBER, CRIMSON, SLATE
    val isLocked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val dateIso: String, // YYYY-MM-DD
    val timeLabel: String = "09:00",
    val locationLabel: String = "London, UK",
    val description: String = ""
)

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val daysSummary: String = "Mon–Fri",
    val isEnabled: Boolean = true
)

@Entity(tableName = "gallery_items")
data class GalleryItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val uriString: String,
    val sourceType: String, // "CAMERA" or "IMPORTED"
    val widthPx: Int = 0,
    val heightPx: Int = 0,
    val capturedAt: Long = System.currentTimeMillis()
)
