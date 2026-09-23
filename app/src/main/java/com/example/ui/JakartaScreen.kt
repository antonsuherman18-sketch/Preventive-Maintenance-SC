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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.util.WibDateUtils
import kotlinx.coroutines.launch
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
                        onValueChange = { newInput ->
                            passwordInput = newInput
                            passwordError = false
                            val trimmed = newInput.trim()
                            if (trimmed == "6321" || trimmed.equals("mill6321", ignoreCase = true) || trimmed.equals("admin6321", ignoreCase = true)) {
                                isUnlocked = true
                                passwordError = false
                                passwordInput = ""
                            }
                        },
                        label = { Text("Password / PIN", color = Color(0xFF333333)) },
                        placeholder = { Text("Masukkan Password (6321)", color = Color(0xFF666666)) },
                        singleLine = true,
                        isError = passwordError,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.Black,
                            fontSize = 14.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val trimmed = passwordInput.trim()
                                if (trimmed == "6321" || trimmed.equals("mill6321", ignoreCase = true) || trimmed.equals("admin6321", ignoreCase = true)) {
                                    isUnlocked = true
                                    passwordError = false
                                    passwordInput = ""
                                } else if (trimmed.isNotEmpty()) {
                                    passwordError = true
                                }
                            }
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
    val cycleVibLogs = vibrationLogs.filter { log ->
        val isMock = log.comments.contains("Historis", ignoreCase = true) ||
                     log.comments.contains("pasca penerapan PM", ignoreCase = true)
        !isMock && log.timestamp in cycleStartTime..checkTimeMillis
    }

    // Check which SC units have input from 07:00 to current check time
    val unitsWithVibToday = units.associateWith { unitId ->
        cycleVibLogs.filter { log ->
            val num = unitId.substringAfter("SC-") // "01"
            val shortNum = num.trimStart('0') // "1"
            log.comments.contains(unitId, ignoreCase = true) ||
            log.comments.contains(unitId.replace("-", " "), ignoreCase = true) ||
            log.comments.contains("SC$num", ignoreCase = true) ||
            log.comments.contains("SC-$shortNum", ignoreCase = true) ||
            log.comments.contains("SC $shortNum", ignoreCase = true)
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

            if (!log.isGreased && log.greasingStatus != "Belum Masuk Jadwal") {
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

    // Operator CILT tracking (Wahyu & Abdul Aziz)
    val standardCiltOperators = listOf("Wahyu", "Abdul Aziz")
    val operatorsDoneCiltToday = todayCiltChecks.map { it.operatorName.trim() }
    val unperformedCiltOperators = standardCiltOperators.filter { op ->
        operatorsDoneCiltToday.none { it.contains(op, ignoreCase = true) }
    }
    val performedCiltOperators = standardCiltOperators.filter { op ->
        operatorsDoneCiltToday.any { it.contains(op, ignoreCase = true) }
    }

    data class CiltDefect(
        val check: CiltCheck,
        val findings: List<String>
    )

    val ciltFindings = mutableListOf<CiltDefect>()
    todayCiltChecks.forEach { c ->
        val issues = mutableListOf<String>()
        // 1. Cleaning
        if (!c.isAreaCleaned) issues.add("Area sekitar mesin belum bersih dari tumpahan sludge & oli")
        if (!c.isMachineCleaned) issues.add("Mesin & area sekitar belum bersih dari kerak")
        if (!c.isDrainageCleaned) issues.add("Drainase tersumbat / belum diperiksa")
        if (c.cleaningNotes.isNotBlank()) issues.add("Temuan Cleaning: ${c.cleaningNotes}")

        // 2. Inspection
        if (!c.isVibrationSoundChecked) issues.add("Vibrasi & suara abnormal terdeteksi")
        if (!c.isTemperatureChecked) issues.add("Temperatur bearing & motor di luar batas normal")
        if (!c.isLeakChecked) issues.add("Terdapat kebocoran pada pipa, valve, coupling, atau gland packing")
        if (!c.isComponentsConditionChecked) issues.add("Nozzle, holder, belt, coupling, atau baut tidak dalam kondisi baik")
        if (c.inspectionNotes.isNotBlank()) issues.add("Temuan Inspection: ${c.inspectionNotes}")

        // 3. Lubrication
        if (!c.isGreasingBearingChecked) issues.add("Greasing bearing belum sesuai jadwal & takaran")
        if (!c.isOilLevelChecked) issues.add("Level oli transfluid kopling di luar batas normal")
        if (c.lubricationNotes.isNotBlank()) issues.add("Temuan Lubrication: ${c.lubricationNotes}")

        // 4. Tightening
        if (!c.isFoundationBoltsTightened) issues.add("Baut pondasi mesin, cover, flange, motor, coupling belum dikencangkan")
        if (!c.isNoLooseBoltsChecked) issues.add("Terdapat baut longgar atau lepas (tidak terpasang)")
        if (c.tighteningNotes.isNotBlank()) issues.add("Temuan Tightening: ${c.tighteningNotes}")

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
    val totalCiltAlerts = unperformedCiltOperators.size + ciltFindings.size
    val totalPmAlerts = recentAbnormalityFindings.size
    val totalFlushingAlerts = if (isFlushingNotDoneToday) 1 else incompleteFlushingLogs.size

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        state = listState,
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
                                onClick = {
                                    viewModel.syncData { success ->
                                        if (success) {
                                            android.widget.Toast.makeText(context, "Sinkronisasi Cloud Berhasil!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Gagal sinkron, pastikan mode online aktif!", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Sync Sekarang", color = BrandYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // QUICK STATS SUMMARY TILES (DAPAT DIPILIH & SINKRON KE RINCIAN)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportSummaryTile(
                        title = "Monitoring",
                        alertCount = totalMonitoringAlerts,
                        color = if (totalMonitoringAlerts > 0) Color(0xFFE53935) else BrandGreen,
                        isSelected = selectedFilterTab == "1. Monitoring SC",
                        onClick = {
                            selectedFilterTab = if (selectedFilterTab == "1. Monitoring SC") "Semua" else "1. Monitoring SC"
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = 2)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ReportSummaryTile(
                        title = "CILT",
                        alertCount = totalCiltAlerts,
                        color = if (totalCiltAlerts > 0) Color(0xFFFB8C00) else BrandGreen,
                        isSelected = selectedFilterTab == "2. CILT",
                        onClick = {
                            selectedFilterTab = if (selectedFilterTab == "2. CILT") "Semua" else "2. CILT"
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = 2)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ReportSummaryTile(
                        title = "Reliability PM",
                        alertCount = totalPmAlerts,
                        color = if (totalPmAlerts > 0) Color(0xFF5E35B1) else BrandGreen,
                        isSelected = selectedFilterTab == "3. Reliability PM",
                        onClick = {
                            selectedFilterTab = if (selectedFilterTab == "3. Reliability PM") "Semua" else "3. Reliability PM"
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = 2)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ReportSummaryTile(
                        title = "Flushing",
                        alertCount = totalFlushingAlerts,
                        color = if (totalFlushingAlerts > 0) Color(0xFFD81B60) else BrandGreen,
                        isSelected = selectedFilterTab == "4. Flushing",
                        onClick = {
                            selectedFilterTab = if (selectedFilterTab == "4. Flushing") "Semua" else "4. Flushing"
                            coroutineScope.launch {
                                listState.animateScrollToItem(index = 2)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Bar petunjuk & status pilihan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedFilterTab == "Semua") "💡 Sentuh kartu di atas untuk membuka rincian temuan" else "Menampilkan rincian: $selectedFilterTab",
                        fontSize = 11.sp,
                        fontWeight = if (selectedFilterTab == "Semua") FontWeight.Normal else FontWeight.Bold,
                        color = if (selectedFilterTab == "Semua") Color.Gray else SlateGrey
                    )
                    if (selectedFilterTab != "Semua") {
                        TextButton(
                            onClick = { selectedFilterTab = "Semua" },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("Tampilkan Semua", fontSize = 11.sp, color = BrandGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // PANEL RINCIAN TEMUAN LANGSUNG (MUNCUL TEPAT DI BAWAH KARTU PILIHAN)
        if (selectedFilterTab != "Semua") {
            item {
                when (selectedFilterTab) {
                    "1. Monitoring SC" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF1E88E5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFF1E88E5),
                                            shape = CircleShape,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Analytics, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Rincian $totalMonitoringAlerts Temuan Monitoring SC",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SlateGrey
                                            )
                                            Text(
                                                text = "Pengecekan siklus hari ini ($checkRangeString)",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (totalMonitoringAlerts > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (totalMonitoringAlerts > 0) "$totalMonitoringAlerts Perlu Tindakan" else "Aman",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (totalMonitoringAlerts > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Divider(color = Color(0xFFBBDEFB), thickness = 0.8.dp)

                                if (unitsMissingVibInput.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "1. Mesin Belum Input (${unitsMissingVibInput.size} Unit):",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD84315)
                                        )
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            unitsMissingVibInput.forEach { u ->
                                                Surface(
                                                    color = Color(0xFFFFE0B2),
                                                    shape = RoundedCornerShape(6.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                                                ) {
                                                    Text(
                                                        text = "$u (Belum Input)",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFBF360C),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (vibAlerts.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "2. Anomali & Parameter Alert (${vibAlerts.size} Temuan):",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                        vibAlerts.forEach { alert ->
                                            Surface(
                                                color = Color(0xFFFFEBEE),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Text(
                                                        text = "Unit: ${alert.unitId}",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFB71C1C)
                                                    )
                                                    alert.reasons.forEach { r ->
                                                        Text("• $r", fontSize = 10.5.sp, color = Color(0xFF424242))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (unitsMissingVibInput.isEmpty() && vibAlerts.isEmpty()) {
                                    Text(
                                        text = "Seluruh unit Sludge Centrifuge (SC-01 s/d SC-08) telah diinput dan semua parameter dalam kondisi normal 100%.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                    "2. CILT" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFFFB8C00)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFFFB8C00),
                                            shape = CircleShape,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.FactCheck, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Rincian $totalCiltAlerts Temuan CILT",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SlateGrey
                                            )
                                            Text(
                                                text = "Inspeksi CILT hari ini ($checkRangeString)",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (totalCiltAlerts > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (totalCiltAlerts > 0) "$totalCiltAlerts Perlu Tindakan" else "Lengkap",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (totalCiltAlerts > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Divider(color = Color(0xFFFFE082), thickness = 0.8.dp)

                                if (unperformedCiltOperators.isNotEmpty()) {
                                    Surface(
                                        color = Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.WarningAmber,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD32F2F),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Operator Belum Melakukan CILT Hari Ini:",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFB71C1C)
                                                )
                                            }

                                            Text(
                                                text = "Waktu/jam pengecekan saat ini: $checkRangeString",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFFC62828),
                                                fontWeight = FontWeight.Medium
                                            )

                                            unperformedCiltOperators.forEach { opName ->
                                                Surface(
                                                    color = Color.White,
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, Color(0xFFFFCDD2)),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(24.dp)
                                                                    .clip(CircleShape)
                                                                    .background(Color(0xFFFFEBEE)),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    imageVector = Icons.Default.PersonOff,
                                                                    contentDescription = null,
                                                                    tint = Color(0xFFD32F2F),
                                                                    modifier = Modifier.size(14.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Text(
                                                                    text = opName,
                                                                    fontSize = 12.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFFB71C1C)
                                                                )
                                                                Text(
                                                                    text = "Belum ada catatan input CILT hari ini",
                                                                    fontSize = 10.sp,
                                                                    color = Color.Gray
                                                                )
                                                            }
                                                        }
                                                        Surface(
                                                            color = Color(0xFFFFCDD2),
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "BELUM INPUT",
                                                                fontSize = 9.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFFB71C1C),
                                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (performedCiltOperators.isNotEmpty()) {
                                    Surface(
                                        color = Color(0xFFF0FDF4),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color(0xFF16A34A),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Operator Sudah Melakukan CILT Hari Ini:",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF166534)
                                                )
                                            }

                                            performedCiltOperators.forEach { opName ->
                                                val checks = todayCiltChecks.filter { it.operatorName.contains(opName, ignoreCase = true) }
                                                val lastCheck = checks.maxByOrNull { it.timestamp }
                                                val timeStr = if (lastCheck != null) WibDateUtils.format("HH:mm", lastCheck.timestamp) + " WIB" else ""

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 4.dp, vertical = 3.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(8.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFF16A34A))
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = opName,
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color(0xFF14532D)
                                                        )
                                                    }
                                                    Text(
                                                        text = "Tercatat pukul $timeStr (${checks.size}x)",
                                                        fontSize = 10.5.sp,
                                                        color = Color(0xFF15803D),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (ciltFindings.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "Temuan Ketidaksesuaian Standar CILT:",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD84315)
                                        )
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
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("Inspektor: ${defect.check.operatorName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBF360C))
                                                        Text(WibDateUtils.format("HH:mm", defect.check.timestamp) + " WIB", fontSize = 10.sp, color = Color.Gray)
                                                    }
                                                    defect.findings.forEach { issue ->
                                                        Text("• $issue", fontSize = 10.5.sp, color = Color(0xFF3E2723))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (unperformedCiltOperators.isEmpty() && ciltFindings.isEmpty()) {
                                    Text(
                                        text = "Seluruh operator (${standardCiltOperators.joinToString(", ")}) telah melaksanakan CILT hari ini ($checkRangeString) dan seluruh standar terpenuhi 100%.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                    "3. Reliability PM" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF5E35B1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFF5E35B1),
                                            shape = CircleShape,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Rincian $totalPmAlerts Temuan Reliability PM",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SlateGrey
                                            )
                                            Text(
                                                text = "Daftar temuan abnormality aktif & FMEA",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (totalPmAlerts > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (totalPmAlerts > 0) "$totalPmAlerts Temuan" else "Nihil",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (totalPmAlerts > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Divider(color = Color(0xFFCE93D8), thickness = 0.8.dp)

                                if (recentAbnormalityFindings.isNotEmpty()) {
                                    recentAbnormalityFindings.take(5).forEach { rep ->
                                        Surface(
                                            color = Color(0xFFFAFAFA),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(rep.title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = SlateGrey)
                                                    Text("RPN: ${rep.rpn}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (rep.rpn >= 100) Color(0xFFC62828) else Color(0xFF2E7D32))
                                                }
                                                Text(rep.description, fontSize = 10.5.sp, color = Color.DarkGray)
                                                Text("PIC: ${rep.picName} • Tag: ${rep.tagType}", fontSize = 9.5.sp, color = Color.Gray)
                                            }
                                        }
                                    }
                                    if (recentAbnormalityFindings.size > 5) {
                                        Text(
                                            text = "Lihat ${recentAbnormalityFindings.size - 5} temuan lainnya pada tabel lengkap di bawah.",
                                            fontSize = 10.5.sp,
                                            color = Color.Gray,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
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
                    "4. Flushing" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, Color(0xFF00897B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = Color(0xFF00897B),
                                            shape = CircleShape,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Rincian $totalFlushingAlerts Temuan Flushing",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SlateGrey
                                            )
                                            Text(
                                                text = "Siklus flushing centrifuge ($checkRangeString)",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (totalFlushingAlerts > 0) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (totalFlushingAlerts > 0) "$totalFlushingAlerts Perlu Tindakan" else "Tuntas",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (totalFlushingAlerts > 0) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Divider(color = Color(0xFF80CBC4), thickness = 0.8.dp)

                                if (isFlushingNotDoneToday) {
                                    Surface(
                                        color = Color(0xFFFFE0B2),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFFB74D)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.WarningAmber, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Peringatan Flushing: Belum ada jadwal flushing yang dieksekusi hari ini ($checkRangeString).",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFBF360C)
                                            )
                                        }
                                    }
                                }

                                if (incompleteFlushingLogs.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Siklus Flushing Belum Tuntas:",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD84315)
                                        )
                                        incompleteFlushingLogs.forEach { log ->
                                            Surface(
                                                color = Color(0xFFFFEBEE),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(8.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("${log.unitName}: ${log.completedCount}/${log.totalCount} Siklus", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                                                    Text(WibDateUtils.format("HH:mm", log.timestamp) + " WIB", fontSize = 10.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!isFlushingNotDoneToday && incompleteFlushingLogs.isEmpty()) {
                                    Text(
                                        text = "Seluruh siklus kegiatan flushing hari ini telah tuntas 100% terlaksana.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                }
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

        item {
            Spacer(modifier = Modifier.height(24.dp))
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
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val needsAttention = alertCount > 0
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> color.copy(alpha = 0.16f)
                needsAttention -> color.copy(alpha = 0.08f)
                else -> Color.White
            }
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = when {
                isSelected -> color
                needsAttention -> color.copy(alpha = 0.6f)
                else -> Color(0xFFE2E8F0)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 0.dp),
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$alertCount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Dipilih",
                        tint = color,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) color else SlateGrey,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (needsAttention) "$alertCount Temuan" else "0 Temuan (Aman)",
                fontSize = 8.sp,
                fontWeight = if (needsAttention || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (needsAttention) color else Color(0xFF2E7D32)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(3.dp))
                Surface(
                    color = color,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "AKTIF",
                        fontSize = 7.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
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
