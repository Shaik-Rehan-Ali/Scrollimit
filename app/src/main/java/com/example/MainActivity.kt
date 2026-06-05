package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScrollCount
import com.example.ui.Classics
import com.example.ui.ScrollViewModel
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CozyObsidianBg
import com.example.ui.theme.ForestSage
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PaperParchmentBig
import com.example.ui.theme.SandAmberLight
import com.example.ui.theme.SoftTeal
import com.example.ui.theme.SoftTerracotta
import com.example.ui.theme.WarmWoodGold
import com.example.ui.theme.SlateTextSecondary

class MainActivity : ComponentActivity() {

    private val viewModel: ScrollViewModel by viewModels()

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // Persistent storage book file launcher
    private val pickBookLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            var name = "Selected Book"
            try {
                contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                // Return generic file name if display name retrieval fails
                name = it.lastPathSegment ?: "Recommended Book"
            }
            viewModel.saveBookSelection(it, name)
            Toast.makeText(this, "Book successfully recommended: $name", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        // Check if opened from accessibility service auto-redirection
        val showExceededOverlayOnLaunch = intent?.getBooleanExtra("extra_exceeded_limit", false) ?: false
        if (showExceededOverlayOnLaunch) {
            viewModel.triggerLimitExceeded(true)
        }

        setContent {
            MyApplicationTheme {
                val showExceededOverlay by viewModel.isLimitExceededTriggered.collectAsState()
                var showReaderOverlay by remember { mutableStateOf(false) }
                var activeClassicIndex by remember { mutableIntStateOf(0) }

                val config by viewModel.configState.collectAsState()
                val totalScrolls by viewModel.totalScrollsState.collectAsState()
                val scrollCounts by viewModel.scrollCountsState.collectAsState()
                val isServiceActive by viewModel.isAccessibilityActive.collectAsState()
                val isBatteryIgnored by viewModel.isBatteryOptimizationIgnored.collectAsState()

                // Poll status when window resumes to give accurate monitoring badge state
                LaunchedEffect(Unit) {
                    viewModel.checkAccessibilityServiceStatus()
                    viewModel.checkBatteryOptimizationStatus()
                    // If total scrolls exceeds, also pop up overlay automatically the first time
                    if (totalScrolls > config.dailyScrollLimit && config.isMonitoringEnabled) {
                        viewModel.triggerLimitExceeded(true)
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("app_main_container"),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // MAIN DASHBOARD LAYOUT
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Header Title
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "ScrollLimit",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = WarmWoodGold,
                                        fontFamily = FontFamily.Serif
                                    )
                                    Text(
                                        text = "Calm Minds, Deep Reading",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                    )
                                }

                                // Status Badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(
                                            if (isServiceActive) ForestSage.copy(alpha = 0.2f)
                                            else SoftTerracotta.copy(alpha = 0.2f)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isServiceActive) ForestSage else SoftTerracotta,
                                            shape = CircleShape
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isServiceActive) ForestSage else SoftTerracotta)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isServiceActive) "Shield Active" else "Inactive",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isServiceActive) ForestSage else SoftTerracotta
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Accessibility Setup Block
                            if (!isServiceActive) {
                                ServiceSetupCard(
                                    onEnableClick = {
                                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Battery Optimization Setup Block
                            if (!isBatteryIgnored && isServiceActive) {
                                BatteryOptimizationCard(
                                    onDisableBatteryClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = Uri.parse("package:${packageName}")
                                            }
                                            startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "Battery Settings not accessible. Please exclude ScrollLimit manually.", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Circular Progress Ring Card
                            CircularScrollRingCard(
                                current = totalScrolls,
                                limit = config.dailyScrollLimit,
                                onLimitAdjustClick = {
                                    // Trigger focus modal or screen block
                                    if (totalScrolls > config.dailyScrollLimit) {
                                        viewModel.triggerLimitExceeded(true)
                                    } else {
                                        Toast.makeText(this@MainActivity, "Goal within safe bounds. Keep it up!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Scroll Limit Slider Controls Card
                            ScrollLimitConfigCard(
                                currentLimit = config.dailyScrollLimit,
                                onLimitChanged = { viewModel.setScrollLimit(it) },
                                isMonitoringEnabled = config.isMonitoringEnabled,
                                onMonitoringToggle = { viewModel.setMonitoringEnabled(it) }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Recommended Library File Chooser Card
                            BookPickerCard(
                                selectedTitle = config.selectedBookTitle,
                                selectedUri = config.selectedBookUri,
                                onChooseFileClick = {
                                    // Launch SAF document picker to grab PDF, EPUB, TXT, etc
                                    pickBookLauncher.launch(
                                        arrayOf("application/pdf", "text/plain", "application/epub+zip")
                                    )
                                },
                                onReadClassicClick = {
                                    activeClassicIndex = 0
                                    showReaderOverlay = true
                                }
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Monitored Social Feeds Breakdown with Live Action Simulator
                            MonitoredFeedsCard(
                                scrollCounts = scrollCounts,
                                onSimulateScroll = { pack ->
                                    viewModel.simulateScrollInApp(pack)
                                    // Trigger overlay view if exceeds
                                    if (totalScrolls + 1 > config.dailyScrollLimit && config.isMonitoringEnabled) {
                                        viewModel.triggerLimitExceeded(true)
                                    }
                                },
                                onClearCounts = {
                                    viewModel.clearTodayCounts()
                                    Toast.makeText(this@MainActivity, "Counts successfully reset", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // HIGH-CONTRAST FULLSCREEN LOCKED OVERLAY SCREEN (Exceeded Scroll Limit Modal)
                        AnimatedVisibility(
                            visible = showExceededOverlay,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            LimitExceededOverlay(
                                current = totalScrolls,
                                limit = config.dailyScrollLimit,
                                bookTitle = config.selectedBookTitle ?: "Alice's Adventures in Wonderland (Default Classic)",
                                bookUri = config.selectedBookUri,
                                onLaunchBookClick = {
                                    val uriStr = config.selectedBookUri
                                    if (uriStr != null) {
                                        try {
                                            val i = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse(uriStr)
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            startActivity(i)
                                        } catch (e: Exception) {
                                            Toast.makeText(this@MainActivity, "No external reader app found for this book file. Launching Cozy Reader...", Toast.LENGTH_LONG).show()
                                            showReaderOverlay = true
                                        }
                                    } else {
                                        // Default classic reader fallback
                                        showReaderOverlay = true
                                    }
                                },
                                onOpenCozyClassicReader = {
                                    showReaderOverlay = true
                                    viewModel.triggerLimitExceeded(false)
                                },
                                onSnoozeLimit = {
                                    // Add temporary scroll limit for testing easily
                                    viewModel.setScrollLimit(config.dailyScrollLimit + 50)
                                    viewModel.triggerLimitExceeded(false)
                                    Toast.makeText(this@MainActivity, "Snoozed. Daily limit expanded by +50 scrolls.", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }

                        // COZY PARCHMENT BUILT-IN E-READER OVERLAY
                        AnimatedVisibility(
                            visible = showReaderOverlay,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CozyReaderOverlay(
                                classicIndex = activeClassicIndex,
                                onClassicChanged = { activeClassicIndex = it },
                                onCloseReader = { showReaderOverlay = false }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure status active indicator updates when returning to application screen
        viewModel.checkAccessibilityServiceStatus()
        viewModel.checkBatteryOptimizationStatus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val showExceeded = intent.getBooleanExtra("extra_exceeded_limit", false)
        if (showExceeded) {
            viewModel.triggerLimitExceeded(true)
        }
    }
}

// --------------------------- COMPOSE UI SUB-COMPONENTS ---------------------------

@Composable
fun ServiceSetupCard(onEnableClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("accessibility_setup_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SoftTerracotta.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, SoftTerracotta.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Shield Setup Required",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SoftTerracotta
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Counting scrolls on YouTube, Instagram, Snap, or Facebook requires activating ScrollLimit's accessibility service in Android Settings. No personal details are ever saved.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEnableClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("enable_service_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SoftTerracotta)
            ) {
                Text("Enable Scroll Count Shield", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BatteryOptimizationCard(onDisableBatteryClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("battery_setup_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ForestSage.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, ForestSage.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Keep Service Running 🛡️",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ForestSage
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Modern Android systems aggressively pause background apps when removed from recents. Disable Battery Optimizations for ScrollLimit to prevent the shield from being stopped.",
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDisableBatteryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("disable_battery_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ForestSage)
            ) {
                Text("Exclude from Battery Savings", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CircularScrollRingCard(
    current: Int,
    limit: Int,
    onLimitAdjustClick: () -> Unit
) {
    val progressFraction = if (limit > 0) (current.toFloat() / limit.toFloat()).coerceIn(0f, 1.2f) else 1f
    val isOverLimit = current > limit

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TODAY'S SCROLL SHIELD",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = SlateTextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Background Circle Track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.DarkGray.copy(alpha = 0.3f),
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Foreground Progress Arc Ring
                val sweepAngle = 360f * (if (progressFraction > 1f) 1f else progressFraction)
                val strokeColor = if (isOverLimit) SoftTerracotta else WarmWoodGold
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = strokeColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Inner Stats Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$current",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isOverLimit) SoftTerracotta else Color.White
                    )
                    Text(
                        text = "BUDGET: $limit",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextSecondary,
                        letterSpacing = 1.sp
                    )
                    if (isOverLimit) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "EXCEEDED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SoftTerracotta,
                            modifier = Modifier
                                .background(SoftTerracotta.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOverLimit) "Scroll Limit Exceeded" else "Doom-Scroll Range: Safe",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverLimit) SoftTerracotta else ForestSage
                    )
                    Text(
                        text = if (isOverLimit) "Launch your book now to read" else "Scrolling will trigger book at limit limit",
                        fontSize = 10.sp,
                        color = SlateTextSecondary
                    )
                }

                TextButton(
                    onClick = onLimitAdjustClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = WarmWoodGold)
                ) {
                    Text(if (isOverLimit) "LOCK NOW" else "INFO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ScrollLimitConfigCard(
    currentLimit: Int,
    onLimitChanged: (Int) -> Unit,
    isMonitoringEnabled: Boolean,
    onMonitoringToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal & Limit Limits",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Toggle monitoring active state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isMonitoringEnabled) WarmWoodGold.copy(alpha = 0.15f)
                            else Color.DarkGray.copy(alpha = 0.2f)
                        )
                        .clickable { onMonitoringToggle(!isMonitoringEnabled) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isMonitoringEnabled) "ACTIVE LOCK" else "SNOOZED",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMonitoringEnabled) WarmWoodGold else SlateTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Scroll limit: $currentLimit counts per day",
                fontSize = 13.sp,
                color = SlateTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Slider(
                value = currentLimit.toFloat(),
                onValueChange = { onLimitChanged(it.toInt()) },
                valueRange = 3f..300f,
                steps = 297,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_limit_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = WarmWoodGold,
                    activeTrackColor = WarmWoodGold,
                    inactiveTrackColor = Color.DarkGray
                )
            )

            Text(
                text = "Adjust the slider limit value. Lower limits (e.g. 50-80 scrolls) help you break scrolling momentum quickly so you can focus on reading.",
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = SlateTextSecondary
            )
        }
    }
}

@Composable
fun BookPickerCard(
    selectedTitle: String?,
    selectedUri: String?,
    onChooseFileClick: () -> Unit,
    onReadClassicClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Reading Recommendation",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "This book will open automatically when your doomscrolling limit triggers.",
                fontSize = 11.sp,
                color = SlateTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Current book display card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book spine mock design
                Box(
                    modifier = Modifier
                        .size(height = 54.dp, width = 38.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(WarmWoodGold, SoftTeal)
                            )
                        )
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(Color.White.copy(alpha = 0.25f))
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedTitle ?: "Alice's Adventures in Wonderland",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (selectedUri != null) "File Source: SAF Document (Google Drive / Files)"
                               else "Default Classical Book Companion",
                        fontSize = 10.sp,
                        color = SlateTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onChooseFileClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pick_book_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("📁 Choose Book", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onReadClassicClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_reader_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("📖 Read Preview", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MonitoredFeedsCard(
    scrollCounts: List<ScrollCount>,
    onSimulateScroll: (String) -> Unit,
    onClearCounts: () -> Unit
) {
    val monitoredApps = listOf(
        Pair("com.google.android.youtube", "YouTube"),
        Pair("com.instagram.android", "Instagram"),
        Pair("com.snapchat.android", "Snapchat"),
        Pair("com.facebook.katana", "Facebook"),
        Pair("com.twitter.android", "Twitter / X")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CharcoalCard)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monitored Social Feeds",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "CLEAR ALL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SoftTerracotta,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onClearCounts() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            monitoredApps.forEach { (packageName, readableName) ->
                val currentAppRecord = scrollCounts.firstOrNull { it.packageName == packageName }
                val count = currentAppRecord?.count ?: 0

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scroll_stat_card_${readableName.lowercase().substringBefore(" ")}")
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = readableName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "($count scrolls)",
                                fontSize = 10.sp,
                                color = SlateTextSecondary
                            )
                        }

                        // Simulation button next to list element so they can test immediately in editor!
                        IconButton(
                            onClick = { onSimulateScroll(packageName) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Simulate Scroll",
                                tint = WarmWoodGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Safe Progress bounds
                    LinearProgressIndicator(
                        progress = { (count.toFloat() / 50f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (count > 40) SoftTerracotta else SoftTeal,
                        trackColor = Color.DarkGray.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Tip: Tap the '+' button to simulate doomscroll inputs. This instantly triggers the limit block state to experience the focus warning logic!",
                fontSize = 10.sp,
                lineHeight = 14.sp,
                color = SlateTextSecondary,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

// Fullscreen high contrast block mode when user scroll limit is exceeded!
@Composable
fun LimitExceededOverlay(
    current: Int,
    limit: Int,
    bookTitle: String,
    bookUri: String?,
    onLaunchBookClick: () -> Unit,
    onOpenCozyClassicReader: () -> Unit,
    onSnoozeLimit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1014).copy(alpha = 0.98f))
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(SoftTerracotta.copy(alpha = 0.2f))
                .border(2.dp, SoftTerracotta, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📚", fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Feeds Paused. Mind Rested.",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Serif,
            color = WarmWoodGold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "You've recorded $current scrolls today, exceeding your daily limit of $limit.",
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Large quote
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CharcoalCard)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\"The reading of all good books is like conversation with the finest minds of past centuries.\"",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "— René Descartes",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WarmWoodGold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "YOUR RECOMMENDED BOOK",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = SlateTextSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = bookTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onLaunchBookClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("open_drive_book_button"),
            colors = ButtonDefaults.buttonColors(containerColor = WarmWoodGold),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Launch Recommended Book", color = Color(0xFF1E1400), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onOpenCozyClassicReader,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("open_cozy_reader_button"),
            colors = ButtonDefaults.buttonColors(containerColor = SoftTeal),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Open Cozy Reader Inside App", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Limit Override test key
        TextButton(
            onClick = onSnoozeLimit,
            modifier = Modifier.testTag("snooze_limit_button")
        ) {
            Text("Snooze / Temp Reset (+50 Scrolls)", color = SoftTerracotta, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

// Built-in cozy reader overlay utilizing charming paper tones and serif typography
@Composable
fun CozyReaderOverlay(
    classicIndex: Int,
    onClassicChanged: (Int) -> Unit,
    onCloseReader: () -> Unit
) {
    val book = Classics.list[classicIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperParchmentBig)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        // Reader header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCloseReader,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.08f))
            ) {
                Text("✕", color = Color.DarkGray, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "COZY COMPANION READER",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.DarkGray.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Classic Selector Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Classics.list.forEachIndexed { idx, item ->
                val isActive = idx == classicIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) Color(0xFF6F5E3E) else Color.DarkGray.copy(alpha = 0.05f))
                        .clickable { onClassicChanged(idx) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.substringBefore("'"),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else Color.DarkGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reading Body Block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.6f))
                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = book.title,
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C2415)
            )

            Text(
                text = "By ${book.author}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6F5E3E)
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = book.text,
                fontSize = 14.sp,
                fontFamily = FontFamily.Serif,
                lineHeight = 22.sp,
                color = Color(0xFF332B1E),
                textAlign = TextAlign.Justify
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Enjoy focus. Swipe closed to return to the ScrollLimit configuration panel.",
            textAlign = TextAlign.Center,
            fontSize = 10.sp,
            color = Color.DarkGray.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        )
    }
}



