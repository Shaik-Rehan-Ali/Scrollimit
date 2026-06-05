package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ScrollCount
import com.example.data.ScrollDatabase
import com.example.data.ScrollLimitConfig
import com.example.data.ScrollRepository
import com.example.service.ScrollAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScrollViewModel(application: Application) : AndroidViewModel(application) {

    private val scrollDao = ScrollDatabase.getDatabase(application).scrollDao
    private val repository = ScrollRepository(scrollDao)

    private val _isAccessibilityActive = MutableStateFlow(false)
    val isAccessibilityActive: StateFlow<Boolean> = _isAccessibilityActive.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(false)
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    private val _isLimitExceededTriggered = MutableStateFlow(false)
    val isLimitExceededTriggered: StateFlow<Boolean> = _isLimitExceededTriggered.asStateFlow()

    private val todayDate: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Config Flow
    val configState: StateFlow<ScrollLimitConfig> = repository.config
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ScrollLimitConfig()
        )

    // Today's total scrolls Flow
    val totalScrollsState: StateFlow<Int> = repository.getTotalScrollsForDate(todayDate)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Today's list breakdown Flow
    val scrollCountsState: StateFlow<List<ScrollCount>> = repository.getScrollCountsForDate(todayDate)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        checkAccessibilityServiceStatus()
        checkBatteryOptimizationStatus()
    }

    fun triggerLimitExceeded(exceeded: Boolean) {
        _isLimitExceededTriggered.value = exceeded
    }

    fun checkAccessibilityServiceStatus() {
        val context = getApplication<Application>()
        val serviceName = "${context.packageName}/${ScrollAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: ""
        _isAccessibilityActive.value = enabledServices.contains(serviceName)
    }

    fun checkBatteryOptimizationStatus() {
        val context = getApplication<Application>()
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        _isBatteryOptimizationIgnored.value = pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun setScrollLimit(limit: Int) {
        viewModelScope.launch {
            repository.setScrollLimit(limit)
        }
    }

    fun setMonitoringEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMonitoringEnabled(enabled)
        }
    }

    fun saveBookSelection(uri: Uri?, title: String?) {
        viewModelScope.launch {
            val uriStr = uri?.toString()
            repository.setSelectedBook(uriStr, title)
            
            // Persist read access if we got a document URI
            uri?.let {
                try {
                    val context = getApplication<Application>()
                    context.contentResolver.takePersistableUriPermission(
                        it,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun simulateScrollInApp(packageName: String) {
        viewModelScope.launch {
            repository.incrementScrollCount(todayDate, packageName)
        }
    }

    fun clearTodayCounts() {
        viewModelScope.launch {
            val todayList = repository.getScrollCountsForDateDirect(todayDate)
            for (record in todayList) {
                repository.saveScrollCount(record.copy(count = 0))
            }
        }
    }
}
