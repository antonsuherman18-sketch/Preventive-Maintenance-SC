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

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Monitoring & Trend Analysis SC", Icons.Default.Analytics)
    object Checklist : Screen("checklist", "CILT", Icons.Default.FactCheck)
    object Abnormality : Screen("abnormality", "Reliability PM", Icons.Default.Build)
    object Training : Screen("training", "Flushing", Icons.Default.WaterDrop)
    object Jakarta : Screen("jakarta", "Report Center", Icons.Default.Assessment)
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
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Integrasi Monitoring Sludge Centrifuge - Autonomous CILT - Reliability Preventive Maintenance",
                                fontSize = 10.sp,
                                color = Color(0xFFCCE8E8),
                                fontWeight = FontWeight.Medium,
                                lineHeight = 13.sp
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
                    modifier = Modifier.height(76.dp)
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
                                    fontSize = 9.sp, 
                                    lineHeight = 11.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
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
                                unselectedIconColor = Color.DarkGray,
                                unselectedTextColor = Color.DarkGray
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
                        verticalArrangement = Arrangement.spacedBy(3.dp)
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
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF4ADE80) else Color(0xFF94A3B8))
                            )
                            Text(
                                text = syncStatus,
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

fun getUnitSummaryMetrics(
    unit: CentrifugeUnitState,
    timeRangeSelection: Int, // 0: Harian, 1: Mingguan, 2: Bulanan
    vibrationLogs: List<VibrationLog>
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
    val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000L
    val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000L

    val unitLogs = vibrationLogs.filter { log ->
        log.comments.contains("[${unit.id}]") || log.comments.contains(unit.id)
    }
    val todayLogs = unitLogs.filter { it.timestamp in startOfToday..endOfToday }
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
                val soundState = latestTodayLog?.soundState ?: "Normal"
                val hasLeakage = latestTodayLog?.hasLeakage ?: false

                reportValues = UnitReportValues(
                    dev = deAvg, nde = ndeAvg, motorVib = mAvg, gearboxVib = gAvg, bowlVib = bAvg,
                    bearingTemp = bTempAvg, motorTemp = mTempAvg,
                    isGreased = isGreased,
                    soundState = soundState,
                    hasLeakage = hasLeakage,
                    greasingRatioText = if (isGreased) "Sudah Dilakukan" else "Belum Dilakukan",
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
        1 -> { // Mingguan
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
        else -> { // Bulanan
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
    }

    val alarmStatus = if (timeRangeSelection == 0 && !isMeasuredToday) {
        if (!unit.isRunning) "STANDBY" else "UNMEASURED"
    } else if (!unit.isRunning && weeklyLogs.isEmpty() && monthlyLogs.isEmpty()) {
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
fun ReportRowItem(
    number: String,
    label: String,
    value: Float?,
    unit: String,
    threshold: Float,
    isDegree: Boolean = false
) {
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
        Text(
            text = "$number. $label :",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = SlateGrey,
            modifier = Modifier.width(135.dp)
        )
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
fun DashboardScreen(
    vibrationLogs: List<VibrationLog>,
    abnormalityReports: List<AbnormalityReport>,
    ciltChecks: List<CiltCheck>,
    viewModel: CentrifugeViewModel
) {
    val context = LocalContext.current
    var selectedUnitIndex by remember { mutableStateOf(0) } // Default to SC-01 (index 0)
    var trendTabSelection by remember { mutableStateOf(0) } // 0: Vibrasi (mm/s), 1: Suhu (°C)
    var timeRangeSelection by remember { mutableStateOf(0) } // 0: Harian, 1: Mingguan, 2: Bulanan

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
            val unitWeeklyLogs = vibrationLogs.filter { log ->
                log.comments.contains("[${selectedUnit.id}]") || log.comments.contains(selectedUnit.id)
            }

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
                val logsOnDay = unitWeeklyLogs.filter { it.timestamp in s..e }
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
                val logsOnDay = unitWeeklyLogs.filter { it.timestamp in s..e }
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
                val logsOnDay = unitWeeklyLogs.filter { it.timestamp in s..e }
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
                val logsOnDay = unitWeeklyLogs.filter { it.timestamp in s..e }
                if (logsOnDay.isNotEmpty()) {
                    if (logsOnDay.any { it.isGreased }) 100f else 0f
                } else {
                    selectedUnit.weeklyGreasingHistory.getOrElse(i) { if (selectedUnit.isRunning) 100f else 0f }
                }
            }

            // Get data lists based on time range selection
            val devVibPoints = when (timeRangeSelection) {
                0 -> selectedUnit.devVibHistory
                1 -> weeklyDevVibPoints
                else -> selectedUnit.monthlyDevVibHistory
            }
            val ndevVibPoints = when (timeRangeSelection) {
                0 -> selectedUnit.ndevVibHistory
                1 -> weeklyNdevVibPoints
                else -> selectedUnit.monthlyNdevVibHistory
            }
            val bearingTempPoints = when (timeRangeSelection) {
                0 -> selectedUnit.bearingTempHistory
                1 -> weeklyBearingTempPoints
                else -> selectedUnit.monthlyBearingTempHistory
            }
            val greasingPoints = when (timeRangeSelection) {
                0 -> {
                    if (selectedUnit.greasingHistory.isNotEmpty()) selectedUnit.greasingHistory
                    else List(12) { if (selectedUnit.isRunning) 100f else 0f }
                }
                1 -> weeklyGreasingPoints
                else -> {
                    if (selectedUnit.monthlyGreasingHistory.isNotEmpty()) selectedUnit.monthlyGreasingHistory
                    else List(12) { if (selectedUnit.isRunning) 100f else 0f }
                }
            }

            val currentDev = devVibPoints.lastOrNull() ?: selectedUnit.baseDeVib
            val currentNdev = ndevVibPoints.lastOrNull() ?: selectedUnit.baseNdeVib
            val currentTemp = bearingTempPoints.lastOrNull() ?: selectedUnit.baseBearingTemp

            val selectedUnitSummary = getUnitSummaryMetrics(selectedUnit, timeRangeSelection, vibrationLogs)
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Input Pengukuran Mesin Sludge Centrifuge",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                        }
                        IconButton(
                            onClick = { showVibDialog = true },
                            modifier = Modifier.testTag("add_vibration_log_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Log Manual", tint = BrandGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pilihan Periode Report (Harian | Mingguan | Bulanan) - DI ATAS "Pilih Unit Mesin"
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
                        listOf("Harian", "Mingguan", "Bulanan").forEachIndexed { index, label ->
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
                            val summary = getUnitSummaryMetrics(unit, timeRangeSelection, vibrationLogs)

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

                                    val isZeroHarian = timeRangeSelection == 0 && !summary.isMeasuredToday
                                    Text(
                                        text = if (isZeroHarian) "0.0 mm/s"
                                               else if (unit.isRunning) "${String.format(Locale.US, "%.1f", summary.avgVib)} mm/s" 
                                               else "Standby",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isZeroHarian || summary.alarmStatus == "NORMAL" || summary.alarmStatus == "UNMEASURED") SlateGrey else alarmColor
                                    )
                                    Text(
                                        text = if (isZeroHarian) "0.0 °C"
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
                                                "Rata-Rata Pengukuran Harian (${selectedUnitSummary.todayLogCount}x Pengukuran)"
                                            } else if (isMeasuredToday) {
                                                "Hasil Pengukuran Harian (${WibDateUtils.format("dd MMMM yyyy", Date())})"
                                            } else {
                                                "Laporan Harian (${WibDateUtils.format("dd MMMM yyyy", Date())}) - Belum Diukur"
                                            }
                                        }
                                        1 -> "Rata-Rata Pengukuran Mingguan (7 Hari Terakhir)"
                                        else -> "Rata-Rata Pengukuran Bulanan (30 Hari Terakhir)"
                                    }
                                    Text(
                                        text = subtitleText,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                val hasValues = if (timeRangeSelection == 0) isMeasuredToday else (reportValues.dev != null && (reportValues.dev ?: 0f) > 0f)
                                val isAbnormal = hasValues && (
                                    (reportValues.dev ?: 0f) > 4.0f ||
                                    (reportValues.nde ?: 0f) > 4.0f ||
                                    (reportValues.motorVib ?: 0f) > 4.0f ||
                                    (reportValues.gearboxVib ?: 0f) > 4.0f ||
                                    (reportValues.bowlVib ?: 0f) > 4.0f ||
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

                            ReportRowItem("1", "Drive End", reportValues.dev, "mm/s", 4.0f)
                            ReportRowItem("2", "Non Drive End", reportValues.nde, "mm/s", 4.0f)
                            ReportRowItem("3", "Bearing Motor", reportValues.motorVib, "mm/s", 4.0f)
                            ReportRowItem("4", "Bearing Gearbox", reportValues.gearboxVib, "mm/s", 4.0f)
                            ReportRowItem("5", "Bowl", reportValues.bowlVib, "mm/s", 4.0f)

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
                                                color = SlateGrey
                                            )
                                            val obsNote = latestTodayLog.comments.replace(Regex("\\[SC-[0-9]{2}\\]"), "").trim()
                                            Text(
                                                text = obsNote.ifEmpty { "Kondisi operasi centrifuge normal dan stabil." },
                                                fontSize = 11.sp,
                                                color = Color.DarkGray,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Waktu terakhir: ${WibDateUtils.format("HH:mm", latestTodayLog.timestamp)} WIB",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = "Operator: ${latestTodayLog.operatorName.ifEmpty { "Operator Centrifuge" }}",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
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
                                    text = if (selectedUnit.isRunning) "Mesin Beroperasi" else "Standby Mode",
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

                    Spacer(modifier = Modifier.height(12.dp))

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
                                .height(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (hasCriticalValue) Color(0xFFFFF5F5) else Color(0xFFFAFAFA))
                                .border(
                                    width = if (hasCriticalValue) 1.5.dp else 1.dp,
                                    color = if (hasCriticalValue) BrandRed.copy(alpha = 0.7f) else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val width = size.width
                                val height = size.height
                                val points = historyPoints.size
                                val spacing = width / (points - 1).coerceAtLeast(1)

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
                                        val prevX = (index - 1) * spacing
                                        val prevY = (height - (prevVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                                        val currX = index * spacing
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
                                        val x = index * spacing
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

                                    // Plot NDE Vibration Line Segments
                                    for (index in 1 until ndevVibPoints.size) {
                                        val prevVib = ndevVibPoints[index - 1]
                                        val currVib = ndevVibPoints[index]
                                        val prevX = (index - 1) * spacing
                                        val prevY = (height - (prevVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                                        val currX = index * spacing
                                        val currY = (height - (currVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)

                                        val isCritical = currVib >= selectedUnit.criticalVib || prevVib >= selectedUnit.criticalVib
                                        val segColor = if (isCritical) BrandRed.copy(alpha = 0.85f) else Color.Gray.copy(alpha = 0.5f)
                                        val strokeW = if (isCritical) 3.5f else 2.5f

                                        drawLine(
                                            color = segColor,
                                            start = Offset(prevX, prevY),
                                            end = Offset(currX, currY),
                                            strokeWidth = strokeW,
                                            cap = StrokeCap.Round
                                        )
                                    }

                                    // Draw Vibration measurement numbers along the line with red color for critical
                                    drawIntoCanvas { canvas ->
                                        val paintCritical = Paint().apply {
                                            color = android.graphics.Color.rgb(186, 26, 26) // BrandRed
                                            textSize = 25f
                                            typeface = Typeface.DEFAULT_BOLD
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        val paintWarning = Paint().apply {
                                            color = android.graphics.Color.rgb(245, 124, 0) // BrandOrange
                                            textSize = 24f
                                            typeface = Typeface.DEFAULT_BOLD
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        val paintDe = Paint().apply {
                                            color = android.graphics.Color.rgb(0, 106, 106) // BrandGreen
                                            textSize = 24f
                                            typeface = Typeface.DEFAULT_BOLD
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        val paintNde = Paint().apply {
                                            color = android.graphics.Color.rgb(100, 116, 139) // SlateGrey
                                            textSize = 20f
                                            typeface = Typeface.DEFAULT
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }

                                        devVibPoints.forEachIndexed { index, valVib ->
                                            val x = index * spacing
                                            val yDe = (height - (valVib / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                                            val labelDe = String.format(Locale.US, "%.1f", valVib)
                                            val p = if (valVib >= selectedUnit.criticalVib) paintCritical else if (valVib >= selectedUnit.warningVib) paintWarning else paintDe
                                            canvas.nativeCanvas.drawText(labelDe, x, (yDe - 10f).coerceAtLeast(20f), p)
                                        }

                                        ndevVibPoints.forEachIndexed { index, valNde ->
                                            val x = index * spacing
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
                                        val prevX = (index - 1) * spacing
                                        val prevY = (height - (prevTemp / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                                        val currX = index * spacing
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
                                        val x = index * spacing
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

                                    // Draw Temperature measurement numbers along the line with red color for critical
                                    drawIntoCanvas { canvas ->
                                        val paintTempCritical = Paint().apply {
                                            color = android.graphics.Color.rgb(186, 26, 26) // BrandRed
                                            textSize = 25f
                                            typeface = Typeface.DEFAULT_BOLD
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        val paintTempWarning = Paint().apply {
                                            color = android.graphics.Color.rgb(245, 124, 0) // BrandOrange
                                            textSize = 24f
                                            typeface = Typeface.DEFAULT_BOLD
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        val paintTempNormal = Paint().apply {
                                            color = android.graphics.Color.rgb(0, 106, 106) // BrandGreen
                                            textSize = 24f
                                            typeface = Typeface.DEFAULT_BOLD
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }

                                        bearingTempPoints.forEachIndexed { index, valTemp ->
                                            val x = index * spacing
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
                                        val prevX = (index - 1) * spacing
                                        val prevY = (height - (prevGreas / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                                        val currX = index * spacing
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
                                        val x = index * spacing
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
                                            textSize = 21f
                                            typeface = Typeface.DEFAULT_BOLD
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }
                                        val paintGreasNormal = Paint().apply {
                                            color = android.graphics.Color.rgb(0, 106, 106) // BrandGreen
                                            textSize = 21f
                                            typeface = Typeface.DEFAULT_BOLD
                                            textAlign = Paint.Align.CENTER
                                            isAntiAlias = true
                                        }

                                        greasingPoints.forEachIndexed { index, valGreas ->
                                            val x = index * spacing
                                            val y = (height - (valGreas / maxScale).coerceIn(0f, 1f) * height).coerceIn(16f, height - 16f)
                                            val isCritical = valGreas < 50f
                                            val labelGreas = if (isCritical) "0% Belum" else "100% OK"
                                            val p = if (isCritical) paintGreasCritical else paintGreasNormal
                                            canvas.nativeCanvas.drawText(labelGreas, x, (y - 10f).coerceAtLeast(20f), p)
                                        }
                                    }
                                }
                            }

                            if (hasCriticalValue) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .background(BrandRed, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text("KRITIKAL", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // X-axis Time Labels with Dates for Weekly View
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (timeRangeSelection == 1) {
                                // Grafik Mingguan: 7 hari kebelakang (H-6 s/d Hari Ini dalam WIB)
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
                                        Pair(dayName, dateStr)
                                    }
                                }

                                weeklyLabels.forEach { (dayName, dateStr) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayName,
                                            fontSize = 9.sp,
                                            color = SlateGrey,
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
                            } else {
                                val xLabels = when (timeRangeSelection) {
                                    0 -> listOf("12 Jam Lalu", "9 Jam Lalu", "6 Jam Lalu", "3 Jam Lalu", "Sekarang")
                                    else -> listOf("Jan", "Mar", "Mei", "Jul", "Sep", "Nov", "Des")
                                }
                                xLabels.forEach { label ->
                                    Text(
                                        text = label,
                                        fontSize = 8.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Chart Legend / Info
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            when (trendTabSelection) {
                                0 -> {
                                    Text("Garis: DE (Hijau/Tebal), NDE (Abu-Abu/Tipis)", fontSize = 8.sp, color = Color.DarkGray)
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




                    } else {
                        Text("Mempersiapkan tren sensor...", fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    }
                }
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
                                        text = "Operator: ${log.operatorName.ifEmpty { "Operator Centrifuge" }}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Vibrasi (mm/s):\n• DE: ${log.driveEndVibration}  • NDE: ${log.nonDriveEndVibration}\n• Motor: ${log.motorBearingVibration}  • G-box: ${log.gearboxBearingVibration}  • Bowl: ${log.bowlVibration}",
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
                                        Text(
                                            text = "Greasing: ${if (log.isGreased) "Ya" else "Tidak"}",
                                            fontSize = 10.sp,
                                            color = if (log.isGreased) BrandGreen else BrandRed,
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
                                            color = Color.DarkGray,
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
            var operatorName by remember { mutableStateOf("Wahyu") }
            var shiftSelection by remember { mutableStateOf("Pagi") }
            var deVib by remember { mutableStateOf("") }
            var ndeVib by remember { mutableStateOf("") }
            var motorVib by remember { mutableStateOf("") }
            var gearboxVib by remember { mutableStateOf("") }
            var bowlVib by remember { mutableStateOf("") }
            var bTemp by remember { mutableStateOf("") }
            var mTemp by remember { mutableStateOf("") }
            var isGreasedSelection by remember { mutableStateOf(false) }
            var soundSelection by remember { mutableStateOf("Normal") }
            var hasLeakageSelection by remember { mutableStateOf(false) }
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

                    // Nama Operator  :       |   Dropdown   |
                    var operatorDropdownExpanded by remember { mutableStateOf(false) }
                    val operatorList = listOf("Wahyu", "Abdul Aziz")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Nama Operator  :",
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
                                    .testTag("operator_name_dropdown"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = operatorName.ifEmpty { "Pilih Operator" },
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
                                operatorList.forEach { name ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                name,
                                                fontSize = 13.sp,
                                                fontWeight = if (operatorName == name) FontWeight.Bold else FontWeight.Normal,
                                                color = Color.Black
                                            )
                                        },
                                        onClick = {
                                            operatorName = name
                                            operatorDropdownExpanded = false
                                        },
                                        leadingIcon = if (operatorName == name) {
                                            { Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        modifier = Modifier.background(Color.White)
                                    )
                                }
                            }
                        }
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
                        // Greasing : |Ya| |Tidak|
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Greasing :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey,
                                modifier = Modifier.width(95.dp)
                            )
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Ya (Warna Hijau)
                                val yaSelected = isGreasedSelection
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isGreasedSelection = true }
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
                                val tidakSelected = !isGreasedSelection
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isGreasedSelection = false }
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
                        val vibrationFields = listOf(
                            Triple("1. Drive End :", deVib, "drive_end_input") to { v: String -> deVib = v },
                            Triple("2. Non Drive End :", ndeVib, "non_drive_end_input") to { v: String -> ndeVib = v },
                            Triple("3. Bearing Motor :", motorVib, "motor_vibration_input") to { v: String -> motorVib = v },
                            Triple("4. Bearing Gearbox :", gearboxVib, "gearbox_vibration_input") to { v: String -> gearboxVib = v },
                            Triple("5. Bowl :", bowlVib, "bowl_vibration_input") to { v: String -> bowlVib = v }
                        )

                        vibrationFields.forEach { (fieldInfo, onValueChange) ->
                            val (labelText, currentVal, testTagStr) = fieldInfo
                            val fVal = currentVal.toFloatOrNull()
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
                                color = SlateGrey
                            )
                            OutlinedTextField(
                                value = comments,
                                onValueChange = { comments = it },
                                placeholder = { Text("Tulis catatan hasil observasi...", fontSize = 12.sp, color = Color.Gray) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = BrandGreen,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                ),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
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
                                val bowlVibValue = bowlVib.toFloatOrNull() ?: 1.5f
                                val bTempValue = bTemp.toFloatOrNull() ?: 55f
                                val mTempValue = mTemp.toFloatOrNull() ?: 48f
                                val alarm = if (deValue >= 8.8f || bTempValue >= 70f) "Critical" else if (deValue >= 4.5f) "Warning" else "Normal"

                                val finalComments = if (comments.trim().isEmpty()) {
                                    "Log manual [$selectedUnitForLog]"
                                } else {
                                    "$comments [$selectedUnitForLog]"
                                }

                                viewModel.addVibrationLog(
                                    VibrationLog(
                                        operatorName = operatorName.ifEmpty { "Operator Centrifuge" },
                                        shift = shiftSelection,
                                        driveEndVibration = deValue,
                                        nonDriveEndVibration = ndeValue,
                                        motorBearingVibration = motorVibValue,
                                        gearboxBearingVibration = gearboxVibValue,
                                        bowlVibration = bowlVibValue,
                                        bearingTemp = bTempValue,
                                        motorTemp = mTempValue,
                                        isGreased = isGreasedSelection,
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
    var selectedPeriod by remember { mutableStateOf("Harian") } // "Harian", "Mingguan", "Bulanan"
    var selectedOperatorFilter by remember { mutableStateOf("Semua") } // "Semua", "Wahyu", "Abdul Aziz"
    var expandedLogId by remember { mutableStateOf<Long?>(null) }

    val now = System.currentTimeMillis()
    val startOfToday = remember(now) {
        WibDateUtils.getStartOfDay(now)
    }

    // Filter berdasarkan Periode (Harian, Mingguan, Bulanan)
    val periodFilteredChecks = remember(ciltHistory, selectedPeriod, now, startOfToday) {
        when (selectedPeriod) {
            "Harian" -> {
                val daily = ciltHistory.filter { it.timestamp >= startOfToday || it.timestamp >= (now - 24 * 3600 * 1000L) }
                if (daily.isNotEmpty()) daily else ciltHistory.take(2)
            }
            "Mingguan" -> {
                val weekThreshold = now - 7L * 24 * 3600 * 1000L
                val weekly = ciltHistory.filter { it.timestamp >= weekThreshold }
                if (weekly.isNotEmpty()) weekly else ciltHistory.take(6)
            }
            else -> { // "Bulanan"
                val monthThreshold = now - 30L * 24 * 3600 * 1000L
                val monthly = ciltHistory.filter { it.timestamp >= monthThreshold }
                if (monthly.isNotEmpty()) monthly else ciltHistory
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
    val periodDateRangeText = remember(selectedPeriod, now) {
        when (selectedPeriod) {
            "Harian" -> "Hari Ini • ${WibDateUtils.format("dd MMM yyyy", now)}"
            "Mingguan" -> {
                val weekStart = WibDateUtils.format("dd MMM", now - 7L * 24 * 3600 * 1000L)
                "7 Hari Terakhir • $weekStart - ${WibDateUtils.format("dd MMM yyyy", now)}"
            }
            else -> {
                val monthStart = WibDateUtils.format("dd MMM", now - 30L * 24 * 3600 * 1000L)
                "30 Hari Terakhir • $monthStart - ${WibDateUtils.format("dd MMM yyyy", now)}"
            }
        }
    }

    // Metrik Kalkulasi
    val totalChecks = finalFilteredChecks.size
    fun countCheckCompleted(c: CiltCheck): Int {
        var count = 0
        if (c.nozzleCleaned) count++
        if (c.bowlCleaned) count++
        if (c.areaCleaned) count++
        if (c.nozzleChecked) count++
        if (c.vibrationChecked) count++
        if (c.leakChecked) count++
        if (c.instrumentChecked) count++
        if (c.bearingGreased) count++
        if (c.couplingGreased) count++
        if (c.oilLevelChecked) count++
        if (c.nozzleBoltsTightened) count++
        if (c.fittingPipesTightened) count++
        if (c.beltTensionChecked) count++
        return count
    }

    // 1. Cleaning
    val nozzleCleanedCount = finalFilteredChecks.count { it.nozzleCleaned }
    val bowlCleanedCount = finalFilteredChecks.count { it.bowlCleaned }
    val areaCleanedCount = finalFilteredChecks.count { it.areaCleaned }
    val totalCleaningDone = nozzleCleanedCount + bowlCleanedCount + areaCleanedCount
    val cleaningRate = if (totalChecks > 0) (totalCleaningDone * 100) / (totalChecks * 3) else 0

    // 2. Inspection
    val nozzleCheckedCount = finalFilteredChecks.count { it.nozzleChecked }
    val vibrationCheckedCount = finalFilteredChecks.count { it.vibrationChecked }
    val leakCheckedCount = finalFilteredChecks.count { it.leakChecked }
    val instrumentCheckedCount = finalFilteredChecks.count { it.instrumentChecked }
    val totalInspectionDone = nozzleCheckedCount + vibrationCheckedCount + leakCheckedCount + instrumentCheckedCount
    val inspectionRate = if (totalChecks > 0) (totalInspectionDone * 100) / (totalChecks * 4) else 0

    // 3. Lubrication
    val bearingGreasedCount = finalFilteredChecks.count { it.bearingGreased }
    val couplingGreasedCount = finalFilteredChecks.count { it.couplingGreased }
    val oilLevelCheckedCount = finalFilteredChecks.count { it.oilLevelChecked }
    val totalLubricationDone = bearingGreasedCount + couplingGreasedCount + oilLevelCheckedCount
    val lubricationRate = if (totalChecks > 0) (totalLubricationDone * 100) / (totalChecks * 3) else 0

    // 4. Tightening
    val nozzleBoltsTightenedCount = finalFilteredChecks.count { it.nozzleBoltsTightened }
    val fittingPipesTightenedCount = finalFilteredChecks.count { it.fittingPipesTightened }
    val beltTensionCheckedCount = finalFilteredChecks.count { it.beltTensionChecked }
    val totalTighteningDone = nozzleBoltsTightenedCount + fittingPipesTightenedCount + beltTensionCheckedCount
    val tighteningRate = if (totalChecks > 0) (totalTighteningDone * 100) / (totalChecks * 3) else 0

    // Overall Compliance
    val totalItemsDone = totalCleaningDone + totalInspectionDone + totalLubricationDone + totalTighteningDone
    val totalPossibleItems = totalChecks * 13
    val overallComplianceRate = if (totalPossibleItems > 0) (totalItemsDone * 100) / totalPossibleItems else 0
    val perfectChecksCount = finalFilteredChecks.count { countCheckCompleted(it) == 13 }

    // Operator Contribution
    val wahyuChecks = finalFilteredChecks.filter { it.operatorName.contains("Wahyu", ignoreCase = true) }
    val wahyuCompliance = if (wahyuChecks.isNotEmpty()) (wahyuChecks.sumOf { countCheckCompleted(it) } * 100) / (wahyuChecks.size * 13) else 0

    val abdulAzizChecks = finalFilteredChecks.filter { it.operatorName.contains("Abdul", ignoreCase = true) }
    val abdulAzizCompliance = if (abdulAzizChecks.isNotEmpty()) (abdulAzizChecks.sumOf { countCheckCompleted(it) } * 100) / (abdulAzizChecks.size * 13) else 0

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

                    // 3 Tombol Periode Segmented: | Harian | | Mingguan | | Bulanan |
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
                            listOf("Harian", "Mingguan", "Bulanan").forEach { period ->
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
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SlateGrey
                                    )
                                }
                            }
                        }
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

        // B. RINGKASAN EKSEKUTIF / KPI CARDS (2x2 Grid)
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
                        Column {
                            Text(
                                text = "Ringkasan Eksekutif CILT",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Text(
                                text = "Laporan $selectedPeriod ($periodDateRangeText)",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (overallComplianceRate >= 90) Color(0xFFDCFCE7) else Color(0xFFFEF3C7))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "$overallComplianceRate% Kepatuhan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (overallComplianceRate >= 90) Color(0xFF166534) else Color(0xFF92400E)
                            )
                        }
                    }

                    // 4 KPI Mini Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // KPI 1: Total Checklist
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Checklist", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$totalChecks Kali", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                Text("Pemeriksaan", fontSize = 9.sp, color = SlateGrey)
                            }
                        }

                        // KPI 2: Kepatuhan SOP
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Kepatuhan SOP", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$overallComplianceRate%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                Text("13 Item CILT", fontSize = 9.sp, color = SlateGrey)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // KPI 3: 100% Selesai Sempurna
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Sempurna (100%)", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$perfectChecksCount / $totalChecks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                Text("Seluruh item OK", fontSize = 9.sp, color = SlateGrey)
                            }
                        }

                        // KPI 4: Temuan Lapangan
                        val notesCount = finalFilteredChecks.count { it.comments.replace("[Pagi]", "").replace("[Malam]", "").trim().isNotBlank() }
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Catatan / Temuan", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$notesCount Catatan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandOrange)
                                Text("Tercatat di lapangan", fontSize = 9.sp, color = SlateGrey)
                            }
                        }
                    }
                }
            }
        }

        // C. ANALISIS 4 PILAR CILT (Cleaning, Inspection, Lubrication, Tightening)
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
                        text = "Pencapaian 4 Pilar CILT ($selectedPeriod)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )

                    // Pilar 1: Cleaning
                    PillarReportItem(
                        letter = "C",
                        title = "Cleaning (Pembersihan)",
                        rate = cleaningRate,
                        items = listOf(
                            "Pembersihan Nozzle & Holder" to "$nozzleCleanedCount / $totalChecks",
                            "Pembersihan Bowl & Endapan" to "$bowlCleanedCount / $totalChecks",
                            "Pembersihan Area Luar Mesin" to "$areaCleanedCount / $totalChecks"
                        )
                    )

                    Divider(color = Color(0xFFF1F5F9))

                    // Pilar 2: Inspection
                    PillarReportItem(
                        letter = "I",
                        title = "Inspection (Inspeksi)",
                        rate = inspectionRate,
                        items = listOf(
                            "Inspeksi Keausan Nozzle" to "$nozzleCheckedCount / $totalChecks",
                            "Inspeksi Getaran & Suara Mesin" to "$vibrationCheckedCount / $totalChecks",
                            "Inspeksi Kebocoran Seal & Fitting" to "$leakCheckedCount / $totalChecks",
                            "Inspeksi Pressure Gauge & Instrumen" to "$instrumentCheckedCount / $totalChecks"
                        )
                    )

                    Divider(color = Color(0xFFF1F5F9))

                    // Pilar 3: Lubrication
                    PillarReportItem(
                        letter = "L",
                        title = "Lubrication (Pelumasan)",
                        rate = lubricationRate,
                        items = listOf(
                            "Greasing Bearing Buffer Penuh" to "$bearingGreasedCount / $totalChecks",
                            "Greasing Coupling Transfluid" to "$couplingGreasedCount / $totalChecks",
                            "Pengecekan Level Oli Coupling" to "$oilLevelCheckedCount / $totalChecks"
                        )
                    )

                    Divider(color = Color(0xFFF1F5F9))

                    // Pilar 4: Tightening
                    PillarReportItem(
                        letter = "T",
                        title = "Tightening (Pengencangan)",
                        rate = tighteningRate,
                        items = listOf(
                            "Baut Pengunci Nozzle Holder" to "$nozzleBoltsTightenedCount / $totalChecks",
                            "Fitting Pipa & Valve Sambungan" to "$fittingPipesTightenedCount / $totalChecks",
                            "Ketegangan V-Belt (>50 N)" to "$beltTensionCheckedCount / $totalChecks"
                        )
                    )
                }
            }
        }

        // D. KINERJA OPERATOR (Wahyu vs Abdul Aziz)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Kinerja Operator Pelaksana",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Wahyu Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                                    Text("Wahyu", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
                                }
                                Text("${wahyuChecks.size} Laporan", fontSize = 12.sp, color = Color.Gray)
                                LinearProgressIndicator(
                                    progress = { (wahyuCompliance / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = BrandGreen,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                                Text("$wahyuCompliance% Kepatuhan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandGreen)
                            }
                        }

                        // Abdul Aziz Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BrandGreen, modifier = Modifier.size(16.dp))
                                    Text("Abdul Aziz", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
                                }
                                Text("${abdulAzizChecks.size} Laporan", fontSize = 12.sp, color = Color.Gray)
                                LinearProgressIndicator(
                                    progress = { (abdulAzizCompliance / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = BrandGreen,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                                Text("$abdulAzizCompliance% Kepatuhan", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandGreen)
                            }
                        }
                    }
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
                val isPerfect = completedCount == 13
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
                                    text = if (isPerfect) "13/13 (100% OK)" else "$completedCount/13 Item",
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
                            val cOk = check.nozzleCleaned && check.bowlCleaned && check.areaCleaned
                            val iOk = check.nozzleChecked && check.vibrationChecked && check.leakChecked && check.instrumentChecked
                            val lOk = check.bearingGreased && check.couplingGreased && check.oilLevelChecked
                            val tOk = check.nozzleBoltsTightened && check.fittingPipesTightened && check.beltTensionChecked

                            PillarStatusChip("C", cOk, Modifier.weight(1f))
                            PillarStatusChip("I", iOk, Modifier.weight(1f))
                            PillarStatusChip("L", lOk, Modifier.weight(1f))
                            PillarStatusChip("T", tOk, Modifier.weight(1f))
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
                                    color = SlateGrey,
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
                                    text = if (isExpanded) "Sembunyikan Rincian" else "Lihat 13 Rincian Item",
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

                        // Accordion Rincian 13 Item
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
                                    CheckDetailRow("Bersihkan nozzle & holder", check.nozzleCleaned)
                                    CheckDetailRow("Bersihkan cover bowl & kerak", check.bowlCleaned)
                                    CheckDetailRow("Bersihkan area luar & tumpahan", check.areaCleaned)
                                    CheckDetailRow("Inspeksi keausan & ukuran nozzle", check.nozzleChecked)
                                    CheckDetailRow("Inspeksi getaran & suara abnormal", check.vibrationChecked)
                                    CheckDetailRow("Inspeksi kebocoran seal & packing", check.leakChecked)
                                    CheckDetailRow("Inspeksi pressure & suhu instrumen", check.instrumentChecked)
                                    CheckDetailRow("Greasing bearing buffer penuh", check.bearingGreased)
                                    CheckDetailRow("Greasing coupling transfluid", check.couplingGreased)
                                    CheckDetailRow("Cek level & kondisi oli", check.oilLevelChecked)
                                    CheckDetailRow("Kencangkan baut nozzle holder", check.nozzleBoltsTightened)
                                    CheckDetailRow("Kencangkan fitting pipa & valve", check.fittingPipesTightened)
                                    CheckDetailRow("Pengecekan ketegangan V-belt", check.beltTensionChecked)
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
    var operatorName by remember { mutableStateOf("Wahyu") }
    var shiftSelection by remember { mutableStateOf("Pagi") }
    var selectedUnit by remember { mutableStateOf("SC-01") }
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

    // Real-time calculation
    val completedCount = listOf(
        nozzleCleaned, bowlCleaned, areaCleaned,
        nozzleChecked, vibrationChecked, leakChecked, instrumentChecked,
        bearingGreased, couplingGreased, oilLevelChecked,
        nozzleBoltsTightened, fittingPipesTightened, beltTensionChecked
    ).count { it }
    val totalItems = 13
    val completionPercent = (completedCount * 100) / totalItems
    val allCompleted = completedCount == totalItems

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // A. HERO PROGRESS & QUICK ACTION CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (allCompleted) Color(0xFF86EFAC) else Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FactCheck,
                                    contentDescription = null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Formulir CILT Centrifuge",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGrey
                                )
                            }
                            Text(
                                text = "Standar Perawatan Mandiri Harian • Mill 6321",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (allCompleted) Color(0xFFDCFCE7) else if (completedCount > 0) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (allCompleted) Color(0xFF86EFAC) else Color(0xFFCBD5E1))
                        ) {
                            Text(
                                text = if (allCompleted) "✓ 100% Lengkap" else "$completedCount/$totalItems Selesai",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (allCompleted) Color(0xFF166534) else if (completedCount > 0) Color(0xFF92400E) else SlateGrey,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Progress Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Kemajuan Checklist",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$completionPercent%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (allCompleted) Color(0xFF166534) else BrandGreen
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (completedCount.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (allCompleted) Color(0xFF16A34A) else BrandGreen,
                            trackColor = Color(0xFFF1F5F9)
                        )
                    }

                    // Quick Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                nozzleCleaned = true
                                bowlCleaned = true
                                areaCleaned = true
                                nozzleChecked = true
                                vibrationChecked = true
                                leakChecked = true
                                instrumentChecked = true
                                bearingGreased = true
                                couplingGreased = true
                                oilLevelChecked = true
                                nozzleBoltsTightened = true
                                fittingPipesTightened = true
                                beltTensionChecked = true
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandGreen),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BrandGreen),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Centang Semua", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B)),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // B. OPERATOR, SHIFT, & UNIT MESIN SELECTION CARD
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
                        text = "Informasi Pelaksana & Unit Mesin",
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

                    // 3. Pilih Unit Centrifuge (SC-01 s/d SC-08)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Unit Centrifuge :",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val units = listOf("Semua SC", "SC-01", "SC-02", "SC-03", "SC-04", "SC-05", "SC-06", "SC-07", "SC-08")
                            items(units) { u ->
                                val isUnitSelected = selectedUnit == u
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedUnit = u },
                                    color = if (isUnitSelected) BrandGreen else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isUnitSelected) BrandGreen else Color(0xFFCBD5E1)
                                    )
                                ) {
                                    Text(
                                        text = u,
                                        fontSize = 11.sp,
                                        fontWeight = if (isUnitSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isUnitSelected) Color.White else SlateGrey,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
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
                    ChecklistRowItem("Bersihkan nozzle & nozzle holder (bebas dari sumbatan pasir/serat)", nozzleCleaned) { nozzleCleaned = it },
                    ChecklistRowItem("Bersihkan cover bowl & bagian dalam bowl dari endapan keras", bowlCleaned) { bowlCleaned = it },
                    ChecklistRowItem("Bersihkan area luar mesin dari tumpahan minyak/sludge", areaCleaned) { areaCleaned = it }
                )
            )
        }

        // D. 2. INSPECTION CARD (Amber Orange)
        item {
            ChecklistCategoryCard(
                title = "2. INSPECTION (Inspeksi)",
                icon = Icons.Default.Search,
                accentColor = Color(0xFFEA580C),
                items = listOf(
                    ChecklistRowItem("Periksa keausan nozzle & diameter male nut", nozzleChecked) { nozzleChecked = it },
                    ChecklistRowItem("Periksa getaran & suara tidak normal saat mesin beroperasi", vibrationChecked) { vibrationChecked = it },
                    ChecklistRowItem("Periksa kebocoran seal, packing, sambungan fitting pipa & valve", leakChecked) { leakChecked = it },
                    ChecklistRowItem("Periksa pressure gauge, suhu, & level oli gearbox", instrumentChecked) { instrumentChecked = it }
                )
            )
        }

        // E. 3. LUBRICATION CARD (Deep Purple)
        item {
            ChecklistCategoryCard(
                title = "3. LUBRICATION (Pelumasan)",
                icon = Icons.Default.Opacity,
                accentColor = Color(0xFF7C3AED),
                items = listOf(
                    ChecklistRowItem("Pemberian grease pada bearing buffer sampai penuh (Slide 23)", bearingGreased) { bearingGreased = it },
                    ChecklistRowItem("Lakukan greasing coupling transfluid sesuai jadwal", couplingGreased) { couplingGreased = it },
                    ChecklistRowItem("Cek level oli transfluid fluid-coupling, tambah jika kurang", oilLevelChecked) { oilLevelChecked = it }
                )
            )
        }

        // F. 4. TIGHTENING CARD (Emerald Green)
        item {
            ChecklistCategoryCard(
                title = "4. TIGHTENING (Pengencangan)",
                icon = Icons.Default.Build,
                accentColor = Color(0xFF059669),
                items = listOf(
                    ChecklistRowItem("Kencangkan baut pengunci nozzle holder dengan torsi tepat", nozzleBoltsTightened) { nozzleBoltsTightened = it },
                    ChecklistRowItem("Pastikan fitting pipa & sambungan valve kencang tidak longgar", fittingPipesTightened) { fittingPipesTightened = it },
                    ChecklistRowItem("Periksa ketegangan & kelayakan V-belt (Tension > 50 N)", beltTensionChecked) { beltTensionChecked = it }
                )
            )
        }

        // G. CATATAN / TEMUAN KHUSUS & TOMBOL SIMPAN
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
                        text = "Catatan / Temuan Khusus di Lapangan",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateGrey
                    )

                    OutlinedTextField(
                        value = comments,
                        onValueChange = { comments = it },
                        placeholder = { Text("Tuliskan jika ada temuan abnormalitas, kebocoran, baut dol, vibrasi, dsb...", fontSize = 12.sp, color = Color.Gray) },
                        leadingIcon = {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = BrandGreen)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (operatorName.isBlank()) {
                                operatorName = "Operator Berau"
                            }
                            val unitTag = if (selectedUnit.isNotBlank()) "[$selectedUnit]" else ""
                            val shiftTag = if (shiftSelection.isNotBlank()) "[$shiftSelection]" else ""
                            val formattedComments = "$unitTag $shiftTag $comments".trim()

                            onSave(
                                CiltCheck(
                                    timestamp = System.currentTimeMillis(),
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
                                    comments = formattedComments
                                )
                            )
                            // Reset form fields
                            operatorName = "Wahyu"
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
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_cilt_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simpan & Kirim Laporan CILT ($completedCount/13)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // H. HISTORI PEMERIKSAAN CILT
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
                    text = "${history.size} Riwayat",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (history.isEmpty()) {
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
            items(history) { check ->
                val checkCompleted = listOf(
                    check.nozzleCleaned, check.bowlCleaned, check.areaCleaned,
                    check.nozzleChecked, check.vibrationChecked, check.leakChecked, check.instrumentChecked,
                    check.bearingGreased, check.couplingGreased, check.oilLevelChecked,
                    check.nozzleBoltsTightened, check.fittingPipesTightened, check.beltTensionChecked
                ).count { it }
                val isPerfect = checkCompleted == 13

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
                                    text = if (isPerfect) "13/13 (100% OK)" else "$checkCompleted/13 Selesai",
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
                                    color = SlateGrey,
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
    accentColor: Color = Color(0xFF059669)
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
    val totalSeverity: Int,
    val totalOccurrence: Int,
    val totalDetection: Int,
    val totalRpn: Int,
    val count: Int
)

@Composable
fun AbnormalityScreen(
    reports: List<AbnormalityReport>,
    onAddReport: (AbnormalityReport) -> Unit,
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
    var tagType by remember { mutableStateOf("None") } // Default to None

    // State untuk sub-menu "Report"
    var selectedPeriod by remember { mutableStateOf("Harian") } // "Harian", "Mingguan", "Bulanan", "Semua"
    var selectedReportMachine by remember { mutableStateOf("Semua Mesin") }
    var reportMachineDropdownExpanded by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val filteredReports = remember(reports, selectedPeriod, selectedReportMachine) {
        reports.filter { r ->
            val inPeriod = when (selectedPeriod) {
                "Harian" -> {
                    val cal = WibDateUtils.getCalendar()
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    r.timestamp >= cal.timeInMillis || (now - r.timestamp) <= 24L * 3600 * 1000
                }
                "Mingguan" -> {
                    r.timestamp >= (now - 7L * 24 * 3600 * 1000)
                }
                "Bulanan" -> {
                    r.timestamp >= (now - 30L * 24 * 3600 * 1000)
                }
                else -> true
            }
            val inMachine = if (selectedReportMachine == "Semua Mesin") {
                true
            } else {
                r.title.contains(selectedReportMachine, ignoreCase = true) || r.description.contains(selectedReportMachine, ignoreCase = true)
            }
            inPeriod && inMachine
        }
    }

    val context = LocalContext.current

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
                            // 1. FREEZE PANE (Kolom No & Komponen tetap berada di posisi kiri saat di-scroll)
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
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
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("No", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.width(4.dp))
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
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.no.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateGrey,
                                            modifier = Modifier.width(28.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        if (item.isCustomInput) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(34.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.White)
                                                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                if (item.component.isEmpty()) {
                                                    Text(
                                                        text = "Ketik komponen...",
                                                        fontSize = 11.sp,
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
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.Black,
                                                modifier = Modifier.weight(1f)
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
                                        Text("Level Severity (keparahan)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(135.dp), textAlign = TextAlign.Center)
                                        Text("Occurence (Jumlah kejadian)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(145.dp), textAlign = TextAlign.Center)
                                        Text("Detection (Level Deteksi)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(135.dp), textAlign = TextAlign.Center)
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
                                                ScoreDropdown(
                                                    value = item.severity,
                                                    onValueChange = { item.severity = it }
                                                )
                                            }
                                            Box(modifier = Modifier.width(145.dp), contentAlignment = Alignment.Center) {
                                                ScoreDropdown(
                                                    value = item.occurrence,
                                                    onValueChange = { item.occurrence = it }
                                                )
                                            }
                                            Box(modifier = Modifier.width(135.dp), contentAlignment = Alignment.Center) {
                                                ScoreDropdown(
                                                    value = item.detection,
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
                                        tagType = tagType
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

                        // PILIHAN PERIODE: HARIAN, MINGGUAN, BULANAN, SEMUA
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Pilihan Periode Report :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Harian", "Mingguan", "Bulanan", "Semua").forEach { period ->
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
                        val avgAccRpn = if (filteredReports.isNotEmpty()) totalAccRpn / filteredReports.size else 0
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BrandGreenLight),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Total Temuan", fontSize = 10.sp, color = SlateGrey)
                                    Text("${filteredReports.size} Kasus", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Akumulasi RPN", fontSize = 10.sp, color = Color(0xFF991B1B))
                                    Text("$totalAccRpn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandRed)
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Rata-rata RPN", fontSize = 10.sp, color = Color(0xFF92400E))
                                    Text("$avgAccRpn", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = BrandOrange)
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
                    ReportRowAccumulation(
                        no = no,
                        component = name,
                        failureModesSummary = modeSummary,
                        totalSeverity = matched.sumOf { it.severityScore },
                        totalOccurrence = matched.sumOf { it.occurrenceScore },
                        totalDetection = matched.sumOf { it.detectionScore },
                        totalRpn = matched.sumOf { it.rpn },
                        count = matched.size
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
                reportRows.add(
                    ReportRowAccumulation(
                        no = 8,
                        component = customCompName,
                        failureModesSummary = customModeSummary,
                        totalSeverity = nonStandardReports.sumOf { it.severityScore },
                        totalOccurrence = nonStandardReports.sumOf { it.occurrenceScore },
                        totalDetection = nonStandardReports.sumOf { it.detectionScore },
                        totalRpn = nonStandardReports.sumOf { it.rpn },
                        count = nonStandardReports.size
                    )
                )

                val sumAllSeverity = reportRows.sumOf { it.totalSeverity }
                val sumAllOccurrence = reportRows.sumOf { it.totalOccurrence }
                val sumAllDetection = reportRows.sumOf { it.totalDetection }
                val sumAllRpn = reportRows.sumOf { it.totalRpn }
                val sumAllCount = reportRows.sumOf { it.count }

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
                                text = "Akumulasi FMEA ($selectedPeriod • $selectedReportMachine)",
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
                            // 1. FREEZE PANE REPORT (Kolom No & Komponen tetap berada di posisi kiri saat di-scroll)
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
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
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("No", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                                    Spacer(modifier = Modifier.width(4.dp))
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
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = row.no.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateGrey,
                                            modifier = Modifier.width(28.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = row.component,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.Black,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
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
                                        .padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "∑",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.width(28.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "TOTAL",
                                        fontSize = 12.sp,
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
                                    // Header Kolom Scrollable (RPN dan Jml Temuan ditempatkan setelah kolom Komponen)
                                    Row(
                                        modifier = Modifier
                                            .height(44.dp)
                                            .background(Color(0xFF1E293B))
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Risk Priority Number (RPN)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(160.dp), textAlign = TextAlign.Center)
                                        Text("Jml Temuan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(100.dp), textAlign = TextAlign.Center)
                                        Text("Failure Mode", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(185.dp))
                                        Text("Level Severity (Total)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(135.dp), textAlign = TextAlign.Center)
                                        Text("Occurence (Total)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(145.dp), textAlign = TextAlign.Center)
                                        Text("Detection (Total)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.width(135.dp), textAlign = TextAlign.Center)
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
                                            Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
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

                                            // 3. Failure Mode
                                            Text(
                                                text = row.failureModesSummary,
                                                fontSize = 11.sp,
                                                color = if (row.count > 0) Color.Black else Color.Gray,
                                                fontWeight = if (row.count > 0) FontWeight.SemiBold else FontWeight.Normal,
                                                modifier = Modifier
                                                    .width(185.dp)
                                                    .padding(end = 8.dp),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // 4. Level Severity (Total)
                                            Box(modifier = Modifier.width(135.dp), contentAlignment = Alignment.Center) {
                                                Surface(
                                                    color = if (row.totalSeverity > 0) BrandGreenLight else Color(0xFFF1F5F9),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = row.totalSeverity.toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (row.totalSeverity > 0) BrandGreen else Color.Gray,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            // 5. Occurence (Total)
                                            Box(modifier = Modifier.width(145.dp), contentAlignment = Alignment.Center) {
                                                Surface(
                                                    color = if (row.totalOccurrence > 0) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = row.totalOccurrence.toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (row.totalOccurrence > 0) Color(0xFFB45309) else Color.Gray,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            // 6. Detection (Total)
                                            Box(modifier = Modifier.width(135.dp), contentAlignment = Alignment.Center) {
                                                Surface(
                                                    color = if (row.totalDetection > 0) Color(0xFFE0E7FF) else Color(0xFFF1F5F9),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = row.totalDetection.toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (row.totalDetection > 0) Color(0xFF3730A3) else Color.Gray,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
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
                                        Box(modifier = Modifier.width(100.dp), contentAlignment = Alignment.Center) {
                                            Text("${sumAllCount}x", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        // 3. Failure Mode label
                                        Text(
                                            text = "Semua Komponen",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.width(185.dp)
                                        )

                                        // 4. Total Severity
                                        Box(modifier = Modifier.width(135.dp), contentAlignment = Alignment.Center) {
                                            Text(sumAllSeverity.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        // 5. Total Occurence
                                        Box(modifier = Modifier.width(145.dp), contentAlignment = Alignment.Center) {
                                            Text(sumAllOccurrence.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }

                                        // 6. Total Detection
                                        Box(modifier = Modifier.width(135.dp), contentAlignment = Alignment.Center) {
                                            Text(sumAllDetection.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. DETAIL RINCIAN RIWAYAT LAPORAN YANG MEMBENTUK AKUMULASI
            item {
                Text(
                    text = "Rincian Data Laporan ($selectedPeriod • $selectedReportMachine) :",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey
                )
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
                                text = "Belum ada temuan abnormality untuk periode $selectedPeriod ($selectedReportMachine).",
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
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(r.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SlateGrey)
                                    Text(
                                        WibDateUtils.format("dd MMM yyyy, HH:mm", r.timestamp) + " WIB",
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
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(r.description, fontSize = 11.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(6.dp))
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
                                IconButton(onClick = { onDeleteReport(r) }, modifier = Modifier.size(24.dp)) {
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
    var expandedMasterHour by remember { mutableStateOf(false) }
    var expandedOperatorDropdown by remember { mutableStateOf(false) }

    // Dropdown hours list: 00:00 to 24:00 (25 items)
    val hoursList = remember { (0..24).map { String.format("%02d:00", it) } }

    // Report Filter States
    var filterShift by remember { mutableStateOf("Semua") }
    var filterUnit by remember { mutableStateOf("Semua") }
    var logToDelete by remember { mutableStateOf<FlushingLog?>(null) }

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

                                // Waktu Pelaksanaan  :    |       | (Requested: background putih, tulisan jam hitam dan judul Waktu Pelaksanaan  :    |       |)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.5.dp, Color.Black),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 10.dp)
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
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Waktu Pelaksanaan  :",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }

                                        Box {
                                            Surface(
                                                onClick = { expandedMasterHour = true },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.5.dp, Color.Black),
                                                color = Color.White,
                                                modifier = Modifier.testTag("flushing_time_selector")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "|   $masterHour   |",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.Black
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.ArrowDropDown,
                                                        contentDescription = "Pilih Jam",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }

                                            DropdownMenu(
                                                expanded = expandedMasterHour,
                                                onDismissRequest = { expandedMasterHour = false },
                                                modifier = Modifier
                                                    .heightIn(max = 240.dp)
                                                    .background(Color.White)
                                            ) {
                                                hoursList.forEach { hr ->
                                                    DropdownMenuItem(
                                                        modifier = Modifier.background(Color.White),
                                                        text = {
                                                            Text(
                                                                text = "|   $hr   |",
                                                                fontSize = 13.sp,
                                                                fontWeight = if (hr == masterHour) FontWeight.Bold else FontWeight.Normal,
                                                                color = Color.Black
                                                            )
                                                        },
                                                        onClick = {
                                                            masterHour = hr
                                                            tasksState = tasksState.map { it.copy(time = hr) }
                                                            expandedMasterHour = false
                                                        }
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
                                    label = { Text("Catatan / Evaluasi Flushing") },
                                    placeholder = { Text("Contoh: Air panas mencapai 93°C, getaran bowl halus, tidak ada kotoran tersisa.") },
                                    minLines = 2,
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
                val filteredLogs = flushingLogs.filter { log ->
                    (filterShift == "Semua" || log.shift == filterShift) &&
                    (filterUnit == "Semua" || log.unitName == filterUnit)
                }

                val shiftPagiCount = flushingLogs.count { it.shift == "Shift Pagi" }
                val shiftMalamCount = flushingLogs.count { it.shift == "Shift Malam" }
                val avgCompliance = if (flushingLogs.isNotEmpty()) {
                    (flushingLogs.sumOf { it.completedCount }.toDouble() / (flushingLogs.size * 11) * 100).toInt()
                } else 100

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // KPI Highlights
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SlateGrey),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Rekapitulasi Laporan Flushing Mill",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    FlushingStatBox(label = "Total Laporan", value = "${flushingLogs.size}", color = Color.White)
                                    FlushingStatBox(label = "Shift Pagi", value = "$shiftPagiCount", color = Color(0xFFFFD54F))
                                    FlushingStatBox(label = "Shift Malam", value = "$shiftMalamCount", color = Color(0xFFB39DDB))
                                    FlushingStatBox(label = "Kepatuhan SOP", value = "$avgCompliance%", color = Color(0xFF81C784))
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
                Text(
                    text = "Catatan: ${log.notes}",
                    fontSize = 11.sp,
                    color = Color.DarkGray,
                    lineHeight = 15.sp
                )
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

