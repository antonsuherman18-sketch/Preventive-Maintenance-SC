package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.util.WibDateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JakartaScreen(
    isOnline: Boolean,
    ciltChecks: List<CiltCheck>,
    reliabilityChecks: List<ReliabilityPmCheck>,
    reports: List<AbnormalityReport>,
    mentorLogs: List<MentorPairingLog>,
    vibrationLogs: List<VibrationLog>,
    flushingLogs: List<FlushingLog> = emptyList(),
    viewModel: CentrifugeViewModel
) {
    // Password Authentication State for Report Center (Default PIN/Password: 6321)
    var isUnlocked by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    if (!isUnlocked) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(BrandGreen.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Report Center Terkunci",
                            tint = BrandGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Akses Report Center",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Laporan ini khusus manajemen & evaluasi mill. Masukkan password / PIN otorisasi untuk membuka.",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = false
                        },
                        label = { Text("Password / PIN", color = Color(0xFF333333)) },
                        placeholder = { Text("Masukkan Password / PIN", color = Color(0xFF666666)) },
                        singleLine = true,
                        isError = passwordError,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp
                        ),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Sembunyikan password" else "Lihat password",
                                    tint = Color.Black
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = BrandGreen,
                            focusedLabelColor = BrandGreen,
                            unfocusedLabelColor = Color(0xFF333333),
                            cursorColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (passwordError) {
                        Text(
                            text = "Password salah! Silakan periksa kembali.",
                            color = Color(0xFFD32F2F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = {
                            // Support standard Mill PIN "6321" or "admin6321" or "mill6321"
                            if (passwordInput.trim() == "6321" || passwordInput.trim().equals("mill6321", ignoreCase = true) || passwordInput.trim().equals("admin6321", ignoreCase = true)) {
                                isUnlocked = true
                                passwordError = false
                                passwordInput = ""
                            } else {
                                passwordError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buka Laporan",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        return
    }

    val context = LocalContext.current
    var selectedFilterTab by remember { mutableStateOf("Semua") }

    // State Dialog Tarik Data Excel dengan Rentang Tanggal
    var showExcelExportDialog by remember { mutableStateOf(false) }
    var exportReportType by remember { mutableStateOf("all") }

    val totalUnsynced = ciltChecks.count { !it.isSynced } +
            reliabilityChecks.count { !it.isSynced } +
            reports.count { !it.isSynced } +
            mentorLogs.count { !it.isSynced } +
            vibrationLogs.count { !it.isSynced } +
            flushingLogs.count { !it.isSynced }

    // Time helper for Report Center operational cycle (from 07:00 WIB to current check time)
    val now = WibDateUtils.getCalendar()
    val nowMillis = now.timeInMillis
    val currentHour = now.get(Calendar.HOUR_OF_DAY)
    val currentHourMinute = WibDateUtils.format("HH:mm", Date()) + " WIB"

    // Operational cycle starts at 07:00 WIB:
    // If currently >= 07:00 WIB, cycle started today at 07:00 WIB.
    // If currently < 07:00 WIB, cycle started yesterday at 07:00 WIB and renews today at 07:00 WIB.
    val cycleStartCal = WibDateUtils.getCalendar().apply {
        if (currentHour < 7) {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        set(Calendar.HOUR_OF_DAY, 7)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val cycleStartTime = cycleStartCal.timeInMillis
    val checkTimeMillis = nowMillis

    // Time window display string, e.g. "07:00 s/d 16:54 WIB"
    val checkRangeString = "07:00 s/d $currentHourMinute"
    val nextRenewalText = if (currentHour < 7) "hari ini pukul 07:00 WIB" else "besok pukul 07:00 WIB"

    // Unit list SC-01 to SC-08
    val units = listOf("SC-01", "SC-02", "SC-03", "SC-04", "SC-05", "SC-06", "SC-07", "SC-08")

    // 1. MONITORING & TREND ANALYSIS CALCULATIONS
    // Filter vibration logs within the active cycle window: from 07:00 up to current check time
    val cycleVibLogs = vibrationLogs.filter { it.timestamp in cycleStartTime..checkTimeMillis }

    // Check which SC units have input from 07:00 to current check time
    val unitsWithVibToday = units.associateWith { unitId ->
        cycleVibLogs.filter { log ->
            log.comments.contains(unitId, ignoreCase = true) ||
            log.comments.contains(unitId.replace("-", " "), ignoreCase = true) ||
            (unitId == "SC-01" && !units.any { other -> other != "SC-01" && (log.comments.contains(other) || log.comments.contains(other.replace("-", " "))) })
        }
    }

    // Units missing input from 07:00 to current check time
    val unitsMissingVibInput = units.filter { unitId ->
        unitsWithVibToday[unitId].isNullOrEmpty()
    }

    // Missing input findings from 07:00 to current check time
    val missingVibInputCount = unitsMissingVibInput.size

    // Units with critical vibration or temperature, ungreased, leak, abnormal sound
    data class VibAlert(
        val unitId: String,
        val log: VibrationLog,
        val reasons: List<String>
    )

    val vibAlerts = mutableListOf<VibAlert>()
    units.forEach { unitId ->
        val logsForUnit = unitsWithVibToday[unitId] ?: emptyList()
        logsForUnit.forEach { log ->
            val reasons = mutableListOf<String>()
            val maxVib = maxOf(log.driveEndVibration, log.nonDriveEndVibration, log.motorBearingVibration, log.gearboxBearingVibration, log.bowlVibration)
            val maxTemp = maxOf(log.bearingTemp, log.motorTemp)

            if (maxVib >= 8.8f || log.alarmState.equals("Critical", ignoreCase = true)) {
                reasons.add("Vibrasi Kritikal (${String.format(Locale.US, "%.1f", maxVib)} mm/s > 8.8 mm/s)")
            } else if (maxVib >= 4.5f || log.alarmState.equals("Warning", ignoreCase = true)) {
                reasons.add("Vibrasi Warning (${String.format(Locale.US, "%.1f", maxVib)} mm/s)")
            }

            if (maxTemp >= 75f) {
                reasons.add("Suhu Bearing/Motor Kritikal (${String.format(Locale.US, "%.1f", maxTemp)}°C > 75°C)")
            } else if (maxTemp >= 65f) {
                reasons.add("Suhu Bearing/Motor Warning (${String.format(Locale.US, "%.1f", maxTemp)}°C)")
            }

            if (!log.isGreased) {
                reasons.add("Unit belum dilakukan greasing")
            }

            if (log.hasLeakage) {
                reasons.add("Terdapat kebocoran oli / cairan")
            }

            if (log.soundState.equals("Abnormal", ignoreCase = true)) {
                reasons.add("Suara abnormal terdeteksi")
            }

            if (reasons.isNotEmpty()) {
                vibAlerts.add(VibAlert(unitId, log, reasons))
            }
        }
    }

    // 2. CILT CALCULATIONS (From 07:00 to current check time)
    val todayCiltChecks = ciltChecks.filter { it.timestamp in cycleStartTime..checkTimeMillis }
    val isCiltNotDoneToday = todayCiltChecks.isEmpty()

    data class CiltDefect(
        val check: CiltCheck,
        val findings: List<String>
    )

    val ciltFindings = mutableListOf<CiltDefect>()
    todayCiltChecks.forEach { c ->
        val issues = mutableListOf<String>()
        if (!c.nozzleCleaned) issues.add("Pembersihan nozzle & holder belum dilakukan")
        if (!c.bowlCleaned) issues.add("Pembersihan cover bowl & kerak sludge belum dilakukan")
        if (!c.areaCleaned) issues.add("Pembersihan area luar centrifuge belum dilakukan")
        if (!c.nozzleChecked) issues.add("Inspeksi keausan & ukuran nozzle belum dicek")
        if (!c.vibrationChecked) issues.add("Inspeksi getaran & suara abnormal belum dicek")
        if (!c.leakChecked) issues.add("Inspeksi kebocoran seal & packing pipa belum dicek")
        if (!c.instrumentChecked) issues.add("Inspeksi pressure & suhu instrumen belum dicek")
        if (!c.bearingGreased) issues.add("Greasing bearing buffer belum dilakukan")
        if (!c.couplingGreased) issues.add("Greasing coupling transfluid belum dilakukan")
        if (!c.oilLevelChecked) issues.add("Level & kondisi oli gearbox belum dicek")
        if (!c.nozzleBoltsTightened) issues.add("Pengencangan baut nozzle holder belum dilakukan")
        if (!c.fittingPipesTightened) issues.add("Pengencangan baut fitting pipa belum dilakukan")
        if (!c.beltTensionChecked) issues.add("Pemeriksaan tegangan belt transmisi belum dicek")
        if (c.comments.isNotBlank()) issues.add("Catatan: ${c.comments}")

        if (issues.isNotEmpty()) {
            ciltFindings.add(CiltDefect(c, issues))
        }
    }

    // 3. RELIABILITY PM CALCULATIONS (Real-time Abnormality Findings)
    val recentAbnormalityFindings = reports.sortedByDescending { it.timestamp }
    val todayAbnormalityCount = reports.count { it.timestamp in cycleStartTime..checkTimeMillis }

    // 4. FLUSHING CALCULATIONS (From 07:00 to current check time)
    val todayFlushingLogs = flushingLogs.filter { it.timestamp in cycleStartTime..checkTimeMillis }
    val isFlushingNotDoneToday = todayFlushingLogs.isEmpty()
    val incompleteFlushingLogs = todayFlushingLogs.filter { it.completedCount < it.totalCount }

    val previousCycleStart = cycleStartTime - 24 * 60 * 60 * 1000L
    val yesterdayFlushingLogs = flushingLogs.filter { it.timestamp in previousCycleStart until cycleStartTime }

    // Synchronized total findings per category
    val totalMonitoringAlerts = vibAlerts.size + missingVibInputCount
    val totalCiltAlerts = if (isCiltNotDoneToday) 1 else ciltFindings.size
    val totalPmAlerts = recentAbnormalityFindings.size
    val totalFlushingAlerts = if (isFlushingNotDoneToday) 1 else incompleteFlushingLogs.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("report_center_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP BANNER: REPORT CENTER HEADER & STATUS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateGrey),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(BrandGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = "Report Center",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Report Center",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Evaluasi Operasional Harian Mill 6321",
                                    color = Color(0xFFCFD8DC),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Relock Button
                        IconButton(
                            onClick = { isUnlocked = false },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Kunci Kembali",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Cut-Off Data Banner (07:00 s/d Saat Pengecekan)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (unitsMissingVibInput.isNotEmpty()) Color(0xFFFFB74D) else Color(0xFF81C784))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (unitsMissingVibInput.isNotEmpty()) Icons.Default.Schedule else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (unitsMissingVibInput.isNotEmpty()) Color(0xFFFFCC80) else Color(0xFFA5D6A7),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Cut-Off Data: $checkRangeString",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (unitsMissingVibInput.isNotEmpty())
                                            "Menampilkan data dari jam 07:00 sampai saat pengecekan ($currentHourMinute) • ${unitsMissingVibInput.size} mesin belum input • Diperbarui lagi $nextRenewalText"
                                        else
                                            "Semua mesin lengkap diinput (07:00 s/d $currentHourMinute) • Diperbarui lagi $nextRenewalText",
                                        fontSize = 10.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cloud Sync Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (totalUnsynced > 0) Icons.Default.CloudSync else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = if (totalUnsynced > 0) BrandYellow else Color(0xFF81C784),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (totalUnsynced > 0) "$totalUnsynced Laporan belum sinkron ke Server" else "Semua laporan ter-sinkronisasi",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                        if (totalUnsynced > 0) {
                            TextButton(
                                onClick = { viewModel.syncData() },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Sync Sekarang", color = BrandYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // QUICK STATS SUMMARY TILES
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReportSummaryTile(
                    title = "Monitoring",
                    alertCount = totalMonitoringAlerts,
                    color = if (totalMonitoringAlerts > 0) Color(0xFFE53935) else BrandGreen,
                    modifier = Modifier.weight(1f)
                )
                ReportSummaryTile(
                    title = "CILT",
                    alertCount = totalCiltAlerts,
                    color = if (totalCiltAlerts > 0) Color(0xFFFB8C00) else BrandGreen,
                    modifier = Modifier.weight(1f)
                )
                ReportSummaryTile(
                    title = "Reliability PM",
                    alertCount = totalPmAlerts,
                    color = if (totalPmAlerts > 0) Color(0xFF5E35B1) else BrandGreen,
                    modifier = Modifier.weight(1f)
                )
                ReportSummaryTile(
                    title = "Flushing",
                    alertCount = totalFlushingAlerts,
                    color = if (totalFlushingAlerts > 0) Color(0xFFD81B60) else BrandGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // TAB FILTER CHIPS
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Semua", "1. Monitoring SC", "2. CILT", "3. Reliability PM", "4. Flushing").forEach { tab ->
                    val isTabSelected = selectedFilterTab == tab
                    FilterChip(
                        selected = isTabSelected,
                        onClick = { selectedFilterTab = tab },
                        label = {
                            Text(
                                text = tab,
                                fontSize = 11.sp,
                                fontWeight = if (isTabSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isTabSelected) Color.White else Color.Black
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
        }

        // TARIK DATA EXCEL BUTTON & BANNER
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Tarik Data Excel",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Tarik Data Laporan Excel",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Tentukan data ditarik dari kapan sampai kapan",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            exportReportType = when (selectedFilterTab) {
                                "1. Monitoring SC" -> "monitoring"
                                "2. CILT" -> "cilt"
                                "3. Reliability PM" -> "reliability"
                                "4. Flushing" -> "flushing"
                                else -> "all"
                            }
                            showExcelExportDialog = true
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(1.2.dp, Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tarik Data",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        // ---------------------------------------------------------------------------------------------
        // SECTION 1: MONITORING DAN TREND ANALYSIS SC
        // ---------------------------------------------------------------------------------------------
        if (selectedFilterTab == "Semua" || selectedFilterTab == "1. Monitoring SC") {
            item {
                SectionHeader(
                    icon = Icons.Default.Analytics,
                    title = "1. Laporan Monitoring & Trend Analysis",
                    subtitle = "Status input $checkRangeString, vibrasi, temperatur, greasing, kebocoran & suara abnormal",
                    accentColor = Color(0xFF1E88E5),
                    alertCount = totalMonitoringAlerts,
                    onExportExcel = {
                        exportReportType = "monitoring"
                        showExcelExportDialog = true
                    }
                )
            }

            // Sub-item 1A: Status Belum Melakukan Input dari Jam 07:00 s/d Jam Saat Pengecekan
            val card1ANeedsAttention = unitsMissingVibInput.isNotEmpty()
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (card1ANeedsAttention) Color(0xFFFFF8E1) else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (card1ANeedsAttention) Color(0xFFFFB74D) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = if (unitsMissingVibInput.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (unitsMissingVibInput.isNotEmpty()) Color(0xFFF57C00) else Color(0xFF388E3C),
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Status Input Mesin Hari Ini ($checkRangeString)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGrey
                                )
                                Surface(
                                    color = if (unitsMissingVibInput.isEmpty()) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (unitsMissingVibInput.isEmpty()) "Lengkap (8/8 Diinput)" else "${unitsMissingVibInput.size} Belum Input",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (unitsMissingVibInput.isEmpty()) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (unitsMissingVibInput.isNotEmpty()) {
                            Text(
                                text = "Unit mesin yang belum melakukan input dari jam 07:00 sampai jam $currentHourMinute:",
                                fontSize = 11.sp,
                                color = Color(0xFFD84315),
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                unitsMissingVibInput.forEach { unitId ->
                                    Surface(
                                        color = Color(0xFFFFE0B2),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFB74D))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ErrorOutline,
                                                contentDescription = null,
                                                tint = Color(0xFFE65100),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$unitId (Belum Input)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFBF360C)
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Data dihitung dari jam 07:00 sampai saat pengecekan ($currentHourMinute) dan akan diperbarui kembali $nextRenewalText.",
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        } else {
                            Text(
                                text = "Semua unit mesin Sludge Centrifuge (SC-01 s/d SC-08) telah melakukan input dari jam 07:00 sampai jam $currentHourMinute. Data akan diperbarui kembali $nextRenewalText.",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            // Sub-item 1B: Anomali Parameter (Vibrasi/Suhu Kritikal, Greasing, Bocor, Suara Abnormal)
            val card1BNeedsAttention = vibAlerts.isNotEmpty()
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (card1BNeedsAttention) Color(0xFFFFF8E1) else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (card1BNeedsAttention) Color(0xFFFFB74D) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Temuan Parameter Abnormal & Kepatuhan Greasing",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Text(
                                text = "${vibAlerts.size} Temuan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (vibAlerts.isNotEmpty()) Color(0xFFD32F2F) else Color(0xFF388E3C)
                            )
                        }

                        if (vibAlerts.isNotEmpty()) {
                            vibAlerts.forEach { alert ->
                                Surface(
                                    color = Color(0xFFFFF8E1),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD54F)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = SlateGrey,
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = alert.unitId,
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Shift: ${alert.log.shift} (${alert.log.operatorName})",
                                                    fontSize = 11.sp,
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = WibDateUtils.format("HH:mm", alert.log.timestamp) + " WIB",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        alert.reasons.forEach { r ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(Color(0xFFD32F2F), CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = r,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFFB71C1C)
                                                )
                                            }
                                        }

                                        if (alert.log.comments.isNotBlank()) {
                                            Text(
                                                text = "Catatan: ${alert.log.comments}",
                                                fontSize = 10.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Tidak ada unit dengan temperatur/vibrasi kritikal, kebocoran, atau suara abnormal. Semua unit di-greasing dengan baik.",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------------------------------
        // SECTION 2: LAPORAN CILT
        // ---------------------------------------------------------------------------------------------
        if (selectedFilterTab == "Semua" || selectedFilterTab == "2. CILT") {
            item {
                SectionHeader(
                    icon = Icons.Default.FactCheck,
                    title = "2. Laporan CILT (Cleaning, Inspection, Lubricating, Tightening)",
                    subtitle = "Status eksekusi $checkRangeString & pelaporan temuan ketidaksesuaian di lapangan",
                    accentColor = Color(0xFFFB8C00),
                    alertCount = totalCiltAlerts,
                    onExportExcel = {
                        exportReportType = "cilt"
                        showExcelExportDialog = true
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

                val card2NeedsAttention = isCiltNotDoneToday || ciltFindings.isNotEmpty()
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (card2NeedsAttention) Color(0xFFFFF8E1) else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (card2NeedsAttention) Color(0xFFFFB74D) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Status Pelaksanaan CILT Hari Ini ($checkRangeString)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateGrey
                                )
                                Surface(
                                    color = if (isCiltNotDoneToday) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isCiltNotDoneToday) "1 Temuan (Belum Dilakukan)" else "${todayCiltChecks.size} Checklist Selesai",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCiltNotDoneToday) Color(0xFFC62828) else Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (isCiltNotDoneToday) {
                                Surface(
                                    color = Color(0xFFFFF8E1),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFE082)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = Color(0xFFF57C00),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Peringatan: Belum ada checklist CILT yang tercatat dari jam 07:00 sampai jam $currentHourMinute. Data akan diperbarui lagi $nextRenewalText.",
                                            fontSize = 10.5.sp,
                                            lineHeight = 14.sp,
                                            color = Color(0xFFBF360C),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = "Kegiatan CILT telah tercatat hari ini sebanyak ${todayCiltChecks.size} kali inspeksi (07:00 s/d $currentHourMinute). Data akan diperbarui lagi $nextRenewalText.",
                                    fontSize = 10.5.sp,
                                    lineHeight = 14.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }

                        // CILT Findings / Defects
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Temuan Ketidaksesuaian CILT di Lapangan:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Surface(
                                color = if (ciltFindings.isNotEmpty()) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (ciltFindings.isNotEmpty()) "${ciltFindings.size} Temuan" else "0 Temuan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ciltFindings.isNotEmpty()) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (ciltFindings.isNotEmpty()) {
                            ciltFindings.forEach { defect ->
                                Surface(
                                    color = Color(0xFFFBE9E7),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFCCBC)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Inspektor: ${defect.check.operatorName}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFBF360C)
                                            )
                                            Text(
                                                text = WibDateUtils.format("dd MMM, HH:mm", defect.check.timestamp) + " WIB",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        defect.findings.forEach { issue ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.PriorityHigh,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD84315),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = issue,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF3E2723)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (isCiltNotDoneToday) "Belum ada data checklist untuk melihat temuan CILT." else "Tidak ada temuan ketidaksesuaian CILT. Seluruh poin standar terpenuhi.",
                                fontSize = 11.sp,
                                color = if (isCiltNotDoneToday) Color.Gray else Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------------------------------
        // SECTION 3: LAPORAN RELIABILITY PM REAL-TIME
        // ---------------------------------------------------------------------------------------------
        if (selectedFilterTab == "Semua" || selectedFilterTab == "3. Reliability PM") {
            item {
                SectionHeader(
                    icon = Icons.Default.Build,
                    title = "3. Laporan Real-Time Reliability PM (Temuan Abnormality)",
                    subtitle = "Laporan anomali mesin, RPN Score, White/Yellow Tag & respon cepat",
                    accentColor = Color(0xFF5E35B1),
                    alertCount = totalPmAlerts,
                    onExportExcel = {
                        exportReportType = "reliability"
                        showExcelExportDialog = true
                    }
                )
            }

            val card3NeedsAttention = recentAbnormalityFindings.isNotEmpty()
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (card3NeedsAttention) Color(0xFFF3E5F5) else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (card3NeedsAttention) Color(0xFFBA68C8) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Daftar Temuan Real-Time Reliability PM",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Surface(
                                color = if (recentAbnormalityFindings.isNotEmpty()) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (recentAbnormalityFindings.isNotEmpty()) "${recentAbnormalityFindings.size} Temuan Abnormality" else "0 Temuan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (recentAbnormalityFindings.isNotEmpty()) Color(0xFFC62828) else Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (recentAbnormalityFindings.isNotEmpty()) {
                            recentAbnormalityFindings.forEach { rep ->
                                val isHighRpn = rep.rpn >= 100
                                Surface(
                                    color = if (isHighRpn) Color(0xFFFFEBEE) else Color(0xFFFAFAFA),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (isHighRpn) Color(0xFFEF9A9A) else Color(0xFFE0E0E0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = if (rep.tagType.contains("Yellow", ignoreCase = true)) Color(0xFFFBC02D) else Color(0xFF78909C),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = rep.tagType,
                                                        color = if (rep.tagType.contains("Yellow", ignoreCase = true)) Color.Black else Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = rep.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SlateGrey
                                                )
                                            }

                                            Surface(
                                                color = if (isHighRpn) Color(0xFFC62828) else BrandGreen,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "RPN: ${rep.rpn}",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Text(
                                            text = rep.description,
                                            fontSize = 11.sp,
                                            color = Color.Black
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Faktor: ${rep.factor} • PIC: ${rep.picName}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                            Text(
                                                text = WibDateUtils.format("dd MMM yyyy, HH:mm", rep.timestamp) + " WIB",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Belum ada temuan abnormality yang tercatat di menu Reliability PM.",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------------------------------------------
        // SECTION 4: LAPORAN KEGIATAN FLUSHING & PROGRES AKTUAL H+1
        // ---------------------------------------------------------------------------------------------
        if (selectedFilterTab == "Semua" || selectedFilterTab == "4. Flushing") {
            item {
                SectionHeader(
                    icon = Icons.Default.WaterDrop,
                    title = "4. Laporan Kegiatan Flushing & Progres Aktual H+1",
                    subtitle = "Pelaporan kegiatan $checkRangeString & monitoring pelaksanaan bertahap",
                    accentColor = Color(0xFF00897B),
                    alertCount = totalFlushingAlerts,
                    onExportExcel = {
                        exportReportType = "flushing"
                        showExcelExportDialog = true
                    }
                )
            }

            val card4NeedsAttention = totalFlushingAlerts > 0
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (card4NeedsAttention) Color(0xFFFFF8E1) else Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (card4NeedsAttention) Color(0xFFFFB74D) else Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Status Flushing Hari Ini ($checkRangeString)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )
                            Surface(
                                color = if (totalFlushingAlerts > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isFlushingNotDoneToday)
                                        "1 Temuan (Belum Dikerjakan)"
                                    else if (incompleteFlushingLogs.isNotEmpty())
                                        "${incompleteFlushingLogs.size} Temuan (Siklus Tidak Tuntas)"
                                    else
                                        "0 Temuan (${todayFlushingLogs.size} Siklus 100% Selesai)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalFlushingAlerts > 0) Color(0xFFC62828) else Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        if (isFlushingNotDoneToday) {
                            Surface(
                                color = Color(0xFFFFF8E1),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFE082)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = Color(0xFFF57C00),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Peringatan: Belum ada kegiatan flushing yang tercatat dari jam 07:00 sampai jam $currentHourMinute. Data akan diperbarui lagi $nextRenewalText.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFBF360C),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Flushing berjalan aktif dengan total ${todayFlushingLogs.size} kali flushing terlaksana dari jam 07:00 sampai jam $currentHourMinute. Data akan diperbarui lagi $nextRenewalText.",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        // Incomplete Flushing cycle findings
                        if (incompleteFlushingLogs.isNotEmpty()) {
                            Text(
                                "Temuan Siklus Flushing Tidak Tuntas (${incompleteFlushingLogs.size} Temuan):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateGrey
                            )

                            incompleteFlushingLogs.forEach { log ->
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(color = Color(0xFFE65100), shape = RoundedCornerShape(4.dp)) {
                                                Text(log.unitName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text("${log.shift} • Operator: ${log.operatorName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                                Text("${log.totalCount - log.completedCount} Langkah SOP Terlewati (${log.completedCount}/${log.totalCount} Selesai)", fontSize = 10.sp, color = Color(0xFFC62828), fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                        Text(
                                            text = WibDateUtils.format("HH:mm", log.timestamp) + " WIB",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // PROGRES AKTUAL FLUSHING H+1
                        Divider(color = Color(0xFFEEEEEE))

                        Text(
                            "Progres Aktual Flushing H+1 (Analisis Komparasi Kemarin vs Hari Ini):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateGrey
                        )

                        // Calculate KPI H+1
                        val yesterdayUnits = yesterdayFlushingLogs.map { it.unitName }.distinct()
                        val todayUnits = todayFlushingLogs.map { it.unitName }.distinct()
                        val avgTasksCompletedToday = if (todayFlushingLogs.isNotEmpty()) {
                            (todayFlushingLogs.sumOf { it.completedCount }.toFloat() / (todayFlushingLogs.size * 11) * 100).toInt()
                        } else 0

                        val avgTasksCompletedYesterday = if (yesterdayFlushingLogs.isNotEmpty()) {
                            (yesterdayFlushingLogs.sumOf { it.completedCount }.toFloat() / (yesterdayFlushingLogs.size * 11) * 100).toInt()
                        } else 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color(0xFFF1F8E9),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Hari Ini (Aktual)", fontSize = 10.sp, color = Color.Gray)
                                    Text("${todayFlushingLogs.size} Laporan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandGreen)
                                    Text("${todayUnits.size} Unit aktif", fontSize = 10.sp, color = Color.Black)
                                    Text("Kepatuhan: $avgTasksCompletedToday%", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = BrandGreen)
                                }
                            }

                            Surface(
                                color = Color(0xFFECEFF1),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Kemarin (H-1)", fontSize = 10.sp, color = Color.Gray)
                                    Text("${yesterdayFlushingLogs.size} Laporan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                    Text("${yesterdayUnits.size} Unit aktif", fontSize = 10.sp, color = Color.Black)
                                    Text("Kepatuhan: $avgTasksCompletedYesterday%", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SlateGrey)
                                }
                            }
                        }

                        // Progres H+1 Checklist detail
                        if (todayFlushingLogs.isNotEmpty()) {
                            Text(
                                "Detail Log Flushing Terkini Hari Ini:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SlateGrey
                            )
                            todayFlushingLogs.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(color = BrandGreen, shape = RoundedCornerShape(4.dp)) {
                                            Text(log.unitName, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${log.shift} • ${log.operatorName}", fontSize = 11.sp, color = Color.Black)
                                    }
                                    Text(
                                        "${log.completedCount}/${log.totalCount} Selesai",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (log.completedCount >= log.totalCount) BrandGreen else Color(0xFFE65100)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showExcelExportDialog) {
        ExcelExportDateRangeDialog(
            initialExportType = exportReportType,
            vibrationLogs = vibrationLogs,
            ciltChecks = ciltChecks,
            reports = reports,
            reliabilityChecks = reliabilityChecks,
            flushingLogs = flushingLogs,
            onDismiss = { showExcelExportDialog = false },
            onExport = { type, start, end, isAllData ->
                val sDate = if (isAllData) null else start
                val eDate = if (isAllData) null else end
                when (type) {
                    "monitoring" -> ExcelExporter.exportMonitoringExcel(context, vibrationLogs, sDate, eDate)
                    "cilt" -> ExcelExporter.exportCiltExcel(context, ciltChecks, sDate, eDate)
                    "reliability" -> ExcelExporter.exportReliabilityPmExcel(context, reports, reliabilityChecks, sDate, eDate)
                    "flushing" -> ExcelExporter.exportFlushingExcel(context, flushingLogs, sDate, eDate)
                    else -> ExcelExporter.exportAllReportsExcel(context, vibrationLogs, ciltChecks, reports, reliabilityChecks, flushingLogs, sDate, eDate)
                }
            }
        )
    }
}

@Composable
fun ReportSummaryTile(
    title: String,
    alertCount: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val needsAttention = alertCount > 0
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (needsAttention) color.copy(alpha = 0.08f) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (needsAttention) color.copy(alpha = 0.6f) else Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$alertCount",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateGrey,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (needsAttention) "$alertCount Temuan" else "0 Temuan (Aman)",
                fontSize = 8.sp,
                fontWeight = if (needsAttention) FontWeight.Bold else FontWeight.Normal,
                color = if (needsAttention) color else Color(0xFF2E7D32)
            )
        }
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    alertCount: Int = 0,
    onExportExcel: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateGrey
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (alertCount > 0) accentColor.copy(alpha = 0.15f) else Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (alertCount > 0) "$alertCount Temuan" else "0 Temuan",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (alertCount > 0) accentColor else Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }

        if (onExportExcel != null) {
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(
                onClick = onExportExcel,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    contentColor = Color(0xFF1B5E20)
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Ekspor Excel",
                    modifier = Modifier.size(15.dp),
                    tint = Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Excel",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
            }
        }
    }
}

@Composable
fun ExcelExportDateRangeDialog(
    initialExportType: String = "all",
    vibrationLogs: List<VibrationLog>,
    ciltChecks: List<CiltCheck>,
    reports: List<AbnormalityReport>,
    reliabilityChecks: List<ReliabilityPmCheck>,
    flushingLogs: List<FlushingLog>,
    onDismiss: () -> Unit,
    onExport: (type: String, startDate: Long, endDate: Long, isAllData: Boolean) -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(initialExportType) }
    var selectedPreset by remember { mutableStateOf("Hari Ini") }

    val todayStart = remember { WibDateUtils.getStartOfDay() }
    val todayEnd = remember { todayStart + 24 * 60 * 60 * 1000L - 1L }

    var startDate by remember { mutableStateOf(todayStart) }
    var endDate by remember { mutableStateOf(todayEnd) }

    fun applyPreset(preset: String) {
        selectedPreset = preset
        when (preset) {
            "Hari Ini" -> {
                startDate = WibDateUtils.getStartOfDay()
                endDate = startDate + 24 * 60 * 60 * 1000L - 1L
            }
            "7 Hari" -> {
                val cal = WibDateUtils.getCalendar().apply {
                    add(Calendar.DAY_OF_YEAR, -6)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                startDate = cal.timeInMillis
                endDate = WibDateUtils.getStartOfDay() + 24 * 60 * 60 * 1000L - 1L
            }
            "30 Hari" -> {
                val cal = WibDateUtils.getCalendar().apply {
                    add(Calendar.DAY_OF_YEAR, -29)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                startDate = cal.timeInMillis
                endDate = WibDateUtils.getStartOfDay() + 24 * 60 * 60 * 1000L - 1L
            }
            "Bulan Ini" -> {
                val cal = WibDateUtils.getCalendar().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                startDate = cal.timeInMillis
                endDate = WibDateUtils.getStartOfDay() + 24 * 60 * 60 * 1000L - 1L
            }
            "Semua Data" -> {
                startDate = 0L
                endDate = System.currentTimeMillis() + 86400000L
            }
            "Kustom" -> {}
        }
    }

    fun pickStartDate() {
        val cal = WibDateUtils.getCalendar(if (startDate > 0L) startDate else todayStart)
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
                startDate = newStart
                if (endDate < newStart) {
                    endDate = newStart + 24 * 60 * 60 * 1000L - 1L
                }
                selectedPreset = "Kustom"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickEndDate() {
        val cal = WibDateUtils.getCalendar(if (endDate > 0L) endDate else todayEnd)
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
                endDate = newEnd
                if (startDate > newEnd) {
                    startDate = WibDateUtils.getCalendar(newEnd).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                }
                selectedPreset = "Kustom"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val isAllData = selectedPreset == "Semua Data"
    val countMonitoring = remember(startDate, endDate, isAllData, vibrationLogs) {
        if (isAllData) vibrationLogs.size else vibrationLogs.count { it.timestamp in startDate..endDate }
    }
    val countCilt = remember(startDate, endDate, isAllData, ciltChecks) {
        if (isAllData) ciltChecks.size else ciltChecks.count { it.timestamp in startDate..endDate }
    }
    val countAbnormality = remember(startDate, endDate, isAllData, reports) {
        if (isAllData) reports.size else reports.count { it.timestamp in startDate..endDate }
    }
    val countPmChecks = remember(startDate, endDate, isAllData, reliabilityChecks) {
        if (isAllData) reliabilityChecks.size else reliabilityChecks.count { it.timestamp in startDate..endDate }
    }
    val countFlushing = remember(startDate, endDate, isAllData, flushingLogs) {
        if (isAllData) flushingLogs.size else flushingLogs.count { it.timestamp in startDate..endDate }
    }

    val totalRecordsFound = when (selectedType) {
        "monitoring" -> countMonitoring
        "cilt" -> countCilt
        "reliability" -> countAbnormality + countPmChecks
        "flushing" -> countFlushing
        else -> countMonitoring + countCilt + countAbnormality + countPmChecks + countFlushing
    }

    val displayFormatter = remember { WibDateUtils.createFormatter("dd MMM yyyy") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        color = Color.White,
                        shape = CircleShape,
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Tarik Data Excel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Tentukan periode data ditarik dari kapan & sampai kapan",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                HorizontalDivider(color = Color(0xFFE0E0E0))

                // Jenis Laporan Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "1. Pilih Laporan yang Ditarik:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val reportOptions = listOf(
                            "all" to "Semua Laporan",
                            "monitoring" to "1. Monitoring SC",
                            "cilt" to "2. CILT",
                            "reliability" to "3. Reliability PM",
                            "flushing" to "4. Flushing"
                        )
                        reportOptions.forEach { (typeKey, label) ->
                            val isSelected = selectedType == typeKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedType = typeKey },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = Color.Black
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color.White,
                                    selectedContainerColor = Color(0xFFEEEEEE),
                                    labelColor = Color.Black,
                                    selectedLabelColor = Color.Black
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) Color.Black else Color(0xFFCFD8DC),
                                    selectedBorderColor = Color.Black,
                                    borderWidth = if (isSelected) 1.5.dp else 1.dp
                                )
                            )
                        }
                    }
                }

                // Pilihan Cepat Rentang Tanggal (Presets)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "2. Pilihan Cepat Rentang Tanggal:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Hari Ini", "7 Hari", "30 Hari", "Bulan Ini", "Semua Data").forEach { preset ->
                            val isPresetSelected = selectedPreset == preset
                            SuggestionChip(
                                onClick = { applyPreset(preset) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color.White
                                ),
                                label = {
                                    Text(
                                        text = preset,
                                        fontSize = 10.sp,
                                        fontWeight = if (isPresetSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.Black
                                    )
                                },
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = if (isPresetSelected) Color.Black else Color(0xFFCFD8DC),
                                    borderWidth = if (isPresetSelected) 1.5.dp else 1.dp
                                )
                            )
                        }
                    }
                }

                // Pemilihan Tanggal Kustom (Dari Tanggal s/d Sampai Tanggal)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "3. Tentukan Rentang Tanggal Penarikan:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dari Tanggal
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { pickStartDate() },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFB0BEC5))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "DARI TANGGAL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isAllData) "Awal Data" else displayFormatter.format(Date(startDate)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )

                        // Sampai Tanggal
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { pickEndDate() },
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFB0BEC5))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SAMPAI TANGGAL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Event,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isAllData) "Hari Ini" else displayFormatter.format(Date(endDate)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                // Preview Jumlah Data Ditemukan
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (totalRecordsFound > 0) Color(0xFFA5D6A7) else Color(0xFFFFE082)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (totalRecordsFound > 0) Icons.Default.CheckCircle else Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = if (totalRecordsFound > 0) Color(0xFF2E7D32) else Color(0xFFF57F17),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (totalRecordsFound > 0)
                                    "Ditemukan $totalRecordsFound data untuk diekspor"
                                else
                                    "Tidak ada data pada periode tanggal ini",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        if (selectedType == "all" && totalRecordsFound > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Monitoring: $countMonitoring | CILT: $countCilt | Abnormality: $countAbnormality | PM: $countPmChecks | Flushing: $countFlushing",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    onExport(selectedType, startDate, endDate, isAllData)
                    onDismiss()
                },
                enabled = totalRecordsFound > 0,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                border = BorderStroke(1.2.dp, if (totalRecordsFound > 0) Color.Black else Color(0xFFCFD8DC)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    tint = if (totalRecordsFound > 0) Color.Black else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tarik Data Excel",
                    fontWeight = FontWeight.Bold,
                    color = if (totalRecordsFound > 0) Color.Black else Color.Gray,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFCFD8DC))
            ) {
                Text("Batal", color = Color.Black, fontSize = 12.sp)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
