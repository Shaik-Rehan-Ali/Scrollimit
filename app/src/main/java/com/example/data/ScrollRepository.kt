package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScrollRepository(private val scrollDao: ScrollDao) {

    // Emits config, if none exists, returns a default one
    val config: Flow<ScrollLimitConfig> = scrollDao.getConfig().map { 
        it ?: ScrollLimitConfig()
    }

    suspend fun getConfigDirect(): ScrollLimitConfig {
        return scrollDao.getConfigDirect() ?: ScrollLimitConfig()
    }

    suspend fun saveConfig(config: ScrollLimitConfig) {
        scrollDao.saveConfig(config)
    }

    fun getScrollCountsForDate(date: String): Flow<List<ScrollCount>> {
        return scrollDao.getScrollCountsForDate(date)
    }

    fun getTotalScrollsForDate(date: String): Flow<Int> {
        return scrollDao.getTotalScrollsForDate(date).map { it ?: 0 }
    }

    suspend fun incrementScrollCount(date: String, packageName: String): Int {
        val existing = scrollDao.getScrollCount(date, packageName)
        val newCount = (existing?.count ?: 0) + 1
        val updated = ScrollCount(date, packageName, newCount)
        scrollDao.insertOrUpdateScrollCount(updated)
        
        // Return updated total scroll count for today
        val allCounts = scrollDao.getScrollCountsForDateDirect(date)
        return allCounts.sumOf { it.count }
    }

    suspend fun setScrollLimit(limit: Int) {
        val current = getConfigDirect()
        saveConfig(current.copy(dailyScrollLimit = limit))
    }

    suspend fun setSelectedBook(uri: String?, title: String?) {
        val current = getConfigDirect()
        saveConfig(current.copy(selectedBookUri = uri, selectedBookTitle = title))
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        val current = getConfigDirect()
        saveConfig(current.copy(isMonitoringEnabled = enabled))
    }

    suspend fun updateLastRedirectedDate(date: String) {
        val current = getConfigDirect()
        saveConfig(current.copy(lastRedirectedDate = date))
    }

    suspend fun getScrollCountsForDateDirect(date: String): List<ScrollCount> {
        return scrollDao.getScrollCountsForDateDirect(date)
    }

    suspend fun saveScrollCount(scrollCount: ScrollCount) {
        scrollDao.insertOrUpdateScrollCount(scrollCount)
    }
}
