package com.bloom.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [JournalEntry::class, Habit::class], version = 2, exportSchema = false)
abstract class BloomDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: BloomDatabase? = null

        fun getInstance(context: Context): BloomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BloomDatabase::class.java,
                    "bloom_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
