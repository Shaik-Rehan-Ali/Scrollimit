package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScrollDao {
    @Query("SELECT * FROM scroll_config WHERE id = 1 LIMIT 1")
    fun getConfig(): Flow<ScrollLimitConfig?>

    @Query("SELECT * FROM scroll_config WHERE id = 1 LIMIT 1")
    suspend fun getConfigDirect(): ScrollLimitConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ScrollLimitConfig)

    @Query("SELECT * FROM scroll_counts WHERE date = :date")
    fun getScrollCountsForDate(date: String): Flow<List<ScrollCount>>

    @Query("SELECT * FROM scroll_counts WHERE date = :date")
    suspend fun getScrollCountsForDateDirect(date: String): List<ScrollCount>

    @Query("SELECT SUM(count) FROM scroll_counts WHERE date = :date")
    fun getTotalScrollsForDate(date: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScrollCount(scrollCount: ScrollCount)

    @Query("SELECT * FROM scroll_counts WHERE date = :date AND packageName = :packageName LIMIT 1")
    suspend fun getScrollCount(date: String, packageName: String): ScrollCount?
}
