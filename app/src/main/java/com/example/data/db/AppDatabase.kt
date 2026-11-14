package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@Database(
    entities = [
        ContactEntity::class,
        CallLogEntity::class,
        SmsMessageEntity::class,
        NoteEntity::class,
        CalendarEventEntity::class,
        AlarmEntity::class,
        GalleryItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callLogDao(): CallLogDao
    abstract fun smsMessageDao(): SmsMessageDao
    abstract fun noteDao(): NoteDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun alarmDao(): AlarmDao
    abstract fun galleryItemDao(): GalleryItemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uk_virtual_phone.db"
                )
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val database = INSTANCE ?: return
            CoroutineScope(Dispatchers.IO).launch {
                seedInitialData(database)
            }
        }
    }

    suspend fun resetAndSeedDefaults() {
        contactDao().clearAll()
        callLogDao().clearAll()
        smsMessageDao().clearAll()
        noteDao().clearAll()
        calendarEventDao().clearAll()
        alarmDao().clearAll()
        galleryItemDao().clearAll()
        seedInitialData(this)
    }
}

private suspend fun seedInitialData(db: AppDatabase) {
    // Authentic UK public/civic directory entries (public UK service numbers)
    val contacts = listOf(
        ContactEntity(
            name = "NHS 111 Non-Emergency",
            phoneNumber = "111",
            ukCity = "London",
            notes = "Official UK NHS medical advice helpline (public service)",
            isFavorite = true
        ),
        ContactEntity(
            name = "National Rail Enquiries",
            phoneNumber = "+44 3457 484950",
            ukCity = "London",
            notes = "UK rail timetable and journey support",
            isFavorite = true
        ),
        ContactEntity(
            name = "TfL Customer Services",
            phoneNumber = "+44 343 222 1234",
            ukCity = "London",
            notes = "Transport for London travel assistance",
            isFavorite = false
        ),
        ContactEntity(
            name = "Met Office Weather Desk",
            phoneNumber = "+44 370 900 0100",
            ukCity = "Bristol",
            notes = "UK national meteorological service",
            isFavorite = false
        )
    )
    contacts.forEach { db.contactDao().insert(it) }

    val now = System.currentTimeMillis()
    db.smsMessageDao().insert(
        SmsMessageEntity(
            peerAddress = "+44 7700 900077",
            peerName = "UK Virtual Environment",
            body = "Welcome to UK Virtual Phone. Region set to United Kingdom (Europe/London, GBP £). Note: This local sandbox does not connect to a physical SIM or receive live carrier SMS.",
            isOutgoing = false,
            deliveryNote = "System Notice (Local)",
            timestamp = now - 120_000L
        )
    )

    db.noteDao().insert(
        NoteEntity(
            title = "UK Regional Configuration",
            content = "Default Country Code: +44\nCurrency: GBP (£)\nDate Format: DD/MM/YYYY\nTime Zone: Europe/London\nTemperature: Celsius (°C)",
            colorTag = "NAVY",
            isLocked = false
        )
    )
    db.noteDao().insert(
        NoteEntity(
            title = "Private Security Checklist",
            content = "Enable 4-digit PIN lock in Settings > Security & Privacy to protect locked notes and lock screen access.",
            colorTag = "TEAL",
            isLocked = false
        )
    )

    val todayUk = LocalDate.now(ZoneId.of("Europe/London")).toString()
    db.calendarEventDao().insert(
        CalendarEventEntity(
            title = "UK Virtual Phone Initialised",
            dateIso = todayUk,
            timeLabel = "09:00",
            locationLabel = "London, UK",
            description = "Regional environment verified for Europe/London."
        )
    )

    db.alarmDao().insert(
        AlarmEntity(
            hour = 7,
            minute = 30,
            label = "London Morning Brief",
            daysSummary = "Mon–Fri",
            isEnabled = false
        )
    )
}
