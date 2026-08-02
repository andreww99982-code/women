package com.bloom.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAll(): Flow<List<JournalEntry>>

    @Insert
    suspend fun insert(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)
}
