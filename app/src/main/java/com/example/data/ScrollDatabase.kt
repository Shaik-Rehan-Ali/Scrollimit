package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScrollLimitConfig::class, ScrollCount::class], version = 1, exportSchema = false)
abstract class ScrollDatabase : RoomDatabase() {
    abstract val scrollDao: ScrollDao

    companion object {
        @Volatile
        private var INSTANCE: ScrollDatabase? = null

        fun getDatabase(context: Context): ScrollDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScrollDatabase::class.java,
                    "scroll_database"
                )
                    .enableMultiInstanceInvalidation()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
