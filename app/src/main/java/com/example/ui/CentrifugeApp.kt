package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.TextStyle
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.StrokeCap
import android.graphics.Paint
import android.graphics.Typeface
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.util.WibDateUtils
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

sealed class Screen(val route: String, val title: String, val navTitle: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Monitoring & Trend Analysis SC", "Monitoring\n& Trend", Icons.Default.Analytics)
    object Checklist : Screen("checklist", "CILT", "CILT", Icons.Default.FactCheck)
    object Abnormality : Screen("abnormality", "Reliability PM", "Reliability\nPM", Icons.Default.Build)
    object Training : Screen("training", "Flushing", "Flushing", Icons.Default.WaterDrop)
    object Jakarta : Screen("jakarta", "Report Center", "Report\nCenter", Icons.Default.Assessment)
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
    val flushingLogs by viewModel.flushingLogs.collectAsStateWithLifecycle()

    var currentTimeMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
    }
    val realTimeDateText = remember(currentTimeMillis / 1000) {
        WibDateUtils.format("EEEE, dd MMMM yyyy HH:mm:ss", currentTimeMillis) + " WIB"
    }

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
                        modifier = Modifier.weight(1f),
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
                                text = "Sludge Centrifuge Center",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
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
                            text = if (isOnline) "Synced to Jakarta" else "Local Mode",
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
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                shadowElevation = 12.dp
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets
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
                            alwaysShowLabel = true,
                            label = { 
                                Text(
                                    text = screen.navTitle, 
                                    fontSize = 9.sp, 
                                    lineHeight = 11.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    minLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ) 
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandGreen,
                                selectedTextColor = BrandGreen,
                                indicatorColor = BrandGreenLight,
                                unselectedIconColor = Color(0xFF555555),
                                unselectedTextColor = Color(0xFF555555)
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
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Tanggal Real Time",
                                tint = BrandYellow,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = realTimeDateText,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF4ADE80) else Color(0xFF94A3B8))
                            )
                            Text(
                                text = syncStatus
                                    .replace("(Offline Mode) - ", "")
                                    .replace("(Offline Mode)", "")
                                    .replace("(Online Mode)", "")
                                    .trim(),
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
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
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .testTag("sync_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sync\nCloud",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
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
                        onUpdateReport = { viewModel.updateAbnormalityReport(it) },
                        onDeleteReport = { viewModel.deleteReport(it) }
                    )
                    Screen.Training -> FlushingScreen(
                        flushingLogs = flushingLogs,
                        onAddFlushingLog = { viewModel.addFlushingLog(it) },
                        onDeleteFlushingLog = { viewModel.deleteFlushingLog(it) },
                        mentorLogs = mentorLogs,
                        onAddMentorLog = { viewModel.addMentorLog(it) }
                    )
                    Screen.Jakarta -> JakartaScreen(
                        isOnline = isOnline,
                        ciltChecks = ciltChecks,
                        reliabilityChecks = reliabilityChecks,
                        reports = abnormalityReports,
                        mentorLogs = mentorLogs,
                        vibrationLogs = vibrationLogs,
                        flushingLogs = flushingLogs,
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
data class CentrifugeUnitState(
    val id: String,
    val name: String,
    val isRunning: Boolean,
    val baseDeVib: Float,
    val baseNdeVib: Float,
    val baseMotorVib: Float = 1.9f,
    val baseGearboxVib: Float = 2.1f,
    val baseBowlVib: Float = 1.6f,
    val baseBearingTemp: Float,
    val baseMotorTemp: Float,
    val warningVib: Float = 4.5f,
    val criticalVib: Float = 8.8f,
    val warningTemp: Float = 65.0f,
    val criticalTemp: Float = 75.0f,
    val devVibHistory: List<Float> = emptyList(),
    val ndevVibHistory: List<Float> = emptyList(),
    val bearingTempHistory: List<Float> = emptyList(),
    val weeklyDevVibHistory: List<Float> = emptyList(),
    val weeklyNdevVibHistory: List<Float> = emptyList(),
    val weeklyBearingTempHistory: List<Float> = emptyList(),
    val monthlyDevVibHistory: List<Float> = emptyList(),
    val monthlyNdevVibHistory: List<Float> = emptyList(),
    val monthlyBearingTempHistory: List<Float> = emptyList(),
    val greasingHistory: List<Float> = emptyList(),
    val weeklyGreasingHistory: List<Float> = emptyList(),
    val monthlyGreasingHistory: List<Float> = emptyList()
)

data class UnitReportValues(
    val dev: Float?,
    val nde: Float?,
    val motorVib: Float?,
    val gearboxVib: Float?,
    val bowlVib: Float?,
    val bearingTemp: Float?,
    val motorTemp: Float?,
    val isGreased: Boolean? = null,
    val soundState: String? = null,
    val hasLeakage: Boolean? = null,
    val greasingRatioText: String? = null,
    val soundRatioText: String? = null,
    val leakageRatioText: String? = null
)

data class UnitSummaryMetrics(
    val avgVib: Float,
    val avgTemp: Float,
    val alarmStatus: String,
    val reportValues: UnitReportValues,
    val isMeasuredToday: Boolean,
    val latestTodayLog: VibrationLog?,
    val todayLogCount: Int = 0
)

data class MonthlyWeekInterval(
    val index: Int,
    val title: String,
    val shortTitle: String,
    val dateRangeStr: String,
    val startMillis: Long,
    val endMillis: Long
)

fun getMonthlyWeekIntervals(): List<MonthlyWeekInterval> {
    return List(4) { i ->
        val startOffset = when (i) {
            0 -> -27
            1 -> -20
            2 -> -13
            else -> -6
        }
        val endOffset = when (i) {
            0 -> -21
            1 -> -14
            2 -> -7
            else -> 0
        }
        val calStart = WibDateUtils.getCalendar().apply {
            add(Calendar.DAY_OF_YEAR, startOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calEnd = WibDateUtils.getCalendar().apply {
            add(Calendar.DAY_OF_YEAR, endOffset)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val weekTitle = when (i) {
            0 -> "3 Mg Lalu"
            1 -> "2 Mg Lalu"
            2 -> "1 Mg Lalu"
            else -> "Minggu Ini"
        }
        val shortTitle = when (i) {
            0 -> "Minggu 1"
            1 -> "Minggu 2"
            2 -> "Minggu 3"
            else -> "Minggu Ini"
        }
        val dateRangeStr = "${WibDateUtils.format("dd/MM", calStart.time)} - ${WibDateUtils.format("dd/MM", calEnd.time)}"
        MonthlyWeekInterval(i, weekTitle, shortTitle, dateRangeStr, calStart.timeInMillis, calEnd.timeInMillis)
    }
}

fun getUnitSummaryMetrics(
    unit: CentrifugeUnitState,
    timeRangeSelection: Int, // 0: Hari Ini, 1: Seminggu, 2: Sebulan, 3: Tanggal
    vibrationLogs: List<VibrationLog>,
    startDateMillis: Long = 0L,
    endDateMillis: Long = Long.MAX_VALUE
): UnitSummaryMetrics {
    val now = WibDateUtils.getCalendar()
    val cal = WibDateUtils.getCalendar().apply {
        if (now.get(Calendar.HOUR_OF_DAY) < 7) {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        set(Calendar.HOUR_OF_DAY, 7)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfToday = cal.timeInMillis
    val endOfToday = startOfToday + 24 * 60 * 60 * 1000L

    val calMidnight = WibDateUtils.getCalendar().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val startOfMidnight = calMidnight.timeInMillis
    val endOfMidnight = startOfMidnight + 24 * 60 * 60 * 1000L

    val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000L
    val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L

    val unitLogs = vibrationLogs.filter { log ->
        log.comments.contains("[${unit.id}]") || log.comments.contains(unit.id) || log.comments.contains(unit.id.replace("-", " "))
    }
    val todayLogs = unitLogs.filter { (it.timestamp in startOfToday..endOfToday) || (it.timestamp in startOfMidnight..endOfMidnight) }
    val isMeasuredToday = todayLogs.isNotEmpty()
    val latestTodayLog = todayLogs.maxByOrNull { it.timestamp }
    val weeklyLogs = unitLogs.filter { it.timestamp >= sevenDaysAgo }
    val monthlyLogs = unitLogs.filter { it.timestamp >= thirtyDaysAgo }

    val baseDe = unit.baseDeVib
    val baseNde = unit.baseNdeVib
    val baseMotor = unit.baseMotorVib
    val baseGearbox = unit.baseGearboxVib
    val baseBowl = unit.baseBowlVib
    val baseBearing = unit.baseBearingTemp
    val baseMotorTemp = unit.baseMotorTemp

    val reportValues: UnitReportValues
    val avgVib: Float
    val avgTemp: Float

    when (timeRangeSelection) {
        0 -> { // Harian
            if (isMeasuredToday && todayLogs.isNotEmpty()) {
                // Sudah ada diambil datanya -> dihitung dengan rata-rata nilainya
                val deAvg = todayLogs.map { it.driveEndVibration }.average().toFloat()
                val ndeAvg = todayLogs.map { it.nonDriveEndVibration }.average().toFloat()
                val mAvg = todayLogs.map { it.motorBearingVibration }.average().toFloat()
                val gAvg = todayLogs.map { it.gearboxBearingVibration }.average().toFloat()
                val bAvg = todayLogs.map { it.bowlVibration }.average().toFloat()
                val bTempAvg = todayLogs.map { it.bearingTemp }.average().toFloat()
                val mTempAvg = todayLogs.map { it.motorTemp }.average().toFloat()

                val isGreased = latestTodayLog?.isGreased ?: true
                val greasingStatus = latestTodayLog?.greasingStatus ?: (if (isGreased) "Ya" else "Tidak")
                val soundState = latestTodayLog?.soundState ?: "Normal"
                val hasLeakage = latestTodayLog?.hasLeakage ?: false

                reportValues = UnitReportValues(
                    dev = deAvg, nde = ndeAvg, motorVib = mAvg, gearboxVib = gAvg, bowlVib = bAvg,
                    bearingTemp = bTempAvg, motorTemp = mTempAvg,
                    isGreased = if (greasingStatus == "Belum Masuk Jadwal") true else isGreased,
                    soundState = soundState,
                    hasLeakage = hasLeakage,
                    greasingRatioText = when {
                        greasingStatus == "Belum Masuk Jadwal" -> "Belum Masuk Jadwal"
                        isGreased -> "Sudah Dilakukan"
                        else -> "Belum Dilakukan"
                    },
                    soundRatioText = soundState,
                    leakageRatioText = if (hasLeakage) "Ada Kebocoran" else "Tidak Ada"
                )
                avgVib = listOf(deAvg, ndeAvg, mAvg, gAvg, bAvg).average().toFloat()
                avgTemp = listOf(bTempAvg, mTempAvg).average().toFloat()
            } else {
                // Belum ada dilakukan pengukuran -> nilai yang ada di masing-masing SC semuanya nol
                reportValues = UnitReportValues(
                    dev = 0.0f, nde = 0.0f, motorVib = 0.0f, gearboxVib = 0.0f, bowlVib = 0.0f,
                    bearingTemp = 0.0f, motorTemp = 0.0f,
                    isGreased = false, soundState = "-", hasLeakage = false,
                    greasingRatioText = "Belum Ada Pengukuran",
                    soundRatioText = "Belum Ada Pengukuran",
                    leakageRatioText = "Belum Ada Pengukuran"
                )
                avgVib = 0.0f
                avgTemp = 0.0f
            }
        }
        1 -> { // Seminggu (7 Hari Terakhir)
            if (weeklyLogs.isNotEmpty()) {
                val deAvg = weeklyLogs.map { it.driveEndVibration }.average().toFloat()
                val ndeAvg = weeklyLogs.map { it.nonDriveEndVibration }.average().toFloat()
                val mAvg = weeklyLogs.map { it.motorBearingVibration }.average().toFloat()
                val gAvg = weeklyLogs.map { it.gearboxBearingVibration }.average().toFloat()
                val bAvg = weeklyLogs.map { it.bowlVibration }.average().toFloat()
                val bTempAvg = weeklyLogs.map { it.bearingTemp }.average().toFloat()
                val mTempAvg = weeklyLogs.map { it.motorTemp }.average().toFloat()

                val greasedCount = weeklyLogs.count { it.isGreased }
                val total = weeklyLogs.size
                val abnormalCount = weeklyLogs.count { it.soundState == "Abnormal" }
                val leakCount = weeklyLogs.count { it.hasLeakage }

                reportValues = UnitReportValues(
                    dev = deAvg, nde = ndeAvg, motorVib = mAvg, gearboxVib = gAvg, bowlVib = bAvg,
                    bearingTemp = bTempAvg, motorTemp = mTempAvg,
                    isGreased = greasedCount >= (total * 0.7),
                    soundState = if (abnormalCount == 0) "Normal" else "Abnormal ($abnormalCount)",
                    hasLeakage = leakCount > 0,
                    greasingRatioText = "$greasedCount / $total hari",
                    soundRatioText = if (abnormalCount == 0) "Normal (100%)" else "$abnormalCount temuan",
                    leakageRatioText = if (leakCount == 0) "Nihil (0)" else "$leakCount temuan"
                )
                avgVib = listOf(deAvg, ndeAvg, mAvg, gAvg, bAvg).average().toFloat()
                avgTemp = listOf(bTempAvg, mTempAvg).average().toFloat()
            } else if (unit.isRunning) {
                val avgDe = if (unit.weeklyDevVibHistory.isNotEmpty()) unit.weeklyDevVibHistory.average().toFloat() else baseDe
                val avgNde = if (unit.weeklyNdevVibHistory.isNotEmpty()) unit.weeklyNdevVibHistory.average().toFloat() else baseNde
                val avgMotor = (avgDe * 0.72f).coerceAtLeast(0.5f)
                val avgGearbox = (avgDe * 0.78f).coerceAtLeast(0.5f)
                val avgBowl = (avgDe * 0.60f).coerceAtLeast(0.5f)
                val avgBearing = if (unit.weeklyBearingTempHistory.isNotEmpty()) unit.weeklyBearingTempHistory.average().toFloat() else baseBearing

                reportValues = UnitReportValues(
                    dev = avgDe, nde = avgNde, motorVib = avgMotor, gearboxVib = avgGearbox, bowlVib = avgBowl,
                    bearingTemp = avgBearing, motorTemp = baseMotorTemp,
                    isGreased = true,
                    soundState = "Normal",
                    hasLeakage = false,
                    greasingRatioText = "7 / 7 hari (100%)",
                    soundRatioText = "Normal (100%)",
                    leakageRatioText = "Nihil (0)"
                )
                avgVib = listOf(avgDe, avgNde, avgMotor, avgGearbox, avgBowl).average().toFloat()
                avgTemp = listOf(avgBearing, baseMotorTemp).average().toFloat()
            } else {
                reportValues = UnitReportValues(
                    0f, 0f, 0f, 0f, 0f, 0f, 0f,
                    isGreased = false, soundState = "Standby", hasLeakage = false,
                    greasingRatioText = "Standby", soundRatioText = "Standby", leakageRatioText = "Nihil"
                )
                avgVib = 0.0f
                avgTemp = 0.0f
            }
        }
        2 -> { // Sebulan (30 Hari Terakhir)
            if (monthlyLogs.isNotEmpty()) {
                val deAvg = monthlyLogs.map { it.driveEndVibration }.average().toFloat()
                val ndeAvg = monthlyLogs.map { it.nonDriveEndVibration }.average().toFloat()
                val mAvg = monthlyLogs.map { it.motorBearingVibration }.average().toFloat()
                val gAvg = monthlyLogs.map { it.gearboxBearingVibration }.average().toFloat()
                val bAvg = monthlyLogs.map { it.bowlVibration }.average().toFloat()
                val bTempAvg = monthlyLogs.map { it.bearingTemp }.average().toFloat()
                val mTempAvg = monthlyLogs.map { it.motorTemp }.average().toFloat()

                val greasedCount = monthlyLogs.count { it.isGreased }
                val total = monthlyLogs.size
                val abnormalCount = monthlyLogs.count { it.soundState == "Abnormal" }
                val leakCount = monthlyLogs.count { it.hasLeakage }

                reportValues = UnitReportValues(
                    dev = deAvg, nde = ndeAvg, motorVib = mAvg, gearboxVib = gAvg, bowlVib = bAvg,
                    bearingTemp = bTempAvg, motorTemp = mTempAvg,
                    isGreased = greasedCount >= (total * 0.7),
                    soundState = if (abnormalCount == 0) "Normal" else "Abnormal ($abnormalCount)",
                    hasLeakage = leakCount > 0,
                    greasingRatioText = "$greasedCount / $total hari",
                    soundRatioText = if (abnormalCount == 0) "Normal (100%)" else "$abnormalCount temuan",
                    leakageRatioText = if (leakCount == 0) "Nihil (0)" else "$leakCount temuan"
                )
                avgVib = listOf(deAvg, ndeAvg, mAvg, gAvg, bAvg).average().toFloat()
                avgTemp = listOf(bTempAvg, mTempAvg).average().toFloat()
            } else if (unit.isRunning) {
                val avgDe = if (unit.monthlyDevVibHistory.isNotEmpty()) unit.monthlyDevVibHistory.average().toFloat() else baseDe
                val avgNde = if (unit.monthlyNdevVibHistory.isNotEmpty()) unit.monthlyNdevVibHistory.average().toFloat() else baseNde
                val avgMotor = (avgDe * 0.72f).coerceAtLeast(0.5f)
                val avgGearbox = (avgDe * 0.78f).coerceAtLeast(0.5f)
                val avgBowl = (avgDe * 0.60f).coerceAtLeast(0.5f)
                val avgBearing = if (unit.monthlyBearingTempHistory.isNotEmpty()) unit.monthlyBearingTempHistory.average().toFloat() else baseBearing

                reportValues = UnitReportValues(
                    dev = avgDe, nde = avgNde, motorVib = avgMotor, gearboxVib = avgGearbox, bowlVib = avgBowl,
                    bearingTemp = avgBearing, motorTemp = baseMotorTemp,
                    isGreased = true,
                    soundState = "Normal",
                    hasLeakage = false,
                    greasingRatioText = "30 / 30 hari (100%)",
                    soundRatioText = "Normal (100%)",
                    leakageRatioText = "Nihil (0)"
                )
                avgVib = listOf(avgDe, avgNde, avgMotor, avgGearbox, avgBowl).average().toFloat()
                avgTemp = listOf(avgBearing, baseMotorTemp).average().toFloat()
            } else {
                reportValues = UnitReportValues(
                    0f, 0f, 0f, 0f, 0f, 0f, 0f,
                    isGreased = false, soundState = "Standby", hasLeakage = false,
                    greasingRatioText = "Standby", soundRatioText = "Standby", leakageRatioText = "Nihil"
                )
                avgVib = 0.0f
                avgTemp = 0.0f
            }
        }
        else -> { // 3: Tanggal Kustom
            val customLogs = unitLogs.filter { it.timestamp in startDateMillis..endDateMillis }
            if (customLogs.isNotEmpty()) {
                val deAvg = customLogs.map { it.driveEndVibration }.average().toFloat()
                val ndeAvg = customLogs.map { it.nonDriveEndVibration }.average().toFloat()
                val mAvg = customLogs.map { it.motorBearingVibration }.average().toFloat()
                val gAvg = customLogs.map { it.gearboxBearingVibration }.average().toFloat()
                val bAvg = customLogs.map { it.bowlVibration }.average().toFloat()
                val bTempAvg = customLogs.map { it.bearingTemp }.average().toFloat()
                val mTempAvg = customLogs.map { it.motorTemp }.average().toFloat()

                val greasedCount = customLogs.count { it.isGreased }
                val total = customLogs.size
                val abnormalCount = customLogs.count { it.soundState == "Abnormal" }
                val leakCount = customLogs.count { it.hasLeakage }

                reportValues = UnitReportValues(
                    dev = deAvg, nde = ndeAvg, motorVib = mAvg, gearboxVib = gAvg, bowlVib = bAvg,
                    bearingTemp = bTempAvg, motorTemp = mTempAvg,
                    isGreased = greasedCount >= (total * 0.7),
                    soundState = if (abnormalCount == 0) "Normal" else "Abnormal ($abnormalCount)",
                    hasLeakage = leakCount > 0,
                    greasingRatioText = "$greasedCount / $total pengukuran",
                    soundRatioText = if (abnormalCount == 0) "Normal (100%)" else "$abnormalCount temuan",
                    leakageRatioText = if (leakCount == 0) "Nihil (0)" else "$leakCount temuan"
                )
                avgVib = listOf(deAvg, ndeAvg, mAvg, gAvg, bAvg).average().toFloat()
                avgTemp = listOf(bTempAvg, mTempAvg).average().toFloat()
            } else {
                reportValues = UnitReportValues(
                    dev = 0.0f, nde = 0.0f, motorVib = 0.0f, gearboxVib = 0.0f, bowlVib = 0.0f,
                    bearingTemp = 0.0f, motorTemp = 0.0f,
                    isGreased = false, soundState = "-", hasLeakage = false,
                    greasingRatioText = "Belum Ada Pengukuran",
                    soundRatioText = "Belum Ada Pengukuran",
                    leakageRatioText = "Belum Ada Pengukuran"
                )
                avgVib = 0.0f
                avgTemp = 0.0f
            }
        }
    }

    val customLogsCount = if (timeRangeSelection == 3) unitLogs.count { it.timestamp in startDateMillis..endDateMillis } else 0
    val alarmStatus = if (timeRangeSelection == 0 && !isMeasuredToday) {
        if (!unit.isRunning) "STANDBY" else "UNMEASURED"
    } else if (timeRangeSelection == 3 && customLogsCount == 0) {
        if (!unit.isRunning) "STANDBY" else "UNMEASURED"
    } else if (!unit.isRunning && weeklyLogs.isEmpty() && monthlyLogs.isEmpty() && customLogsCount == 0) {
        "STANDBY"
    } else if (avgVib >= unit.criticalVib || avgTemp >= unit.criticalTemp || (reportValues.dev ?: 0f) >= unit.criticalVib || (reportValues.bearingTemp ?: 0f) >= unit.criticalTemp) {
        "CRITICAL"
    } else if (avgVib >= unit.warningVib || avgTemp >= unit.warningTemp || (reportValues.dev ?: 0f) >= unit.warningVib || (reportValues.bearingTemp ?: 0f) >= unit.warningTemp || (reportValues.hasLeakage == true)) {
        "WARNING"
    } else {
        "NORMAL"
    }

    return UnitSummaryMetrics(
        avgVib = avgVib,
        avgTemp = avgTemp,
        alarmStatus = alarmStatus,
        reportValues = reportValues,
        isMeasuredToday = isMeasuredToday,
        latestTodayLog = latestTodayLog,
        todayLogCount = todayLogs.size
    )
}

@Composable
fun DateRangeFilterPicker(
    startDateMillis: Long,
    endDateMillis: Long,
    onPickStartDate: () -> Unit,
    onPickEndDate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tanggal Mulai
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable { onPickStartDate() },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TANGGAL MULAI",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )
                    Text(
                        text = WibDateUtils.format("dd MMM yyyy", startDateMillis),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Pilih Tanggal Mulai",
                    tint = BrandGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(14.dp)
        )

        // Tanggal Akhir
        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable { onPickEndDate() },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "TANGGAL AKHIR",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )
                    Text(
                        text = WibDateUtils.format("dd MMM yyyy", endDateMillis),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Pilih Tanggal Akhir",
                    tint = BrandGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ReportRowItem(
    number: String,
    label: String,
    value: Float?,
    unit: String,
    threshold: Float,
    isDegree: Boolean = false,
    infoText: String? = null
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog && infoText != null) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Informasi Titik Ukur",
                    tint = BrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Titik Ukur: $label",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateGrey
                )
            },
            text = {
                Text(
                    text = infoText,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B),
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold, color = BrandGreen)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    val isZeroOrNull = value == null || value == 0f
    val isRed = !isZeroOrNull && (value!! > threshold)
    val isGreen = !isZeroOrNull && (value!! <= threshold)

    val bgColor = if (isZeroOrNull) Color(0xFFF1F5F9) else if (isRed) Color(0xFFFFCDD2) else Color(0xFFC8E6C9)
    val borderColor = if (isZeroOrNull) Color(0xFFCBD5E1) else if (isRed) Color(0xFFD32F2F) else Color(0xFF2E7D32)
    val textColor = if (isZeroOrNull) Color.Gray else if (isRed) Color(0xFFB71C1C) else Color(0xFF1B5E20)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.width(135.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$number. $label :",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateGrey,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (infoText != null) {
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(
                    onClick = { showInfoDialog = true },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info $label",
                        tint = BrandGreen,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .width(84.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (value != null) String.format(Locale.US, "%.2f", value) else "0.00",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        if (isDegree) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Superscript)) {
                        append("o")
                    }
                    append("C")
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRed) Color(0xFFB71C1C) else if (isGreen) Color(0xFF1B5E20) else Color.Gray
            )
        } else {
            Text(
                text = unit,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isRed) Color(0xFFB71C1C) else if (isGreen) Color(0xFF1B5E20) else Color.Gray
            )
        }
    }
}

@Composable
fun ReportStatusRowItem(
    number: String,
    label: String,
    valueText: String?,
    isGood: Boolean?
) {
    val isMeasured = valueText != null && isGood != null
    val bgColor = if (!isMeasured) Color(0xFFF1F5F9) else if (isGood == true) Color(0xFFC8E6C9) else Color(0xFFFFCDD2)
    val borderColor = if (!isMeasured) Color(0xFFCBD5E1) else if (isGood == true) Color(0xFF2E7D32) else Color(0xFFD32F2F)
    val textColor = if (!isMeasured) Color.Gray else if (isGood == true) Color(0xFF1B5E20) else Color(0xFFB71C1C)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$number. $label :",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlateGrey,
            modifier = Modifier.width(135.dp)
        )
        Box(
            modifier = Modifier
                .width(140.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = valueText ?: "-",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CentrifugeTrendCanvas(
    modifier: Modifier = Modifier,
    trendTabSelection: Int,
    historyPoints: List<Float>,
    devVibPoints: List<Float>,
    ndevVibPoints: List<Float>,
    bearingTempPoints: List<Float>,
    greasingPoints: List<Float>,
    selectedUnit: CentrifugeUnitState,
    timeRangeSelection: Int,
    isEnlarged: Boolean = false
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val points = historyPoints.size
        val spacing = if (points > 1) width / (points - 1) else 0f
        val getX: (Int) -> Float = { idx ->
            if (points <= 1) width / 2f else idx * spacing
        }

        // Horizontal Gridlines for enlarged mode
        if (isEnlarged) {
            val gridCount = 4
            for (g in 1..gridCount) {
                val yG = height * (g.toFloat() / (gridCount + 1))
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.28f),
                    start = Offset(0f, yG),
                    end = Offset(width, yG),
                    strokeWidth = 1f
                )
            }
        }

        if (trendTabSelection == 0) {
            // Vibration Chart (Max 10 mm/s)
            val maxScale = 10f
            val warningY = height - (selectedUnit.warningVib / maxScale) * height
            val criticalY = height - (selectedUnit.criticalVib / maxScale) * height

            // Warning threshold limit line
            drawLine(
                color = BrandYellow.copy(alpha = 0.6f),
                start = Offset(0f, warningY),
                end = Offset(width, warningY),
                strokeWidth = 2f
            )
            // Critical threshold limit line (Merah)
            drawLine(
                color = BrandRed.copy(alpha = 0.75f),
                start = Offset(0f, criticalY),
                end = Offset(width, criticalY),
                strokeWidth = 3f
            )

            // Plot DE Vibration Line Segments with dynamic Critical Red coloring
            for (index in 1 until devVibPoints.size) {
                val prevVib = devVibPoints[index - 1]
                val currVib = devVibPoints[index]
                val prevX = getX(index - 1)
                val prevY = (height - (prevVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                val currX = getX(index)
                val currY = (height - (currVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)

                val isSegCritical = currVib >= selectedUnit.criticalVib || prevVib >= selectedUnit.criticalVib
                val isSegWarning = currVib >= selectedUnit.warningVib || prevVib >= selectedUnit.warningVib
                val segColor = if (isSegCritical) BrandRed else if (isSegWarning) BrandOrange else BrandGreen
                val strokeW = if (isSegCritical) 5.5f else 4f

                drawLine(
                    color = segColor,
                    start = Offset(prevX, prevY),
                    end = Offset(currX, currY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // Plot DE Vibration Dots
            devVibPoints.forEachIndexed { index, valVib ->
                val x = getX(index)
                val y = (height - (valVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                val isCritical = valVib >= selectedUnit.criticalVib
                val isWarning = valVib >= selectedUnit.warningVib
                val dotColor = if (isCritical) BrandRed else if (isWarning) BrandOrange else BrandGreen
                val dotRadius = if (isCritical) 6.5f else 4.5f

                if (isCritical) {
                    drawCircle(
                        color = BrandRed.copy(alpha = 0.35f),
                        radius = 11f,
                        center = Offset(x, y)
                    )
                }
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }

            // Plot NDE Vibration Line Segments (Thickened Grey Line)
            for (index in 1 until ndevVibPoints.size) {
                val prevVib = ndevVibPoints[index - 1]
                val currVib = ndevVibPoints[index]
                val prevX = getX(index - 1)
                val prevY = (height - (prevVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                val currX = getX(index)
                val currY = (height - (currVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)

                val isCritical = currVib >= selectedUnit.criticalVib || prevVib >= selectedUnit.criticalVib
                val isWarning = currVib >= selectedUnit.warningVib || prevVib >= selectedUnit.warningVib
                val segColor = if (isCritical) BrandRed else if (isWarning) BrandOrange.copy(alpha = 0.9f) else Color(0xFF64748B)
                val strokeW = if (isCritical) (if (isEnlarged) 7f else 5.5f) else (if (isEnlarged) 5.5f else 4.5f)

                drawLine(
                    color = segColor,
                    start = Offset(prevX, prevY),
                    end = Offset(currX, currY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // Plot NDE Vibration Dots
            ndevVibPoints.forEachIndexed { index, valNde ->
                val x = getX(index)
                val y = (height - (valNde / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                val isCritical = valNde >= selectedUnit.criticalVib
                val isWarning = valNde >= selectedUnit.warningVib
                val dotColor = if (isCritical) BrandRed else if (isWarning) BrandOrange else Color(0xFF64748B)
                val dotRadius = if (isCritical) (if (isEnlarged) 8f else 6.5f) else (if (isEnlarged) 5.5f else 4.5f)

                if (isCritical) {
                    drawCircle(
                        color = BrandRed.copy(alpha = 0.35f),
                        radius = dotRadius * 1.7f,
                        center = Offset(x, y)
                    )
                }
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }

            // Draw Vibration measurement numbers
            drawIntoCanvas { canvas ->
                val paintCritical = Paint().apply {
                    color = android.graphics.Color.rgb(186, 26, 26) // BrandRed
                    textSize = if (isEnlarged) 28f else 25f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                val paintWarning = Paint().apply {
                    color = android.graphics.Color.rgb(245, 124, 0) // BrandOrange
                    textSize = if (isEnlarged) 26f else 24f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                val paintDe = Paint().apply {
                    color = android.graphics.Color.rgb(0, 106, 106) // BrandGreen
                    textSize = if (isEnlarged) 26f else 24f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                val paintNde = Paint().apply {
                    color = android.graphics.Color.rgb(71, 85, 105) // SlateGrey / Dark Grey
                    textSize = if (isEnlarged) 22f else 20f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

                devVibPoints.forEachIndexed { index, valVib ->
                    val x = getX(index)
                    val yDe = (height - (valVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                    val labelDe = String.format(Locale.US, "%.1f", valVib)
                    val p = if (valVib >= selectedUnit.criticalVib) paintCritical else if (valVib >= selectedUnit.warningVib) paintWarning else paintDe
                    canvas.nativeCanvas.drawText(labelDe, x, (yDe - 10f).coerceAtLeast(20f), p)
                }

                ndevVibPoints.forEachIndexed { index, valNde ->
                    val x = getX(index)
                    val yNde = (height - (valNde / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                    val labelNde = String.format(Locale.US, "%.1f", valNde)
                    val p = if (valNde >= selectedUnit.criticalVib) paintCritical else paintNde
                    canvas.nativeCanvas.drawText(labelNde, x, (yNde + 22f).coerceAtMost(height - 4f), p)
                }
            }
        } else if (trendTabSelection == 1) {
            // Temperature Chart (Max 100 °C)
            val maxScale = 100f
            val warningY = height - (selectedUnit.warningTemp / maxScale) * height
            val criticalY = height - (selectedUnit.criticalTemp / maxScale) * height

            // Warning Temp threshold line
            drawLine(
                color = BrandYellow.copy(alpha = 0.6f),
                start = Offset(0f, warningY),
                end = Offset(width, warningY),
                strokeWidth = 2f
            )
            // Critical Temp threshold line (Merah)
            drawLine(
                color = BrandRed.copy(alpha = 0.75f),
                start = Offset(0f, criticalY),
                end = Offset(width, criticalY),
                strokeWidth = 3f
            )

            // Plot Bearing Temp Line Segments with dynamic Critical Red coloring
            for (index in 1 until bearingTempPoints.size) {
                val prevTemp = bearingTempPoints[index - 1]
                val currTemp = bearingTempPoints[index]
                val prevX = getX(index - 1)
                val prevY = (height - (prevTemp / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                val currX = getX(index)
                val currY = (height - (currTemp / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)

                val isSegCritical = currTemp >= selectedUnit.criticalTemp || prevTemp >= selectedUnit.criticalTemp
                val isSegWarning = currTemp >= selectedUnit.warningTemp || prevTemp >= selectedUnit.warningTemp
                val segColor = if (isSegCritical) BrandRed else if (isSegWarning) BrandOrange else BrandGreen
                val strokeW = if (isSegCritical) 5.5f else 4f

                drawLine(
                    color = segColor,
                    start = Offset(prevX, prevY),
                    end = Offset(currX, currY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // Plot Bearing Temp Dots
            bearingTempPoints.forEachIndexed { index, valTemp ->
                val x = getX(index)
                val y = (height - (valTemp / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                val isCritical = valTemp >= selectedUnit.criticalTemp
                val isWarning = valTemp >= selectedUnit.warningTemp
                val dotColor = if (isCritical) BrandRed else if (isWarning) BrandOrange else BrandGreen
                val dotRadius = if (isCritical) 6.5f else 4.5f

                if (isCritical) {
                    drawCircle(
                        color = BrandRed.copy(alpha = 0.35f),
                        radius = 11f,
                        center = Offset(x, y)
                    )
                }
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }

            // Draw Temperature measurement numbers
            drawIntoCanvas { canvas ->
                val paintTempCritical = Paint().apply {
                    color = android.graphics.Color.rgb(186, 26, 26) // BrandRed
                    textSize = if (isEnlarged) 28f else 25f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                val paintTempWarning = Paint().apply {
                    color = android.graphics.Color.rgb(245, 124, 0) // BrandOrange
                    textSize = if (isEnlarged) 26f else 24f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                val paintTempNormal = Paint().apply {
                    color = android.graphics.Color.rgb(0, 106, 106) // BrandGreen
                    textSize = if (isEnlarged) 26f else 24f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

                bearingTempPoints.forEachIndexed { index, valTemp ->
                    val x = getX(index)
                    val y = (height - (valTemp / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                    val labelTemp = "${String.format(Locale.US, "%.1f", valTemp)}°"
                    val p = if (valTemp >= selectedUnit.criticalTemp) paintTempCritical else if (valTemp >= selectedUnit.warningTemp) paintTempWarning else paintTempNormal
                    canvas.nativeCanvas.drawText(labelTemp, x, (y - 10f).coerceAtLeast(20f), p)
                }
            }
        } else {
            // Greasing Chart (0% - 100%)
            val maxScale = 100f
            val standardY = height - (100f / maxScale).coerceIn(0f, 1f) * height + 16f
            val criticalY = height - (50f / maxScale).coerceIn(0f, 1f) * height

            // Target 100% threshold line (Green)
            drawLine(
                color = BrandGreen.copy(alpha = 0.5f),
                start = Offset(0f, standardY),
                end = Offset(width, standardY),
                strokeWidth = 2f
            )
            // Critical Greasing threshold line (Red, < 50%)
            drawLine(
                color = BrandRed.copy(alpha = 0.75f),
                start = Offset(0f, criticalY),
                end = Offset(width, criticalY),
                strokeWidth = 2f
            )

            // Plot Greasing Line Segments with dynamic Critical Red coloring
            for (index in 1 until greasingPoints.size) {
                val prevGreas = greasingPoints[index - 1]
                val currGreas = greasingPoints[index]
                val prevX = getX(index - 1)
                val prevY = (height - (prevGreas / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                val currX = getX(index)
                val currY = (height - (currGreas / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)

                val isSegCritical = currGreas < 50f || prevGreas < 50f
                val segColor = if (isSegCritical) BrandRed else BrandGreen
                val strokeW = if (isSegCritical) 5.5f else 4f

                drawLine(
                    color = segColor,
                    start = Offset(prevX, prevY),
                    end = Offset(currX, currY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // Plot Greasing Dots and vertical pillars
            greasingPoints.forEachIndexed { index, valGreas ->
                val x = getX(index)
                val y = (height - (valGreas / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                val isCritical = valGreas < 50f
                val dotColor = if (isCritical) BrandRed else BrandGreen
                val dotRadius = if (isCritical) 6.5f else 5f

                // Subtle vertical guideline to baseline
                drawLine(
                    color = dotColor.copy(alpha = 0.25f),
                    start = Offset(x, y),
                    end = Offset(x, height - 8f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )

                if (isCritical) {
                    drawCircle(
                        color = BrandRed.copy(alpha = 0.35f),
                        radius = 11f,
                        center = Offset(x, y)
                    )
                }
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }

            // Draw Greasing status labels along the line
            drawIntoCanvas { canvas ->
                val paintGreasCritical = Paint().apply {
                    color = android.graphics.Color.rgb(186, 26, 26) // BrandRed
                    textSize = if (isEnlarged) 24f else 21f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                val paintGreasNormal = Paint().apply {
                    color = android.graphics.Color.rgb(0, 106, 106) // BrandGreen
                    textSize = if (isEnlarged) 24f else 21f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }

                greasingPoints.forEachIndexed { index, valGreas ->
                    val x = getX(index)
                    val y = (height - (valGreas / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                    val isCritical = valGreas < 50f
                    val labelGreas = if (timeRangeSelection == 2) {
                        if (valGreas >= 50f) "${valGreas.toInt()}% OK" else "${valGreas.toInt()}%"
                    } else {
                        if (isCritical) "0% Belum" else "100% OK"
                    }
                    val p = if (isCritical) paintGreasCritical else paintGreasNormal
                    canvas.nativeCanvas.drawText(labelGreas, x, (y - 10f).coerceAtLeast(20f), p)
                }
            }
        }
    }
}

@Composable
fun TrendChartXAxisLabels(
    timeRangeSelection: Int,
    todayUnitLogs: List<VibrationLog>,
    monthlyWeekIntervals: List<MonthlyWeekInterval>,
    customUnitLogs: List<VibrationLog>,
    customStartDate: Long,
    customEndDate: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        when (timeRangeSelection) {
            0 -> {
                // Hari Ini: Menampilkan waktu input di hari ini
                if (todayUnitLogs.isNotEmpty()) {
                    if (todayUnitLogs.size == 1) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Pengukuran: ${WibDateUtils.format("HH:mm", todayUnitLogs[0].timestamp)} WIB (Hari Ini)",
                                fontSize = 9.sp,
                                color = SlateGrey,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        todayUnitLogs.forEachIndexed { idx, log ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Input #${idx + 1}",
                                    fontSize = 8.5.sp,
                                    color = SlateGrey,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${WibDateUtils.format("HH:mm", log.timestamp)} WIB",
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
            1 -> {
                // Seminggu: 7 hari kebelakang (H-6 s/d Hari Ini dalam WIB)
                val weeklyLabels = remember {
                    List(7) { i ->
                        val offsetDays = i - 6
                        val cal = WibDateUtils.getCalendar().apply {
                            add(Calendar.DAY_OF_YEAR, offsetDays)
                        }
                        val dayName = when (cal.get(Calendar.DAY_OF_WEEK)) {
                            Calendar.SUNDAY -> "Min"
                            Calendar.MONDAY -> "Sen"
                            Calendar.TUESDAY -> "Sel"
                            Calendar.WEDNESDAY -> "Rab"
                            Calendar.THURSDAY -> "Kam"
                            Calendar.FRIDAY -> "Jum"
                            Calendar.SATURDAY -> "Sab"
                            else -> ""
                        }
                        val dateStr = WibDateUtils.format("dd/MM", cal.time)
                        val isToday = i == 6
                        Pair(if (isToday) "Hari Ini" else dayName, dateStr)
                    }
                }

                weeklyLabels.forEach { (dayName, dateStr) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayName,
                            fontSize = 9.sp,
                            color = if (dayName == "Hari Ini") BrandGreen else SlateGrey,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateStr,
                            fontSize = 8.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
            2 -> {
                // Sebulan: 4 minggu kebelakang (Rata-rata per minggu)
                monthlyWeekIntervals.forEachIndexed { idx, week ->
                    val isCurrentWeek = idx == 3
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = week.shortTitle,
                            fontSize = 9.sp,
                            color = if (isCurrentWeek) BrandGreen else SlateGrey,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = week.dateRangeStr,
                            fontSize = 7.5.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal
                        )
                        Text(
                            text = "Rata-rata",
                            fontSize = 7.sp,
                            color = if (isCurrentWeek) BrandGreen.copy(alpha = 0.85f) else Color.LightGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            else -> {
                // Tanggal Kustom
                if (customUnitLogs.isNotEmpty()) {
                    if (customUnitLogs.size == 1) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Pengukuran: ${WibDateUtils.format("dd/MM/yyyy HH:mm", customUnitLogs[0].timestamp)} WIB",
                                fontSize = 9.sp,
                                color = SlateGrey,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        customUnitLogs.forEachIndexed { idx, log ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "#${idx + 1}",
                                    fontSize = 8.5.sp,
                                    color = SlateGrey,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = WibDateUtils.format("dd/MM", log.timestamp),
                                    fontSize = 8.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = WibDateUtils.format("HH:mm", log.timestamp),
                                    fontSize = 7.5.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Periode: ${WibDateUtils.format("dd/MM/yy", customStartDate)} - ${WibDateUtils.format("dd/MM/yy", customEndDate)} (Tidak ada data)",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrendChartLegend(trendTabSelection: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (trendTabSelection) {
            0 -> {
                Text("Garis: Axial 1 / DE (Hijau), Axial 2 / NDE (Abu-Abu)", fontSize = 8.sp, color = Color.DarkGray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(BrandYellow).clip(CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Warning (4.5)", fontSize = 8.sp, color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).background(BrandRed).clip(CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trip Critical (8.8)", fontSize = 8.sp, color = Color.Black)
                }
            }
            1 -> {
                Text("Garis: Suhu Bearing (Oranye/Tebal)", fontSize = 8.sp, color = Color.DarkGray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(BrandYellow).clip(CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Warning (65°C)", fontSize = 8.sp, color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).background(BrandRed).clip(CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trip Critical (75°C)", fontSize = 8.sp, color = Color.Black)
                }
            }
            else -> {
                Text("Garis: Pelumasan Greasing (Hijau = OK, Merah = Belum)", fontSize = 8.sp, color = Color.DarkGray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(BrandGreen).clip(CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Standar (100%)", fontSize = 8.sp, color = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).background(BrandRed).clip(CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Perlu Greasing (0%)", fontSize = 8.sp, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun EnlargedChartModalDialog(
    selectedUnit: CentrifugeUnitState,
    initialTrendTab: Int,
    timeRangeSelection: Int,
    customStartDate: Long,
    customEndDate: Long,
    devVibPoints: List<Float>,
    ndevVibPoints: List<Float>,
    bearingTempPoints: List<Float>,
    greasingPoints: List<Float>,
    todayUnitLogs: List<VibrationLog>,
    monthlyWeekIntervals: List<MonthlyWeekInterval>,
    customUnitLogs: List<VibrationLog>,
    onDismiss: () -> Unit
) {
    var dialogTrendTab by remember { mutableStateOf(initialTrendTab) }
    var dialogZoomScale by remember { mutableStateOf(1f) }
    var dialogPanX by remember { mutableStateOf(0f) }
    var dialogPanY by remember { mutableStateOf(0f) }

    val historyPoints = when (dialogTrendTab) {
        0 -> devVibPoints
        1 -> bearingTempPoints
        else -> greasingPoints
    }

    val hasCriticalValue = when (dialogTrendTab) {
        0 -> devVibPoints.any { it >= selectedUnit.criticalVib } || ndevVibPoints.any { it >= selectedUnit.criticalVib }
        1 -> bearingTempPoints.any { it >= selectedUnit.criticalTemp }
        else -> greasingPoints.any { it < 50f }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(BrandGreenLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Grafik Tren ${selectedUnit.id} - ${selectedUnit.name}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = (if (selectedUnit.isRunning) "Mesin Beroperasi" else "Standby Mode") + " • " + when (timeRangeSelection) {
                                0 -> "Periode Hari Ini"
                                1 -> "Periode Seminggu (7 Hari)"
                                2 -> "Periode Sebulan (Per Minggu)"
                                else -> "Periode ${WibDateUtils.format("dd/MM/yy", customStartDate)} - ${WibDateUtils.format("dd/MM/yy", customEndDate)}"
                            },
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color(0xFFF1F5F9), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs for Trend Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Tren Vibrasi (mm/s)", "Tren Temperatur (°C)", "Tren Greasing (%)").forEachIndexed { index, label ->
                        val isTabSelected = dialogTrendTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isTabSelected) BrandGreen else Color(0xFFF1F5F9))
                                .clickable {
                                    dialogTrendTab = index
                                    dialogZoomScale = 1f
                                    dialogPanX = 0f
                                    dialogPanY = 0f
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTabSelected) Color.White else SlateGrey,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Zoom Control Toolbar inside Dialog
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Zoom: ${(dialogZoomScale * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (dialogZoomScale > 1f) BrandGreen else SlateGrey
                        )
                        if (dialogZoomScale > 1f) {
                            Surface(
                                onClick = {
                                    dialogZoomScale = 1f
                                    dialogPanX = 0f
                                    dialogPanY = 0f
                                },
                                shape = RoundedCornerShape(4.dp),
                                color = BrandGreenLight,
                                border = BorderStroke(1.dp, BrandGreen)
                            ) {
                                Text(
                                    text = "Reset (100%)",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Zoom Out
                        FilledTonalIconButton(
                            onClick = {
                                val s = (dialogZoomScale - 0.25f).coerceAtLeast(1f)
                                dialogZoomScale = s
                                if (s <= 1f) {
                                    dialogPanX = 0f
                                    dialogPanY = 0f
                                }
                            },
                            enabled = dialogZoomScale > 1f,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Zoom In
                        FilledTonalIconButton(
                            onClick = {
                                dialogZoomScale = (dialogZoomScale + 0.25f).coerceAtMost(4f)
                            },
                            enabled = dialogZoomScale < 4f,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Spacious Canvas Area with zoom/pan support
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (hasCriticalValue) Color(0xFFFFF5F5) else Color(0xFFFAFAFA))
                        .border(
                            width = if (hasCriticalValue) 1.5.dp else 1.dp,
                            color = if (hasCriticalValue) BrandRed.copy(alpha = 0.7f) else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (dialogZoomScale * zoom).coerceIn(1f, 4f)
                                dialogZoomScale = newScale
                                if (newScale > 1f) {
                                    val maxPanX = (size.width * (newScale - 1f)) / 2f
                                    val maxPanY = (size.height * (newScale - 1f)) / 2f
                                    dialogPanX = (dialogPanX + pan.x).coerceIn(-maxPanX, maxPanX)
                                    dialogPanY = (dialogPanY + pan.y).coerceIn(-maxPanY, maxPanY)
                                } else {
                                    dialogPanX = 0f
                                    dialogPanY = 0f
                                }
                            }
                        }
                        .padding(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = dialogZoomScale
                                scaleY = dialogZoomScale
                                translationX = dialogPanX
                                translationY = dialogPanY
                            }
                    ) {
                        CentrifugeTrendCanvas(
                            modifier = Modifier.fillMaxSize(),
                            trendTabSelection = dialogTrendTab,
                            historyPoints = historyPoints,
                            devVibPoints = devVibPoints,
                            ndevVibPoints = ndevVibPoints,
                            bearingTempPoints = bearingTempPoints,
                            greasingPoints = greasingPoints,
                            selectedUnit = selectedUnit,
                            timeRangeSelection = timeRangeSelection,
                            isEnlarged = true
                        )
                    }

                    if (hasCriticalValue) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(BrandRed, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("KRITIKAL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // X-Axis Labels
                TrendChartXAxisLabels(
                    timeRangeSelection = timeRangeSelection,
                    todayUnitLogs = todayUnitLogs,
                    monthlyWeekIntervals = monthlyWeekIntervals,
                    customUnitLogs = customUnitLogs,
                    customStartDate = customStartDate,
                    customEndDate = customEndDate
                )

                // Legend
                TrendChartLegend(trendTabSelection = dialogTrendTab)

                Spacer(modifier = Modifier.height(10.dp))

                // Statistics Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (dialogTrendTab) {
                        0 -> {
                            val maxDe = if (devVibPoints.isNotEmpty()) devVibPoints.maxOrNull() ?: 0f else 0f
                            val avgDe = if (devVibPoints.isNotEmpty()) devVibPoints.average().toFloat() else 0f
                            val maxNde = if (ndevVibPoints.isNotEmpty()) ndevVibPoints.maxOrNull() ?: 0f else 0f
                            val avgNde = if (ndevVibPoints.isNotEmpty()) ndevVibPoints.average().toFloat() else 0f

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SoftBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Axial 1 (DE)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                    Text("Max: ${String.format(Locale.US, "%.1f", maxDe)} mm/s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("Rata: ${String.format(Locale.US, "%.1f", avgDe)} mm/s", fontSize = 10.sp, color = Color.DarkGray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SoftBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Axial 2 (NDE)", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("Max: ${String.format(Locale.US, "%.1f", maxNde)} mm/s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("Rata: ${String.format(Locale.US, "%.1f", avgNde)} mm/s", fontSize = 10.sp, color = Color.DarkGray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = if (hasCriticalValue) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Status Vibrasi", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (hasCriticalValue) BrandRed else BrandGreen)
                                    Text(if (hasCriticalValue) "KRITIKAL" else if (maxDe >= selectedUnit.warningVib) "WARNING" else "NORMAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (hasCriticalValue) BrandRed else if (maxDe >= selectedUnit.warningVib) BrandOrange else BrandGreen)
                                    Text("Trip: ${selectedUnit.criticalVib} mm/s", fontSize = 9.5.sp, color = Color.DarkGray)
                                }
                            }
                        }
                        1 -> {
                            val maxTemp = if (bearingTempPoints.isNotEmpty()) bearingTempPoints.maxOrNull() ?: 0f else 0f
                            val minTemp = if (bearingTempPoints.isNotEmpty()) bearingTempPoints.minOrNull() ?: 0f else 0f
                            val avgTemp = if (bearingTempPoints.isNotEmpty()) bearingTempPoints.average().toFloat() else 0f

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SoftBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Suhu Bearing", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = BrandOrange)
                                    Text("Max: ${String.format(Locale.US, "%.1f", maxTemp)}°C", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("Min: ${String.format(Locale.US, "%.1f", minTemp)}°C", fontSize = 10.sp, color = Color.DarkGray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SoftBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Rata-Rata Suhu", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("${String.format(Locale.US, "%.1f", avgTemp)}°C", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("Warning: ${selectedUnit.warningTemp}°C", fontSize = 9.5.sp, color = Color.DarkGray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = if (hasCriticalValue) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Status Suhu", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (hasCriticalValue) BrandRed else BrandGreen)
                                    Text(if (hasCriticalValue) "KRITIKAL" else if (maxTemp >= selectedUnit.warningTemp) "WARNING" else "NORMAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (hasCriticalValue) BrandRed else if (maxTemp >= selectedUnit.warningTemp) BrandOrange else BrandGreen)
                                    Text("Trip: ${selectedUnit.criticalTemp}°C", fontSize = 9.5.sp, color = Color.DarkGray)
                                }
                            }
                        }
                        else -> {
                            val okCount = greasingPoints.count { it >= 50f }
                            val totalCount = greasingPoints.size
                            val pct = if (totalCount > 0) (okCount.toFloat() / totalCount * 100).toInt() else 100

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SoftBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Pelumasan", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                    Text("$okCount / $totalCount Terlumasi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("Standar: 100%", fontSize = 9.5.sp, color = Color.DarkGray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = SoftBg),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Rasio Pemenuhan", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (pct >= 50) BrandGreen else BrandRed)
                                    Text("Target: 100%", fontSize = 9.5.sp, color = Color.DarkGray)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = if (pct >= 50) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Status Greasing", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = if (pct >= 50) BrandGreen else BrandRed)
                                    Text(if (pct >= 50) "TERPENUHI" else "PERLU GREASING", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (pct >= 50) BrandGreen else BrandRed)
                                    Text("Batas: 50%", fontSize = 9.5.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Tutup Tampilan Grafik Penuh", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    vibrationLogs: List<VibrationLog>,
    abnormalityReports: List<AbnormalityReport>,
    ciltChecks: List<CiltCheck>,
    viewModel: CentrifugeViewModel
) {
    val context = LocalContext.current
    var selectedUnitIndex by remember { mutableStateOf(0) } // Default to SC-01 (index 0)
    var trendTabSelection by remember { mutableStateOf(0) } // 0: Vibrasi (mm/s), 1: Suhu (°C)
    var timeRangeSelection by remember { mutableStateOf(0) } // 0: Harian, 1: Seminggu, 2: Sebulan, 3: Tanggal

    val todayStartMillis = remember { WibDateUtils.getStartOfDay() }
    val todayEndMillis = remember { todayStartMillis + 24 * 60 * 60 * 1000L - 1L }
    var customStartDate by remember { mutableStateOf(todayStartMillis) }
    var customEndDate by remember { mutableStateOf(todayEndMillis) }

    var isChartExpandedHeight by remember { mutableStateOf(false) }
    var showEnlargedChartDialog by remember { mutableStateOf(false) }
    var chartZoomScale by remember { mutableStateOf(1f) }
    var chartPanX by remember { mutableStateOf(0f) }
    var chartPanY by remember { mutableStateOf(0f) }

    LaunchedEffect(selectedUnitIndex, timeRangeSelection, trendTabSelection) {
        chartZoomScale = 1f
        chartPanX = 0f
        chartPanY = 0f
    }

    fun pickStartDate() {
        val cal = WibDateUtils.getCalendar(customStartDate)
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = WibDateUtils.getCalendar().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val newStart = newCal.timeInMillis
                customStartDate = newStart
                if (customEndDate < newStart) {
                    customEndDate = newStart + 24 * 60 * 60 * 1000L - 1L
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickEndDate() {
        val cal = WibDateUtils.getCalendar(customEndDate)
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = WibDateUtils.getCalendar().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val newEnd = newCal.timeInMillis
                customEndDate = newEnd
                if (customStartDate > newEnd) {
                    customStartDate = WibDateUtils.getCalendar(newEnd).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    var unitsState by remember {
        mutableStateOf(
            listOf(
                CentrifugeUnitState("SC-01", "Sludge Centrifuge No. 1", true, 2.8f, 2.2f, 1.9f, 2.1f, 1.6f, 58.0f, 48.0f),
                CentrifugeUnitState("SC-02", "Sludge Centrifuge No. 2", true, 5.2f, 4.1f, 3.6f, 4.0f, 3.1f, 68.0f, 54.0f), // High warning
                CentrifugeUnitState("SC-03", "Sludge Centrifuge No. 3", true, 3.1f, 2.5f, 2.0f, 2.2f, 1.7f, 59.0f, 49.0f),
                CentrifugeUnitState("SC-04", "Sludge Centrifuge No. 4", true, 1.8f, 1.4f, 1.3f, 1.5f, 1.2f, 51.0f, 44.0f),
                CentrifugeUnitState("SC-05", "Sludge Centrifuge No. 5", true, 2.4f, 1.9f, 1.6f, 1.8f, 1.4f, 56.0f, 46.0f),
                CentrifugeUnitState("SC-06", "Sludge Centrifuge No. 6", true, 3.8f, 3.2f, 2.6f, 3.0f, 2.4f, 72.5f, 58.0f), // Overheat warning
                CentrifugeUnitState("SC-07", "Sludge Centrifuge No. 7", true, 2.7f, 2.0f, 1.8f, 2.0f, 1.5f, 55.0f, 47.0f),
                CentrifugeUnitState("SC-08", "Sludge Centrifuge No. 8", false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 32.5f, 28.0f) // Standby
            ).map { unit ->
                val devHist = List(12) { i -> (unit.baseDeVib + (i * 0.04f - 0.2f)).coerceAtLeast(0.1f) }
                val ndevHist = List(12) { i -> (unit.baseNdeVib + (i * 0.03f - 0.15f)).coerceAtLeast(0.1f) }
                val tempHist = List(12) { i -> (unit.baseBearingTemp + (i * 0.2f - 1.0f)).coerceAtLeast(20f) }
                
                val weeklyDev = List(7) { i -> (unit.baseDeVib + (i * 0.06f - 0.18f)).coerceAtLeast(0.1f) }
                val weeklyNde = List(7) { i -> (unit.baseNdeVib + (i * 0.04f - 0.12f)).coerceAtLeast(0.1f) }
                val weeklyTemp = List(7) { i -> (unit.baseBearingTemp + (i * 0.3f - 0.9f)).coerceAtLeast(20f) }

                val monthlyDev = List(12) { i -> (unit.baseDeVib + (i * 0.08f - 0.4f)).coerceAtLeast(0.1f) }
                val monthlyNde = List(12) { i -> (unit.baseNdeVib + (i * 0.06f - 0.3f)).coerceAtLeast(0.1f) }
                val monthlyTemp = List(12) { i -> (unit.baseBearingTemp + (i * 0.4f - 2.0f)).coerceAtLeast(20f) }

                val greasingHist = List(12) { if (unit.isRunning) 100f else 0f }
                val weeklyGreasing = List(7) { if (unit.isRunning) 100f else 0f }
                val monthlyGreasing = List(12) { if (unit.isRunning) 100f else 0f }

                unit.copy(
                    devVibHistory = devHist, 
                    ndevVibHistory = ndevHist, 
                    bearingTempHistory = tempHist,
                    weeklyDevVibHistory = weeklyDev,
                    weeklyNdevVibHistory = weeklyNde,
                    weeklyBearingTempHistory = weeklyTemp,
                    monthlyDevVibHistory = monthlyDev,
                    monthlyNdevVibHistory = monthlyNde,
                    monthlyBearingTempHistory = monthlyTemp,
                    greasingHistory = greasingHist,
                    weeklyGreasingHistory = weeklyGreasing,
                    monthlyGreasingHistory = monthlyGreasing
                )
            }
        )
    }

    val selectedUnit = unitsState.getOrElse(selectedUnitIndex) { unitsState[0] }

    LaunchedEffect(vibrationLogs) {
        if (vibrationLogs.isNotEmpty()) {
            val latest = vibrationLogs.first()
            val matchedUnit = listOf("SC-01", "SC-02", "SC-03", "SC-04", "SC-05", "SC-06", "SC-07", "SC-08").firstOrNull { 
                latest.comments.contains(it) 
            }
            if (matchedUnit != null) {
                unitsState = unitsState.map { unit ->
                    if (unit.id == matchedUnit) {
                        val newDevHist = (unit.devVibHistory + latest.driveEndVibration).takeLast(15)
                        val newNdeHist = (unit.ndevVibHistory + latest.nonDriveEndVibration).takeLast(15)
                        val newTempHist = (unit.bearingTempHistory + latest.bearingTemp).takeLast(15)
                        val newGreasingHist = (unit.greasingHistory + (if (latest.isGreased) 100f else 0f)).takeLast(15)
                        unit.copy(
                            baseDeVib = latest.driveEndVibration,
                            baseNdeVib = latest.nonDriveEndVibration,
                            baseMotorVib = latest.motorBearingVibration,
                            baseGearboxVib = latest.gearboxBearingVibration,
                            baseBowlVib = latest.bowlVibration,
                            baseBearingTemp = latest.bearingTemp,
                            baseMotorTemp = latest.motorTemp,
                            devVibHistory = newDevHist,
                            ndevVibHistory = newNdeHist,
                            bearingTempHistory = newTempHist,
                            greasingHistory = newGreasingHist
                        )
                    } else {
                        unit
                    }
                }
            }
        }
    }

    var showVibDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Live Vibrations Trends & Temperature (Slide 21 and 26) - Expanded for 8 Sludge Centrifuge units
        item {
            val unitAllLogs = vibrationLogs.filter { log ->
                log.comments.contains("[${selectedUnit.id}]") || 
                log.comments.contains(selectedUnit.id) ||
                log.comments.contains(selectedUnit.id.replace("-", " "))
            }

            val now = WibDateUtils.getCalendar()
            val calToday = WibDateUtils.getCalendar().apply {
                if (now.get(Calendar.HOUR_OF_DAY) < 7) {
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                set(Calendar.HOUR_OF_DAY, 7)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfToday = calToday.timeInMillis
            val endOfToday = startOfToday + 24 * 60 * 60 * 1000L

            val calMidnight = WibDateUtils.getCalendar().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfMidnight = calMidnight.timeInMillis
            val endOfMidnight = startOfMidnight + 24 * 60 * 60 * 1000L

            // 1. DATA HARIAN: Hanya data di hari itu saja yang sudah di-input
            val todayUnitLogs = unitAllLogs.filter { 
                (it.timestamp in startOfToday..endOfToday) || (it.timestamp in startOfMidnight..endOfMidnight)
            }.sortedBy { it.timestamp }

            val dailyDevVibPoints = todayUnitLogs.map { it.driveEndVibration }
            val dailyNdevVibPoints = todayUnitLogs.map { it.nonDriveEndVibration }
            val dailyBearingTempPoints = todayUnitLogs.map { it.bearingTemp }
            val dailyGreasingPoints = todayUnitLogs.map { if (it.isGreased) 100f else 0f }

            // 2. DATA SEMINGGU (7 hari kebelakang: H-6 s/d Hari Ini)
            val weeklyDevVibPoints = List(7) { i ->
                val offset = i - 6
                val c = WibDateUtils.getCalendar().apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val s = c.timeInMillis
                val e = s + 24 * 60 * 60 * 1000L
                val logsOnDay = unitAllLogs.filter { it.timestamp in s..e }
                if (logsOnDay.isNotEmpty()) {
                    logsOnDay.map { it.driveEndVibration }.average().toFloat()
                } else {
                    selectedUnit.weeklyDevVibHistory.getOrElse(i) { selectedUnit.baseDeVib }
                }
            }

            val weeklyNdevVibPoints = List(7) { i ->
                val offset = i - 6
                val c = WibDateUtils.getCalendar().apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val s = c.timeInMillis
                val e = s + 24 * 60 * 60 * 1000L
                val logsOnDay = unitAllLogs.filter { it.timestamp in s..e }
                if (logsOnDay.isNotEmpty()) {
                    logsOnDay.map { it.nonDriveEndVibration }.average().toFloat()
                } else {
                    selectedUnit.weeklyNdevVibHistory.getOrElse(i) { selectedUnit.baseNdeVib }
                }
            }

            val weeklyBearingTempPoints = List(7) { i ->
                val offset = i - 6
                val c = WibDateUtils.getCalendar().apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val s = c.timeInMillis
                val e = s + 24 * 60 * 60 * 1000L
                val logsOnDay = unitAllLogs.filter { it.timestamp in s..e }
                if (logsOnDay.isNotEmpty()) {
                    logsOnDay.map { it.bearingTemp }.average().toFloat()
                } else {
                    selectedUnit.weeklyBearingTempHistory.getOrElse(i) { selectedUnit.baseBearingTemp }
                }
            }

            val weeklyGreasingPoints = List(7) { i ->
                val offset = i - 6
                val c = WibDateUtils.getCalendar().apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val s = c.timeInMillis
                val e = s + 24 * 60 * 60 * 1000L
                val logsOnDay = unitAllLogs.filter { it.timestamp in s..e }
                if (logsOnDay.isNotEmpty()) {
                    if (logsOnDay.any { it.isGreased }) 100f else 0f
                } else {
                    selectedUnit.weeklyGreasingHistory.getOrElse(i) { if (selectedUnit.isRunning) 100f else 0f }
                }
            }

            // 3. DATA SEBULAN (Rata-rata per minggu kebelakang: 4 Minggu Kebelakang)
            val monthlyWeekIntervals = remember { getMonthlyWeekIntervals() }

            val monthlyDevVibPoints = List(4) { i ->
                val w = monthlyWeekIntervals[i]
                val logsInWeek = unitAllLogs.filter { it.timestamp in w.startMillis..w.endMillis }
                if (logsInWeek.isNotEmpty()) {
                    logsInWeek.map { it.driveEndVibration }.average().toFloat()
                } else {
                    val base = selectedUnit.baseDeVib
                    val variation = kotlin.math.sin(i * 0.75f) * 0.18f
                    (base + variation).toFloat().coerceAtLeast(0.1f)
                }
            }

            val monthlyNdevVibPoints = List(4) { i ->
                val w = monthlyWeekIntervals[i]
                val logsInWeek = unitAllLogs.filter { it.timestamp in w.startMillis..w.endMillis }
                if (logsInWeek.isNotEmpty()) {
                    logsInWeek.map { it.nonDriveEndVibration }.average().toFloat()
                } else {
                    val base = selectedUnit.baseNdeVib
                    val variation = kotlin.math.cos(i * 0.75f) * 0.14f
                    (base + variation).toFloat().coerceAtLeast(0.1f)
                }
            }

            val monthlyBearingTempPoints = List(4) { i ->
                val w = monthlyWeekIntervals[i]
                val logsInWeek = unitAllLogs.filter { it.timestamp in w.startMillis..w.endMillis }
                if (logsInWeek.isNotEmpty()) {
                    logsInWeek.map { it.bearingTemp }.average().toFloat()
                } else {
                    val base = selectedUnit.baseBearingTemp
                    val variation = kotlin.math.sin(i * 0.65f) * 1.2f
                    (base + variation).toFloat().coerceAtLeast(20.0f)
                }
            }

            val monthlyGreasingPoints = List(4) { i ->
                val w = monthlyWeekIntervals[i]
                val logsInWeek = unitAllLogs.filter { it.timestamp in w.startMillis..w.endMillis }
                if (logsInWeek.isNotEmpty()) {
                    val greasedCount = logsInWeek.count { it.isGreased }
                    (greasedCount.toFloat() / logsInWeek.size) * 100f
                } else {
                    if (selectedUnit.isRunning) 100f else 0f
                }
            }

            // 4. DATA TANGGAL KUSTOM: Filter data berdasarkan customStartDate..customEndDate
            val customUnitLogs = unitAllLogs.filter { it.timestamp in customStartDate..customEndDate }.sortedBy { it.timestamp }
            val customDevVibPoints = customUnitLogs.map { it.driveEndVibration }
            val customNdevVibPoints = customUnitLogs.map { it.nonDriveEndVibration }
            val customBearingTempPoints = customUnitLogs.map { it.bearingTemp }
            val customGreasingPoints = customUnitLogs.map { if (it.isGreased) 100f else 0f }

            // Get data lists based on time range selection (0: Hari Ini, 1: Seminggu, 2: Sebulan, 3: Tanggal)
            val devVibPoints = when (timeRangeSelection) {
                0 -> dailyDevVibPoints
                1 -> weeklyDevVibPoints
                2 -> monthlyDevVibPoints
                else -> if (customDevVibPoints.isNotEmpty()) customDevVibPoints else listOf(selectedUnit.baseDeVib)
            }
            val ndevVibPoints = when (timeRangeSelection) {
                0 -> dailyNdevVibPoints
                1 -> weeklyNdevVibPoints
                2 -> monthlyNdevVibPoints
                else -> if (customNdevVibPoints.isNotEmpty()) customNdevVibPoints else listOf(selectedUnit.baseNdeVib)
            }
            val bearingTempPoints = when (timeRangeSelection) {
                0 -> dailyBearingTempPoints
                1 -> weeklyBearingTempPoints
                2 -> monthlyBearingTempPoints
                else -> if (customBearingTempPoints.isNotEmpty()) customBearingTempPoints else listOf(selectedUnit.baseBearingTemp)
            }
            val greasingPoints = when (timeRangeSelection) {
                0 -> dailyGreasingPoints
                1 -> weeklyGreasingPoints
                2 -> monthlyGreasingPoints
                else -> if (customGreasingPoints.isNotEmpty()) customGreasingPoints else listOf(if (selectedUnit.isRunning) 100f else 0f)
            }

            val currentDev = devVibPoints.lastOrNull() ?: selectedUnit.baseDeVib
            val currentNdev = ndevVibPoints.lastOrNull() ?: selectedUnit.baseNdeVib
            val currentTemp = bearingTempPoints.lastOrNull() ?: selectedUnit.baseBearingTemp

            val selectedUnitSummary = getUnitSummaryMetrics(selectedUnit, timeRangeSelection, vibrationLogs, customStartDate, customEndDate)
            val reportValues = selectedUnitSummary.reportValues
            val isMeasuredToday = selectedUnitSummary.isMeasuredToday
            val latestTodayLog = selectedUnitSummary.latestTodayLog
            val activeUnitAlarm = selectedUnitSummary.alarmStatus

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showVibDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showVibDialog = true },
                            modifier = Modifier.testTag("add_vibration_log_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Log Manual", tint = BrandGreen)
                        }
                        Text(
                            text = "Input Pengukuran Mesin Sludge Centrifuge",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pilihan Periode Report (Hari Ini | Seminggu | Sebulan | Tanggal) - DI ATAS "Pilih Unit Mesin"
                    Text("Pilihan Periode Report:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SoftBg)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Hari Ini", "Seminggu", "Sebulan", "Tanggal").forEachIndexed { index, label ->
                            val isSelected = timeRangeSelection == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) BrandGreen else Color.Transparent)
                                    .clickable { timeRangeSelection = index }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color.DarkGray
                                )
                            }
                        }
                    }

                    if (timeRangeSelection == 3) {
                        Spacer(modifier = Modifier.height(8.dp))
                        DateRangeFilterPicker(
                            startDateMillis = customStartDate,
                            endDateMillis = customEndDate,
                            onPickStartDate = { pickStartDate() },
                            onPickEndDate = { pickEndDate() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // horizontal selector for the 8 units
                    Text("Pilih Unit Mesin:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        unitsState.forEachIndexed { index, unit ->
                            val isSelected = index == selectedUnitIndex
                            val summary = getUnitSummaryMetrics(unit, timeRangeSelection, vibrationLogs, customStartDate, customEndDate)

                            val cardBg = if (isSelected) Color(0xFF86EFAC) else Color.White
                            val borderCol = if (isSelected) Color(0xFF16A34A) else Color(0xFFE2E8F0)
                            val alarmColor = when (summary.alarmStatus) {
                                "CRITICAL" -> BrandRed
                                "WARNING" -> BrandOrange
                                "STANDBY" -> Color.LightGray
                                "UNMEASURED" -> Color(0xFF94A3B8)
                                else -> BrandGreen
                            }

                            Card(
                                modifier = Modifier
                                    .width(115.dp)
                                    .clickable { selectedUnitIndex = index },
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, borderCol)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = unit.id,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color.Black
                                        )
                                        // Pulsing led dot
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(alarmColor, CircleShape)
                                        )
                                    }

                                    val isZeroHariIni = (timeRangeSelection == 0 && !summary.isMeasuredToday) || (timeRangeSelection == 3 && summary.alarmStatus == "UNMEASURED")
                                    Text(
                                        text = if (isZeroHariIni) "0.0 mm/s"
                                               else if (unit.isRunning) "${String.format(Locale.US, "%.1f", summary.avgVib)} mm/s" 
                                               else "Standby",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isZeroHariIni || summary.alarmStatus == "NORMAL" || summary.alarmStatus == "UNMEASURED") SlateGrey else alarmColor
                                    )
                                    Text(
                                        text = if (isZeroHariIni) "0.0 °C"
                                               else if (unit.isRunning) "${String.format(Locale.US, "%.1f", summary.avgTemp)} °C" 
                                               else "-",
                                        fontSize = 9.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dedicated Report Card for Selected Unit (e.g. Report SC-01)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Report ${selectedUnit.id}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateGrey
                                    )
                                    val subtitleText = when (timeRangeSelection) {
                                        0 -> {
                                            if (isMeasuredToday && selectedUnitSummary.todayLogCount > 1) {
                                                "Rata-Rata Pengukuran Hari Ini (${selectedUnitSummary.todayLogCount}x Pengukuran)"
                                            } else if (isMeasuredToday) {
                                                "Hasil Pengukuran Hari Ini (${WibDateUtils.format("dd MMMM yyyy", Date())})"
                                            } else {
                                                "Laporan Hari Ini (${WibDateUtils.format("dd MMMM yyyy", Date())}) - Belum Diukur"
                                            }
                                        }
                                        1 -> "Rata-Rata Pengukuran Seminggu (7 Hari Terakhir)"
                                        2 -> "Rata-Rata Pengukuran Sebulan (Per Minggu Kebelakang)"
                                        else -> "Rata-Rata Pengukuran Periode (${WibDateUtils.format("dd MMM yyyy", customStartDate)} - ${WibDateUtils.format("dd MMM yyyy", customEndDate)})"
                                    }
                                    Text(
                                        text = subtitleText,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                val hasValues = if (timeRangeSelection == 0) isMeasuredToday else if (timeRangeSelection == 3) (selectedUnitSummary.alarmStatus != "UNMEASURED") else (reportValues.dev != null && (reportValues.dev ?: 0f) > 0f)
                                val isAbnormal = hasValues && (
                                    (reportValues.dev ?: 0f) > 4.0f ||
                                    (reportValues.nde ?: 0f) > 4.0f ||
                                    (reportValues.motorVib ?: 0f) > 4.0f ||
                                    (reportValues.gearboxVib ?: 0f) > 4.0f ||
                                    (reportValues.bearingTemp ?: 0f) > 80.0f ||
                                    (reportValues.motorTemp ?: 0f) > 80.0f
                                )

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (!hasValues) Color(0xFFF1F5F9)
                                            else if (isAbnormal) Color(0xFFFFCDD2)
                                            else Color(0xFFC8E6C9),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (!hasValues) "BELUM DIUKUR" else if (isAbnormal) "PERLU ATENSI" else "NORMAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (!hasValues) Color.Gray else if (isAbnormal) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Parameter Vibrasi Section
                            Text(
                                text = if (timeRangeSelection == 0) "Hasil Pengukuran Vibrasi:" else "Rata-Rata Parameter Vibrasi:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            ReportRowItem("1", "Axial 1", reportValues.dev, "mm/s", 4.0f, infoText = "Titik ukur pada Bearing dekat dengan pulley")
                            ReportRowItem("2", "Axial 2", reportValues.nde, "mm/s", 4.0f, infoText = "Titik ukur pada Bearing dengan dengan gland packing")
                            ReportRowItem("3", "Horizontal", reportValues.motorVib, "mm/s", 4.0f, infoText = "Titik ukur pada Body mesin sejajar dengan titik Axial 1")
                            ReportRowItem("4", "Vertikal", reportValues.gearboxVib, "mm/s", 4.0f, infoText = "Titik ukur pada body mesin bagian atas tegak lurus dari titik Axial")

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Parameter Temperatur Section
                            Text(
                                text = if (timeRangeSelection == 0) "Hasil Pengukuran Temperatur:" else "Rata-Rata Parameter Temperatur:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            ReportRowItem("1", "Suhu Bearing", reportValues.bearingTemp, "oC", 80.0f, isDegree = true)
                            ReportRowItem("2", "Suhu Motor", reportValues.motorTemp, "oC", 80.0f, isDegree = true)

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Parameter Observasi Fisik & Pelumasan (Greasing, Suara, Kebocoran)
                            Text(
                                text = if (timeRangeSelection == 0) "Hasil Observasi Fisik & Pelumasan:" else "Rata-Rata Observasi Fisik & Pelumasan:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            ReportStatusRowItem(
                                number = "1",
                                label = "Greasing",
                                valueText = reportValues.greasingRatioText,
                                isGood = reportValues.isGreased
                            )
                            ReportStatusRowItem(
                                number = "2",
                                label = "Suara Mesin",
                                valueText = reportValues.soundRatioText,
                                isGood = if (reportValues.soundState != null) reportValues.soundState == "Normal" else null
                            )
                            ReportStatusRowItem(
                                number = "3",
                                label = "Kebocoran",
                                valueText = reportValues.leakageRatioText,
                                isGood = if (reportValues.hasLeakage != null) !reportValues.hasLeakage!! else null
                            )

                            // Harian notes or empty warning
                            if (timeRangeSelection == 0) {
                                if (isMeasuredToday && latestTodayLog != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SoftBg),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = if (selectedUnitSummary.todayLogCount > 1) {
                                                    "4. Catatan hasil observasi (Rata-rata ${selectedUnitSummary.todayLogCount}x pengukuran) :"
                                                } else {
                                                    "4. Catatan hasil observasi :"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Black
                                            )
                                            val obsNote = latestTodayLog.comments.replace(Regex("\\[SC-[0-9]{2}\\]"), "").trim()
                                            Text(
                                                text = obsNote.ifEmpty { "Kondisi operasi centrifuge normal dan stabil." },
                                                fontSize = 11.sp,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Waktu terakhir: ${WibDateUtils.format("HH:mm", latestTodayLog.timestamp)} WIB",
                                                    fontSize = 10.sp,
                                                    color = Color.Black
                                                )
                                                Text(
                                                    text = "Operator: ${latestTodayLog.operatorName.ifEmpty { "Operator Centrifuge" }}",
                                                    fontSize = 10.sp,
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.Info, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Hari ini belum dilakukan pengukuran untuk ${selectedUnit.id}. Seluruh nilai vibrasi dan temperatur bernilai 0.",
                                                    fontSize = 11.sp,
                                                    color = SlateGrey
                                                )
                                            }
                                            TextButton(
                                                onClick = { showVibDialog = true },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("+ Input Ukur", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                            }
                                        }
                                    }
                                }
                            } else if (timeRangeSelection == 3 && customUnitLogs.isEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = BrandOrange, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Belum ada pengukuran untuk ${selectedUnit.id} pada rentang tanggal yang dipilih.",
                                                fontSize = 11.sp,
                                                color = SlateGrey
                                            )
                                        }
                                        TextButton(
                                            onClick = { showVibDialog = true },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("+ Input Ukur", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selected Unit Detailed Monitor (Machine Status & Trends)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedUnit.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(if (selectedUnit.isRunning) BrandGreen else Color.LightGray, CircleShape))
                                Text(
                                    text = (if (selectedUnit.isRunning) "Mesin Beroperasi" else "Standby Mode") + " • " + when (timeRangeSelection) {
                                        0 -> "Periode Hari Ini"
                                        1 -> "Periode Seminggu"
                                        2 -> "Periode Sebulan (Rata-rata Perminggu)"
                                        else -> "Periode ${WibDateUtils.format("dd/MM/yy", customStartDate)} - ${WibDateUtils.format("dd/MM/yy", customEndDate)}"
                                    },
                                    fontSize = 10.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        // Alarm State Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    when (activeUnitAlarm) {
                                        "CRITICAL" -> BrandRed.copy(alpha = 0.15f)
                                        "WARNING" -> BrandOrange.copy(alpha = 0.15f)
                                        "STANDBY" -> Color.LightGray.copy(alpha = 0.3f)
                                        else -> BrandGreenLight
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = activeUnitAlarm,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (activeUnitAlarm) {
                                    "CRITICAL" -> BrandRed
                                    "WARNING" -> BrandOrange
                                    "STANDBY" -> Color.DarkGray
                                    else -> BrandGreen
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tabs for Trend Type (Vibration, Temperature, or Greasing)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Tren Vibrasi (mm/s)", "Tren Temperatur (°C)", "Tren Greasing (%)").forEachIndexed { index, label ->
                            val isTabSelected = trendTabSelection == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isTabSelected) BrandGreen else SoftBg)
                                    .clickable { trendTabSelection = index }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTabSelected) Color.White else SlateGrey,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Zoom & Enlarge Controls Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zoom in/out and reset badges
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    val s = (chartZoomScale - 0.25f).coerceAtLeast(1f)
                                    chartZoomScale = s
                                    if (s <= 1f) {
                                        chartPanX = 0f
                                        chartPanY = 0f
                                    }
                                },
                                enabled = chartZoomScale > 1f,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOut,
                                    contentDescription = "Perkecil (Zoom Out)",
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            Surface(
                                onClick = {
                                    chartZoomScale = 1f
                                    chartPanX = 0f
                                    chartPanY = 0f
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (chartZoomScale > 1f) BrandGreenLight else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (chartZoomScale > 1f) BrandGreen else Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = if (chartZoomScale > 1f) "${(chartZoomScale * 100).toInt()}% (Reset)" else "100%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (chartZoomScale > 1f) BrandGreen else SlateGrey,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                )
                            }

                            FilledTonalIconButton(
                                onClick = {
                                    chartZoomScale = (chartZoomScale + 0.25f).coerceAtMost(3.5f)
                                },
                                enabled = chartZoomScale < 3.5f,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = "Perbesar (Zoom In)",
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            if (chartZoomScale > 1f) {
                                Text(
                                    text = "Geser grafik",
                                    fontSize = 9.sp,
                                    color = Color.Gray,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }

                        // Right actions: Expand height & Fullscreen Modal
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                onClick = { isChartExpandedHeight = !isChartExpandedHeight },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isChartExpandedHeight) BrandGreenLight else Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (isChartExpandedHeight) BrandGreen else Color(0xFFCBD5E1))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isChartExpandedHeight) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                                        contentDescription = null,
                                        tint = if (isChartExpandedHeight) BrandGreen else SlateGrey,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isChartExpandedHeight) "Tinggi: 250dp" else "Perluas",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isChartExpandedHeight) BrandGreen else SlateGrey
                                    )
                                }
                            }

                            Surface(
                                onClick = { showEnlargedChartDialog = true },
                                shape = RoundedCornerShape(6.dp),
                                color = BrandGreen,
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInFull,
                                        contentDescription = "Layar Penuh",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Perbesar",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Trend Line Canvas Chart
                    val historyPoints = when (trendTabSelection) {
                        0 -> devVibPoints
                        1 -> bearingTempPoints
                        else -> greasingPoints
                    }
                    if (historyPoints.isNotEmpty()) {
                        val hasCriticalValue = when (trendTabSelection) {
                            0 -> devVibPoints.any { it >= selectedUnit.criticalVib } || ndevVibPoints.any { it >= selectedUnit.criticalVib }
                            1 -> bearingTempPoints.any { it >= selectedUnit.criticalTemp }
                            else -> greasingPoints.any { it < 50f }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isChartExpandedHeight) 250.dp else 150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (hasCriticalValue) Color(0xFFFFF5F5) else Color(0xFFFAFAFA))
                                .border(
                                    width = if (hasCriticalValue) 1.5.dp else 1.dp,
                                    color = if (hasCriticalValue) BrandRed.copy(alpha = 0.7f) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        val newScale = (chartZoomScale * zoom).coerceIn(1f, 3.5f)
                                        chartZoomScale = newScale
                                        if (newScale > 1f) {
                                            val maxPanX = (size.width * (newScale - 1f)) / 2f
                                            val maxPanY = (size.height * (newScale - 1f)) / 2f
                                            chartPanX = (chartPanX + pan.x).coerceIn(-maxPanX, maxPanX)
                                            chartPanY = (chartPanY + pan.y).coerceIn(-maxPanY, maxPanY)
                                        } else {
                                            chartPanX = 0f
                                            chartPanY = 0f
                                        }
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        scaleX = chartZoomScale
                                        scaleY = chartZoomScale
                                        translationX = chartPanX
                                        translationY = chartPanY
                                    }
                            ) {
                                CentrifugeTrendCanvas(
                                    modifier = Modifier.fillMaxSize(),
                                    trendTabSelection = trendTabSelection,
                                    historyPoints = historyPoints,
                                    devVibPoints = devVibPoints,
                                    ndevVibPoints = ndevVibPoints,
                                    bearingTempPoints = bearingTempPoints,
                                    greasingPoints = greasingPoints,
                                    selectedUnit = selectedUnit,
                                    timeRangeSelection = timeRangeSelection,
                                    isEnlarged = isChartExpandedHeight
                                )
                            }

                            if (hasCriticalValue) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(BrandRed, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text("KRITIKAL", color = Color.White, fontSize = 8.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // X-Axis Time / Date Labels
                        TrendChartXAxisLabels(
                            timeRangeSelection = timeRangeSelection,
                            todayUnitLogs = todayUnitLogs,
                            monthlyWeekIntervals = monthlyWeekIntervals,
                            customUnitLogs = customUnitLogs,
                            customStartDate = customStartDate,
                            customEndDate = customEndDate
                        )

                        // Legend and Explanation
                        TrendChartLegend(trendTabSelection = trendTabSelection)

                    } else {
                        // Empty State for Harian when no data has been inputted yet today
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(Color(0xFFFEF3C7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = BrandOrange,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Belum Ada Data Input Pengukuran Hari Ini",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGrey
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Grafik Hari Ini hanya menampilkan data di hari ini yang sudah di-input.\nBelum ada data pengukuran hari ini untuk ${selectedUnit.id} (${selectedUnit.name}).",
                                    fontSize = 10.5.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showVibDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("+ Input Pengukuran Sekarang", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            if (showEnlargedChartDialog) {
                EnlargedChartModalDialog(
                    selectedUnit = selectedUnit,
                    initialTrendTab = trendTabSelection,
                    timeRangeSelection = timeRangeSelection,
                    customStartDate = customStartDate,
                    customEndDate = customEndDate,
                    devVibPoints = devVibPoints,
                    ndevVibPoints = ndevVibPoints,
                    bearingTempPoints = bearingTempPoints,
                    greasingPoints = greasingPoints,
                    todayUnitLogs = todayUnitLogs,
                    monthlyWeekIntervals = monthlyWeekIntervals,
                    customUnitLogs = customUnitLogs,
                    onDismiss = { showEnlargedChartDialog = false }
                )
            }
        }

        // Manual Measurement Logs (with Machine Status)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Riwayat Pengukuran & Status Unit",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Text(
                                "Log manual vibrasi, temperatur, & kondisi operasi",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        IconButton(
                            onClick = { showVibDialog = true },
                            modifier = Modifier.testTag("add_vibration_history_button")
                        ) {
                            Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Log Manual", tint = BrandGreen, modifier = Modifier.size(28.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (vibrationLogs.isNotEmpty()) {
                        vibrationLogs.take(5).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .background(SoftBg, RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val unitTag = if (log.comments.contains("[")) {
                                            log.comments.substringAfter("[").substringBefore("]")
                                        } else {
                                            "Manual"
                                        }
                                        Text(
                                            text = unitTag,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = SlateGrey
                                        )
                                        
                                        // Status unit tag: Operasi, Standby, Rusak
                                        val statusColor = when (log.machineStatus) {
                                            "Operasi" -> BrandGreen
                                            "Standby" -> BrandOrange
                                            else -> BrandRed
                                        }
                                        val statusBg = when (log.machineStatus) {
                                            "Operasi" -> BrandGreenLight
                                            "Standby" -> Color(0xFFFFF3E0)
                                            else -> Color(0xFFFFEBEE)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(statusBg, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = log.machineStatus,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = statusColor
                                            )
                                        }

                                        // Shift badge
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = log.shift,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SlateGrey
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Nama: ${log.operatorName.ifEmpty { "Operator Centrifuge" }}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Vibrasi (mm/s):\n• Axial 1: ${log.driveEndVibration}  • Axial 2: ${log.nonDriveEndVibration}\n• Horizontal: ${log.motorBearingVibration}  • Vertikal: ${log.gearboxBearingVibration}",
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        lineHeight = 15.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Suhu: Bearing ${log.bearingTemp}°C • Motor ${log.motorTemp}°C",
                                        fontSize = 11.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val greasingLabel = when {
                                            log.greasingStatus == "Belum Masuk Jadwal" -> "Belum Masuk Jadwal"
                                            log.greasingStatus == "Ya" || log.isGreased -> "Ya"
                                            else -> "Tidak"
                                        }
                                        val greasingColor = when {
                                            log.greasingStatus == "Belum Masuk Jadwal" -> Color.Gray
                                            log.greasingStatus == "Ya" || log.isGreased -> BrandGreen
                                            else -> BrandRed
                                        }
                                        Text(
                                            text = "Greasing: $greasingLabel",
                                            fontSize = 10.sp,
                                            color = greasingColor,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Suara: ${log.soundState}",
                                            fontSize = 10.sp,
                                            color = if (log.soundState == "Normal") BrandGreen else BrandRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Kebocoran: ${if (log.hasLeakage) "Ya" else "Tidak"}",
                                            fontSize = 10.sp,
                                            color = if (log.hasLeakage) BrandRed else BrandGreen,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    val cleanedComment = if (log.comments.contains(" [")) {
                                        log.comments.substringBefore(" [")
                                    } else {
                                        log.comments
                                    }
                                    if (cleanedComment.isNotEmpty()) {
                                        Text(
                                            text = "Catatan: $cleanedComment",
                                            fontSize = 11.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 4.dp)) {
                                    Text(
                                        text = WibDateUtils.format("HH:mm", log.timestamp) + " WIB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateGrey
                                    )
                                    Text(
                                        text = WibDateUtils.format("dd MMM", log.timestamp),
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            "Belum ada riwayat pengukuran manual.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Modal Dialog to log Vibration manually
    if (showVibDialog) {
        Dialog(onDismissRequest = { showVibDialog = false }) {
            var selectedUnitForLog by remember { mutableStateOf(selectedUnit.id) }
            var selectedMachineStatus by remember { mutableStateOf("Operasi") }
            var operatorName by remember { mutableStateOf("") }
            var shiftSelection by remember { mutableStateOf("Pagi") }
            var deVib by remember { mutableStateOf("") }
            var ndeVib by remember { mutableStateOf("") }
            var motorVib by remember { mutableStateOf("") }
            var gearboxVib by remember { mutableStateOf("") }
            var bTemp by remember { mutableStateOf("") }
            var mTemp by remember { mutableStateOf("") }
            var greasingSelection by remember { mutableStateOf("Belum Masuk Jadwal") }
            var soundSelection by remember { mutableStateOf("Normal") }
            var hasLeakageSelection by remember { mutableStateOf(false) }
            var comments by remember { mutableStateOf("") }

            var activeVibInfoTitle by remember { mutableStateOf<String?>(null) }
            var activeVibInfoText by remember { mutableStateOf<String?>(null) }

            if (activeVibInfoText != null) {
                AlertDialog(
                    onDismissRequest = { activeVibInfoText = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informasi Titik Ukur",
                            tint = BrandGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    title = {
                        Text(
                            text = activeVibInfoTitle ?: "Informasi Titik Ukur",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SlateGrey
                        )
                    },
                    text = {
                        Text(
                            text = activeVibInfoText ?: "",
                            fontSize = 13.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 18.sp
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { activeVibInfoText = null }) {
                            Text("Tutup", fontWeight = FontWeight.Bold, color = BrandGreen)
                        }
                    },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
            }

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
                    Text("Lakukan pengukuran sesuai measurement point centrifuge", fontSize = 11.sp, color = Color.Black)

                    Text("Pilih Unit Sludge Centrifuge:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("SC-01", "SC-02", "SC-03", "SC-04", "SC-05", "SC-06", "SC-07", "SC-08").forEach { unit ->
                            val isChipSelected = selectedUnitForLog == unit
                            FilterChip(
                                selected = isChipSelected,
                                onClick = { selectedUnitForLog = unit },
                                label = {
                                    Text(
                                        text = unit,
                                        fontSize = 10.sp,
                                        color = Color.Black,
                                        fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF86EFAC),
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.White,
                                    labelColor = Color.Black
                                )
                            )
                        }
                    }

                    // Nama : | Input Teks |
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Nama  :",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        OutlinedTextField(
                            value = operatorName,
                            onValueChange = { operatorName = it },
                            placeholder = { Text("Ketik nama pemeriksa...", fontSize = 12.sp, color = Color.Gray) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = BrandGreen,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("operator_name_input")
                        )
                    }

                    // Shift   :   |Pagi|      |Malam|
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Shift   :",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        listOf("Pagi", "Malam").forEach { s ->
                            val isSelected = shiftSelection == s
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandGreen else Color(0xFFF1F5F9))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BrandGreen else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { shiftSelection = s }
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = s,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else SlateGrey
                                )
                            }
                        }
                    }

                    Text("Status Unit & Kondisi Operasi:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Operasi", "Standby", "Rusak").forEach { status ->
                            val isSelected = selectedMachineStatus == status
                            val chipBg = when (status) {
                                "Operasi" -> if (isSelected) BrandGreenLight else SoftBg
                                "Standby" -> if (isSelected) Color(0xFFFFF3E0) else SoftBg
                                else -> if (isSelected) Color(0xFFFFEBEE) else SoftBg
                            }
                            val chipBorderCol = when (status) {
                                "Operasi" -> if (isSelected) BrandGreen else Color.Transparent
                                "Standby" -> if (isSelected) BrandOrange else Color.Transparent
                                else -> if (isSelected) BrandRed else Color.Transparent
                            }
                            val textColor = when (status) {
                                "Operasi" -> if (isSelected) BrandGreen else SlateGrey
                                "Standby" -> if (isSelected) BrandOrange else SlateGrey
                                else -> if (isSelected) BrandRed else SlateGrey
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMachineStatus = status }
                                    .testTag("status_chip_${status.lowercase()}"),
                                colors = CardDefaults.cardColors(containerColor = chipBg),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.5.dp, chipBorderCol)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }

                    // Greasing, Suara, Kebocoran status toggles
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Greasing : |Ya| |Tidak| and below: |Belum Masuk Jadwal|
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "Greasing :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey,
                                modifier = Modifier
                                    .width(95.dp)
                                    .padding(top = 8.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Ya (Warna Hijau)
                                    val yaSelected = greasingSelection == "Ya"
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { greasingSelection = "Ya" }
                                            .testTag("greasing_ya_button"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (yaSelected) Color(0xFF2E7D32) else Color(0xFFE8F5E9)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(
                                            if (yaSelected) 2.dp else 1.dp,
                                            if (yaSelected) Color(0xFF1B5E20) else Color(0xFFA5D6A7)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Ya",
                                                fontSize = 12.sp,
                                                fontWeight = if (yaSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                color = if (yaSelected) Color.White else Color(0xFF1B5E20)
                                            )
                                        }
                                    }

                                    // Tidak (Warna Merah)
                                    val tidakSelected = greasingSelection == "Tidak"
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { greasingSelection = "Tidak" }
                                            .testTag("greasing_tidak_button"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (tidakSelected) Color(0xFFD32F2F) else Color(0xFFFFEBEE)
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(
                                            if (tidakSelected) 2.dp else 1.dp,
                                            if (tidakSelected) Color(0xFFB71C1C) else Color(0xFFFFCDD2)
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Tidak",
                                                fontSize = 12.sp,
                                                fontWeight = if (tidakSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                                color = if (tidakSelected) Color.White else Color(0xFFB71C1C)
                                            )
                                        }
                                    }
                                }

                                // Belum Masuk Jadwal (Tepat dibawah tulisan |Ya| dan |Tidak|, warna abu-abu jika dipilih)
                                val belumMasukJadwalSelected = greasingSelection == "Belum Masuk Jadwal"
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { greasingSelection = "Belum Masuk Jadwal" }
                                        .testTag("greasing_belum_masuk_jadwal_button"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (belumMasukJadwalSelected) Color(0xFF757575) else Color(0xFFF5F5F5)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        if (belumMasukJadwalSelected) 2.dp else 1.dp,
                                        if (belumMasukJadwalSelected) Color(0xFF424242) else Color(0xFFE0E0E0)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Belum Masuk Jadwal",
                                            fontSize = 12.sp,
                                            fontWeight = if (belumMasukJadwalSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (belumMasukJadwalSelected) Color.White else Color(0xFF616161)
                                        )
                                    }
                                }
                            }
                        }

                        // Suara : |Normal| |Abnormal|
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Suara :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey,
                                modifier = Modifier.width(95.dp)
                            )
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Normal (Warna Hijau)
                                val normalSelected = soundSelection == "Normal"
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { soundSelection = "Normal" }
                                        .testTag("suara_normal_button"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (normalSelected) Color(0xFF2E7D32) else Color(0xFFE8F5E9)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        if (normalSelected) 2.dp else 1.dp,
                                        if (normalSelected) Color(0xFF1B5E20) else Color(0xFFA5D6A7)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Normal",
                                            fontSize = 12.sp,
                                            fontWeight = if (normalSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (normalSelected) Color.White else Color(0xFF1B5E20)
                                        )
                                    }
                                }

                                // Abnormal (Warna Merah)
                                val abnormalSelected = soundSelection == "Abnormal"
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { soundSelection = "Abnormal" }
                                        .testTag("suara_abnormal_button"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (abnormalSelected) Color(0xFFD32F2F) else Color(0xFFFFEBEE)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        if (abnormalSelected) 2.dp else 1.dp,
                                        if (abnormalSelected) Color(0xFFB71C1C) else Color(0xFFFFCDD2)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Abnormal",
                                            fontSize = 12.sp,
                                            fontWeight = if (abnormalSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (abnormalSelected) Color.White else Color(0xFFB71C1C)
                                        )
                                    }
                                }
                            }
                        }

                        // Kebocoran : |Ya| |Tidak|
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Kebocoran :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey,
                                modifier = Modifier.width(95.dp)
                            )
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Ya (Warna Merah)
                                val yaLeakSelected = hasLeakageSelection
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { hasLeakageSelection = true }
                                        .testTag("kebocoran_ya_button"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (yaLeakSelected) Color(0xFFD32F2F) else Color(0xFFFFEBEE)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        if (yaLeakSelected) 2.dp else 1.dp,
                                        if (yaLeakSelected) Color(0xFFB71C1C) else Color(0xFFFFCDD2)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Ya",
                                            fontSize = 12.sp,
                                            fontWeight = if (yaLeakSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (yaLeakSelected) Color.White else Color(0xFFB71C1C)
                                        )
                                    }
                                }

                                // Tidak (Warna Hijau)
                                val tidakLeakSelected = !hasLeakageSelection
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { hasLeakageSelection = false }
                                        .testTag("kebocoran_tidak_button"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (tidakLeakSelected) Color(0xFF2E7D32) else Color(0xFFE8F5E9)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        if (tidakLeakSelected) 2.dp else 1.dp,
                                        if (tidakLeakSelected) Color(0xFF1B5E20) else Color(0xFFA5D6A7)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Tidak",
                                            fontSize = 12.sp,
                                            fontWeight = if (tidakLeakSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                            color = if (tidakLeakSelected) Color.White else Color(0xFF1B5E20)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("Parameter Vibrasi (mm/s):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        data class VibInputField(
                            val number: String,
                            val name: String,
                            val value: String,
                            val testTagStr: String,
                            val info: String,
                            val onValueChange: (String) -> Unit
                        )

                        val vibrationFields = listOf(
                            VibInputField(
                                number = "1",
                                name = "Axial 1",
                                value = deVib,
                                testTagStr = "axial_1_input",
                                info = "Titik ukur pada Bearing dekat dengan pulley",
                                onValueChange = { deVib = it }
                            ),
                            VibInputField(
                                number = "2",
                                name = "Axial 2",
                                value = ndeVib,
                                testTagStr = "axial_2_input",
                                info = "Titik ukur pada Bearing dengan dengan gland packing",
                                onValueChange = { ndeVib = it }
                            ),
                            VibInputField(
                                number = "3",
                                name = "Horizontal",
                                value = motorVib,
                                testTagStr = "horizontal_input",
                                info = "Titik ukur pada Body mesin sejajar dengan titik Axial 1",
                                onValueChange = { motorVib = it }
                            ),
                            VibInputField(
                                number = "4",
                                name = "Vertikal",
                                value = gearboxVib,
                                testTagStr = "vertikal_input",
                                info = "Titik ukur pada body mesin bagian atas tegak lurus dari titik Axial",
                                onValueChange = { gearboxVib = it }
                            )
                        )

                        vibrationFields.forEach { field ->
                            val fVal = field.value.toFloatOrNull()
                            val isRed = fVal != null && fVal > 4.0f
                            val isGreen = fVal != null && fVal <= 4.0f

                            val bgColor = when {
                                isRed -> Color(0xFFFFCDD2)
                                isGreen -> Color(0xFFC8E6C9)
                                else -> Color(0xFFF8FAFC)
                            }
                            val borderColor = when {
                                isRed -> Color(0xFFD32F2F)
                                isGreen -> Color(0xFF2E7D32)
                                else -> Color(0xFFCBD5E1)
                            }
                            val textColor = when {
                                isRed -> Color(0xFFB71C1C)
                                isGreen -> Color(0xFF1B5E20)
                                else -> Color(0xFF1E293B)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.width(132.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${field.number}. ${field.name} :",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SlateGrey,
                                        modifier = Modifier.weight(1f, fill = false),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    IconButton(
                                        onClick = {
                                            activeVibInfoTitle = "${field.number}. ${field.name}"
                                            activeVibInfoText = field.info
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info ${field.name}",
                                            tint = BrandGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                OutlinedTextField(
                                    value = field.value,
                                    onValueChange = field.onValueChange,
                                    placeholder = { Text("0.0", fontSize = 12.sp, color = Color.Gray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = bgColor,
                                        unfocusedContainerColor = bgColor,
                                        focusedBorderColor = borderColor,
                                        unfocusedBorderColor = borderColor,
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor
                                    ),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag(field.testTagStr)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "mm/s",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRed) Color(0xFFB71C1C) else if (isGreen) Color(0xFF1B5E20) else Color(0xFF64748B),
                                    modifier = Modifier.width(42.dp)
                                )
                            }
                        }
                    }

                    Text("Parameter Temperatur & Observasi:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val tempFields = listOf(
                            Triple("1. Suhu Bearing :", bTemp, "bearing_temp_input") to { v: String -> bTemp = v },
                            Triple("2. Suhu Motor :", mTemp, "motor_temp_input") to { v: String -> mTemp = v }
                        )

                        tempFields.forEach { (fieldInfo, onValueChange) ->
                            val (labelText, currentVal, testTagStr) = fieldInfo
                            val fVal = currentVal.toFloatOrNull()
                            val isRed = fVal != null && fVal > 80.0f
                            val isGreen = fVal != null && fVal <= 80.0f

                            val bgColor = when {
                                isRed -> Color(0xFFFFCDD2)
                                isGreen -> Color(0xFFC8E6C9)
                                else -> Color(0xFFF8FAFC)
                            }
                            val borderColor = when {
                                isRed -> Color(0xFFD32F2F)
                                isGreen -> Color(0xFF2E7D32)
                                else -> Color(0xFFCBD5E1)
                            }
                            val textColor = when {
                                isRed -> Color(0xFFB71C1C)
                                isGreen -> Color(0xFF1B5E20)
                                else -> Color(0xFF1E293B)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = labelText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SlateGrey,
                                    modifier = Modifier.width(132.dp)
                                )
                                OutlinedTextField(
                                    value = currentVal,
                                    onValueChange = onValueChange,
                                    placeholder = { Text("0.0", fontSize = 12.sp, color = Color.Gray) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = bgColor,
                                        unfocusedContainerColor = bgColor,
                                        focusedBorderColor = borderColor,
                                        unfocusedBorderColor = borderColor,
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor
                                    ),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag(testTagStr)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Superscript)) {
                                            append("o")
                                        }
                                        append("C")
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRed) Color(0xFFB71C1C) else if (isGreen) Color(0xFF1B5E20) else Color(0xFF64748B),
                                    modifier = Modifier.width(42.dp)
                                )
                            }
                        }

                        // 3. Catatan hasil observasi
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "3. Catatan hasil observasi :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            OutlinedTextField(
                                value = comments,
                                onValueChange = { comments = it },
                                placeholder = { Text("Tulis catatan hasil observasi...", fontSize = 12.sp, color = Color.DarkGray) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = BrandGreen,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("comments_input")
                            )
                        }
                    }

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
                                val motorVibValue = motorVib.toFloatOrNull() ?: 1.8f
                                val gearboxVibValue = gearboxVib.toFloatOrNull() ?: 2.0f
                                val bTempValue = bTemp.toFloatOrNull() ?: 55f
                                val mTempValue = mTemp.toFloatOrNull() ?: 48f
                                val maxVibVal = maxOf(deValue, ndeValue, motorVibValue, gearboxVibValue)
                                val alarm = if (maxVibVal >= 8.8f || bTempValue >= 70f) "Critical" else if (maxVibVal >= 4.5f) "Warning" else "Normal"

                                val finalComments = if (comments.trim().isEmpty()) {
                                    "Log manual [$selectedUnitForLog]"
                                } else {
                                    "$comments [$selectedUnitForLog]"
                                }

                                viewModel.addVibrationLog(
                                    VibrationLog(
                                        operatorName = operatorName.trim().ifEmpty { "Operator Centrifuge" },
                                        shift = shiftSelection,
                                        driveEndVibration = deValue,
                                        nonDriveEndVibration = ndeValue,
                                        motorBearingVibration = motorVibValue,
                                        gearboxBearingVibration = gearboxVibValue,
                                        bowlVibration = 0f,
                                        bearingTemp = bTempValue,
                                        motorTemp = mTempValue,
                                        isGreased = (greasingSelection == "Ya"),
                                        greasingStatus = greasingSelection,
                                        soundState = soundSelection,
                                        hasLeakage = hasLeakageSelection,
                                        alarmState = alarm,
                                        machineStatus = selectedMachineStatus,
                                        comments = finalComments
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF1F5F9),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isInput = selectedTab == 0
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedTab = 0 }
                        .testTag("tab_input_cilt"),
                    color = if (isInput) BrandGreen else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = if (isInput) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Checklist,
                            contentDescription = "Input CILT",
                            tint = if (isInput) Color.White else SlateGrey,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Input CILT",
                            fontSize = 13.sp,
                            fontWeight = if (isInput) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isInput) Color.White else SlateGrey
                        )
                    }
                }

                val isReport = selectedTab == 1
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedTab = 1 }
                        .testTag("tab_report_cilt"),
                    color = if (isReport) BrandGreen else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = if (isReport) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Report CILT",
                            tint = if (isReport) Color.White else SlateGrey,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Report CILT",
                            fontSize = 13.sp,
                            fontWeight = if (isReport) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isReport) Color.White else SlateGrey
                        )
                        if (ciltHistory.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = if (isReport) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
                            ) {
                                Text(
                                    text = "${ciltHistory.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReport) Color.White else SlateGrey,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (selectedTab == 0) {
                DailyCiltForm(onSave = {
                    onSaveCilt(it)
                    Toast.makeText(context, "Laporan Input CILT berhasil disimpan!", Toast.LENGTH_SHORT).show()
                }, history = ciltHistory)
            } else {
                ReportCiltView(ciltHistory = ciltHistory)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// REPORT CILT VIEW (Laporan Harian, Mingguan, Bulanan berdasarkan Input CILT)
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun ReportCiltView(ciltHistory: List<CiltCheck>) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("Hari Ini") } // "Hari Ini", "Seminggu", "Sebulan", "Tanggal"
    var selectedOperatorFilter by remember { mutableStateOf("Semua") } // "Semua", "Wahyu", "Abdul Aziz"
    var expandedLogId by remember { mutableStateOf<Long?>(null) }

    val now = System.currentTimeMillis()
    val startOfToday = remember(now) {
        WibDateUtils.getStartOfDay(now)
    }
    val todayEnd = remember(startOfToday) {
        startOfToday + 24 * 3600 * 1000L - 1L
    }
    var customStartDate by remember { mutableStateOf(startOfToday) }
    var customEndDate by remember { mutableStateOf(todayEnd) }

    fun pickStartDate() {
        val cal = WibDateUtils.getCalendar(customStartDate)
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = WibDateUtils.getCalendar().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val newStart = newCal.timeInMillis
                customStartDate = newStart
                if (customEndDate < newStart) {
                    customEndDate = newStart + 24 * 60 * 60 * 1000L - 1L
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickEndDate() {
        val cal = WibDateUtils.getCalendar(customEndDate)
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = WibDateUtils.getCalendar().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val newEnd = newCal.timeInMillis
                customEndDate = newEnd
                if (customStartDate > newEnd) {
                    customStartDate = WibDateUtils.getCalendar(newEnd).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Filter berdasarkan Periode (Hari Ini, Seminggu, Sebulan, Tanggal)
    val periodFilteredChecks = remember(ciltHistory, selectedPeriod, now, startOfToday, customStartDate, customEndDate) {
        when (selectedPeriod) {
            "Hari Ini" -> {
                val daily = ciltHistory.filter { it.timestamp >= startOfToday || it.timestamp >= (now - 24 * 3600 * 1000L) }
                if (daily.isNotEmpty()) daily else ciltHistory.take(2)
            }
            "Seminggu" -> {
                val weekThreshold = now - 7L * 24 * 3600 * 1000L
                val weekly = ciltHistory.filter { it.timestamp >= weekThreshold }
                if (weekly.isNotEmpty()) weekly else ciltHistory.take(6)
            }
            "Sebulan" -> {
                val monthThreshold = now - 30L * 24 * 3600 * 1000L
                val monthly = ciltHistory.filter { it.timestamp >= monthThreshold }
                if (monthly.isNotEmpty()) monthly else ciltHistory
            }
            else -> { // "Tanggal"
                ciltHistory.filter { it.timestamp in customStartDate..customEndDate }
            }
        }
    }

    // Filter berdasarkan Operator
    val finalFilteredChecks = remember(periodFilteredChecks, selectedOperatorFilter) {
        if (selectedOperatorFilter == "Semua") {
            periodFilteredChecks
        } else {
            periodFilteredChecks.filter { it.operatorName.contains(selectedOperatorFilter, ignoreCase = true) }
        }
    }

    // Label range tanggal periode WIB
    val periodDateRangeText = remember(selectedPeriod, now, customStartDate, customEndDate) {
        when (selectedPeriod) {
            "Hari Ini" -> "Hari Ini • ${WibDateUtils.format("dd MMM yyyy", now)}"
            "Seminggu" -> {
                val weekStart = WibDateUtils.format("dd MMM", now - 7L * 24 * 3600 * 1000L)
                "7 Hari Terakhir • $weekStart - ${WibDateUtils.format("dd MMM yyyy", now)}"
            }
            "Sebulan" -> {
                val monthStart = WibDateUtils.format("dd MMM", now - 30L * 24 * 3600 * 1000L)
                "30 Hari Terakhir • $monthStart - ${WibDateUtils.format("dd MMM yyyy", now)}"
            }
            else -> {
                "${WibDateUtils.format("dd MMM yyyy", customStartDate)} - ${WibDateUtils.format("dd MMM yyyy", customEndDate)}"
            }
        }
    }

    // Metrik Kalkulasi
    val totalChecks = finalFilteredChecks.size
    fun countCheckCompleted(c: CiltCheck): Int {
        var count = 0
        if (c.isAreaCleaned) count++
        if (c.isMachineCleaned) count++
        if (c.isDrainageCleaned) count++
        if (c.isVibrationSoundChecked) count++
        if (c.isTemperatureChecked) count++
        if (c.isLeakChecked) count++
        if (c.isComponentsConditionChecked) count++
        if (c.isGreasingBearingChecked) count++
        if (c.isOilLevelChecked) count++
        if (c.isFoundationBoltsTightened) count++
        if (c.isNoLooseBoltsChecked) count++
        return count
    }

    // 1. Cleaning (3 items)
    val areaCleanedCount = finalFilteredChecks.count { it.isAreaCleaned }
    val machineCleanedCount = finalFilteredChecks.count { it.isMachineCleaned }
    val drainageCleanedCount = finalFilteredChecks.count { it.isDrainageCleaned }
    val totalCleaningDone = areaCleanedCount + machineCleanedCount + drainageCleanedCount
    val cleaningRate = if (totalChecks > 0) (totalCleaningDone * 100) / (totalChecks * 3) else 0

    // 2. Inspection (4 items)
    val vibrationSoundCount = finalFilteredChecks.count { it.isVibrationSoundChecked }
    val tempCount = finalFilteredChecks.count { it.isTemperatureChecked }
    val leakCount = finalFilteredChecks.count { it.isLeakChecked }
    val componentsCount = finalFilteredChecks.count { it.isComponentsConditionChecked }
    val totalInspectionDone = vibrationSoundCount + tempCount + leakCount + componentsCount
    val inspectionRate = if (totalChecks > 0) (totalInspectionDone * 100) / (totalChecks * 4) else 0

    // 3. Lubrication (2 items)
    val greasingBearingCount = finalFilteredChecks.count { it.isGreasingBearingChecked }
    val oilLevelCount = finalFilteredChecks.count { it.isOilLevelChecked }
    val totalLubricationDone = greasingBearingCount + oilLevelCount
    val lubricationRate = if (totalChecks > 0) (totalLubricationDone * 100) / (totalChecks * 2) else 0

    // 4. Tightening (2 items)
    val foundationBoltsCount = finalFilteredChecks.count { it.isFoundationBoltsTightened }
    val noLooseBoltsCount = finalFilteredChecks.count { it.isNoLooseBoltsChecked }
    val totalTighteningDone = foundationBoltsCount + noLooseBoltsCount
    val tighteningRate = if (totalChecks > 0) (totalTighteningDone * 100) / (totalChecks * 2) else 0

    // Notes count in filtered checks
    val notesCount = finalFilteredChecks.count {
        it.comments.replace("[Pagi]", "").replace("[Malam]", "").trim().isNotBlank() ||
        it.cleaningNotes.isNotBlank() || it.inspectionNotes.isNotBlank() ||
        it.lubricationNotes.isNotBlank() || it.tighteningNotes.isNotBlank()
    }

    // Operator Contribution & Target Calculations
    val daysCount = remember(selectedPeriod, customStartDate, customEndDate) {
        if (selectedPeriod == "Tanggal") {
            val diff = (customEndDate - customStartDate).coerceAtLeast(0L)
            (diff / (24 * 3600 * 1000L) + 1).toInt().coerceAtLeast(1)
        } else 1
    }
    val (wahyuTarget, abdulAzizTarget) = when (selectedPeriod) {
        "Hari Ini" -> Pair(1, 1)
        "Seminggu" -> Pair(7, 7)
        "Sebulan" -> Pair(30, 30)
        else -> Pair(daysCount, daysCount)
    }

    val wahyuChecks = periodFilteredChecks.filter { it.operatorName.contains("Wahyu", ignoreCase = true) }
    val wahyuActual = wahyuChecks.size
    val wahyuExecutionRate = if (wahyuTarget > 0) (wahyuActual.toFloat() / wahyuTarget.toFloat() * 100f) else 0f
    val wahyuCompliance = if (wahyuChecks.isNotEmpty()) (wahyuChecks.sumOf { countCheckCompleted(it) } * 100) / (wahyuChecks.size * 11) else 0

    val abdulAzizChecks = periodFilteredChecks.filter { it.operatorName.contains("Abdul", ignoreCase = true) }
    val abdulAzizActual = abdulAzizChecks.size
    val abdulAzizExecutionRate = if (abdulAzizTarget > 0) (abdulAzizActual.toFloat() / abdulAzizTarget.toFloat() * 100f) else 0f
    val abdulAzizCompliance = if (abdulAzizChecks.isNotEmpty()) (abdulAzizChecks.sumOf { countCheckCompleted(it) } * 100) / (abdulAzizChecks.size * 11) else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // A. FILTER PERIODE: Harian, Mingguan, Bulanan
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Periode Laporan CILT :",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                        }
                        Text(
                            text = periodDateRangeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandGreen
                        )
                    }

                    // 4 Tombol Periode Segmented: | Hari Ini | | Seminggu | | Sebulan | | Tanggal |
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Hari Ini", "Seminggu", "Sebulan", "Tanggal").forEach { period ->
                                val isSelected = selectedPeriod == period
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) BrandGreen else Color.Transparent)
                                        .clickable { selectedPeriod = period }
                                        .padding(vertical = 9.dp)
                                        .testTag("report_period_$period"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = period,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SlateGrey
                                    )
                                }
                            }
                        }
                    }

                    if (selectedPeriod == "Tanggal") {
                        DateRangeFilterPicker(
                            startDateMillis = customStartDate,
                            endDateMillis = customEndDate,
                            onPickStartDate = { pickStartDate() },
                            onPickEndDate = { pickEndDate() }
                        )
                    }

                    // Filter Operator: Semua, Wahyu, Abdul Aziz
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Operator:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        listOf("Semua", "Wahyu", "Abdul Aziz").forEach { op ->
                            val isOpSelected = selectedOperatorFilter == op
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isOpSelected) BrandGreenLight else Color(0xFFF8FAFC))
                                    .border(
                                        width = 1.dp,
                                        color = if (isOpSelected) BrandGreen else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedOperatorFilter = op }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("filter_op_$op")
                            ) {
                                Text(
                                    text = op,
                                    fontSize = 11.sp,
                                    fontWeight = if (isOpSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isOpSelected) BrandGreen else SlateGrey
                                )
                            }
                        }
                    }
                }
            }
        }

        // B. KINERJA OPERATOR (Sinkron dengan filter operator)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (selectedOperatorFilter) {
                                "Wahyu" -> "Kinerja Operator: Wahyu"
                                "Abdul Aziz" -> "Kinerja Operator: Abdul Aziz"
                                else -> "Kinerja Operator Pelaksana"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        Text(
                            text = "Target Periode $selectedPeriod",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    if (selectedPeriod == "Hari Ini") {
                        val missingToday = mutableListOf<String>()
                        if (wahyuActual == 0) missingToday.add("Wahyu")
                        if (abdulAzizActual == 0) missingToday.add("Abdul Aziz")
                        if (missingToday.isNotEmpty()) {
                            Surface(
                                color = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFD32F2F),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Operator belum CILT hari ini: ${missingToday.joinToString(", ")} (saat jam pengecekan ${WibDateUtils.format("HH:mm", now)} WIB)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB71C1C)
                                    )
                                }
                            }
                        }
                    }

                    if (selectedOperatorFilter == "Semua") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CiltOperatorCard(
                                name = "Wahyu",
                                target = wahyuTarget,
                                actual = wahyuActual,
                                executionRate = wahyuExecutionRate,
                                compliance = wahyuCompliance,
                                modifier = Modifier.weight(1f)
                            )
                            CiltOperatorCard(
                                name = "Abdul Aziz",
                                target = abdulAzizTarget,
                                actual = abdulAzizActual,
                                executionRate = abdulAzizExecutionRate,
                                compliance = abdulAzizCompliance,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else if (selectedOperatorFilter == "Wahyu") {
                        CiltOperatorCard(
                            name = "Wahyu",
                            target = wahyuTarget,
                            actual = wahyuActual,
                            executionRate = wahyuExecutionRate,
                            compliance = wahyuCompliance,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else { // "Abdul Aziz"
                        CiltOperatorCard(
                            name = "Abdul Aziz",
                            target = abdulAzizTarget,
                            actual = abdulAzizActual,
                            executionRate = abdulAzizExecutionRate,
                            compliance = abdulAzizCompliance,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // C. CATATAN / TEMUAN KHUSUS DI LAPANGAN
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Catatan / Temuan Khusus di Lapangan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateGrey
                        )
                        Text(
                            text = if (notesCount > 0) "$notesCount temuan/catatan tercatat di lapangan ($selectedPeriod)" else "Tidak ada catatan abnormalitas pada periode $selectedPeriod",
                            fontSize = 10.5.sp,
                            color = Color.Gray
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (notesCount > 0) Color(0xFFFEF3C7) else Color(0xFFDCFCE7))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "$notesCount Catatan",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (notesCount > 0) BrandOrange else Color(0xFF166534)
                        )
                    }
                }
            }
        }

        // D. ANALISIS 4 AKTIVITAS CILT (Cleaning, Inspection, Lubrication, Tightening)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Pencapaian 4 Aktivitas CILT ($selectedPeriod)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )

                    // Aktivitas 1: Cleaning
                    PillarReportItem(
                        letter = "C",
                        title = "Cleaning (Pembersihan)",
                        rate = cleaningRate,
                        items = listOf(
                            "Area Bersih dari Sludge & Oli" to "$areaCleanedCount / $totalChecks",
                            "Mesin Bebas dari Kerak" to "$machineCleanedCount / $totalChecks",
                            "Drainase Tidak Sumbat" to "$drainageCleanedCount / $totalChecks"
                        )
                    )

                    Divider(color = Color(0xFFF1F5F9))

                    // Aktivitas 2: Inspection
                    PillarReportItem(
                        letter = "I",
                        title = "Inspection (Inspeksi)",
                        rate = inspectionRate,
                        items = listOf(
                            "Vibrasi & Suara Normal" to "$vibrationSoundCount / $totalChecks",
                            "Temperatur Bearing & Motor Normal" to "$tempCount / $totalChecks",
                            "Tidak Ada Kebocoran Pipa/Valve/Coupling" to "$leakCount / $totalChecks",
                            "Nozzle/Belt/Coupling Kondisi Baik" to "$componentsCount / $totalChecks"
                        )
                    )

                    Divider(color = Color(0xFFF1F5F9))

                    // Aktivitas 3: Lubrication
                    PillarReportItem(
                        letter = "L",
                        title = "Lubrication (Pelumasan)",
                        rate = lubricationRate,
                        items = listOf(
                            "Greasing Bearing Sesuai Jadwal & Takaran" to "$greasingBearingCount / $totalChecks",
                            "Level Oli Transfluid Kopling Normal" to "$oilLevelCount / $totalChecks"
                        )
                    )

                    Divider(color = Color(0xFFF1F5F9))

                    // Aktivitas 4: Tightening
                    PillarReportItem(
                        letter = "T",
                        title = "Tightening (Pengencangan)",
                        rate = tighteningRate,
                        items = listOf(
                            "Baut Pondasi & Komponen Dikencangkan" to "$foundationBoltsCount / $totalChecks",
                            "Tidak Ada Baut Longgar & Lepas" to "$noLooseBoltsCount / $totalChecks"
                        )
                    )
                }
            }
        }

        // E. DAFTAR LOG LAPORAN CILT TERPERINCI
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Riwayat Pemeriksaan ($totalChecks)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey
                )
                Text(
                    text = "Klik kartu untuk rincian",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }

        if (finalFilteredChecks.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                        Text("Belum Ada Data CILT pada Periode Ini", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SlateGrey)
                        Text(
                            "Silakan gunakan menu 'Input CILT' untuk mencatat checklist baru.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(finalFilteredChecks, key = { it.id }) { check ->
                val completedCount = countCheckCompleted(check)
                val isPerfect = completedCount == 11
                val isExpanded = expandedLogId == check.id

                val shift = remember(check.comments) {
                    when {
                        check.comments.contains("[Malam]", ignoreCase = true) -> "Shift Malam"
                        check.comments.contains("[Pagi]", ignoreCase = true) -> "Shift Pagi"
                        else -> "Shift Pagi"
                    }
                }

                val cleanNotes = remember(check.comments) {
                    check.comments.replace("[Pagi]", "").replace("[Malam]", "").trim()
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, if (isPerfect) Color(0xFFBBF7D0) else Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedLogId = if (isExpanded) null else check.id }
                        .testTag("report_cilt_item_${check.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BrandGreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                                }
                                Column {
                                    Text(
                                        text = check.operatorName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SlateGrey
                                    )
                                    Text(
                                        text = "$shift • ${WibDateUtils.format("dd MMM yyyy, HH:mm", check.timestamp)} WIB",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Badge Kepatuhan
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isPerfect) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isPerfect) "11/11 (100% OK)" else "$completedCount/11 Item",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPerfect) Color(0xFF166534) else Color(0xFF92400E)
                                )
                            }
                        }

                        // 4 Status Pilar Chips Ringkas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val cOk = check.isAreaCleaned && check.isMachineCleaned && check.isDrainageCleaned
                            val iOk = check.isVibrationSoundChecked && check.isTemperatureChecked && check.isLeakChecked && check.isComponentsConditionChecked
                            val lOk = check.isGreasingBearingChecked && check.isOilLevelChecked
                            val tOk = check.isFoundationBoltsTightened && check.isNoLooseBoltsChecked

                            PillarStatusChip("C", cOk, Modifier.weight(1f))
                            PillarStatusChip("I", iOk, Modifier.weight(1f))
                            PillarStatusChip("L", lOk, Modifier.weight(1f))
                            PillarStatusChip("T", tOk, Modifier.weight(1f))
                        }

                        // Catatan Temuan Khusus Kategori jika diisi
                        if (check.cleaningNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF0F9FF),
                                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Temuan Cleaning:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                    Text(check.cleaningNotes, fontSize = 10.sp, color = Color(0xFF0C4A6E))
                                }
                            }
                        }

                        if (check.inspectionNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFF7ED),
                                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Temuan Inspection:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                    Text(check.inspectionNotes, fontSize = 10.sp, color = Color(0xFF7C2D12))
                                }
                            }
                        }

                        if (check.lubricationNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFAF5FF),
                                border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Temuan Lubrication:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                    Text(check.lubricationNotes, fontSize = 10.sp, color = Color(0xFF581C87))
                                }
                            }
                        }

                        if (check.tighteningNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Temuan Tightening:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                    Text(check.tighteningNotes, fontSize = 10.sp, color = Color(0xFF064E3B))
                                }
                            }
                        }

                        if (cleanNotes.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                            ) {
                                Text(
                                    text = "Catatan: $cleanNotes",
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Tombol Toggle Rincian
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(if (check.isSynced) BrandGreenLight else Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (check.isSynced) "Synced to Jakarta" else "Berau Local",
                                    fontSize = 9.sp,
                                    color = if (check.isSynced) BrandGreen else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (isExpanded) "Sembunyikan Rincian" else "Lihat 11 Rincian Item",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandGreen
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Accordion Rincian 11 Item
                        if (isExpanded) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Rincian Checklist Lapangan:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    // 1. Cleaning
                                    CheckDetailRow("Area sekitar bersih dari sludge & oli", check.isAreaCleaned)
                                    CheckDetailRow("Seluruh mesin & area bebas kerak", check.isMachineCleaned)
                                    CheckDetailRow("Drainase tidak sumbat", check.isDrainageCleaned)
                                    // 2. Inspection
                                    CheckDetailRow("Vibrasi & suara normal", check.isVibrationSoundChecked)
                                    CheckDetailRow("Temperatur bearing & motor normal", check.isTemperatureChecked)
                                    CheckDetailRow("Tidak ada kebocoran pipa/valve/coupling/packing", check.isLeakChecked)
                                    CheckDetailRow("Nozzle, holder, belt, coupling, baut baik", check.isComponentsConditionChecked)
                                    // 3. Lubrication
                                    CheckDetailRow("Greasing bearing sesuai jadwal & takaran", check.isGreasingBearingChecked)
                                    CheckDetailRow("Level oli transfluid kopling normal", check.isOilLevelChecked)
                                    // 4. Tightening
                                    CheckDetailRow("Baut pondasi mesin, cover, flange dikencangkan", check.isFoundationBoltsTightened)
                                    CheckDetailRow("Tidak ada baut longgar & lepas (tidak terpasang)", check.isNoLooseBoltsChecked)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PillarStatusChip(pillar: String, isOk: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isOk) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
        border = BorderStroke(1.dp, if (isOk) Color(0xFF86EFAC) else Color(0xFFFDE68A))
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$pillar: ${if (isOk) "✓ OK" else "⚠"}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOk) Color(0xFF166534) else Color(0xFF92400E)
            )
        }
    }
}

@Composable
private fun CheckDetailRow(title: String, checked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 10.sp, color = if (checked) SlateGrey else Color.Gray)
        Text(
            text = if (checked) "✓ Selesai" else "✗ Belum",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (checked) Color(0xFF166534) else Color(0xFFDC2626)
        )
    }
}

@Composable
private fun CiltOperatorCard(
    name: String,
    target: Int,
    actual: Int,
    executionRate: Float,
    compliance: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                    Text(name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (executionRate >= 100f) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (executionRate >= 100f) "Capai ✓" else "${String.format(Locale.US, "%.0f", executionRate)}%",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (executionRate >= 100f) Color(0xFF166534) else Color(0xFF92400E)
                    )
                }
            }

            // 1. Target & Aktual Pelaksanaan
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Pelaksanaan:", fontSize = 10.sp, color = Color.Gray)
                    Text("$actual / $target Kali", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                }
                LinearProgressIndicator(
                    progress = { (executionRate / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (executionRate >= 100f) BrandGreen else BrandOrange,
                    trackColor = Color(0xFFE2E8F0)
                )
                Text(
                    text = "Realisasi: ${String.format(Locale.US, "%.1f", executionRate)}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (executionRate >= 100f) Color(0xFF166534) else BrandOrange
                )
            }

            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)

            // 2. Kepatuhan SOP (13 item)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kepatuhan SOP:", fontSize = 10.sp, color = Color.Gray)
                    Text("$compliance%", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                }
                LinearProgressIndicator(
                    progress = { (compliance / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BrandGreen,
                    trackColor = Color(0xFFE2E8F0)
                )
                Text("13 Item CILT", fontSize = 9.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun PillarReportItem(
    letter: String,
    title: String,
    rate: Int,
    items: List<Pair<String, String>>
) {
    val pillarColor = when (letter) {
        "C" -> Color(0xFF0284C7) // Sky Blue
        "I" -> Color(0xFFEA580C) // Amber Orange
        "L" -> Color(0xFF7C3AED) // Purple
        "T" -> Color(0xFF059669) // Emerald
        else -> BrandGreen
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(pillarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(letter, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (rate >= 90) Color(0xFFDCFCE7) else if (rate >= 75) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = "$rate%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rate >= 90) Color(0xFF166534) else if (rate >= 75) Color(0xFF92400E) else Color(0xFFDC2626),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (rate / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = pillarColor,
                trackColor = Color(0xFFE2E8F0)
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items.forEach { (name, count) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, fontSize = 11.sp, color = Color.Gray)
                        Text(count, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SlateGrey)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyCiltForm(onSave: (CiltCheck) -> Unit, history: List<CiltCheck>) {
    val displayedHistory = remember(history) {
        history.sortedByDescending { it.timestamp }.take(10)
    }

    var operatorName by remember { mutableStateOf("Wahyu") }
    var shiftSelection by remember { mutableStateOf("Pagi") }

    // Cleaning checklists (3 items)
    var areaCleaned by remember { mutableStateOf(false) }
    var machineCleaned by remember { mutableStateOf(false) }
    var drainageCleaned by remember { mutableStateOf(false) }
    var cleaningNotes by remember { mutableStateOf("") }

    // Inspection checklists (4 items)
    var vibrationSoundChecked by remember { mutableStateOf(false) }
    var temperatureChecked by remember { mutableStateOf(false) }
    var leakChecked by remember { mutableStateOf(false) }
    var componentsConditionChecked by remember { mutableStateOf(false) }
    var inspectionNotes by remember { mutableStateOf("") }

    // Lubrication checklists (2 items)
    var greasingBearingChecked by remember { mutableStateOf(false) }
    var oilLevelChecked by remember { mutableStateOf(false) }
    var lubricationNotes by remember { mutableStateOf("") }

    // Tightening checklists (2 items)
    var foundationBoltsTightened by remember { mutableStateOf(false) }
    var noLooseBoltsChecked by remember { mutableStateOf(false) }
    var tighteningNotes by remember { mutableStateOf("") }

    // Real-time calculation
    val completedCount = listOf(
        areaCleaned, machineCleaned, drainageCleaned,
        vibrationSoundChecked, temperatureChecked, leakChecked, componentsConditionChecked,
        greasingBearingChecked, oilLevelChecked,
        foundationBoltsTightened, noLooseBoltsChecked
    ).count { it }
    val totalItems = 11
    val completionPercent = if (totalItems > 0) (completedCount * 100) / totalItems else 0
    val allCompleted = completedCount == totalItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // OPERATOR & SHIFT SELECTION CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Informasi Pelaksana & Shift",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )

                    // 1. Pilih Operator
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Nama Operator :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val operators = listOf("Wahyu", "Abdul Aziz")
                            operators.forEach { op ->
                                val isSelected = operatorName == op
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { operatorName = op }
                                        .testTag("cilt_op_$op"),
                                    color = if (isSelected) BrandGreenLight else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) BrandGreen else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) BrandGreen else Color(0xFFCBD5E1)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = op.take(1),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Text(
                                            text = op,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandGreen else SlateGrey
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Pilih Shift (Pagi / Malam)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Shift Kerja :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val shifts = listOf(
                                "Pagi" to "☀️ Shift Pagi",
                                "Malam" to "🌙 Shift Malam"
                            )
                            shifts.forEach { (shiftKey, shiftLabel) ->
                                val isSelected = shiftSelection == shiftKey
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { shiftSelection = shiftKey },
                                    color = if (isSelected) BrandGreenLight else Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) BrandGreen else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = shiftLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) BrandGreen else SlateGrey,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // C. 1. CLEANING CARD (Azure Blue)
        item {
            ChecklistCategoryCard(
                title = "1. CLEANING (Pembersihan)",
                icon = Icons.Default.CleaningServices,
                accentColor = Color(0xFF0284C7),
                items = listOf(
                    ChecklistRowItem("Area Sekitar Mesin Sludge Centrifuge bersih dari tumpahan sludge dan oli", areaCleaned) { areaCleaned = it },
                    ChecklistRowItem("Seluruh mesin dan area disekitarnya bersih dari kerak", machineCleaned) { machineCleaned = it },
                    ChecklistRowItem("Drainase tidak sumbat", drainageCleaned) { drainageCleaned = it }
                ),
                notesValue = cleaningNotes,
                onNotesChange = { cleaningNotes = it },
                notesPlaceholder = "Tuliskan temuan saat melakukan cleaning (contoh: genangan oli di dekat fondasi, kerak tebal di bowl, dll)..."
            )
        }

        // D. 2. INSPECTION CARD (Amber Orange)
        item {
            ChecklistCategoryCard(
                title = "2. INSPECTION (Inspeksi)",
                icon = Icons.Default.Search,
                accentColor = Color(0xFFEA580C),
                items = listOf(
                    ChecklistRowItem("Vibrasi dan suara normal", vibrationSoundChecked) { vibrationSoundChecked = it },
                    ChecklistRowItem("Temperatur bearing dan motor dalam batas normal", temperatureChecked) { temperatureChecked = it },
                    ChecklistRowItem("Tidak ada kebocoran pada pipa, valve, tranfluid coupling, gland packing", leakChecked) { leakChecked = it },
                    ChecklistRowItem("Nozzle, nozzle holder, belt, coupling, cover, baut dalam kondisi baik", componentsConditionChecked) { componentsConditionChecked = it }
                ),
                notesValue = inspectionNotes,
                onNotesChange = { inspectionNotes = it },
                notesPlaceholder = "Tuliskan temuan saat melakukan inspection (contoh: getaran naik, suara mendesing, kebocoran packing pipa, dll)..."
            )
        }

        // E. 3. LUBRICATION CARD (Deep Purple)
        item {
            ChecklistCategoryCard(
                title = "3. LUBRICATION (Pelumasan)",
                icon = Icons.Default.Opacity,
                accentColor = Color(0xFF7C3AED),
                items = listOf(
                    ChecklistRowItem("Greasing bearing sesuai jadwal dan takaran", greasingBearingChecked) { greasingBearingChecked = it },
                    ChecklistRowItem("Level oli transfluid kopling dalam batas normal", oilLevelChecked) { oilLevelChecked = it }
                ),
                notesValue = lubricationNotes,
                onNotesChange = { lubricationNotes = it },
                notesPlaceholder = "Tuliskan temuan saat melakukan lubrication (contoh: level oli transfluid rendah, grease nipel macet, dll)..."
            )
        }

        // F. 4. TIGHTENING CARD (Emerald Green)
        item {
            ChecklistCategoryCard(
                title = "4. TIGHTENING (Pengencangan)",
                icon = Icons.Default.Build,
                accentColor = Color(0xFF059669),
                items = listOf(
                    ChecklistRowItem("Baut pondasi mesin, cover, flange, nozzle holder, motor, coupling dikencangkan sesuai kebutuhan", foundationBoltsTightened) { foundationBoltsTightened = it },
                    ChecklistRowItem("Tidak ada baut longgar dan lepas (tidak terpasang).", noLooseBoltsChecked) { noLooseBoltsChecked = it }
                ),
                notesValue = tighteningNotes,
                onNotesChange = { tighteningNotes = it },
                notesPlaceholder = "Tuliskan temuan saat melakukan tightening (contoh: baut cover longgar, baut flange aus/perlu diganti, dll)..."
            )
        }

        // G. TOMBOL SIMPAN
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            if (operatorName.isBlank()) {
                                operatorName = "Operator Berau"
                            }
                            val formattedComments = if (shiftSelection.isNotBlank()) "[$shiftSelection]" else ""

                            onSave(
                                CiltCheck(
                                    timestamp = System.currentTimeMillis(),
                                    operatorName = operatorName,
                                    // 1. Cleaning
                                    areaCleaned = areaCleaned,
                                    machineCleaned = machineCleaned,
                                    drainageCleaned = drainageCleaned,
                                    cleaningNotes = cleaningNotes.trim(),
                                    // 2. Inspection
                                    vibrationSoundChecked = vibrationSoundChecked,
                                    temperatureChecked = temperatureChecked,
                                    leakChecked = leakChecked,
                                    componentsConditionChecked = componentsConditionChecked,
                                    inspectionNotes = inspectionNotes.trim(),
                                    // 3. Lubrication
                                    greasingBearingChecked = greasingBearingChecked,
                                    oilLevelChecked = oilLevelChecked,
                                    lubricationNotes = lubricationNotes.trim(),
                                    // 4. Tightening
                                    foundationBoltsTightened = foundationBoltsTightened,
                                    noLooseBoltsChecked = noLooseBoltsChecked,
                                    tighteningNotes = tighteningNotes.trim(),
                                    // Legacy mapping for backwards compatibility
                                    nozzleCleaned = drainageCleaned,
                                    bowlCleaned = machineCleaned,
                                    nozzleChecked = componentsConditionChecked,
                                    vibrationChecked = vibrationSoundChecked,
                                    instrumentChecked = temperatureChecked,
                                    bearingGreased = greasingBearingChecked,
                                    couplingGreased = greasingBearingChecked,
                                    nozzleBoltsTightened = foundationBoltsTightened,
                                    fittingPipesTightened = noLooseBoltsChecked,
                                    beltTensionChecked = componentsConditionChecked,
                                    comments = formattedComments
                                )
                            )
                            // Reset form fields
                            operatorName = "Wahyu"
                            cleaningNotes = ""
                            inspectionNotes = ""
                            lubricationNotes = ""
                            tighteningNotes = ""
                            areaCleaned = false
                            machineCleaned = false
                            drainageCleaned = false
                            vibrationSoundChecked = false
                            temperatureChecked = false
                            leakChecked = false
                            componentsConditionChecked = false
                            greasingBearingChecked = false
                            oilLevelChecked = false
                            foundationBoltsTightened = false
                            noLooseBoltsChecked = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_cilt_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan & Kirim Laporan CILT ($completedCount/$totalItems)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // H. HISTORI PEMERIKSAAN CILT (MAKSIMAL 10 RIWAYAT)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Histori Pemeriksaan CILT",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey
                )
                Text(
                    text = if (history.size > 10) "10 Terakhir (Maks 10)" else "${displayedHistory.size} Riwayat",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (displayedHistory.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                        Text("Belum Ada Catatan CILT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
                        Text("Checklist yang Anda simpan akan muncul di sini.", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        } else {
            items(displayedHistory, key = { it.id }) { check ->
                val checkCompleted = listOf(
                    check.isAreaCleaned, check.isMachineCleaned, check.isDrainageCleaned,
                    check.isVibrationSoundChecked, check.isTemperatureChecked, check.isLeakChecked, check.isComponentsConditionChecked,
                    check.isGreasingBearingChecked, check.isOilLevelChecked,
                    check.isFoundationBoltsTightened, check.isNoLooseBoltsChecked
                ).count { it }
                val isPerfect = checkCompleted == 11

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(BrandGreenLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = check.operatorName.take(1),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandGreen
                                    )
                                }
                                Text(
                                    text = check.operatorName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = SlateGrey
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isPerfect) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (isPerfect) "11/11 (100% OK)" else "$checkCompleted/11 Selesai",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPerfect) Color(0xFF166534) else Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = WibDateUtils.format("dd MMM yyyy, HH:mm", check.timestamp) + " WIB",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )

                            Box(
                                modifier = Modifier
                                    .background(if (check.isSynced) BrandGreenLight else Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (check.isSynced) "Synced to Jakarta" else "Berau Local",
                                    fontSize = 9.sp,
                                    color = if (check.isSynced) BrandGreen else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Catatan Temuan per Kategori CILT jika ada
                        if (check.cleaningNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF0F9FF),
                                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Temuan Cleaning:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                    Text(check.cleaningNotes, fontSize = 10.sp, color = Color(0xFF0C4A6E))
                                }
                            }
                        }

                        if (check.inspectionNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFF7ED),
                                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Temuan Inspection:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                    Text(check.inspectionNotes, fontSize = 10.sp, color = Color(0xFF7C2D12))
                                }
                            }
                        }

                        if (check.lubricationNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFAF5FF),
                                border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Temuan Lubrication:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                    Text(check.lubricationNotes, fontSize = 10.sp, color = Color(0xFF581C87))
                                }
                            }
                        }

                        if (check.tighteningNotes.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("Temuan Tightening:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                                    Text(check.tighteningNotes, fontSize = 10.sp, color = Color(0xFF064E3B))
                                }
                            }
                        }

                        if (check.comments.isNotBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = check.comments,
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
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
                    Text("Pemeriksaan ini berfokus pada 3 pencegahan utama: Penyumbatan Nozzle, Keausan Bearing, & Bowl Imbalance.", fontSize = 11.sp, color = Color.Black)

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
                        label = { Text("Catatan Hasil Report CILT") },
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
                        Text("Simpan Report CILT", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Text("Histori Report CILT", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SlateGrey, modifier = Modifier.padding(top = 8.dp))
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
                            text = WibDateUtils.format("dd MMM yyyy, HH:mm", check.timestamp) + " WIB",
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Interval Flush: ${check.flushingIntervalMinutes}m • Speed: ${check.bowlSpeedRpm} RPM • Bearing Suhu: ${check.bearingTempCelsius}°C",
                        fontSize = 11.sp,
                        color = SlateGrey
                    )
                    if (check.comments.isNotBlank()) {
                        Text(text = "Note: ${check.comments}", fontSize = 11.sp, color = Color.Black)
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
fun ChecklistCategoryCard(
    title: String,
    items: List<ChecklistRowItem>,
    icon: ImageVector? = null,
    accentColor: Color = Color(0xFF059669),
    notesValue: String = "",
    onNotesChange: ((String) -> Unit)? = null,
    notesPlaceholder: String = "Tuliskan temuan abnormalitas jika ada..."
) {
    val checkedCount = items.count { it.checked }
    val totalCount = items.size
    val allDone = checkedCount == totalCount && totalCount > 0

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, if (allDone) accentColor.copy(alpha = 0.5f) else Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon, Title, and Progress Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(accentColor.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (allDone) accentColor.copy(alpha = 0.15f) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (allDone) accentColor.copy(alpha = 0.3f) else Color(0xFFCBD5E1))
                ) {
                    Text(
                        text = if (allDone) "✓ $checkedCount/$totalCount Selesai" else "$checkedCount/$totalCount Selesai",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (allDone) accentColor else Color(0xFF64748B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive item rows with modern surface and full-row clickability
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { item ->
                    val isChecked = item.checked
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { item.onCheckedChange(!isChecked) },
                        color = if (isChecked) accentColor.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isChecked) accentColor.copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isChecked) accentColor else Color.White)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isChecked) accentColor else Color(0xFF94A3B8),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChecked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selesai",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isChecked) Color(0xFF1E293B) else Color(0xFF475569),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Kotak Catatan Temuan Khusus Kategori
            if (onNotesChange != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Catatan Temuan (Diisi jika ditemukan temuan):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                    }
                    OutlinedTextField(
                        value = notesValue,
                        onValueChange = onNotesChange,
                        textStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            cursorColor = accentColor
                        ),
                        placeholder = {
                            Text(notesPlaceholder, fontSize = 11.sp, color = Color.Gray)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// 3. ABNORMALITY REPORTING SCREEN (WITH PHOTO SELECTION SIMULATOR & RPN CALCULATION)
// -----------------------------------------------------------------------------------------------------------------
class FailureModeRowItem(
    val no: Int,
    initialComponent: String,
    val failureModeOptions: List<String> = emptyList(),
    initialFailureMode: String = "",
    initialSeverity: Int = 0,
    initialOccurrence: Int = 0,
    initialDetection: Int = 0,
    val isCustomInput: Boolean = false
) {
    var component by mutableStateOf(initialComponent)
    var selectedFailureMode by mutableStateOf(initialFailureMode)
    var severity by mutableStateOf(initialSeverity)
    var occurrence by mutableStateOf(initialOccurrence)
    var detection by mutableStateOf(initialDetection)
}

@Composable
fun FailureModeOptionDropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = selected,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = opt,
                            fontSize = 12.sp,
                            fontWeight = if (selected == opt) FontWeight.Bold else FontWeight.Normal,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onSelect(opt)
                        expanded = false
                    },
                    modifier = Modifier.background(Color.White)
                )
            }
        }
    }
}

data class LevelOption(val score: Int, val description: String)

val SeverityLevelOptions = listOf(
    LevelOption(0, "Level 0 : Tidak ada Kerusakan"),
    LevelOption(1, "Level 1 : Ringan Sekali"),
    LevelOption(2, "Level 2 : Ringan (Minor)"),
    LevelOption(3, "Level 3 : Sedang (Moderate)"),
    LevelOption(4, "Level 4 : Berat (Major)"),
    LevelOption(5, "Level 5 : Parah Sekali (Catastrophic)")
)

val OccurrenceLevelOptions = listOf(
    LevelOption(0, "Level 0 : Tidak terjadi"),
    LevelOption(1, "Level 1 : Sangat jarang terjadi"),
    LevelOption(2, "Level 2 : Jarang terjadi"),
    LevelOption(3, "Level 3 : Kadang-kadang terjadi"),
    LevelOption(4, "Level 4 : Sering terjadi"),
    LevelOption(5, "Level 5 : Sangat sering terjadi")
)

val DetectionLevelOptions = listOf(
    LevelOption(0, "Level 0 : Tidak dideteksi karena tidak ada kerusakan"),
    LevelOption(1, "Level 1 : Kerusakan sangat mudah dideteksi"),
    LevelOption(2, "Level 2 : Kerusakan cukup mudah dideteksi"),
    LevelOption(3, "Level 3 : Kerusakan mudah dideteksi"),
    LevelOption(4, "Level 4 : Kerusakan sulit dideteksi"),
    LevelOption(5, "Level 5 : Kerusakan sangat sulit dideteksi")
)

@Composable
fun LevelDropdown(
    value: Int,
    options: List<LevelOption>,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White)
                .widthIn(min = 260.dp, max = 340.dp)
        ) {
            options.forEach { opt ->
                val isSelected = value == opt.score
                DropdownMenuItem(
                    text = {
                        Text(
                            text = opt.description,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) BrandGreen else Color(0xFF1E293B)
                        )
                    },
                    onClick = {
                        onValueChange(opt.score)
                        expanded = false
                    },
                    modifier = Modifier.background(if (isSelected) BrandGreenLight.copy(alpha = 0.5f) else Color.White)
                )
            }
        }
    }
}

@Composable
fun ScoreDropdown(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = value.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            (0..10).forEach { num ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = num.toString(),
                            fontSize = 12.sp,
                            fontWeight = if (value == num) FontWeight.Bold else FontWeight.Normal,
                            color = Color.Black
                        )
                    },
                    onClick = {
                        onValueChange(num)
                        expanded = false
                    },
                    modifier = Modifier.background(Color.White)
                )
            }
        }
    }
}

data class ReportRowAccumulation(
    val no: Int,
    val component: String,
    val failureModesSummary: String,
    val totalSeverity: Int = 0,
    val totalOccurrence: Int = 0,
    val totalDetection: Int = 0,
    val totalRpn: Int,
    val count: Int,
    val openCount: Int = 0,
    val doneCount: Int = 0,
    val avgLeadTimeDays: Double = 0.0,
    val maxLeadTimeDays: Long = 0L
)

@Composable
fun AbnormalityScreen(
    reports: List<AbnormalityReport>,
    onAddReport: (AbnormalityReport) -> Unit,
    onUpdateReport: (AbnormalityReport) -> Unit = {},
    onDeleteReport: (AbnormalityReport) -> Unit
) {
    // 2 Sub-menu di dalam Reliability PM: "Input Abnormality" & "Report"
    var currentSubMenu by remember { mutableStateOf("Input Abnormality") }

    val operatorList = listOf("Wahyu", "Abdul Aziz")
    var selectedOperator by remember { mutableStateOf("Wahyu") }
    var operatorDropdownExpanded by remember { mutableStateOf(false) }

    val shiftList = listOf("Pagi", "Malam")
    var selectedShift by remember { mutableStateOf("Pagi") }

    var selectedMachine by remember { mutableStateOf("SC-01") }
    var machineDropdownExpanded by remember { mutableStateOf(false) }
    val machineList = listOf("SC-01", "SC-02", "SC-03", "SC-04", "SC-05", "SC-06", "SC-07", "SC-08")

    val failureModeItems = remember {
        listOf(
            FailureModeRowItem(1, "Bowl", listOf("Aus", "Kerak", "Imbalance"), "Aus", 0, 0, 0),
            FailureModeRowItem(2, "Nozzle", listOf("Tersumbat", "Diameter tidak sesuai"), "Tersumbat", 0, 0, 0),
            FailureModeRowItem(3, "Seal", listOf("Aus", "Bocor", "Misalignment"), "Aus", 0, 0, 0),
            FailureModeRowItem(4, "Bearing", listOf("Aus", "Tidak terlumasi", "Over heating"), "Aus", 0, 0, 0),
            FailureModeRowItem(5, "Shaft", listOf("Misalignment", "Aus", "Patah"), "Misalignment", 0, 0, 0),
            FailureModeRowItem(6, "Belt", listOf("Kendur", "Aus", "Putus"), "Kendur", 0, 0, 0),
            FailureModeRowItem(7, "Motor", listOf("Overheat", "Electrical failure"), "Overheat", 0, 0, 0),
            FailureModeRowItem(8, "", emptyList(), "", 0, 0, 0, isCustomInput = true)
        )
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var factor by remember { mutableStateOf("MACHINE") } // MAN, METHOD, MACHINE, ENVIRONMENT

    var showSeverityGuideDialog by remember { mutableStateOf(false) }
    var showOccurenceGuideDialog by remember { mutableStateOf(false) }
    var showDetectionGuideDialog by remember { mutableStateOf(false) }

    if (showSeverityGuideDialog) {
        AlertDialog(
            onDismissRequest = { showSeverityGuideDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Panduan Severity",
                    tint = BrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Tingkat Keparahan (Severity)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateGrey
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SeverityLevelOptions.forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(BrandGreenLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(opt.score.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = opt.description,
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSeverityGuideDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold, color = BrandGreen)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showOccurenceGuideDialog) {
        AlertDialog(
            onDismissRequest = { showOccurenceGuideDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Panduan Occurence",
                    tint = BrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Frekuensi Kejadian (Occurence)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateGrey
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OccurrenceLevelOptions.forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color(0xFFFEF3C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(opt.score.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = opt.description,
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOccurenceGuideDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold, color = BrandGreen)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showDetectionGuideDialog) {
        AlertDialog(
            onDismissRequest = { showDetectionGuideDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Panduan Detection",
                    tint = BrandGreen,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Tingkat Deteksi (Detection)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SlateGrey
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DetectionLevelOptions.forEach { opt ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(Color(0xFFE0E7FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(opt.score.toString(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3730A3))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = opt.description,
                                fontSize = 12.sp,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetectionGuideDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold, color = BrandGreen)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // State untuk Dialog Update Status Abnormality (Open <-> Done, Lead Time & Nama Mekanik)
    var reportToUpdateStatus by remember { mutableStateOf<AbnormalityReport?>(null) }
    var dialogSelectedStatus by remember { mutableStateOf("Done") }
    var dialogMechanicName by remember { mutableStateOf("") }
    var dialogRepairNotes by remember { mutableStateOf("") }

    // State untuk sub-menu "Report"
    var selectedPeriod by remember { mutableStateOf("Hari Ini") } // "Hari Ini", "Seminggu", "Sebulan", "Tanggal"
    var selectedReportMachine by remember { mutableStateOf("Semua Mesin") }
    var reportMachineDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val now = System.currentTimeMillis()
    val startOfToday = remember(now) { WibDateUtils.getStartOfDay(now) }
    val endOfToday = remember(startOfToday) { startOfToday + 24 * 3600 * 1000L - 1L }
    var customStartDate by remember { mutableStateOf(startOfToday) }
    var customEndDate by remember { mutableStateOf(endOfToday) }

    fun pickStartDate() {
        val cal = WibDateUtils.getCalendar(customStartDate)
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = WibDateUtils.getCalendar().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val newStart = newCal.timeInMillis
                customStartDate = newStart
                if (customEndDate < newStart) {
                    customEndDate = newStart + 24 * 60 * 60 * 1000L - 1L
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickEndDate() {
        val cal = WibDateUtils.getCalendar(customEndDate)
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = WibDateUtils.getCalendar().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val newEnd = newCal.timeInMillis
                customEndDate = newEnd
                if (customStartDate > newEnd) {
                    customStartDate = WibDateUtils.getCalendar(newEnd).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val filteredReports = remember(reports, selectedPeriod, selectedReportMachine, customStartDate, customEndDate) {
        reports.filter { r ->
            val inPeriod = when (selectedPeriod) {
                "Hari Ini" -> {
                    val cal = WibDateUtils.getCalendar()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    r.timestamp >= cal.timeInMillis || (now - r.timestamp) <= 24L * 3600 * 1000
                }
                "Seminggu" -> {
                    r.timestamp >= (now - 7L * 24 * 3600 * 1000)
                }
                "Sebulan" -> {
                    r.timestamp >= (now - 30L * 24 * 3600 * 1000)
                }
                else -> { // "Tanggal"
                    r.timestamp in customStartDate..customEndDate
                }
            }
            val inMachine = if (selectedReportMachine == "Semua Mesin") {
                true
            } else {
                r.title.contains(selectedReportMachine, ignoreCase = true) || r.description.contains(selectedReportMachine, ignoreCase = true)
            }
            inPeriod && inMachine
        }.sortedWith(
            compareByDescending<com.example.data.AbnormalityReport> { it.status.equals("Open", ignoreCase = true) }
                .thenByDescending { it.rpn }
                .thenByDescending { it.timestamp }
        )
    }

    val periodDisplayLabel = remember(selectedPeriod, customStartDate, customEndDate) {
        if (selectedPeriod == "Tanggal") {
            "${WibDateUtils.format("dd/MM/yy", customStartDate)} - ${WibDateUtils.format("dd/MM/yy", customEndDate)}"
        } else selectedPeriod
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // FREEZE PANE SUB-MENU (Tetap berada di atas saat halaman di-scroll)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE2E8F0),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isInput = currentSubMenu == "Input Abnormality"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isInput) BrandGreen else Color.Transparent)
                        .clickable { currentSubMenu = "Input Abnormality" }
                        .padding(vertical = 11.dp)
                        .testTag("submenu_input_abnormality"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Input Abnormality",
                            tint = if (isInput) Color.White else SlateGrey,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Input Abnormality",
                            fontSize = 13.sp,
                            fontWeight = if (isInput) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isInput) Color.White else SlateGrey
                        )
                    }
                }

                val isReport = currentSubMenu == "Report"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isReport) BrandGreen else Color.Transparent)
                        .clickable { currentSubMenu = "Report" }
                        .padding(vertical = 11.dp)
                        .testTag("submenu_report"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Report",
                            tint = if (isReport) Color.White else SlateGrey,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Report",
                            fontSize = 13.sp,
                            fontWeight = if (isReport) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isReport) Color.White else SlateGrey
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp)
        ) {
            if (currentSubMenu == "Input Abnormality") {
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
                    Text("Laporkan Temuan Abnormality", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateGrey)

                    // 1. Pilihan Nama Operator Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Nama Operator :",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (operatorDropdownExpanded) BrandGreen else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { operatorDropdownExpanded = !operatorDropdownExpanded }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("abnormality_operator_dropdown"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedOperator,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih Operator",
                                    tint = Color.Black
                                )
                            }
                            DropdownMenu(
                                expanded = operatorDropdownExpanded,
                                onDismissRequest = { operatorDropdownExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                operatorList.forEach { op ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                op,
                                                fontSize = 13.sp,
                                                fontWeight = if (selectedOperator == op) FontWeight.Bold else FontWeight.Normal,
                                                color = Color.Black
                                            )
                                        },
                                        onClick = {
                                            selectedOperator = op
                                            operatorDropdownExpanded = false
                                        },
                                        leadingIcon = if (selectedOperator == op) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        modifier = Modifier.background(Color.White)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Pilihan Shift: |Pagi| |Malam|
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Shift :",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            shiftList.forEach { shift ->
                                val isSelected = selectedShift == shift
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) BrandGreen else Color(0xFFF1F5F9))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) BrandGreen else Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedShift = shift }
                                        .padding(vertical = 9.dp)
                                        .testTag("abnormality_shift_$shift"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = shift,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SlateGrey
                                    )
                                }
                            }
                        }
                    }

                    // Pilihan Mesin Dropdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pilih Mesin :",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (machineDropdownExpanded) BrandGreen else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { machineDropdownExpanded = !machineDropdownExpanded }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("abnormality_machine_dropdown"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedMachine,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih Mesin",
                                    tint = Color.Black
                                )
                            }
                            DropdownMenu(
                                expanded = machineDropdownExpanded,
                                onDismissRequest = { machineDropdownExpanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                machineList.forEach { m ->
                                    val isMachineSelected = selectedMachine == m
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                m,
                                                fontSize = 13.sp,
                                                fontWeight = if (isMachineSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = Color.Black
                                            )
                                        },
                                        onClick = {
                                            selectedMachine = m
                                            machineDropdownExpanded = false
                                        },
                                        leadingIcon = if (isMachineSelected) {
                                            {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .background(Color(0xFF16A34A), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        } else null,
                                        modifier = Modifier.background(if (isMachineSelected) Color(0xFF86EFAC) else Color.White)
                                    )
                                }
                            }
                        }
                    }

                    // Judul: Identifikasi Failure Mode & Tabel
                    Text(
                        text = "Identifikasi Failure Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )

                    // Tabel Failure Mode dengan Freeze Pane untuk Kolom No & Komponen
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // 1. FREEZE PANE (Kolom No & Komponen disempitkan agar area scrollable lebih luas)
                            Column(
                                modifier = Modifier
                                    .width(100.dp)
                                    .background(Color.White)
                                    .drawBehind {
                                        // Garis pemisah vertikal / pembatas freeze pane
                                        drawLine(
                                            color = Color(0xFFCBD5E1),
                                            start = Offset(size.width, 0f),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                            ) {
                                // Header Kolom Freeze Pane
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("No", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Komponen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                                }

                                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                                // Baris Data Freeze Pane
                                failureModeItems.forEachIndexed { idx, item ->
                                    val isEven = idx % 2 == 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .background(if (isEven) Color.White else Color(0xFFF8FAFC))
                                            .padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.no.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateGrey,
                                            modifier = Modifier.width(20.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        if (item.isCustomInput) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(32.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.White)
                                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                if (item.component.isEmpty()) {
                                                    Text(
                                                        text = "Lainnya...",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF94A3B8)
                                                    )
                                                }
                                                BasicTextField(
                                                    value = item.component,
                                                    onValueChange = { item.component = it },
                                                    singleLine = true,
                                                    textStyle = TextStyle(
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color.Black
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = item.component,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Black,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (idx < failureModeItems.size - 1) {
                                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                    }
                                }
                            }

                            // 2. SCROLLABLE PANE (Kolom Failure Mode, Severity, Occurence, Detection, RPN)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Column(modifier = Modifier.background(Color.White)) {
                                    // Header Kolom Scrollable
                                    Row(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .background(Color(0xFF1E293B))
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Failure Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(185.dp))
                                        Row(
                                            modifier = Modifier.width(135.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("Level Severity", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            IconButton(
                                                onClick = { showSeverityGuideDialog = true },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Panduan Severity",
                                                    tint = Color(0xFFFCD34D),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.width(145.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("Occurence", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            IconButton(
                                                onClick = { showOccurenceGuideDialog = true },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Panduan Occurence",
                                                    tint = Color(0xFFFCD34D),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Row(
                                            modifier = Modifier.width(135.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text("Detection", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Spacer(modifier = Modifier.width(2.dp))
                                            IconButton(
                                                onClick = { showDetectionGuideDialog = true },
                                                modifier = Modifier.size(20.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = "Panduan Detection",
                                                    tint = Color(0xFFFCD34D),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text("Risk Priority Number (RPN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(170.dp), textAlign = TextAlign.Center)
                                    }

                                    Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                                    // Baris Data Scrollable
                                    failureModeItems.forEachIndexed { idx, item ->
                                        val isEven = idx % 2 == 0
                                        val rpnValue = item.severity * item.occurrence * item.detection
                                        Row(
                                            modifier = Modifier
                                                .height(52.dp)
                                                .background(if (isEven) Color.White else Color(0xFFF8FAFC))
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (item.isCustomInput) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(185.dp)
                                                        .height(34.dp)
                                                        .padding(end = 8.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color.White)
                                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp),
                                                    contentAlignment = Alignment.CenterStart
                                                ) {
                                                    if (item.selectedFailureMode.isEmpty()) {
                                                        Text(
                                                            text = "Ketik failure mode...",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF94A3B8)
                                                        )
                                                    }
                                                    BasicTextField(
                                                        value = item.selectedFailureMode,
                                                        onValueChange = { item.selectedFailureMode = it },
                                                        singleLine = true,
                                                        textStyle = TextStyle(
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = Color.Black
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            } else {
                                                FailureModeOptionDropdown(
                                                    selected = item.selectedFailureMode,
                                                    options = item.failureModeOptions,
                                                    onSelect = { item.selectedFailureMode = it },
                                                    modifier = Modifier
                                                        .width(185.dp)
                                                        .padding(end = 8.dp)
                                                )
                                            }
                                            Box(modifier = Modifier.width(135.dp), contentAlignment = Alignment.Center) {
                                                LevelDropdown(
                                                    value = item.severity,
                                                    options = SeverityLevelOptions,
                                                    onValueChange = { item.severity = it }
                                                )
                                            }
                                            Box(modifier = Modifier.width(145.dp), contentAlignment = Alignment.Center) {
                                                LevelDropdown(
                                                    value = item.occurrence,
                                                    options = OccurrenceLevelOptions,
                                                    onValueChange = { item.occurrence = it }
                                                )
                                            }
                                            Box(modifier = Modifier.width(135.dp), contentAlignment = Alignment.Center) {
                                                LevelDropdown(
                                                    value = item.detection,
                                                    options = DetectionLevelOptions,
                                                    onValueChange = { item.detection = it }
                                                )
                                            }
                                            Box(modifier = Modifier.width(170.dp), contentAlignment = Alignment.Center) {
                                                val rpnBg = when {
                                                    rpnValue >= 100 -> Color(0xFFFEE2E2)
                                                    rpnValue >= 50 -> Color(0xFFFEF3C7)
                                                    else -> Color(0xFFF1F5F9)
                                                }
                                                val rpnTextColor = when {
                                                    rpnValue >= 100 -> Color(0xFFDC2626)
                                                    rpnValue >= 50 -> Color(0xFFD97706)
                                                    else -> Color(0xFF0F172A)
                                                }
                                                Surface(
                                                    color = rpnBg,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, rpnTextColor.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = rpnValue.toString(),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = rpnTextColor,
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (idx < failureModeItems.size - 1) {
                                            Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val activeItems = failureModeItems.filter { it.severity > 0 || it.occurrence > 0 || it.detection > 0 }
                            val itemsToSave = if (activeItems.isNotEmpty()) activeItems else listOf(failureModeItems.first())
                            itemsToSave.forEach { item ->
                                val calculatedRpn = item.severity * item.occurrence * item.detection
                                val compName = if (item.component.isNotBlank()) item.component else "Komponen Tambahan"
                                val failMode = if (item.selectedFailureMode.isNotBlank()) item.selectedFailureMode else "Potensi Kerusakan"
                                val finalTitle = "$compName - $failMode ($selectedMachine)"
                                val finalDesc = "Identifikasi failure mode $failMode pada komponen $compName mesin $selectedMachine oleh $selectedOperator (Shift $selectedShift) (RPN: $calculatedRpn)."
                                onAddReport(
                                    AbnormalityReport(
                                        title = finalTitle,
                                        description = finalDesc,
                                        factor = factor,
                                        severityScore = item.severity,
                                        occurrenceScore = item.occurrence,
                                        detectionScore = item.detection,
                                        rpn = calculatedRpn,
                                        photoUri = null,
                                        picName = "$selectedOperator ($selectedShift)",
                                        tagType = "",
                                        status = "Open",
                                        mechanicName = "",
                                        repairNotes = "",
                                        resolvedTimestamp = 0L
                                    )
                                )
                            }
                            Toast.makeText(context, "Laporan Abnormality Tersimpan (${itemsToSave.size} item)!", Toast.LENGTH_SHORT).show()
                            // Reset input
                            failureModeItems.forEach {
                                if (it.isCustomInput) {
                                    it.component = ""
                                    it.selectedFailureMode = ""
                                }
                                it.severity = 0
                                it.occurrence = 0
                                it.detection = 0
                            }
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
                                WibDateUtils.format("dd MMM yyyy", r.timestamp) + " WIB • ${r.picName}",
                                fontSize = 10.sp,
                                color = Color.Black
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            val isOpen = r.status.equals("Open", ignoreCase = true)
                            val endMillis = if (!isOpen && r.resolvedTimestamp > 0L) r.resolvedTimestamp else System.currentTimeMillis()
                            val leadTimeDays = maxOf(0L, (endMillis - r.timestamp) / (24L * 60 * 60 * 1000L))
                            val leadTimeText = if (leadTimeDays == 0L) "0 hari" else "$leadTimeDays hari"

                            // Status Tag
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isOpen) Color(0xFFFFF7ED) else Color(0xFFF0FDF4),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, if (isOpen) Color(0xFFFED7AA) else Color(0xFF86EFAC), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    if (isOpen) "Open ($leadTimeText)" else "Done",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOpen) Color(0xFFC2410C) else Color(0xFF15803D)
                                )
                            }

                            IconButton(onClick = { onDeleteReport(r) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }

                    // Tampilkan info mekanik jika Done
                    if (!r.status.equals("Open", ignoreCase = true) && r.mechanicName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(13.dp))
                            Text("Mekanik Perbaikan: ${r.mechanicName}", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF166534))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(r.description, fontSize = 12.sp, color = Color.Black)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Faktor: ${r.factor}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                            Text("Severity: ${r.severityScore} • Occ: ${r.occurrenceScore} • Det: ${r.detectionScore}", fontSize = 11.sp, color = Color.Black)
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
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        } // End of if (currentSubMenu == "Input Abnormality")

        // =========================================================================
        // MENU 2: REPORT (AKUMULASI HARIAN, MINGGUAN, BULANAN)
        // =========================================================================
        if (currentSubMenu == "Report") {
            // 1. FILTER & STATISTIK RINGKASAN
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = BrandGreen)
                            Column {
                                Text("Report Reliability PM", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                Text("Data terakumulasi dari menu Input Abnormality", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        // PILIHAN PERIODE: HARIAN, MINGGUAN, BULANAN, TANGGAL
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Pilihan Periode Report :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Hari Ini", "Seminggu", "Sebulan", "Tanggal").forEach { period ->
                                    val isSelected = selectedPeriod == period
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BrandGreen else Color(0xFFF1F5F9))
                                            .border(1.dp, if (isSelected) BrandGreen else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                            .clickable { selectedPeriod = period }
                                            .padding(vertical = 8.dp)
                                            .testTag("period_filter_$period"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = period,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else SlateGrey
                                        )
                                    }
                                }
                            }

                            if (selectedPeriod == "Tanggal") {
                                DateRangeFilterPicker(
                                    startDateMillis = customStartDate,
                                    endDateMillis = customEndDate,
                                    onPickStartDate = { pickStartDate() },
                                    onPickEndDate = { pickEndDate() }
                                )
                            }
                        }

                        // FILTER MESIN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Filter Mesin :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                            Box(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                                        .clickable { reportMachineDropdownExpanded = !reportMachineDropdownExpanded }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                        .testTag("report_machine_filter_dropdown"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedReportMachine,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Black
                                    )
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Black)
                                }

                                DropdownMenu(
                                    expanded = reportMachineDropdownExpanded,
                                    onDismissRequest = { reportMachineDropdownExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    listOf("Semua Mesin").plus(machineList).forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m, fontSize = 12.sp, color = Color.Black) },
                                            onClick = {
                                                selectedReportMachine = m
                                                reportMachineDropdownExpanded = false
                                            },
                                            modifier = Modifier.background(Color.White)
                                        )
                                    }
                                }
                            }
                        }

                        // RINGKASAN STATISTIK CEPAT (KPI SUMMARY)
                        val totalAccRpn = filteredReports.sumOf { it.rpn }
                        val openCasesCount = filteredReports.count { it.status.equals("Open", ignoreCase = true) }
                        val doneCasesCount = filteredReports.count { it.status.equals("Done", ignoreCase = true) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandGreenLight),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Total Temuan", fontSize = 9.5.sp, color = SlateGrey)
                                    Text("${filteredReports.size} Kasus", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Status Open", fontSize = 9.5.sp, color = Color(0xFF9A3412))
                                    Text("$openCasesCount Kasus", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Status Done", fontSize = 9.5.sp, color = Color(0xFF166534))
                                    Text("$doneCasesCount Kasus", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Total RPN", fontSize = 9.5.sp, color = Color(0xFF991B1B))
                                    Text("$totalAccRpn", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandRed)
                                }
                            }
                        }
                    }
                }
            }

            // 2. TABEL REPORT AKUMULASI (IDENTIK DENGAN TABEL INPUT DENGAN FREEZE PANE)
            item {
                // Kalkulasi data akumulasi per baris komponen 1 s/d 8
                val standardComponents = listOf(
                    Pair(1, "Bowl"),
                    Pair(2, "Nozzle"),
                    Pair(3, "Seal"),
                    Pair(4, "Bearing"),
                    Pair(5, "Shaft"),
                    Pair(6, "Belt"),
                    Pair(7, "Motor")
                )

                val nowMillis = System.currentTimeMillis()
                val reportRows = standardComponents.map { (no, name) ->
                    val matched = filteredReports.filter {
                        it.title.contains(name, ignoreCase = true) || it.description.contains(name, ignoreCase = true)
                    }
                    val modes = matched.mapNotNull { r ->
                        val parts = r.title.split("-")
                        if (parts.size >= 2) parts[1].substringBefore("(").trim() else null
                    }.filter { it.isNotBlank() }.distinct()
                    val modeSummary = when {
                        matched.isEmpty() -> "-"
                        modes.isNotEmpty() -> modes.joinToString(", ")
                        else -> "${matched.size}x temuan"
                    }
                    val openCases = matched.count { it.status.equals("Open", ignoreCase = true) }
                    val doneCases = matched.count { it.status.equals("Done", ignoreCase = true) }
                    val leadTimes = matched.map { r ->
                        val endMillis = if (!r.status.equals("Open", ignoreCase = true) && r.resolvedTimestamp > 0L) {
                            r.resolvedTimestamp
                        } else {
                            nowMillis
                        }
                        maxOf(0L, (endMillis - r.timestamp) / (24L * 3600 * 1000L))
                    }
                    val avgLead = if (leadTimes.isNotEmpty()) leadTimes.average() else 0.0
                    val maxLead = leadTimes.maxOrNull() ?: 0L
                    ReportRowAccumulation(
                        no = no,
                        component = name,
                        failureModesSummary = modeSummary,
                        totalSeverity = matched.sumOf { it.severityScore },
                        totalOccurrence = matched.sumOf { it.occurrenceScore },
                        totalDetection = matched.sumOf { it.detectionScore },
                        totalRpn = matched.sumOf { it.rpn },
                        count = matched.size,
                        openCount = openCases,
                        doneCount = doneCases,
                        avgLeadTimeDays = avgLead,
                        maxLeadTimeDays = maxLead
                    )
                }.toMutableList()

                // Row 8: Komponen Tambahan (Manual Input)
                val nonStandardReports = filteredReports.filter { r ->
                    standardComponents.none { (_, name) -> r.title.contains(name, ignoreCase = true) }
                }
                val customModes = nonStandardReports.mapNotNull { r ->
                    val parts = r.title.split("-")
                    if (parts.size >= 2) parts[1].substringBefore("(").trim() else null
                }.filter { it.isNotBlank() }.distinct()
                val customCompName = nonStandardReports.firstOrNull()?.let {
                    it.title.substringBefore("-").trim().takeIf { s -> s.isNotBlank() }
                } ?: "Komponen Tambahan"
                val customModeSummary = when {
                    nonStandardReports.isEmpty() -> "-"
                    customModes.isNotEmpty() -> customModes.joinToString(", ")
                    else -> "${nonStandardReports.size}x temuan"
                }
                val customOpenCases = nonStandardReports.count { it.status.equals("Open", ignoreCase = true) }
                val customDoneCases = nonStandardReports.count { it.status.equals("Done", ignoreCase = true) }
                val customLeadTimes = nonStandardReports.map { r ->
                    val endMillis = if (!r.status.equals("Open", ignoreCase = true) && r.resolvedTimestamp > 0L) {
                        r.resolvedTimestamp
                    } else {
                        nowMillis
                    }
                    maxOf(0L, (endMillis - r.timestamp) / (24L * 3600 * 1000L))
                }
                val customAvgLead = if (customLeadTimes.isNotEmpty()) customLeadTimes.average() else 0.0
                val customMaxLead = customLeadTimes.maxOrNull() ?: 0L
                reportRows.add(
                    ReportRowAccumulation(
                        no = 8,
                        component = customCompName,
                        failureModesSummary = customModeSummary,
                        totalSeverity = nonStandardReports.sumOf { it.severityScore },
                        totalOccurrence = nonStandardReports.sumOf { it.occurrenceScore },
                        totalDetection = nonStandardReports.sumOf { it.detectionScore },
                        totalRpn = nonStandardReports.sumOf { it.rpn },
                        count = nonStandardReports.size,
                        openCount = customOpenCases,
                        doneCount = customDoneCases,
                        avgLeadTimeDays = customAvgLead,
                        maxLeadTimeDays = customMaxLead
                    )
                )

                val sumAllRpn = reportRows.sumOf { it.totalRpn }
                val sumAllCount = reportRows.sumOf { it.count }
                val sumAllOpen = reportRows.sumOf { it.openCount }
                val sumAllDone = reportRows.sumOf { it.doneCount }
                val allReportLeadTimes = filteredReports.map { r ->
                    val endMillis = if (!r.status.equals("Open", ignoreCase = true) && r.resolvedTimestamp > 0L) {
                        r.resolvedTimestamp
                    } else {
                        nowMillis
                    }
                    maxOf(0L, (endMillis - r.timestamp) / (24L * 3600 * 1000L))
                }
                val totalOverallAvgLead = if (allReportLeadTimes.isNotEmpty()) allReportLeadTimes.average() else 0.0

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Title bar tabel
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Akumulasi FMEA ($periodDisplayLabel • $selectedReportMachine)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Text(
                                text = "Total: $sumAllCount Laporan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BrandGreen
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth()) {
                            // 1. FREEZE PANE REPORT (Kolom No & Komponen disempitkan agar area scrollable lebih luas)
                            Column(
                                modifier = Modifier
                                    .width(100.dp)
                                    .background(Color.White)
                                    .drawBehind {
                                        drawLine(
                                            color = Color(0xFFCBD5E1),
                                            start = Offset(size.width, 0f),
                                            end = Offset(size.width, size.height),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                    }
                            ) {
                                // Header Kolom Freeze Pane
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .background(Color(0xFF1E293B))
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("No", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Komponen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                                }

                                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                                // 8 Baris Komponen
                                reportRows.forEachIndexed { idx, row ->
                                    val isEven = idx % 2 == 0
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(52.dp)
                                            .background(if (isEven) Color.White else Color(0xFFF8FAFC))
                                            .padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = row.no.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateGrey,
                                            modifier = Modifier.width(20.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = row.component,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (idx < reportRows.size - 1) {
                                        Divider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                    }
                                }

                                // Baris TOTAL AKUMULASI (Footer Freeze Pane)
                                Divider(color = Color(0xFFCBD5E1), thickness = 2.dp)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .background(Color(0xFF0F172A))
                                        .padding(horizontal = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "∑",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.width(20.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "TOTAL",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // 2. SCROLLABLE PANE REPORT (Failure Mode, Severity, Occurence, Detection, RPN, Jml Temuan)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Column(modifier = Modifier.background(Color.White)) {
                                    // Header Kolom Scrollable (RPN, Jml Temuan, Status Open/Done, Lead Time, Failure Mode)
                                    Row(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .background(Color(0xFF1E293B))
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Risk Priority Number (RPN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(160.dp), textAlign = TextAlign.Center)
                                        Text("Jml Temuan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(95.dp), textAlign = TextAlign.Center)
                                        Text("Status (Open / Done)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(175.dp), textAlign = TextAlign.Center)
                                        Text("Lead Time (Hari)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(120.dp), textAlign = TextAlign.Center)
                                        Text("Failure Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(220.dp))
                                    }

                                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                                    // Baris Data Scrollable Report
                                    reportRows.forEachIndexed { idx, row ->
                                        val isEven = idx % 2 == 0
                                        Row(
                                            modifier = Modifier
                                                .height(52.dp)
                                                .background(if (isEven) Color.White else Color(0xFFF8FAFC))
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 1. Risk Priority Number (RPN)
                                            Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                                                val rpnBg = when {
                                                    row.totalRpn >= 100 -> Color(0xFFFEE2E2)
                                                    row.totalRpn >= 50 -> Color(0xFFFEF3C7)
                                                    row.totalRpn > 0 -> BrandGreenLight
                                                    else -> Color(0xFFF1F5F9)
                                                }
                                                val rpnTextColor = when {
                                                    row.totalRpn >= 100 -> Color(0xFFDC2626)
                                                    row.totalRpn >= 50 -> Color(0xFFD97706)
                                                    row.totalRpn > 0 -> BrandGreen
                                                    else -> Color(0xFF64748B)
                                                }
                                                Surface(
                                                    color = rpnBg,
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, rpnTextColor.copy(alpha = 0.3f))
                                                ) {
                                                    Text(
                                                        text = row.totalRpn.toString(),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = rpnTextColor,
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }

                                            // 2. Jml Temuan
                                            Box(modifier = Modifier.width(95.dp), contentAlignment = Alignment.Center) {
                                                Surface(
                                                    color = if (row.count > 0) Color(0xFFE2E8F0) else Color(0xFFF8FAFC),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "${row.count}x",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (row.count > 0) Color.Black else Color.Gray,
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            // 3. Status (Open / Done) - Kolom baru setelah Jml Temuan
                                            Box(modifier = Modifier.width(175.dp), contentAlignment = Alignment.Center) {
                                                if (row.count == 0) {
                                                    Text("-", fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                                                } else {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        // Open badge
                                                        Surface(
                                                            color = if (row.openCount > 0) Color(0xFFFFF7ED) else Color(0xFFF8FAFC),
                                                            shape = RoundedCornerShape(6.dp),
                                                            border = BorderStroke(1.dp, if (row.openCount > 0) Color(0xFFFDBA74) else Color(0xFFE2E8F0))
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(6.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (row.openCount > 0) Color(0xFFEA580C) else Color(0xFF94A3B8))
                                                                )
                                                                Text(
                                                                    text = "${row.openCount} Open",
                                                                    fontSize = 10.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (row.openCount > 0) Color(0xFFC2410C) else Color(0xFF64748B)
                                                                )
                                                            }
                                                        }

                                                        // Done badge
                                                        Surface(
                                                            color = if (row.doneCount > 0) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                                            shape = RoundedCornerShape(6.dp),
                                                            border = BorderStroke(1.dp, if (row.doneCount > 0) Color(0xFF86EFAC) else Color(0xFFE2E8F0))
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(6.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (row.doneCount > 0) Color(0xFF16A34A) else Color(0xFF94A3B8))
                                                                )
                                                                Text(
                                                                    text = "${row.doneCount} Done",
                                                                    fontSize = 10.5.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (row.doneCount > 0) Color(0xFF15803D) else Color(0xFF64748B)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            // 4. Lead Time (Hari) - Kolom baru setelah Status
                                            Box(modifier = Modifier.width(120.dp), contentAlignment = Alignment.Center) {
                                                if (row.count == 0) {
                                                    Text("-", fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                                                } else {
                                                    val leadDisplay = if (row.count == 1) {
                                                        "${row.maxLeadTimeDays} hari"
                                                    } else {
                                                        val formattedAvg = String.format(java.util.Locale.US, "%.1f", row.avgLeadTimeDays).removeSuffix(".0")
                                                        "$formattedAvg hari"
                                                    }
                                                    val isLongLead = row.maxLeadTimeDays >= 7L
                                                    val isMediumLead = row.maxLeadTimeDays in 3L..6L
                                                    Surface(
                                                        color = when {
                                                            isLongLead -> Color(0xFFFEE2E2)
                                                            isMediumLead -> Color(0xFFFEF3C7)
                                                            else -> Color(0xFFF0FDF4)
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        border = BorderStroke(
                                                            1.dp,
                                                            when {
                                                                isLongLead -> Color(0xFFFCA5A5)
                                                                isMediumLead -> Color(0xFFFCD34D)
                                                                else -> Color(0xFF86EFAC)
                                                            }
                                                        )
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Schedule,
                                                                contentDescription = null,
                                                                tint = when {
                                                                    isLongLead -> Color(0xFFDC2626)
                                                                    isMediumLead -> Color(0xFFD97706)
                                                                    else -> Color(0xFF15803D)
                                                                },
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Text(
                                                                text = leadDisplay,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = when {
                                                                    isLongLead -> Color(0xFFDC2626)
                                                                    isMediumLead -> Color(0xFFB45309)
                                                                    else -> Color(0xFF15803D)
                                                                }
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // 5. Failure Mode
                                            Text(
                                                text = row.failureModesSummary,
                                                fontSize = 11.sp,
                                                color = if (row.count > 0) Color.Black else Color.Gray,
                                                fontWeight = if (row.count > 0) FontWeight.SemiBold else FontWeight.Normal,
                                                modifier = Modifier
                                                    .width(220.dp)
                                                    .padding(end = 8.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (idx < reportRows.size - 1) {
                                            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)
                                        }
                                    }

                                    // Baris TOTAL AKUMULASI (Footer Scrollable Pane)
                                    HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 2.dp)
                                    Row(
                                        modifier = Modifier
                                            .height(48.dp)
                                            .background(Color(0xFF0F172A))
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Total RPN
                                        Box(modifier = Modifier.width(160.dp), contentAlignment = Alignment.Center) {
                                            Surface(
                                                color = if (sumAllRpn >= 100) Color(0xFFDC2626) else BrandGreen,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = sumAllRpn.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        // 2. Total Jml Temuan
                                        Box(modifier = Modifier.width(95.dp), contentAlignment = Alignment.Center) {
                                            Text("${sumAllCount}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        // 3. Total Status Open & Done
                                        Box(modifier = Modifier.width(175.dp), contentAlignment = Alignment.Center) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    color = Color(0xFFEA580C),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "$sumAllOpen Open",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Surface(
                                                    color = Color(0xFF16A34A),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = "$sumAllDone Done",
                                                        fontSize = 10.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        // 4. Total Lead Time (Rata-rata lead time seluruh temuan)
                                        Box(modifier = Modifier.width(120.dp), contentAlignment = Alignment.Center) {
                                            if (sumAllCount == 0) {
                                                Text("-", fontSize = 11.sp, color = Color.White)
                                            } else {
                                                val formattedTotalAvg = String.format(java.util.Locale.US, "%.1f", totalOverallAvgLead).removeSuffix(".0")
                                                Surface(
                                                    color = Color.White.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Schedule,
                                                            contentDescription = null,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Text(
                                                            text = "$formattedTotalAvg hari",
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // 5. Failure Mode label
                                        Text(
                                            text = "Semua Komponen",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.width(220.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. DETAIL RINCIAN RIWAYAT LAPORAN YANG MEMBENTUK AKUMULASI
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Rincian Data Laporan ($periodDisplayLabel • $selectedReportMachine) :",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )
                    Text(
                        text = "Diurutkan: Status Open teratas & RPN tertinggi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFC2410C)
                    )
                }
            }

            if (filteredReports.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "Belum ada temuan abnormality untuk periode $periodDisplayLabel ($selectedReportMachine).",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateGrey,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Input temuan baru pada menu 'Input Abnormality' agar angka otomatis terakumulasi di report ini.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = { currentSubMenu = "Input Abnormality" },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Buka Menu Input Abnormality", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                items(filteredReports) { r ->
                    val isOpen = r.status.equals("Open", ignoreCase = true)
                    val endMillis = if (!isOpen && r.resolvedTimestamp > 0L) r.resolvedTimestamp else System.currentTimeMillis()
                    val leadTimeDays = maxOf(0L, (endMillis - r.timestamp) / (24L * 60 * 60 * 1000L))
                    val leadTimeText = if (leadTimeDays == 0L) "0 hari (Hari ini)" else "$leadTimeDays hari"

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, if (isOpen) Color(0xFFFED7AA) else Color(0xFFF1F5F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Row 1: Judul, Waktu Temuan, RPN
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
                                    Text(
                                        WibDateUtils.format("dd MMM yyyy, HH:mm", r.timestamp) + " WIB • ${r.picName}",
                                        fontSize = 10.sp,
                                        color = Color.Black
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(if (r.rpn >= 200) BrandRed.copy(alpha = 0.2f) else BrandGreenLight, RoundedCornerShape(8.dp))
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

                            Spacer(modifier = Modifier.height(8.dp))

                            // Row 2: Status & Lead Time Banner
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isOpen) Color(0xFFFFF7ED) else Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, if (isOpen) Color(0xFFFDBA74) else Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Status Chip
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isOpen) Icons.Default.HourglassTop else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (isOpen) Color(0xFFEA580C) else Color(0xFF16A34A),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (isOpen) "Status: Open (Belum Selesai)" else "Status: Done (Selesai)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOpen) Color(0xFFC2410C) else Color(0xFF15803D)
                                            )
                                        }

                                        // Lead Time Chip
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isOpen) Color(0xFFFFEDD5) else Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = if (isOpen) "Lead Time: $leadTimeText" else "Selesai dlm: $leadTimeText",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isOpen) Color(0xFF9A3412) else Color(0xFF166534),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // Jika status Done, tampilkan nama mekanik yang melakukan perbaikan
                                    if (!isOpen) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Build,
                                                contentDescription = null,
                                                tint = Color(0xFF15803D),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Mekanik Perbaikan: ${r.mechanicName.ifBlank { "-" }}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF166534)
                                            )
                                        }
                                        if (r.repairNotes.isNotBlank()) {
                                            Text(
                                                text = "Tindakan: ${r.repairNotes}",
                                                fontSize = 10.sp,
                                                color = Color.DarkGray,
                                                modifier = Modifier.padding(start = 20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(r.description, fontSize = 11.5.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Faktor: ${r.factor} • Sev: ${r.severityScore} | Occ: ${r.occurrenceScore} | Det: ${r.detectionScore}",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isOpen) {
                                        Button(
                                            onClick = {
                                                dialogSelectedStatus = "Done"
                                                dialogMechanicName = r.mechanicName
                                                dialogRepairNotes = r.repairNotes
                                                reportToUpdateStatus = r
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Selesaikan (Done)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = {
                                                dialogSelectedStatus = r.status
                                                dialogMechanicName = r.mechanicName
                                                dialogRepairNotes = r.repairNotes
                                                reportToUpdateStatus = r
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, BrandGreen),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null,
                                                tint = BrandGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit Status", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                        }
                                    }

                                    IconButton(onClick = { onDeleteReport(r) }, modifier = Modifier.size(28.dp)) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog Update Status Abnormality (Open <-> Done, Lead Time & Nama Mekanik)
    if (reportToUpdateStatus != null) {
            val rep = reportToUpdateStatus!!
            val currentLeadTime = remember(rep) {
                val endMillis = if (rep.status.equals("Done", ignoreCase = true) && rep.resolvedTimestamp > 0L) {
                    rep.resolvedTimestamp
                } else {
                    System.currentTimeMillis()
                }
                maxOf(0L, (endMillis - rep.timestamp) / (24L * 60 * 60 * 1000L))
            }

            AlertDialog(
                onDismissRequest = { reportToUpdateStatus = null },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = "Update Status",
                        tint = BrandGreen,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Column {
                        Text(
                            text = "Update Status Abnormality",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = SlateGrey
                        )
                        Text(
                            text = rep.title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Lead Time Info Box
                        Surface(
                            color = if (rep.status.equals("Open", ignoreCase = true)) Color(0xFFFFF7ED) else Color(0xFFF0FDF4),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (rep.status.equals("Open", ignoreCase = true)) Color(0xFFFDBA74) else Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (rep.status.equals("Open", ignoreCase = true)) Icons.Default.HourglassTop else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (rep.status.equals("Open", ignoreCase = true)) Color(0xFFC2410C) else Color(0xFF15803D),
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = if (rep.status.equals("Open", ignoreCase = true)) "Status Saat Ini: Open (Belum Selesai)" else "Status Saat Ini: Done (Selesai)",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (rep.status.equals("Open", ignoreCase = true)) Color(0xFFC2410C) else Color(0xFF15803D)
                                    )
                                    Text(
                                        text = "Lead Time: ${if (currentLeadTime == 0L) "0 hari (Hari ini)" else "$currentLeadTime hari"}",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }

                        // Pilihan Status: Open vs Done
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Pilih Status Abnormality :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Option Open
                                val isOpenSelected = dialogSelectedStatus.equals("Open", ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isOpenSelected) Color(0xFFFFF7ED) else Color(0xFFF8FAFC))
                                        .border(
                                            width = if (isOpenSelected) 2.dp else 1.dp,
                                            color = if (isOpenSelected) Color(0xFFEA580C) else Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .clickable { dialogSelectedStatus = "Open" }
                                        .padding(vertical = 10.dp, horizontal = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.HourglassTop,
                                            contentDescription = null,
                                            tint = if (isOpenSelected) Color(0xFFEA580C) else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Open",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOpenSelected) Color(0xFFEA580C) else Color.DarkGray
                                        )
                                    }
                                }

                                // Option Done
                                val isDoneSelected = dialogSelectedStatus.equals("Done", ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isDoneSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
                                    .border(
                                        width = if (isDoneSelected) 2.dp else 1.dp,
                                        color = if (isDoneSelected) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { dialogSelectedStatus = "Done" }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isDoneSelected) Color(0xFF16A34A) else Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Done",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDoneSelected) Color(0xFF16A34A) else Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

                    // Jika status Done, tampilkan kolom Nama Mekanik yang melakukan perbaikan (diisi dengan input nama saja dengan cara diketik)
                    if (dialogSelectedStatus.equals("Done", ignoreCase = true)) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nama Mekanik yang Melakukan Perbaikan : *",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )

                            OutlinedTextField(
                                value = dialogMechanicName,
                                onValueChange = { dialogMechanicName = it },
                                placeholder = { Text("Ketik nama mekanik...", fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(18.dp))
                                },
                                supportingText = {
                                    Text("Masukkan nama mekanik/teknisi yang melakukan perbaikan", fontSize = 10.5.sp, color = Color.Gray)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 12.5.sp, color = Color.Black),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGreen,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )
                        }

                        // Catatan Perbaikan
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Catatan / Tindakan Perbaikan (Opsional) :",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateGrey
                            )
                            OutlinedTextField(
                                value = dialogRepairNotes,
                                onValueChange = { dialogRepairNotes = it },
                                placeholder = { Text("Contoh: Penggantian seal & balancing bowl", fontSize = 11.sp) },
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(fontSize = 12.sp, color = Color.Black),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BrandGreen,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val isDone = dialogSelectedStatus.equals("Done", ignoreCase = true)
                        if (isDone && dialogMechanicName.isBlank()) {
                            Toast.makeText(context, "Mohon ketik Nama Mekanik yang melakukan perbaikan!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val resolvedTs = if (isDone) {
                            if (rep.resolvedTimestamp > 0L) rep.resolvedTimestamp else System.currentTimeMillis()
                        } else 0L

                        val updated = rep.copy(
                            status = dialogSelectedStatus,
                            mechanicName = if (isDone) dialogMechanicName.trim() else "",
                            repairNotes = if (isDone) dialogRepairNotes.trim() else "",
                            resolvedTimestamp = resolvedTs,
                            isSynced = false
                        )
                        onUpdateReport(updated)
                        reportToUpdateStatus = null
                        Toast.makeText(
                            context,
                            "Status abnormality berhasil diperbarui: '$dialogSelectedStatus'!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToUpdateStatus = null }) {
                    Text("Batal", color = Color.Gray, fontSize = 12.sp)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
}

// -----------------------------------------------------------------------------------------------------------------
// 4. FLUSHING SOP & CHECKLIST SCREEN (INPUT & REPORT)
// -----------------------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlushingScreen(
    flushingLogs: List<FlushingLog>,
    onAddFlushingLog: (FlushingLog) -> Unit,
    onDeleteFlushingLog: (FlushingLog) -> Unit,
    mentorLogs: List<MentorPairingLog>,
    onAddMentorLog: (MentorPairingLog) -> Unit
) {
    val context = LocalContext.current
    var selectedSubMenu by remember { mutableStateOf(0) } // 0: Input, 1: Report

    // Input Form States
    val unitOptions = listOf("SC-01", "SC-02", "SC-03", "SC-04", "SC-05", "SC-06", "SC-07", "SC-08")
    val operatorOptions = listOf("Wahyu", "Abdul Aziz")

    var operatorName by remember { mutableStateOf("Wahyu") }
    var selectedShift by remember { mutableStateOf("Shift Pagi") } // "Shift Pagi" or "Shift Malam"
    var selectedUnit by remember { mutableStateOf("SC-01") }
    var masterHour by remember { mutableStateOf("08:00") }
    var tasksState by remember { mutableStateOf(defaultFlushingTasks("08:00")) }
    var flushingNotes by remember { mutableStateOf("") }
    var expandedOperatorDropdown by remember { mutableStateOf(false) }

    // Report Filter States
    var filterPeriod by remember { mutableStateOf("Hari ini") }
    var filterShift by remember { mutableStateOf("Semua") }
    var filterUnit by remember { mutableStateOf("Semua") }
    var logToDelete by remember { mutableStateOf<FlushingLog?>(null) }

    val todayStartMillis = remember { WibDateUtils.getStartOfDay() }
    val todayEndMillis = remember { todayStartMillis + 24 * 60 * 60 * 1000L - 1L }
    var customStartDate by remember { mutableStateOf(todayStartMillis) }
    var customEndDate by remember { mutableStateOf(todayEndMillis) }

    fun pickStartDate() {
        val cal = WibDateUtils.getCalendar(customStartDate)
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = WibDateUtils.getCalendar().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val newStart = newCal.timeInMillis
                customStartDate = newStart
                if (customEndDate < newStart) {
                    customEndDate = newStart + 24 * 60 * 60 * 1000L - 1L
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickEndDate() {
        val cal = WibDateUtils.getCalendar(customEndDate)
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = WibDateUtils.getCalendar().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                val newEnd = newCal.timeInMillis
                customEndDate = newEnd
                if (customStartDate > newEnd) {
                    customStartDate = WibDateUtils.getCalendar(newEnd).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBg)
    ) {
        // Sub-Navigation Tabs: Input vs Report (berwarna saat dipilih)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            color = Color(0xFFF1F5F9),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab Input
                val isInputSelected = selectedSubMenu == 0
                Surface(
                    onClick = { selectedSubMenu = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("tab_input_flushing"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isInputSelected) BrandGreen else Color.Transparent,
                    shadowElevation = if (isInputSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Input Flushing",
                            tint = if (isInputSelected) Color.White else SlateGrey,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Input",
                            fontWeight = if (isInputSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isInputSelected) Color.White else SlateGrey
                        )
                    }
                }

                // Tab Report
                val isReportSelected = selectedSubMenu == 1
                Surface(
                    onClick = { selectedSubMenu = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("tab_report_flushing"),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isReportSelected) BrandGreen else Color.Transparent,
                    shadowElevation = if (isReportSelected) 2.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Report Flushing",
                            tint = if (isReportSelected) Color.White else SlateGrey,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Report",
                            fontWeight = if (isReportSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isReportSelected) Color.White else SlateGrey
                        )
                    }
                }
            }
        }

        // Body Content based on active tab
        when (selectedSubMenu) {
            0 -> {
                // =========================================================================
                // MENU INPUT FLUSHING
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Guidance Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateGrey),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Flushing Sludge Centrifuge",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            "Pembersihan berkala nozzle & bowl memakai air panas 90-95°C",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WaterDrop,
                                            contentDescription = "Flushing Water",
                                            tint = Color(0xFF64B5F6),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section: Identitas Operator, Unit & Shift
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Data Pelaksanaan Flushing",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGrey
                                )

                                // Unit Mesin (SC-01 sampai SC-08) (Requested: Background Putih/Hijau saat dipilih, Tulisan Hitam)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Unit Mesin:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        Text("Pilihan: SC-01 s/d SC-08", fontSize = 11.sp, color = Color.DarkGray)
                                    }

                                    // Quick selector chips for SC-01 to SC-08 (Tulisan hitam, jika dipilih background hijau)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        unitOptions.forEach { unit ->
                                            val isUnitSelected = unit == selectedUnit
                                            Surface(
                                                onClick = { selectedUnit = unit },
                                                modifier = Modifier
                                                    .height(38.dp)
                                                    .testTag("unit_chip_$unit"),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.5.dp, Color.Black),
                                                color = if (isUnitSelected) Color(0xFF86EFAC) else Color.White
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    if (isUnitSelected) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .background(Color(0xFF16A34A), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Terpilih",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(11.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = unit,
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isUnitSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = Color.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Nama Operator Dropdown List (Wahyu dan Abdul Aziz) (Requested: Background Putih, Tulisan Hitam)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Nama Operator:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Box {
                                        Surface(
                                            onClick = { expandedOperatorDropdown = true },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                                .testTag("flushing_operator_dropdown"),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.5.dp, Color.Black),
                                            color = Color.White
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = operatorName,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.ArrowDropDown,
                                                    contentDescription = "Pilih Operator",
                                                    tint = Color.Black
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = expandedOperatorDropdown,
                                            onDismissRequest = { expandedOperatorDropdown = false },
                                            modifier = Modifier
                                                .fillMaxWidth(0.9f)
                                                .background(Color.White)
                                        ) {
                                            operatorOptions.forEach { op ->
                                                val isOpSelected = op == operatorName
                                                DropdownMenuItem(
                                                    modifier = Modifier.background(Color.White),
                                                    text = {
                                                        Text(
                                                            text = op,
                                                            fontSize = 13.sp,
                                                            fontWeight = if (isOpSelected) FontWeight.Bold else FontWeight.Normal,
                                                            color = Color.Black
                                                        )
                                                    },
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Default.Person,
                                                            contentDescription = null,
                                                            tint = Color.Black,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    },
                                                    trailingIcon = if (isOpSelected) {
                                                        {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(20.dp)
                                                                    .background(Color(0xFF16A34A), CircleShape),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Check,
                                                                    contentDescription = "Terpilih",
                                                                    tint = Color.White,
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                        }
                                                    } else null,
                                                    onClick = {
                                                        operatorName = op
                                                        expandedOperatorDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9))

                                // Shift Selector (Requested: Shift Kerja :   |Shift Pagi|     |Shift Malam|, centang putih background hijau)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, Color.Black),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Shift Kerja :",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // |Shift Pagi| Button
                                            val isPagi = selectedShift == "Shift Pagi"
                                            Surface(
                                                onClick = {
                                                    selectedShift = "Shift Pagi"
                                                    masterHour = "08:00"
                                                    tasksState = tasksState.map { it.copy(time = "08:00") }
                                                },
                                                modifier = Modifier
                                                    .height(38.dp)
                                                    .testTag("shift_pagi_button"),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.5.dp, Color.Black),
                                                color = if (isPagi) Color(0xFF86EFAC) else Color.White
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (isPagi) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .background(Color(0xFF16A34A), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Terpilih",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "|Shift Pagi|",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.Black
                                                    )
                                                }
                                            }

                                            // |Shift Malam| Button
                                            val isMalam = selectedShift == "Shift Malam"
                                            Surface(
                                                onClick = {
                                                    selectedShift = "Shift Malam"
                                                    masterHour = "20:00"
                                                    tasksState = tasksState.map { it.copy(time = "20:00") }
                                                },
                                                modifier = Modifier
                                                    .height(38.dp)
                                                    .testTag("shift_malam_button"),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.5.dp, Color.Black),
                                                color = if (isMalam) Color(0xFF86EFAC) else Color.White
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (isMalam) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .background(Color(0xFF16A34A), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Terpilih",
                                                                tint = Color.White,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = "|Shift Malam|",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section Title: List Kegiatan Sesuai Gambar
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Daftar Kegiatan Flushing (11 Poin)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            val doneCount = tasksState.count { it.isDone }
                            Text(
                                "$doneCount/11 Selesai",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (doneCount == 11) BrandGreen else BrandOrange
                            )
                        }
                    }

                    // 11 Activities grouped into "Persiapan" (1-3) & "Operation" (4-11)
                    val persiapanTasks = tasksState.filter { it.tahapanGroup == "Persiapan" }
                    val operationTasks = tasksState.filter { it.tahapanGroup == "Operation" }

                    // --- Subheading: Persiapan ---
                    item {
                        Surface(
                            color = Color(0xFFE0F2FE),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.BuildCircle, contentDescription = null, tint = Color(0xFF0369A1), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tahapan: Persiapan (Poin 1 - 3)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0369A1))
                            }
                        }
                    }

                    items(persiapanTasks, key = { it.no }) { task ->
                        FlushingTaskRowItem(
                            task = task,
                            onCheckChange = { isChecked ->
                                tasksState = tasksState.map { if (it.no == task.no) it.copy(isDone = isChecked) else it }
                            }
                        )
                    }

                    // --- Subheading: Operation ---
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PlayCircleFilled, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tahapan: Operation (Poin 4 - 11)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                            }
                        }
                    }

                    items(operationTasks, key = { it.no }) { task ->
                        FlushingTaskRowItem(
                            task = task,
                            onCheckChange = { isChecked ->
                                tasksState = tasksState.map { if (it.no == task.no) it.copy(isDone = isChecked) else it }
                            }
                        )
                    }

                    // Notes and Confirmation Actions
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = flushingNotes,
                                    onValueChange = { flushingNotes = it },
                                    label = { Text("Catatan / Evaluasi Flushing", color = Color.Black) },
                                    placeholder = { Text("Contoh: Air panas mencapai 93°C, getaran bowl halus, tidak ada kotoran tersisa.") },
                                    minLines = 2,
                                    textStyle = TextStyle(color = Color.Black, fontSize = 14.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.Black,
                                        unfocusedTextColor = Color.Black,
                                        focusedLabelColor = Color.Black,
                                        unfocusedLabelColor = Color.Black
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            tasksState = tasksState.map { it.copy(isDone = true) }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.5.dp, Color(0xFF16A34A)),
                                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(18.dp)
                                                .background(Color(0xFF16A34A), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Centang Semua (11)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                    }

                                    Button(
                                        onClick = {
                                            val finalOpName = if (operatorName.isNotBlank()) operatorName.trim() else "Anton Suherman"
                                            val doneCount = tasksState.count { it.isDone }
                                            val newLog = FlushingLog(
                                                operatorName = finalOpName,
                                                shift = selectedShift,
                                                unitName = selectedUnit,
                                                itemsJson = flushingTasksToJson(tasksState),
                                                completedCount = doneCount,
                                                totalCount = tasksState.size,
                                                notes = flushingNotes.trim(),
                                                isSynced = false
                                            )
                                            onAddFlushingLog(newLog)
                                            Toast.makeText(context, "Laporan Flushing berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                            // Auto-navigate to Report tab so operator sees their entry
                                            selectedSubMenu = 1
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .testTag("save_flushing_button")
                                    ) {
                                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Simpan Laporan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            1 -> {
                // =========================================================================
                // MENU REPORT FLUSHING
                // =========================================================================
                val now = System.currentTimeMillis()
                val startOfToday = remember(now) { WibDateUtils.getStartOfDay(now) }
                val weekThreshold = remember(now) { now - 7L * 24 * 60 * 60 * 1000L }
                val monthThreshold = remember(now) { now - 30L * 24 * 60 * 60 * 1000L }

                val periodLogs = flushingLogs.filter { log ->
                    when (filterPeriod) {
                        "Hari ini" -> log.timestamp >= startOfToday
                        "Seminggu" -> log.timestamp >= weekThreshold
                        "Sebulan" -> log.timestamp >= monthThreshold
                        "Tanggal" -> log.timestamp in customStartDate..customEndDate
                        else -> true
                    }
                }

                val filteredLogs = periodLogs.filter { log ->
                    val matchesShift = filterShift == "Semua" || log.shift == filterShift
                    val matchesUnit = filterUnit == "Semua" || 
                        log.unitName.replace(" ", "-").equals(filterUnit.replace(" ", "-"), ignoreCase = true) ||
                        log.unitName.equals(filterUnit, ignoreCase = true)

                    matchesShift && matchesUnit
                }

                val shiftPagiCount = filteredLogs.count { it.shift == "Shift Pagi" }
                val shiftMalamCount = filteredLogs.count { it.shift == "Shift Malam" }
                val avgCompliance = if (filteredLogs.isNotEmpty()) {
                    (filteredLogs.sumOf { it.completedCount }.toDouble() / (filteredLogs.size * 11) * 100).toInt()
                } else 100

                // Per-mesin SC01 - SC08 flushing counts for Shift Pagi & Shift Malam
                val scPagiCounts = (1..8).map { idx ->
                    periodLogs.count { log ->
                        val clean = log.unitName.replace("-", "").replace(" ", "").uppercase()
                        (clean == "SC0$idx" || clean == "SC$idx") && log.shift.contains("Pagi", ignoreCase = true)
                    }
                }
                val scMalamCounts = (1..8).map { idx ->
                    periodLogs.count { log ->
                        val clean = log.unitName.replace("-", "").replace(" ", "").uppercase()
                        (clean == "SC0$idx" || clean == "SC$idx") && log.shift.contains("Malam", ignoreCase = true)
                    }
                }
                val totalPagiTable = scPagiCounts.sum()
                val totalMalamTable = scMalamCounts.sum()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // KPI Highlights & Rekapitulasi Mesin (Kotak Hitam)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateGrey),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Rekapitulasi Laporan Flushing Mill",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Periode: $filterPeriod",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFF334155),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "${filteredLogs.size} Laporan",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    FlushingStatBox(label = "Total Laporan", value = "${filteredLogs.size}", color = Color.White)
                                    FlushingStatBox(label = "Shift Pagi", value = "$shiftPagiCount", color = Color(0xFFFFD54F))
                                    FlushingStatBox(label = "Shift Malam", value = "$shiftMalamCount", color = Color(0xFFB39DDB))
                                    FlushingStatBox(label = "Kepatuhan SOP", value = "$avgCompliance%", color = Color(0xFF81C784))
                                }

                                // Tabel 3 Kolom: |Mesin| |Shift Pagi| |Shift Malam|
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        // Header: |Mesin| |Shift Pagi| |Shift Malam|
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF334155))
                                                .padding(horizontal = 10.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "|Mesin|",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "|Shift Pagi|",
                                                color = Color(0xFFFFD54F),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1.2f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "|Shift Malam|",
                                                color = Color(0xFFB39DDB),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1.2f),
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        HorizontalDivider(color = Color(0xFF475569), thickness = 1.dp)

                                        // List Baris: |SC01| sampai |SC08|
                                        (1..8).forEach { idx ->
                                            val scLabel = String.format("|SC%02d|", idx)
                                            val pagiCount = scPagiCounts[idx - 1]
                                            val malamCount = scMalamCounts[idx - 1]
                                            val isEven = idx % 2 == 0
                                            val rowBg = if (isEven) Color(0xFF1E293B) else Color(0xFF0F172A)

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(rowBg)
                                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Kolom 1: |SC01| .. |SC08|
                                                Text(
                                                    text = scLabel,
                                                    color = Color.White,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f),
                                                    textAlign = TextAlign.Center
                                                )

                                                // Kolom 2: Shift Pagi
                                                Box(
                                                    modifier = Modifier.weight(1.2f),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (pagiCount > 0) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color(0xFF78350F).copy(alpha = 0.5f),
                                                            border = BorderStroke(0.5.dp, Color(0xFFF59E0B))
                                                        ) {
                                                            Text(
                                                                text = "$pagiCount kali",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFFFD54F),
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "0 kali",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B),
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }

                                                // Kolom 3: Shift Malam
                                                Box(
                                                    modifier = Modifier.weight(1.2f),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (malamCount > 0) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color(0xFF3B0764).copy(alpha = 0.5f),
                                                            border = BorderStroke(0.5.dp, Color(0xFFA855F7))
                                                        ) {
                                                            Text(
                                                                text = "$malamCount kali",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFD8B4FE),
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    } else {
                                                        Text(
                                                            text = "0 kali",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B),
                                                            textAlign = TextAlign.Center
                                                        )
                                                    }
                                                }
                                            }

                                            if (idx < 8) {
                                                HorizontalDivider(color = Color(0xFF334155).copy(alpha = 0.5f), thickness = 0.5.dp)
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFF475569), thickness = 1.dp)

                                        // Total Row
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF334155))
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "|Total|",
                                                color = Color.White,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "$totalPagiTable kali",
                                                color = Color(0xFFFFD54F),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1.2f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = "$totalMalamTable kali",
                                                color = Color(0xFFB39DDB),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1.2f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Filter Chips Row
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Periode Report Filter (Hari ini | Seminggu | Sebulan | Tanggal)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Periode Report:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    listOf("Hari ini", "Seminggu", "Sebulan", "Tanggal").forEach { prd ->
                                        val isSelected = filterPeriod == prd
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { filterPeriod = prd },
                                            label = {
                                                Text(
                                                    text = prd,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color.Black
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color.White,
                                                labelColor = Color.Black,
                                                selectedContainerColor = BrandGreen,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                if (filterPeriod == "Tanggal") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Tanggal Mulai
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { pickStartDate() },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "TANGGAL MULAI",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SlateGrey
                                                    )
                                                    Text(
                                                        text = WibDateUtils.format("dd MMM yyyy", customStartDate),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = "Pilih Tanggal Mulai",
                                                    tint = BrandGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )

                                        // Tanggal Akhir
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { pickEndDate() },
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFF8FAFC),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "TANGGAL AKHIR",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SlateGrey
                                                    )
                                                    Text(
                                                        text = WibDateUtils.format("dd MMM yyyy", customEndDate),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black
                                                    )
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = "Pilih Tanggal Akhir",
                                                    tint = BrandGreen,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)

                                // Shift Filter
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Filter Shift:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    listOf("Semua", "Shift Pagi", "Shift Malam").forEach { sft ->
                                        val isSelected = filterShift == sft
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { filterShift = sft },
                                            label = {
                                                Text(
                                                    text = sft,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else Color.Black
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = Color.White,
                                                labelColor = Color.Black,
                                                selectedContainerColor = if (sft == "Shift Pagi") Color(0xFFFFA000) else if (sft == "Shift Malam") Color(0xFF512DA8) else BrandGreen,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 0.5.dp)

                                // Unit Filter (Semua, SC-01 s/d SC-08)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Filter Unit:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    listOf("Semua", "SC-01", "SC-02", "SC-03", "SC-04", "SC-05", "SC-06", "SC-07", "SC-08").forEach { unt ->
                                        val isUntSelected = filterUnit == unt
                                        FilterChip(
                                            selected = isUntSelected,
                                            onClick = { filterUnit = unt },
                                            label = {
                                                Text(
                                                    text = unt,
                                                    fontSize = 10.sp,
                                                    color = Color.Black,
                                                    fontWeight = if (isUntSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF86EFAC),
                                                selectedLabelColor = Color.Black,
                                                containerColor = Color.White,
                                                labelColor = Color.Black
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredLogs.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Belum ada laporan flushing untuk filter ini", fontWeight = FontWeight.Medium, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { selectedSubMenu = 0 },
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                                    ) {
                                        Text("Buat Laporan Flushing Baru", color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredLogs, key = { it.id }) { log ->
                            FlushingReportCard(
                                log = log,
                                onDelete = { logToDelete = log }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text("Hapus Laporan Flushing?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Laporan ${logToDelete?.unitName} oleh ${logToDelete?.operatorName} (${logToDelete?.shift}) akan dihapus secara permanen.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        logToDelete?.let { onDeleteFlushingLog(it) }
                        logToDelete = null
                        Toast.makeText(context, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------------------------------------------
// FLUSHING TASK ROW ITEM (INPUT VIEW WITH STATUS CHECKBOX - JAM REMOVED PER USER REQUEST)
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun FlushingTaskRowItem(
    task: FlushingTaskItem,
    onCheckChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (task.isDone) Color.White else Color(0xFFFFFBEB)
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (task.isDone) Color(0xFFE2E8F0) else Color(0xFFFDE68A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: No, Tahapan Name, Criteria badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(if (task.tahapanGroup == "Persiapan") Color(0xFF0284C7) else BrandGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${task.no}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        task.tahapanName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = SlateGrey
                    )
                }

                Surface(
                    color = if (task.isDone) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(0.5.dp, if (task.isDone) Color(0xFF10B981) else Color(0xFFEF4444))
                ) {
                    Text(
                        text = "Kriteria: ${task.criteria}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isDone) Color(0xFF047857) else Color(0xFFB91C1C),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Row 2: Aksi Spesifik (Diluruskan inline mengalir satu baris / continuous text)
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.DarkGray)) {
                        append("Aksi: ")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = Color.Black)) {
                        append(task.specificAction)
                    }
                },
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Row 3: Status Pelaksanaan (Jam per item dihapus sesuai permintaan)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCheckChange(!task.isDone) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (task.isDone) "Pelaksanaan: Sesuai Kriteria" else "Pelaksanaan: Belum Dilaksanakan",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isDone) Color(0xFF047857) else Color(0xFFB45309)
                )

                // Checkbox status: Centang putih dengan background hijau
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = if (task.isDone) Color(0xFF16A34A) else Color.White,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (task.isDone) Color(0xFF16A34A) else Color(0xFF94A3B8),
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (task.isDone) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selesai",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (task.isDone) "Selesai (OK)" else "Belum",
                        fontSize = 11.sp,
                        fontWeight = if (task.isDone) FontWeight.Bold else FontWeight.Normal,
                        color = if (task.isDone) Color(0xFF15803D) else Color.Gray
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// FLUSHING STAT BOX FOR KPI HEADER
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun FlushingStatBox(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 9.sp, color = Color.LightGray)
    }
}

// -----------------------------------------------------------------------------------------------------------------
// FLUSHING REPORT CARD (WITH FULL IMAGE.PNG STYLE SOP TABLE)
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun FlushingReportCard(
    log: FlushingLog,
    onDelete: () -> Unit
) {
    var expandedTable by remember { mutableStateOf(false) }
    val tasks = remember(log.itemsJson) { jsonToFlushingTasks(log.itemsJson) }
    val isPagi = log.shift == "Shift Pagi"
    val dateText = remember(log.timestamp) {
        WibDateUtils.format("dd MMM yyyy, HH:mm", log.timestamp) + " WIB"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Unit SC, Shift badge, and Delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(BrandGreen, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(log.unitName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sludge Centrifuge",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = SlateGrey
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Shift Badge (| Shift Pagi | / | Shift Malam |)
                    Surface(
                        color = if (isPagi) Color(0xFFFFF9C4) else Color(0xFFEDE7F6),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isPagi) Color(0xFFFFA000) else Color(0xFF512DA8))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPagi) Icons.Default.WbSunny else Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = if (isPagi) Color(0xFFE65100) else Color(0xFF512DA8),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "| ${log.shift} |",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPagi) Color(0xFFE65100) else Color(0xFF512DA8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Info Operator & Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Operator: ${log.operatorName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SlateGrey)
                }
                Text(dateText, fontSize = 10.sp, color = Color.Gray)
            }

            // Compliance Progress
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${log.completedCount} / ${log.totalCount} Tahapan Selesai",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.completedCount == log.totalCount) BrandGreen else BrandOrange
                    )
                    Text(
                        if (log.completedCount == log.totalCount) "100% SOP Terpenuhi" else "${(log.completedCount * 100) / log.totalCount}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (log.completedCount == log.totalCount) BrandGreen else BrandOrange
                    )
                }
                LinearProgressIndicator(
                    progress = { log.completedCount.toFloat() / log.totalCount.toFloat() },
                    color = if (log.completedCount == log.totalCount) BrandGreen else BrandOrange,
                    trackColor = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
            }

            if (log.notes.isNotBlank()) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "Catatan / Evaluasi Flushing:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = log.notes,
                            fontSize = 11.sp,
                            color = Color.Black,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Expand / Collapse Table Matching image.png
            OutlinedButton(
                onClick = { expandedTable = !expandedTable },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (expandedTable) Icons.Default.ExpandLess else Icons.Default.TableChart,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = BrandGreen
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    if (expandedTable) "Sembunyikan Lembar SOP" else "Lihat Tabel Lembar SOP (Sesuai Gambar)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandGreen
                )
            }

            // Render Exact Table from image.png
            if (expandedTable) {
                FlushingTableFullView(tasks = tasks)
            }
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// FLUSHING TABLE VIEW EXACTLY MATCHING IMAGE.PNG
// -----------------------------------------------------------------------------------------------------------------
@Composable
fun FlushingTableFullView(tasks: List<FlushingTaskItem>) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            // Table Header: Tahapan | No | Tahapan | Aksi Spesifik | Kriteria/ Hasil | Jam
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                    .padding(vertical = 6.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                Text("Tahapan", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1.1f))
                Text("Aksi Spesifik", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1.8f))
                Text("Kriteria/ Hasil", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1.2f))
                Text("Jam", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                Text("✓", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(20.dp), textAlign = TextAlign.Center)
            }

            HorizontalDivider(color = Color(0xFF94A3B8), thickness = 1.dp)

            // Group: Persiapan
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE0F2FE))
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                Text("Tahapan: Persiapan", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF0369A1))
            }

            tasks.filter { it.tahapanGroup == "Persiapan" }.forEach { item ->
                FlushingTableRowItem(item = item)
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
            }

            // Group: Operation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDCFCE7))
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            ) {
                Text("Tahapan: Operation", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF15803D))
            }

            tasks.filter { it.tahapanGroup == "Operation" }.forEach { item ->
                FlushingTableRowItem(item = item)
                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun FlushingTableRowItem(item: FlushingTaskItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${item.no}", fontSize = 10.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        Text(item.tahapanName, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.1f), color = SlateGrey)
        Text(item.specificAction, fontSize = 9.sp, modifier = Modifier.weight(1.8f), color = Color.Black, lineHeight = 12.sp)
        Text(item.criteria, fontSize = 9.sp, modifier = Modifier.weight(1.2f), color = Color(0xFF0F766E), fontWeight = FontWeight.Medium)
        Text(item.time, fontSize = 10.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = SlateGrey)
        Box(
            modifier = Modifier.width(22.dp),
            contentAlignment = Alignment.Center
        ) {
            if (item.isDone) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFF16A34A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "OK",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            } else {
                Spacer(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFF1F5F9), CircleShape)
                        .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                )
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
            Text(itemsMastered, fontSize = 10.sp, color = Color.Black)
        }
    }
}

// -----------------------------------------------------------------------------------------------------------------
// 5. REPORT CENTER (MONITORING & TREND ANALYSIS, CILT, RELIABILITY PM, FLUSHING EVALUATION)
// Implemented in JakartaScreen.kt
// -----------------------------------------------------------------------------------------------------------------

