package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

// Brand Colors (Professional Polish theme)
val BrandGreen = Color(0xFF006A6A)
val BrandGreenLight = Color(0xFFE6F4F4)
val BrandRed = Color(0xFFD32F2F)
val BrandOrange = Color(0xFFF57C00)
val BrandYellow = Color(0xFFFBC02D)
val SlateGrey = Color(0xFF404848)
val SoftBg = Color(0xFFF7F9F9)

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Analytics)
    object Checklist : Screen("checklist", "Checklist", Icons.Default.FactCheck)
    object Abnormality : Screen("abnormality", "Abnormality", Icons.Default.Warning)
    object Training : Screen("training", "Training", Icons.Default.School)
    object Jakarta : Screen("jakarta", "Jakarta HQ", Icons.Default.CloudSync)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CentrifugeApp(viewModel: CentrifugeViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()

    val ciltChecks by viewModel.ciltChecks.collectAsStateWithLifecycle()
    val reliabilityChecks by viewModel.reliabilityPmChecks.collectAsStateWithLifecycle()
    val abnormalityReports by viewModel.abnormalityReports.collectAsStateWithLifecycle()
    val mentorLogs by viewModel.mentorPairingLogs.collectAsStateWithLifecycle()
    val vibrationLogs by viewModel.vibrationLogs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BrandGreen,
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Factory branding container
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFCCE8E8), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Factory,
                                contentDescription = "Factory",
                                tint = Color(0xFF002020),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Column {
                            Text(
                                text = "CentriGuard Pro",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Berau Mill • East Kalimantan",
                                fontSize = 11.sp,
                                color = Color(0xFFCCE8E8),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        // Connection status pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFF4A9999).copy(alpha = 0.5f), CircleShape)
                                .clickable { viewModel.toggleOnlineMode() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                contentDescription = "Connection Status",
                                tint = if (isOnline) Color.Green else Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOnline) "ONLINE" else "OFFLINE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isOnline) "Synced to Jakarta" else "Berau Local Mode",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Normal,
                            style = androidx.compose.ui.text.TextStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 12.dp
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(72.dp)
                ) {
                    val screens = listOf(
                        Screen.Dashboard,
                        Screen.Checklist,
                        Screen.Abnormality,
                        Screen.Training,
                        Screen.Jakarta
                    )
                    screens.forEach { screen ->
                        val selected = currentScreen.route == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { currentScreen = screen },
                            label = { 
                                Text(
                                    text = screen.title, 
                                    fontSize = 10.sp, 
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandGreen,
                                selectedTextColor = BrandGreen,
                                indicatorColor = BrandGreenLight,
                                unselectedIconColor = Color.LightGray,
                                unselectedTextColor = Color.LightGray
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftBg)
                .padding(innerPadding)
        ) {
            // Operational Location Banner and Sync info
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGrey),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Mill YWA, Berau - Kaltim",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = {
                            viewModel.syncData { success ->
                                if (success) {
                                    Toast.makeText(context, "Sinkronisasi Berhasil!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Perangkat offline! Nyalakan mode online.", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isOnline) BrandYellow else Color.Gray),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim ke Jakarta", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = syncStatus,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Animated Screen transition
            Box(modifier = Modifier.fillMaxSize()) {
                when (currentScreen) {
                    Screen.Dashboard -> DashboardScreen(
                        vibrationLogs = vibrationLogs,
                        abnormalityReports = abnormalityReports,
                        ciltChecks = ciltChecks,
                        viewModel = viewModel
                    )
                    Screen.Checklist -> ChecklistScreen(
                        onSaveCilt = { viewModel.addCiltCheck(it) },
                        onSaveReliability = { viewModel.addReliabilityCheck(it) },
                        ciltHistory = ciltChecks,
                        reliabilityHistory = reliabilityChecks
                    )
                    Screen.Abnormality -> AbnormalityScreen(
                        reports = abnormalityReports,
                        onAddReport = { viewModel.addAbnormalityReport(it) },
                        onDeleteReport = { viewModel.deleteReport(it) }
                    )
                    Screen.Training -> TrainingScreen(
                        logs = mentorLogs,
                        onAddLog = { viewModel.addMentorLog(it) }
                    )
                    Screen.Jakarta -> JakartaScreen(
                        isOnline = isOnline,
                        ciltChecks = ciltChecks,
                        reliabilityChecks = reliabilityChecks,
                        reports = abnormalityReports,
                        mentorLogs = mentorLogs,
                        vibrationLogs = vibrationLogs,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// 1. DASHBOARD SCREEN
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun DashboardScreen(
    vibrationLogs: List<VibrationLog>,
    abnormalityReports: List<AbnormalityReport>,
    ciltChecks: List<CiltCheck>,
    viewModel: CentrifugeViewModel
) {
    var showVibDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sludge Centrifuge Hero Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Mesin Centrifuge Sludge",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Text(
                                "Unit Aktif: Centrifuge No. 2 & No. 6",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(BrandGreenLight, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "STABIL / NORMAL",
                                color = BrandGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw a custom layout representing Centrifuge Machine Parts
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SlateGrey)
                            .drawBehind {
                                // Draw stylized background lines representing factory blueprint
                                drawLine(
                                    color = Color.White.copy(alpha = 0.1f),
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    strokeWidth = 2f
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.1f),
                                    start = Offset(size.width / 2, 0f),
                                    end = Offset(size.width / 2, size.height),
                                    strokeWidth = 2f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Engine Icon",
                                tint = BrandYellow,
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(bottom = 4.dp)
                            )
                            Text(
                                text = "Rotational Bowl Speed: 1,450 RPM",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Penerapan Autonomous CILT Terintegrasi",
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Project Performance Targets (Slide 5: Target % Oil Loss & Jammed/Trip reduction)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Metric 1: Oil Loss
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "Oil Loss",
                            tint = BrandGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Oil Losses Sludge", fontSize = 12.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "0.95%",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Target < 1.0%", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 3.dp))
                        }
                        Text(
                            text = "Turun drastis dari 1.55%",
                            fontSize = 9.sp,
                            color = BrandGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Metric 2: Jammed Trip Rate
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.weight(1f),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Trip Jammed Rate",
                            tint = BrandGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Jammed / Trip Mesin", fontSize = 12.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "0",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandGreen
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Target 0 Trip", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 3.dp))
                        }
                        Text(
                            text = "Bebas downtime mendadak!",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Live Vibrations Trends & Temperature (Slide 21 and 26)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Live Vibration & Temp Trends",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Text(
                                "Drive End, Non-Drive End, Bearing Temp",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = { showVibDialog = true },
                            modifier = Modifier.testTag("add_vibration_log_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Log Vibration", tint = BrandGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vibration chart drawn using Canvas (Page 21 & 26 metrics)
                    val history = vibrationLogs.take(15).reversed()
                    if (history.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFAFAFA))
                                .padding(8.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val points = history.size
                                val spacing = width / (points - 1).coerceAtLeast(1)

                                val maxVib = 10f // Max scale is 10 mm/s

                                // Draw limit lines
                                val warningY = height - (4.5f / maxVib) * height
                                val criticalY = height - (8.8f / maxVib) * height

                                // Target Limit 4.5 mm/s (Normal limit)
                                drawLine(
                                    color = BrandYellow.copy(alpha = 0.6f),
                                    start = Offset(0f, warningY),
                                    end = Offset(width, warningY),
                                    strokeWidth = 2f
                                )
                                // Critical Limit 8.8 mm/s
                                drawLine(
                                    color = BrandRed.copy(alpha = 0.6f),
                                    start = Offset(0f, criticalY),
                                    end = Offset(width, criticalY),
                                    strokeWidth = 3f
                                )

                                // Plot drive end vibrations
                                val path = Path()
                                history.forEachIndexed { index, log ->
                                    val x = index * spacing
                                    val y = height - (log.driveEndVibration / maxVib).coerceIn(0f, 1f) * height
                                    if (index == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                    
                                    // Draw node dots
                                    drawCircle(
                                        color = if (log.driveEndVibration >= 8.8f) BrandRed else if (log.driveEndVibration >= 4.5f) BrandOrange else BrandGreen,
                                        radius = 4f,
                                        center = Offset(x, y)
                                    )
                                }
                                drawPath(
                                    path = path,
                                    color = BrandGreen,
                                    style = Stroke(width = 4f)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Historis (Jan-Mar 2026)", fontSize = 8.sp, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(BrandYellow).clip(CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Warning (4.5)", fontSize = 8.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(modifier = Modifier.size(8.dp).background(BrandRed).clip(CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Trip Critical (8.8)", fontSize = 8.sp, color = Color.Gray)
                            }
                            Text("Sekarang (Normal)", fontSize = 8.sp, color = Color.Gray)
                        }

                        // Display Current Vibration Stats
                        val current = history.last()
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Drive End", fontSize = 11.sp, color = Color.Gray)
                                Text("${String.format("%.1f", current.driveEndVibration)} mm/s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (current.driveEndVibration > 4.5f) BrandOrange else BrandGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Non-Drive End", fontSize = 11.sp, color = Color.Gray)
                                Text("${String.format("%.1f", current.nonDriveEndVibration)} mm/s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (current.nonDriveEndVibration > 4.5f) BrandOrange else BrandGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Bearing Temp", fontSize = 11.sp, color = Color.Gray)
                                Text("${String.format("%.1f", current.bearingTemp)} °C", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (current.bearingTemp > 70f) BrandRed else BrandGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Status", fontSize = 11.sp, color = Color.Gray)
                                Box(
                                    modifier = Modifier
                                        .background(if (current.alarmState == "Normal") BrandGreenLight else if (current.alarmState == "Warning") Color(0xFFFFF3E0) else Color(0xFFFFEBEE))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                ) {
                                    Text(current.alarmState, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (current.alarmState == "Normal") BrandGreen else if (current.alarmState == "Warning") BrandOrange else BrandRed)
                                }
                            }
                        }
                    } else {
                        Text("Belum ada logs vibrasi. Klik ikon '+' untuk mencatat vibrasi baru.", fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // Active Abnormality Warnings (Slide 14)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Daftar Abnormality Aktif (FMEA)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )
                    Text(
                        "Segera tindak lanjuti sebelum trip terjadi",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val activeReports = abnormalityReports.take(3)
                    if (activeReports.isNotEmpty()) {
                        activeReports.forEach { report ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(SoftBg, RoundedCornerShape(16.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(
                                                if (report.tagType == "Yellow Tag") BrandYellow.copy(alpha = 0.2f)
                                                else if (report.tagType == "White Tag") Color.LightGray.copy(alpha = 0.5f)
                                                else BrandRed.copy(alpha = 0.1f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Warning",
                                            tint = if (report.tagType == "Yellow Tag") BrandOrange else if (report.tagType == "White Tag") SlateGrey else BrandRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(report.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateGrey, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${report.factor} • RPN: ${report.rpn} • PIC: ${report.picName}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (report.tagType == "Yellow Tag") BrandYellow
                                            else if (report.tagType == "White Tag") Color.White
                                            else BrandRed,
                                            RoundedCornerShape(4.dp)
                                        )
                                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        report.tagType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Semua mesin bersih dari abnormality! (0 Temuan)", fontSize = 12.sp, color = BrandGreen, modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
            }
        }
    }

    // Modal Dialog to log Vibration manually (Slide 26)
    if (showVibDialog) {
        Dialog(onDismissRequest = { showVibDialog = false }) {
            var deVib by remember { mutableStateOf("") }
            var ndeVib by remember { mutableStateOf("") }
            var bTemp by remember { mutableStateOf("") }
            var comments by remember { mutableStateOf("") }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Catat Nilai Vibrasi & Suhu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Text("Lakukan pengukuran sesuai measurement point centrifuge (Slide 21 & 26)", fontSize = 11.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = deVib,
                        onValueChange = { deVib = it },
                        label = { Text("Drive End Vibration (mm/s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("drive_end_input")
                    )

                    OutlinedTextField(
                        value = ndeVib,
                        onValueChange = { ndeVib = it },
                        label = { Text("Non-Drive End Vibration (mm/s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("non_drive_end_input")
                    )

                    OutlinedTextField(
                        value = bTemp,
                        onValueChange = { bTemp = it },
                        label = { Text("Bearing Temperature (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("bearing_temp_input")
                    )

                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        label = { Text("Catatan / Kondisi Belt") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showVibDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val deValue = deVib.toFloatOrNull() ?: 2.5f
                                val ndeValue = ndeVib.toFloatOrNull() ?: 2.2f
                                val tempValue = bTemp.toFloatOrNull() ?: 55f
                                val alarm = if (deValue >= 8.8f || tempValue >= 70f) "Critical" else if (deValue >= 4.5f) "Warning" else "Normal"

                                viewModel.addVibrationLog(
                                    VibrationLog(
                                        driveEndVibration = deValue,
                                        nonDriveEndVibration = ndeValue,
                                        motorVibration = 1.8f,
                                        gearBoxVibration = 2.0f,
                                        bearingTemp = tempValue,
                                        motorTemp = 48f,
                                        alarmState = alarm,
                                        comments = comments
                                    )
                                )
                                showVibDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            modifier = Modifier.testTag("submit_vibration_button")
                        ) {
                            Text("Simpan", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// 2. CHECKLIST SCREEN (DAILY CILT + WEEKLY RELIABILITY PM)
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun ChecklistScreen(
    onSaveCilt: (CiltCheck) -> Unit,
    onSaveReliability: (ReliabilityPmCheck) -> Unit,
    ciltHistory: List<CiltCheck>,
    reliabilityHistory: List<ReliabilityPmCheck>
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = BrandGreen) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Harian CILT", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Reliability PM", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (selectedTab == 0) {
                DailyCiltForm(onSave = {
                    onSaveCilt(it)
                    Toast.makeText(context, "Laporan CILT berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }, history = ciltHistory)
            } else {
                ReliabilityPmForm(onSave = {
                    onSaveReliability(it)
                    Toast.makeText(context, "Laporan Reliability PM berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }, history = reliabilityHistory)
            }
        }
    }
}

@Composable
fun DailyCiltForm(onSave: (CiltCheck) -> Unit, history: List<CiltCheck>) {
    var operatorName by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf("") }

    // Cleaning checklists
    var nozzleCleaned by remember { mutableStateOf(false) }
    var bowlCleaned by remember { mutableStateOf(false) }
    var areaCleaned by remember { mutableStateOf(false) }

    // Inspection checklists
    var nozzleChecked by remember { mutableStateOf(false) }
    var vibrationChecked by remember { mutableStateOf(false) }
    var leakChecked by remember { mutableStateOf(false) }
    var instrumentChecked by remember { mutableStateOf(false) }

    // Lubrication checklists
    var bearingGreased by remember { mutableStateOf(false) }
    var couplingGreased by remember { mutableStateOf(false) }
    var oilLevelChecked by remember { mutableStateOf(false) }

    // Tightening checklists
    var nozzleBoltsTightened by remember { mutableStateOf(false) }
    var fittingPipesTightened by remember { mutableStateOf(false) }
    var beltTensionChecked by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Formulir Harian CILT Operator", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    OutlinedTextField(
                        value = operatorName,
                        onValueChange = { operatorName = it },
                        label = { Text("Nama Operator") },
                        modifier = Modifier.fillMaxWidth().testTag("cilt_operator_name")
                    )
                }
            }
        }

        // 1. CLEANING CARD
        item {
            ChecklistCategoryCard(
                title = "1. CLEANING (Pembersihan)",
                items = listOf(
                    ChecklistRowItem("Bersihkan nozzle & nozzle holder (bebas dari sumbatan pasir/serat)", nozzleCleaned) { nozzleCleaned = it },
                    ChecklistRowItem("Bersihkan cover bowl & bagian dalam bowl dari endapan keras", bowlCleaned) { bowlCleaned = it },
                    ChecklistRowItem("Bersihkan area luar mesin dari tumpahan minyak/sludge", areaCleaned) { areaCleaned = it }
                )
            )
        }

        // 2. INSPECTION CARD
        item {
            ChecklistCategoryCard(
                title = "2. INSPECTION (Inspeksi)",
                items = listOf(
                    ChecklistRowItem("Periksa keausan nozzle & diameter male nut", nozzleChecked) { nozzleChecked = it },
                    ChecklistRowItem("Periksa getaran & suara tidak normal saat mesin beroperasi", vibrationChecked) { vibrationChecked = it },
                    ChecklistRowItem("Periksa kebocoran seal, packing, sambungan fitting pipa & valve", leakChecked) { leakChecked = it },
                    ChecklistRowItem("Periksa pressure gauge, suhu, & level oli gearbox", instrumentChecked) { instrumentChecked = it }
                )
            )
        }

        // 3. LUBRICATION CARD
        item {
            ChecklistCategoryCard(
                title = "3. LUBRICATION (Pelumasan)",
                items = listOf(
                    ChecklistRowItem("Pemberian grease pada bearing buffer sampai penuh (Slide 23)", bearingGreased) { bearingGreased = it },
                    ChecklistRowItem("Lakukan greasing coupling transfluid sesuai jadwal", couplingGreased) { couplingGreased = it },
                    ChecklistRowItem("Cek level oli transfluid fluid-coupling, tambah jika kurang", oilLevelChecked) { oilLevelChecked = it }
                )
            )
        }

        // 4. TIGHTENING CARD
        item {
            ChecklistCategoryCard(
                title = "4. TIGHTENING (Pengencangan)",
                items = listOf(
                    ChecklistRowItem("Kencangkan baut pengunci nozzle holder dengan torsi tepat", nozzleBoltsTightened) { nozzleBoltsTightened = it },
                    ChecklistRowItem("Pastikan fitting pipa & sambungan valve kencang tidak longgar", fittingPipesTightened) { fittingPipesTightened = it },
                    ChecklistRowItem("Periksa ketegangan & kelayakan V-belt (Tension > 50 N)", beltTensionChecked) { beltTensionChecked = it }
                )
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        label = { Text("Catatan / Temuan Temuan Lapangan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (operatorName.isBlank()) {
                                operatorName = "Operator Berau"
                            }
                            onSave(
                                CiltCheck(
                                    operatorName = operatorName,
                                    nozzleCleaned = nozzleCleaned,
                                    bowlCleaned = bowlCleaned,
                                    areaCleaned = areaCleaned,
                                    nozzleChecked = nozzleChecked,
                                    vibrationChecked = vibrationChecked,
                                    leakChecked = leakChecked,
                                    instrumentChecked = instrumentChecked,
                                    bearingGreased = bearingGreased,
                                    couplingGreased = couplingGreased,
                                    oilLevelChecked = oilLevelChecked,
                                    nozzleBoltsTightened = nozzleBoltsTightened,
                                    fittingPipesTightened = fittingPipesTightened,
                                    beltTensionChecked = beltTensionChecked,
                                    comments = comments
                                )
                            )
                            // Reset state
                            operatorName = ""
                            comments = ""
                            nozzleCleaned = false
                            bowlCleaned = false
                            areaCleaned = false
                            nozzleChecked = false
                            vibrationChecked = false
                            leakChecked = false
                            instrumentChecked = false
                            bearingGreased = false
                            couplingGreased = false
                            oilLevelChecked = false
                            nozzleBoltsTightened = false
                            fittingPipesTightened = false
                            beltTensionChecked = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_cilt_button")
                    ) {
                        Text("Simpan & Kirim Laporan CILT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Histori Section
        item {
            Text("Histori Pemeriksaan CILT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey, modifier = Modifier.padding(top = 8.dp))
        }

        items(history) { check ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = check.operatorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrandGreen
                        )
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(check.timestamp)),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cleaning: ${if (check.nozzleCleaned && check.bowlCleaned) "Lengkap" else "Sebagian"} • Lubrication: ${if (check.bearingGreased) "Selesai" else "Belum"}",
                        fontSize = 11.sp,
                        color = SlateGrey
                    )
                    if (check.comments.isNotBlank()) {
                        Text(text = "Note: ${check.comments}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .background(if (check.isSynced) BrandGreenLight else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (check.isSynced) "Synced to Jakarta" else "Berau Local",
                                fontSize = 9.sp,
                                color = if (check.isSynced) BrandGreen else Color.DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReliabilityPmForm(onSave: (ReliabilityPmCheck) -> Unit, history: List<ReliabilityPmCheck>) {
    var comments by remember { mutableStateOf("") }
    var intervalMinutes by remember { mutableStateOf("120") }
    var waterTemp by remember { mutableStateOf("92") }
    var bowlSpeed by remember { mutableStateOf("1450") }
    var bearingTemp by remember { mutableStateOf("60") }

    var hollowBearingChecked by remember { mutableStateOf(false) }
    var couplingOilLeakChecked by remember { mutableStateOf(false) }
    var vibrationChecked by remember { mutableStateOf(false) }
    var unusualNoiseDetected by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Formulir Reliability-Based PM (Slide 21)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Text("Pemeriksaan ini berfokus pada 3 pencegahan utama: Penyumbatan Nozzle, Keausan Bearing, & Bowl Imbalance.", fontSize = 11.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = intervalMinutes,
                        onValueChange = { intervalMinutes = it },
                        label = { Text("Interval Flushing Preventif (Menit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = waterTemp,
                        onValueChange = { waterTemp = it },
                        label = { Text("Suhu Air Panas Flushing (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bowlSpeed,
                        onValueChange = { bowlSpeed = it },
                        label = { Text("Bowl Speed (RPM)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bearingTemp,
                        onValueChange = { bearingTemp = it },
                        label = { Text("Suhu Bearing (°C)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            ChecklistCategoryCard(
                title = "Pencegahan Kerusakan Berbasis Desain (3 Prevention)",
                items = listOf(
                    ChecklistRowItem("Greasing hollow bearing & buffer sesuai jam operasi (Pencegahan Keausan)", hollowBearingChecked) { hollowBearingChecked = it },
                    ChecklistRowItem("Periksa kebocoran oli transfluid coupling (Pencegahan Kerusakan Transmisi)", couplingOilLeakChecked) { couplingOilLeakChecked = it },
                    ChecklistRowItem("Lakukan pengecekan kestabilan getaran unit (Pencegahan Imbalance)", vibrationChecked) { vibrationChecked = it },
                    ChecklistRowItem("Suara menderit abnormal / ketidakstabilan suara terdeteksi", unusualNoiseDetected) { unusualNoiseDetected = it }
                )
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        label = { Text("Catatan Hasil Reliability PM") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val interval = intervalMinutes.toIntOrNull() ?: 120
                            val temp = waterTemp.toFloatOrNull() ?: 92f
                            val speed = bowlSpeed.toFloatOrNull() ?: 1450f
                            val bTempVal = bearingTemp.toFloatOrNull() ?: 60f

                            onSave(
                                ReliabilityPmCheck(
                                    flushingIntervalMinutes = interval,
                                    waterTempCelsius = temp,
                                    hollowBearingChecked = hollowBearingChecked,
                                    couplingOilLeakChecked = couplingOilLeakChecked,
                                    vibrationChecked = vibrationChecked,
                                    bowlSpeedRpm = speed,
                                    bearingTempCelsius = bTempVal,
                                    unusualNoiseDetected = unusualNoiseDetected,
                                    comments = comments
                                )
                            )

                            // reset form
                            comments = ""
                            intervalMinutes = "120"
                            waterTemp = "92"
                            bowlSpeed = "1450"
                            bearingTemp = "60"
                            hollowBearingChecked = false
                            couplingOilLeakChecked = false
                            vibrationChecked = false
                            unusualNoiseDetected = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_reliability_button")
                    ) {
                        Text("Simpan Reliability PM", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Histori Reliability PM", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey, modifier = Modifier.padding(top = 8.dp))
        }

        items(history) { check ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PM - Suhu Air ${check.waterTempCelsius}°C",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = BrandGreen
                        )
                        Text(
                            text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(check.timestamp)),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Interval Flush: ${check.flushingIntervalMinutes}m • Speed: ${check.bowlSpeedRpm} RPM • Bearing Suhu: ${check.bearingTempCelsius}°C",
                        fontSize = 11.sp,
                        color = SlateGrey
                    )
                    if (check.comments.isNotBlank()) {
                        Text(text = "Note: ${check.comments}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

data class ChecklistRowItem(
    val label: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit
)

@Composable
fun ChecklistCategoryCard(title: String, items: List<ChecklistRowItem>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.checked,
                        onCheckedChange = item.onCheckedChange,
                        colors = CheckboxDefaults.colors(checkedColor = BrandGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.label, fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// 3. ABNORMALITY REPORTING SCREEN (WITH PHOTO SELECTION SIMULATOR & RPN CALCULATION)
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun AbnormalityScreen(
    reports: List<AbnormalityReport>,
    onAddReport: (AbnormalityReport) -> Unit,
    onDeleteReport: (AbnormalityReport) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("MACHINE") } // MAN, METHOD, MACHINE, ENVIRONMENT
    var tagType by remember { mutableStateOf("Yellow Tag") } // White Tag, Yellow Tag, None

    // FMEA Parameters
    var severity by remember { mutableStateOf(5) }
    var occurrence by remember { mutableStateOf(4) }
    var detection by remember { mutableStateOf(3) }

    // Simulated Camera / Anomaly Image Choice
    var selectedPhotoTitle by remember { mutableStateOf("Tidak ada foto") }
    var simulatedPhotoUri by remember { mutableStateOf<String?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Laporkan Temuan Abnormality (Slide 14)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Text("Hitung tingkat keparahan risiko menggunakan formula RPN (Severity x Occurrence x Detection)", fontSize = 11.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Judul Temuan") },
                        modifier = Modifier.fillMaxWidth().testTag("abnormality_title")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi Kerusakan Detail") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Factor Dropdown Simulation
                    Text("Faktor Penyebab Abnormality (4M1E):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("MAN", "METHOD", "MACHINE", "ENVIRONMENT").forEach { fact ->
                            val selected = factor == fact
                            FilterChip(
                                selected = selected,
                                onClick = { factor = fact },
                                label = { Text(fact, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandGreenLight,
                                    selectedLabelColor = BrandGreen
                                )
                            )
                        }
                    }

                    // Tagging choice
                    Text("Labeling Tag (Slide 24):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("White Tag", "Yellow Tag", "None").forEach { tag ->
                            val selected = tagType == tag
                            FilterChip(
                                selected = selected,
                                onClick = { tagType = tag },
                                label = { Text(tag, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (tag == "White Tag") Color.LightGray else if (tag == "Yellow Tag") BrandYellow else BrandRed.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }

                    // FMEA Risk Calculation sliders
                    Text("Analisa FMEA & Nilai RPN: ${(severity * occurrence * detection)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Column {
                        Text("Severity (Tingkat Keparahan): $severity", fontSize = 11.sp, color = Color.Gray)
                        Slider(value = severity.toFloat(), onValueChange = { severity = it.toInt() }, valueRange = 1f..10f, colors = SliderDefaults.colors(thumbColor = BrandGreen, activeTrackColor = BrandGreen))
                    }

                    Column {
                        Text("Occurrence (Tingkat Sering Kejadian): $occurrence", fontSize = 11.sp, color = Color.Gray)
                        Slider(value = occurrence.toFloat(), onValueChange = { occurrence = it.toInt() }, valueRange = 1f..10f, colors = SliderDefaults.colors(thumbColor = BrandGreen, activeTrackColor = BrandGreen))
                    }

                    Column {
                        Text("Detection (Tingkat Deteksi Alat/Sensor): $detection", fontSize = 11.sp, color = Color.Gray)
                        Slider(value = detection.toFloat(), onValueChange = { detection = it.toInt() }, valueRange = 1f..10f, colors = SliderDefaults.colors(thumbColor = BrandGreen, activeTrackColor = BrandGreen))
                    }

                    // Condition Photo Attachment (SIMULATED)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SoftBg, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", tint = BrandGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(selectedPhotoTitle, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                        }
                        Button(
                            onClick = { showPhotoDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Pilih / Ambil Foto", fontSize = 10.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                title = "Temuan Abnormality"
                            }
                            onAddReport(
                                AbnormalityReport(
                                    title = title,
                                    description = description,
                                    factor = factor,
                                    severityScore = severity,
                                    occurrenceScore = occurrence,
                                    detectionScore = detection,
                                    rpn = severity * occurrence * detection,
                                    photoUri = simulatedPhotoUri,
                                    tagType = tagType
                                )
                            )
                            Toast.makeText(context, "Laporan Abnormality Tersimpan!", Toast.LENGTH_SHORT).show()
                            // Reset
                            title = ""
                            description = ""
                            severity = 5
                            occurrence = 4
                            detection = 3
                            selectedPhotoTitle = "Tidak ada foto"
                            simulatedPhotoUri = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_abnormality_button")
                    ) {
                        Text("SIMPAN TEMUAN KERUSAKAN", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Daftar Temuan Abnormality Mill", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
        }

        items(reports) { r ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(r.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateGrey)
                            Text(
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(r.timestamp)),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (r.tagType == "Yellow Tag") BrandYellow
                                        else if (r.tagType == "White Tag") Color.LightGray
                                        else BrandRed.copy(alpha = 0.5f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(r.tagType, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            IconButton(onClick = { onDeleteReport(r) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(r.description, fontSize = 12.sp, color = Color.DarkGray)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Faktor: ${r.factor}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                            Text("Severity: ${r.severityScore} • Occ: ${r.occurrenceScore} • Det: ${r.detectionScore}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .background(if (r.rpn >= 200) BrandRed.copy(alpha = 0.2f) else BrandGreenLight, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "RPN: ${r.rpn}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (r.rpn >= 200) BrandRed else BrandGreen
                            )
                        }
                    }

                    // Display mock/simulated photo if attached
                    if (r.photoUri != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftBg, RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = "Attached Image", tint = BrandGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Foto Kondisi: ${r.photoUri} (Disimpan offline)",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // Photo Dialog Selector Simulation
    if (showPhotoDialog) {
        Dialog(onDismissRequest = { showPhotoDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pilih Simulasi Foto Kerusakan Mill Berau", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SlateGrey)

                    val choices = listOf(
                        "Nozzle Clogging (Penyumbatan Nozzle)" to "nozzle_clogging.png",
                        "Bearing Aus / Overheat (62°C+)" to "bearing_wear.png",
                        "Transfluid Coupling Oil Leakage" to "coupling_oil_leak.png",
                        "V-Belt Loose / Cracked" to "loose_belt.png",
                        "Sludge Build-up in Bowl Housing" to "sludge_buildup.png"
                    )

                    choices.forEach { (label, file) ->
                        TextButton(
                            onClick = {
                                selectedPhotoTitle = label
                                simulatedPhotoUri = file
                                showPhotoDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", tint = BrandGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, color = Color.Black, fontSize = 13.sp)
                            }
                        }
                    }

                    TextButton(
                        onClick = { showPhotoDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// 4. TRAINING & MENTOR PAIRING SCREEN
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun TrainingScreen(
    logs: List<MentorPairingLog>,
    onAddLog: (MentorPairingLog) -> Unit
) {
    var showLogDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mentor Pairing Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGrey),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Mentor Pairing Operator CILT",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Meningkatkan skill operator mill ke Level 3",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        Icon(imageVector = Icons.Default.Groups, contentDescription = "Mentor Pairing", tint = BrandYellow, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Program pairing mempertemukan Senior Mechanic (Mentor) dengan Operator Mill (Mentee) untuk transfer skill pembersihan nozzle, pengecekan vibrasi, greasing, & tightening.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Active Operator Competency Levels (Page 22 Profile)
        item {
            Text("Pencapaian Kompetensi Operator Klarifikasi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OperatorCompetencyRow(name = "Ari (Operator Baru)", level = "Level 3", itemsMastered = "4 / 4 Aktivitas", progress = 1.0f)
                    Divider()
                    OperatorCompetencyRow(name = "Asmill (Operator)", level = "Level 3", itemsMastered = "4 / 4 Aktivitas", progress = 1.0f)
                    Divider()
                    OperatorCompetencyRow(name = "Budiman", level = "Level 2", itemsMastered = "2 / 4 Aktivitas", progress = 0.5f)
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Log Bimbingan Lapangan & Tes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                Button(
                    onClick = { showLogDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp).testTag("add_mentor_log_button")
                ) {
                    Text("+ Log Bimbingan", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        items(logs) { log ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Mentee: ${log.menteeName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
                            Text("Aktivitas: ${log.activityName}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .background(if (log.status == "Passed") BrandGreenLight else BrandRed.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                log.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (log.status == "Passed") BrandGreen else BrandRed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pre-Test: ${log.preTestScore} • Post-Test: ${log.postTestScore}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateGrey
                        )
                        Text(
                            text = "Mentor: ${log.mentorName}",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    if (log.comments.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Evaluasi: ${log.comments}",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog to log new Mentor Pairing Session
    if (showLogDialog) {
        Dialog(onDismissRequest = { showLogDialog = false }) {
            var menteeName by remember { mutableStateOf("") }
            var activityName by remember { mutableStateOf("Nozzle Clogging Detection & Cleaning") }
            var preScore by remember { mutableStateOf("") }
            var postScore by remember { mutableStateOf("") }
            var comments by remember { mutableStateOf("") }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Catat Hasil Mentor Pairing", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SlateGrey)

                    OutlinedTextField(
                        value = menteeName,
                        onValueChange = { menteeName = it },
                        label = { Text("Nama Operator (Mentee)") },
                        modifier = Modifier.fillMaxWidth().testTag("mentee_name_input")
                    )

                    Text("Aktivitas Pembelajaran (CILT):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    val activities = listOf(
                        "Nozzle Clogging Detection & Cleaning",
                        "Vibration Checking & Limit Analysis",
                        "Sludge Pit Level & Oil Recovery skimmer",
                        "Sand Cyclone Flushing & Pressure adjustment",
                        "Greasing Bearing hollow & coupling leak checks"
                    )

                    var expandedAct by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { expandedAct = !expandedAct },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftBg, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(activityName, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        DropdownMenu(expanded = expandedAct, onDismissRequest = { expandedAct = false }) {
                            activities.forEach { act ->
                                DropdownMenuItem(
                                    text = { Text(act, fontSize = 11.sp) },
                                    onClick = {
                                        activityName = act
                                        expandedAct = false
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = preScore,
                            onValueChange = { preScore = it },
                            label = { Text("Pre-Test Score") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("pre_test_input")
                        )
                        OutlinedTextField(
                            value = postScore,
                            onValueChange = { postScore = it },
                            label = { Text("Post-Test Score") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("post_test_input")
                        )
                    }

                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        label = { Text("Catatan / Tanggapan Evaluasi") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showLogDialog = false }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val pre = preScore.toIntOrNull() ?: 50
                                val post = postScore.toIntOrNull() ?: 80
                                val pass = if (post >= 70) "Passed" else "Needs Practice"

                                onAddLog(
                                    MentorPairingLog(
                                        menteeName = if (menteeName.isNotBlank()) menteeName else "Mentee Baru",
                                        activityName = activityName,
                                        preTestScore = pre,
                                        postTestScore = post,
                                        status = pass,
                                        comments = comments
                                    )
                                )
                                showLogDialog = false
                                Toast.makeText(context, "Log training berhasil disimpan!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                            modifier = Modifier.testTag("submit_training_button")
                        ) {
                            Text("Simpan", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OperatorCompetencyRow(name: String, level: String, itemsMastered: String, progress: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
            Spacer(modifier = Modifier.height(2.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = if (progress == 1.0f) BrandGreen else BrandOrange,
                trackColor = Color.LightGray.copy(alpha = 0.3f),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .background(if (progress == 1.0f) BrandGreenLight else Color(0xFFFFF3E0), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(level, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (progress == 1.0f) BrandGreen else BrandOrange)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(itemsMastered, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// 5. JAKARTA MONITORING CONSOLE (CLOUD SYNCHRONIZATION AND CONSOLIDATED KPIs)
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun JakartaScreen(
    isOnline: Boolean,
    ciltChecks: List<CiltCheck>,
    reliabilityChecks: List<ReliabilityPmCheck>,
    reports: List<AbnormalityReport>,
    mentorLogs: List<MentorPairingLog>,
    vibrationLogs: List<VibrationLog>,
    viewModel: CentrifugeViewModel
) {
    val totalUnsynced = ciltChecks.count { !it.isSynced } +
            reliabilityChecks.count { !it.isSynced } +
            reports.count { !it.isSynced } +
            mentorLogs.count { !it.isSynced } +
            vibrationLogs.count { !it.isSynced }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Jakarta HQ Cloud Connection Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGrey),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Jakarta HQ Monitoring Console",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Head Office - Wisma Triputra, Jakarta",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                        Icon(imageVector = Icons.Default.Business, contentDescription = "HQ Monitor", tint = BrandYellow, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pending Sync data (Mill Berau):", fontSize = 11.sp, color = Color.White)
                            Text("$totalUnsynced Laporan belum terunggah", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (totalUnsynced > 0) BrandYellow else Color.Green)
                        }
                        if (totalUnsynced > 0) {
                            Button(
                                onClick = { viewModel.syncData() },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandYellow),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Sync Now", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CloudDone, contentDescription = "Done", tint = Color.Green)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fully Synced", color = Color.Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Consolidated KPI Metrics (Slide 5 Target and Achievements)
        item {
            Text("Indikator Keberhasilan Project Mill (YWA Berau)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Target Improvement & Realisasi Lapangan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                    JakartaKpiProgressRow(
                        title = "Frekuensi Kerusakan Centrifuge (Slide 5)",
                        before = "8x / Bulan",
                        after = "0x / Bulan (Stabil)",
                        target = "Turun 50% (4x)",
                        achievementPercent = 1.0f
                    )
                    Divider()
                    JakartaKpiProgressRow(
                        title = "Kompetensi Operator (Level 3)",
                        before = "Maksimal Level 2",
                        after = "Ari & Asmill (Level 3)",
                        target = "100% Level 3",
                        achievementPercent = 1.0f
                    )
                    Divider()
                    JakartaKpiProgressRow(
                        title = "Realisasi PM & Greasing (CILT)",
                        before = "Belum Konsisten",
                        after = "Berjalan Tertib 100%",
                        target = "Tercapai 100%",
                        achievementPercent = 1.0f
                    )
                }
            }
        }

        // Feed of Synced Operations Reports
        item {
            Text("Live Update Laporan dari Berau Mill", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
        }

        val allSyncedCilt = ciltChecks.filter { it.isSynced }
        if (allSyncedCilt.isNotEmpty()) {
            items(allSyncedCilt) { c ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BrandGreenLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.CloudDone, contentDescription = "Synced", tint = BrandGreen)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("CILT Check oleh: ${c.operatorName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                "Nozzles Cleaned: ${if (c.nozzleCleaned) "Ya" else "Tidak"} • Greased: ${if (c.bearingGreased) "Ya" else "Tidak"}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Text(
                                "Diterima Jakarta: ${SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault()).format(Date(c.timestamp))}",
                                fontSize = 9.sp,
                                color = BrandGreen
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.CloudQueue, contentDescription = "Empty", tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada data terkirim di database Jakarta.", fontSize = 12.sp, color = Color.Gray)
                        Text("Aktifkan mode online & klik 'Kirim ke Jakarta' untuk sinkronisasi.", fontSize = 11.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun JakartaKpiProgressRow(
    title: String,
    before: String,
    after: String,
    target: String,
    achievementPercent: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sebelum: $before", fontSize = 11.sp, color = Color.Red)
            Text("Target: $target", fontSize = 11.sp, color = BrandOrange)
            Text("Realisasi: $after", fontSize = 11.sp, color = BrandGreen, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { achievementPercent },
            color = BrandGreen,
            trackColor = Color.LightGray.copy(alpha = 0.3f),
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}
