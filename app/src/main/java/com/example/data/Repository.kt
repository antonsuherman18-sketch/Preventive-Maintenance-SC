package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class Repository(
    private val db: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val ciltDao = db.ciltDao()
    val reliabilityPmDao = db.reliabilityPmDao()
    val abnormalityDao = db.abnormalityDao()
    val mentorPairingDao = db.mentorPairingDao()
    val vibrationDao = db.vibrationDao()
    val flushingDao = db.flushingDao()

    val firestoreSyncManager = FirestoreSyncManager(db, scope)

    // Flow definitions for UI observation
    val allCiltChecks: Flow<List<CiltCheck>> = ciltDao.getAllChecks()
    val allReliabilityPmChecks: Flow<List<ReliabilityPmCheck>> = reliabilityPmDao.getAllChecks()
    val allAbnormalityReports: Flow<List<AbnormalityReport>> = abnormalityDao.getAllReports()
    val allMentorPairingLogs: Flow<List<MentorPairingLog>> = mentorPairingDao.getAllLogs()
    val vibrationHistory: Flow<List<VibrationLog>> = vibrationDao.getVibrationHistory()
    val allFlushingLogs: Flow<List<FlushingLog>> = flushingDao.getAllLogs()

    // Offline / Online Sync State Management
    // Default online (true) so devices sync automatically via Firebase
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline

    private val _syncStatus = MutableStateFlow("Tersambung ke Cloud Firebase")
    val syncStatus: StateFlow<String> = _syncStatus

    init {
        // Start listening to real-time updates from Firebase so all phones stay in sync
        firestoreSyncManager.startRealtimeSync()
    }

    fun setOnlineMode(online: Boolean) {
        _isOnline.value = online
        if (online) {
            _syncStatus.value = "Tersambung ke Cloud Firebase"
            firestoreSyncManager.startRealtimeSync()
            scope.launch {
                syncWithJakarta()
            }
        } else {
            _syncStatus.value = "Mode Offline (Data tersimpan lokal)"
        }
    }

    suspend fun syncWithJakarta(): Boolean {
        if (!_isOnline.value) {
            _syncStatus.value = "Gagal sinkronisasi: Perangkat offline. Aktifkan mode online terlebih dahulu."
            return false
        }

        _syncStatus.value = "Menyinkronkan dengan Cloud..."

        try {
            val pushSuccess = firestoreSyncManager.pushAllLocalDataToFirestore()
            if (pushSuccess) {
                _syncStatus.value = "Sinkronisasi Cloud Berhasil!"
                return true
            } else {
                _syncStatus.value = "Sinkronisasi Cloud tersimpan (offline-queue)"
                return true
            }
        } catch (e: Exception) {
            Log.e("Repository", "Sync failed", e)
            _syncStatus.value = "Kesalahan teknis: ${e.localizedMessage}"
            return false
        }
    }

    // Helper to insert and handle local database state and push to Firebase
    suspend fun insertCiltCheck(check: CiltCheck) {
        val insertedId = ciltDao.insertCheck(check)
        val insertedItem = check.copy(id = insertedId, isSynced = _isOnline.value)
        if (_isOnline.value) {
            try {
                firestoreSyncManager.pushCilt(insertedItem)
                ciltDao.markSynced(insertedId)
            } catch (e: Exception) {
                Log.e("Repository", "Push CILT failed, queued locally", e)
            }
        }
    }

    suspend fun insertReliabilityCheck(check: ReliabilityPmCheck) {
        val insertedId = reliabilityPmDao.insertCheck(check)
        val insertedItem = check.copy(id = insertedId, isSynced = _isOnline.value)
        if (_isOnline.value) {
            try {
                firestoreSyncManager.pushReliability(insertedItem)
                reliabilityPmDao.markSynced(insertedId)
            } catch (e: Exception) {
                Log.e("Repository", "Push Reliability failed, queued locally", e)
            }
        }
    }

    suspend fun insertAbnormalityReport(report: AbnormalityReport) {
        val insertedId = abnormalityDao.insertReport(report)
        val insertedItem = report.copy(id = insertedId, isSynced = _isOnline.value)
        if (_isOnline.value) {
            try {
                firestoreSyncManager.pushAbnormality(insertedItem)
                abnormalityDao.markSynced(insertedId)
            } catch (e: Exception) {
                Log.e("Repository", "Push Abnormality failed, queued locally", e)
            }
        }
    }

    suspend fun deleteAbnormalityReport(report: AbnormalityReport) {
        abnormalityDao.deleteReport(report)
        if (_isOnline.value) {
            firestoreSyncManager.deleteAbnormality(report)
        }
    }

    suspend fun insertMentorPairingLog(log: MentorPairingLog) {
        val insertedId = mentorPairingDao.insertLog(log)
        val insertedItem = log.copy(id = insertedId, isSynced = _isOnline.value)
        if (_isOnline.value) {
            try {
                firestoreSyncManager.pushMentorPairing(insertedItem)
                mentorPairingDao.markSynced(insertedId)
            } catch (e: Exception) {
                Log.e("Repository", "Push Mentor failed, queued locally", e)
            }
        }
    }

    suspend fun insertVibrationLog(log: VibrationLog) {
        val insertedId = vibrationDao.insertLog(log)
        val insertedItem = log.copy(id = insertedId, isSynced = _isOnline.value)
        if (_isOnline.value) {
            try {
                firestoreSyncManager.pushVibration(insertedItem)
                vibrationDao.markSynced(insertedId)
            } catch (e: Exception) {
                Log.e("Repository", "Push Vibration failed, queued locally", e)
            }
        }
    }

    suspend fun insertFlushingLog(log: FlushingLog) {
        val insertedId = flushingDao.insertLog(log)
        val insertedItem = log.copy(id = insertedId, isSynced = _isOnline.value)
        if (_isOnline.value) {
            try {
                firestoreSyncManager.pushFlushing(insertedItem)
                flushingDao.markSynced(insertedId)
            } catch (e: Exception) {
                Log.e("Repository", "Push Flushing failed, queued locally", e)
            }
        }
    }

    suspend fun deleteFlushingLog(log: FlushingLog) {
        flushingDao.deleteLog(log)
        if (_isOnline.value) {
            firestoreSyncManager.deleteFlushing(log)
        }
    }

    // Pre-populate realistic data if empty
    suspend fun prePopulateIfEmpty() {
        // Clean up any historical/mock logs that were erroneously stamped for today
        try {
            val now = System.currentTimeMillis()
            val oneDayAgo = now - 24 * 60 * 60 * 1000L
            val existing = vibrationDao.getAllLogsList()
            existing.filter {
                (it.comments.contains("pasca penerapan PM", ignoreCase = true) || it.comments.contains("Historis sebelum", ignoreCase = true)) &&
                it.timestamp >= oneDayAgo
            }.forEach {
                vibrationDao.deleteLog(it)
            }
        } catch (e: Exception) {
            Log.e("Repository", "Error cleaning up today's mock vibration logs", e)
        }

        val currentVibList = vibrationHistory.first()
        if (currentVibList.isEmpty()) {
            Log.d("Repository", "Pre-populating rich mock data for Sludge Centrifuge PM...")

            // 1. Generate Vibration & Temp History (Jan 2026 to July 2026)
            // Slide 14: Vibrasi tinggi mencapai 8.8 mm/s, trip di Jan-Mar 2026, target <4.5 mm/s.
            val dayMs = 24 * 60 * 60 * 1000L
            val now = System.currentTimeMillis()

            // Historical Critical / Warnings (Jan-Mar 2026)
            for (i in 40 downTo 25) {
                val timestamp = now - i * dayMs
                val devVib = (7.5f + Math.random() * 2.0).toFloat() // 7.5 to 9.5 mm/s
                val ndevVib = (6.0f + Math.random() * 1.5).toFloat()
                val bearingTemp = (68f + Math.random() * 10).toFloat() // 68 to 78 C
                val alarm = if (devVib > 8.8f) "Critical" else "Warning"
                
                vibrationDao.insertLog(
                    VibrationLog(
                        timestamp = timestamp,
                        driveEndVibration = devVib,
                        nonDriveEndVibration = ndevVib,
                        motorBearingVibration = 3.5f,
                        gearboxBearingVibration = 4.0f,
                        bearingTemp = bearingTemp,
                        motorTemp = 55f,
                        alarmState = alarm,
                        comments = "Historis sebelum perbaikan otonomous & PM",
                        isSynced = true
                    )
                )
            }

            // Post-Improvement Stable Trend (Jun-Jul 2026) - ends at yesterday (i=1), never today
            for (i in 15 downTo 1) {
                val timestamp = now - i * dayMs
                val devVib = (2.5f + Math.random() * 1.5).toFloat() // 2.5 to 4.0 mm/s (normal < 4.5)
                val ndevVib = (2.0f + Math.random() * 1.0).toFloat()
                val bearingTemp = (55f + Math.random() * 5).toFloat() // 55 to 60 C
                
                vibrationDao.insertLog(
                    VibrationLog(
                        timestamp = timestamp,
                        driveEndVibration = devVib,
                        nonDriveEndVibration = ndevVib,
                        motorBearingVibration = 1.8f,
                        gearboxBearingVibration = 2.1f,
                        bearingTemp = bearingTemp,
                        motorTemp = 48f,
                        alarmState = "Normal",
                        comments = "Vibrasi aman terkontrol, pasca penerapan PM & Greasing",
                        isSynced = true
                    )
                )
            }

            // 2. Pre-populate Mentor Pairing Logs
            // Based on slide 22 & 23 (Mentor: Anton Suherman, Mentees: Ari, Asmill)
            mentorPairingDao.insertLog(
                MentorPairingLog(
                    timestamp = now - 5 * dayMs,
                    mentorName = "Anton Suherman",
                    menteeName = "Ari (Operator Baru)",
                    activityName = "Pembersihan Nozzle & Bowl",
                    preTestScore = 40,
                    postTestScore = 85,
                    status = "Passed",
                    comments = "Ari sudah mahir menggunakan nozzle gauge untuk memastikan lubang nozzle presisi.",
                    isSynced = true
                )
            )
            mentorPairingDao.insertLog(
                MentorPairingLog(
                    timestamp = now - 4 * dayMs,
                    mentorName = "Anton Suherman",
                    menteeName = "Asmill (Operator Klarifikasi)",
                    activityName = "Pengukuran Vibrasi & Suhu",
                    preTestScore = 50,
                    postTestScore = 90,
                    status = "Passed",
                    comments = "Asmill dapat mengoperasikan vibration analyzer dan thermal gun sesuai titik ukur.",
                    isSynced = true
                )
            )
            mentorPairingDao.insertLog(
                MentorPairingLog(
                    timestamp = now - 3 * dayMs,
                    mentorName = "Anton Suherman",
                    menteeName = "Budiman",
                    activityName = "Greasing Bearing Hollow",
                    preTestScore = 30,
                    postTestScore = 65,
                    status = "Needs Practice",
                    comments = "Perlu latihan tambahan dosis greasing agar grease keluar penuh dari housing.",
                    isSynced = true
                )
            )

            // 3. Pre-populate Abnormality Reports (White & Yellow Tag)
            // Slide 14 & 24: "Vibrasi mesin tinggi mencapai 8.8 mm/s", "Kebocoran minyak dari light phase"
            abnormalityDao.insertReport(
                AbnormalityReport(
                    timestamp = now - 8 * dayMs,
                    title = "Vibrasi Tinggi Sludge Centrifuge No.2",
                    description = "Vibrasi drive end mencapai 8.8 mm/s saat beroperasi penuh. Terdeteksi bunyi menderit kasar.",
                    factor = "MACHINE",
                    severityScore = 8,
                    occurrenceScore = 7,
                    detectionScore = 5,
                    rpn = 280,
                    picName = "Anton Suherman",
                    tagType = "Yellow Tag", // Yellow tag for production operator attention
                    isSynced = true
                )
            )
            abnormalityDao.insertReport(
                AbnormalityReport(
                    timestamp = now - 6 * dayMs,
                    title = "Kebocoran Ring Seal Bowl",
                    description = "Ditemukan rembesan minyak phase ringan pada lantai penopang centrifuge nomor 6.",
                    factor = "ENVIRONMENT",
                    severityScore = 6,
                    occurrenceScore = 4,
                    detectionScore = 3,
                    rpn = 72,
                    picName = "Anton Suherman",
                    tagType = "White Tag", // White tag for mechanic maintenance work
                    isSynced = true
                )
            )

            // 4. Pre-populate initial CILT checks (Daily, Weekly, Monthly data for Wahyu & Abdul Aziz)
            val ciltSeedList = listOf(
                CiltCheck(
                    timestamp = now - 2 * 3600 * 1000L, // Today 2 hours ago
                    operatorName = "Wahyu",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = true,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = true,
                    beltTensionChecked = true,
                    comments = "[Pagi] Checklist CILT lengkap, putaran bowl halus & getaran stabil 2.8 mm/s.",
                    isSynced = true
                ),
                CiltCheck(
                    timestamp = now - 10 * 3600 * 1000L, // Today 10 hours ago
                    operatorName = "Abdul Aziz",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = false,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = true,
                    beltTensionChecked = true,
                    comments = "[Malam] Pelumasan bearing buffer selesai. Greasing coupling dijadwalkan shift berikutnya.",
                    isSynced = true
                ),
                CiltCheck(
                    timestamp = now - 1 * dayMs - 3 * 3600 * 1000L, // Yesterday
                    operatorName = "Abdul Aziz",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = true,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = true,
                    beltTensionChecked = true,
                    comments = "[Pagi] Pembersihan nozzle & bowl tuntas bebas kerak sludge. Suhu bearing 58°C.",
                    isSynced = true
                ),
                CiltCheck(
                    timestamp = now - 2 * dayMs, // 2 days ago
                    operatorName = "Wahyu",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = true,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = true,
                    beltTensionChecked = true,
                    comments = "[Pagi] Seluruh 13 item checklist terverifikasi lengkap. Level oli coupling aman.",
                    isSynced = true
                ),
                CiltCheck(
                    timestamp = now - 4 * dayMs, // 4 days ago
                    operatorName = "Wahyu",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = true,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = false,
                    beltTensionChecked = true,
                    comments = "[Malam] Baut fitting sambungan pipa agak kendor, sudah dikencangkan ulang.",
                    isSynced = true
                ),
                CiltCheck(
                    timestamp = now - 6 * dayMs, // 6 days ago
                    operatorName = "Abdul Aziz",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = true,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = true,
                    beltTensionChecked = true,
                    comments = "[Pagi] Pemeriksaan harian selesai. Kondisi visual nozzle baik.",
                    isSynced = true
                ),
                CiltCheck(
                    timestamp = now - 11 * dayMs, // 11 days ago
                    operatorName = "Wahyu",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = true,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = true,
                    beltTensionChecked = true,
                    comments = "[Pagi] Rutin CILT awal minggu kedua. Transmisi dan belt tension normal.",
                    isSynced = true
                ),
                CiltCheck(
                    timestamp = now - 16 * dayMs, // 16 days ago
                    operatorName = "Abdul Aziz",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = true,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = true,
                    beltTensionChecked = true,
                    comments = "[Malam] CILT shift malam tuntas 100%.",
                    isSynced = true
                ),
                CiltCheck(
                    timestamp = now - 23 * dayMs, // 23 days ago
                    operatorName = "Wahyu",
                    nozzleCleaned = true,
                    bowlCleaned = true,
                    areaCleaned = true,
                    nozzleChecked = true,
                    vibrationChecked = true,
                    leakChecked = true,
                    instrumentChecked = true,
                    bearingGreased = true,
                    couplingGreased = true,
                    oilLevelChecked = true,
                    nozzleBoltsTightened = true,
                    fittingPipesTightened = true,
                    beltTensionChecked = true,
                    comments = "[Pagi] Checklist CILT bulanan terlaksana sesuai SOP.",
                    isSynced = true
                )
            )
            ciltSeedList.forEach { ciltDao.insertCheck(it) }

            reliabilityPmDao.insertCheck(
                ReliabilityPmCheck(
                    timestamp = now - 1 * dayMs,
                    flushingIntervalMinutes = 120, // 2 hours
                    waterTempCelsius = 94f,
                    hollowBearingChecked = true,
                    couplingOilLeakChecked = true,
                    vibrationChecked = true,
                    bowlSpeedRpm = 1450f,
                    bearingTempCelsius = 58f,
                    unusualNoiseDetected = false,
                    comments = "Flushing berjalan tertib setiap 2 jam memakai air panas 94C. Hasil aman.",
                    isSynced = true
                )
            )

            // 5. Pre-populate Flushing Logs
            val tasksPagi1 = defaultFlushingTasks("08:00")
            val tasksMalam1 = defaultFlushingTasks("20:00")
            val tasksPagi2 = defaultFlushingTasks("10:00")

            flushingDao.insertLog(
                FlushingLog(
                    timestamp = now - 4 * 60 * 60 * 1000L,
                    operatorName = "Wahyu",
                    shift = "Shift Pagi",
                    unitName = "SC-01",
                    itemsJson = flushingTasksToJson(tasksPagi1),
                    completedCount = 11,
                    totalCount = 11,
                    notes = "Flushing rutin shift pagi selesai 100%, getaran normal dan air outlet bersih.",
                    isSynced = true
                )
            )

            flushingDao.insertLog(
                FlushingLog(
                    timestamp = now - 14 * 60 * 60 * 1000L,
                    operatorName = "Abdul Aziz",
                    shift = "Shift Malam",
                    unitName = "SC-02",
                    itemsJson = flushingTasksToJson(tasksMalam1),
                    completedCount = 11,
                    totalCount = 11,
                    notes = "Flushing shift malam lancar, nozzle terpasang baik dan tidak ada kebocoran.",
                    isSynced = true
                )
            )

            flushingDao.insertLog(
                FlushingLog(
                    timestamp = now - 1 * dayMs - 2 * 60 * 60 * 1000L,
                    operatorName = "Wahyu",
                    shift = "Shift Pagi",
                    unitName = "SC-03",
                    itemsJson = flushingTasksToJson(tasksPagi2),
                    completedCount = 11,
                    totalCount = 11,
                    notes = "Pelaksanaan flushing air panas sesuai prosedur, getaran stabil < 4.5 mm/s.",
                    isSynced = true
                )
            )
        }
    }
}
