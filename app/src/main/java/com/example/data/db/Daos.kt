package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isFavorite DESC, name ASC")
    fun observeAll(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Update
    suspend fun update(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM contacts")
    suspend fun clearAll()
}

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CallLogEntity): Long

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM call_logs")
    suspend fun clearAll()
}

@Dao
interface SmsMessageDao {
    @Query("SELECT * FROM sms_messages ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<SmsMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: SmsMessageEntity): Long

    @Query("DELETE FROM sms_messages WHERE peerAddress = :peerAddress")
    suspend fun deleteThread(peerAddress: String)

    @Query("DELETE FROM sms_messages")
    suspend fun clearAll()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM notes")
    suspend fun clearAll()
}

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY dateIso ASC, timeLabel ASC")
    fun observeAll(): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: CalendarEventEntity): Long

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM calendar_events")
    suspend fun clearAll()
}

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun observeAll(): Flow<List<AlarmEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity): Long

    @Update
    suspend fun update(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM alarms")
    suspend fun clearAll()
}

@Dao
interface GalleryItemDao {
    @Query("SELECT * FROM gallery_items ORDER BY capturedAt DESC")
    fun observeAll(): Flow<List<GalleryItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GalleryItemEntity): Long

    @Query("DELETE FROM gallery_items WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM gallery_items")
    suspend fun clearAll()
}
