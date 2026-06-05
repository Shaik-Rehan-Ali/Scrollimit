package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scroll_config")
data class ScrollLimitConfig(
    @PrimaryKey val id: Int = 1,
    val dailyScrollLimit: Int = 100,
    val selectedBookUri: String? = null,
    val selectedBookTitle: String? = null,
    val isMonitoringEnabled: Boolean = true,
    val lastRedirectedDate: String = "" // date string of last direct trigger, to prevent infinite loops
)

@Entity(tableName = "scroll_counts", primaryKeys = ["date", "packageName"])
data class ScrollCount(
    val date: String, // format "yyyy-MM-dd"
    val packageName: String,
    val count: Int
)
