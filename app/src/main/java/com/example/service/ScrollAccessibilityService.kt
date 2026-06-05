package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.ScrollDatabase
import com.example.data.ScrollLimitConfig
import com.example.data.ScrollRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScrollAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: ScrollRepository
    private val lastScrollTimes = mutableMapOf<String, Long>()

    private val ONGOING_NOTIFICATION_ID = 1002

    override fun onCreate() {
        super.onCreate()
        val database = ScrollDatabase.getDatabase(this)
        repository = ScrollRepository(database.scrollDao)
    }

    override fun onDestroy() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ONGOING_NOTIFICATION_ID)
        super.onDestroy()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_SCROLLED or AccessibilityEvent.TYPE_VIEW_SELECTED
            packageNames = arrayOf(
                "com.google.android.youtube",
                "com.instagram.android",
                "com.facebook.katana",
                "com.facebook.lite",
                "com.snapchat.android",
                "com.twitter.android",
                "com.zhiliaoapp.musically",
                "com.ss.android.ugc.trill"
            )
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or 
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
        this.serviceInfo = info
        showOngoingNotification()
    }

    private fun showOngoingNotification() {
        val channelId = "scroll_limit_ongoing"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Scroll Shield Status",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Shows when ScrollLimit is actively protecting you from doomscrolling."
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Scroll Count Shield Active 🛡️")
            .setContentText("Actively guarding YouTube, Instagram, Snap, FB & Twitter.")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
            
        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!::repository.isInitialized) {
            try {
                val database = ScrollDatabase.getDatabase(this)
                repository = ScrollRepository(database.scrollDao)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED || 
            event.eventType == AccessibilityEvent.TYPE_VIEW_SELECTED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Exclude our own app's events to prevent loop issues
            if (packageName == this.packageName) return

            // Debounce scrolls: no more than 1 scroll count per 450ms per package
            val now = System.currentTimeMillis()
            val lastTime = lastScrollTimes[packageName] ?: 0L
            if (now - lastTime < 450) {
                return
            }
            lastScrollTimes[packageName] = now

            serviceScope.launch {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val config = repository.getConfigDirect()
                
                if (!config.isMonitoringEnabled) return@launch

                // Increment inside Room
                val totalScrolls = repository.incrementScrollCount(today, packageName)

                // If daily limit exceeded, trigger action
                if (totalScrolls > config.dailyScrollLimit) {
                    // Send alert notification
                    sendExceededNotification(
                        context = this@ScrollAccessibilityService,
                        total = totalScrolls,
                        limit = config.dailyScrollLimit,
                        bookTitle = config.selectedBookTitle
                    )
                    
                    // Launch custom "Limit Exceeded Focus Screen" in ScrollLimit App
                    // Only redirect the user if they scroll while browsing social media
                    val intent = Intent(this@ScrollAccessibilityService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("extra_exceeded_limit", true)
                        putExtra("extra_total_scrolls", totalScrolls)
                        putExtra("extra_scroll_limit", config.dailyScrollLimit)
                        putExtra("extra_book_uri", config.selectedBookUri)
                        putExtra("extra_book_title", config.selectedBookTitle)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        // No-op
    }

    private fun sendExceededNotification(context: Context, total: Int, limit: Int, bookTitle: String?) {
        val channelId = "scroll_limit_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Scroll Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts you when your daily scrolling limit is exceeded so you can switch to reading."
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val title = bookTitle ?: "your chosen book"
        val contentText = "Scrolled: $total / $limit. Swipe closed! Let's read '$title' to stay focused."
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("extra_exceeded_limit", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Doomscroll Limit Exceeded! 📚")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(1001, notification)
    }
}
