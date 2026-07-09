package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.delay

class Repository(private val db: AppDatabase) {
    val ciltDao = db.ciltDao()
    val reliabilityPmDao = db.reliabilityPmDao()
    val abnormalityDao = db.abnormalityDao()
    val mentorPairingDao = db.mentorPairingDao()
    val vibrationDao = db.vibrationDao()

    // Flow definitions for UI observation
    val allCiltChecks: Flow<List<CiltCheck>> = ciltDao.getAllChecks()
    val allReliabilityPmChecks: Flow<List<ReliabilityPmCheck>> = reliabilityPmDao.getAllChecks()
    val allAbnormalityReports: Flow<List<AbnormalityReport>> = abnormalityDao.getAllReports()
    val allMentorPairingLogs: Flow<List<MentorPairingLog>> = mentorPairingDao.getAllLogs()
    val vibrationHistory: Flow<List<VibrationLog>> = vibrationDao.getVibrationHistory()

    // Offline / Online Sync State Management
    private val _isOnline = MutableStateFlow(false) // default offline as Berau mill is remote
    val isOnline: StateFlow<Boolean> = _isOnline

    private val _syncStatus = MutableStateFlow("PKS Berau (Offline Mode) - Data disimpan di database lokal")
    val syncStatus: StateFlow<String> = _syncStatus

    fun setOnlineMode(online: Boolean) {
        _isOnline.value = online
        if (online) {
            _syncStatus.value = "Terhubung dengan Jakarta HQ (Online Mode)"
        } else {
            _syncStatus.value = "PKS Berau (Offline Mode) - Data disimpan di database lokal"
        }
    }

    suspend fun syncWithJakarta(): Boolean {
        if (!_isOnline.value) {
            _syncStatus.value = "Gagal sinkronisasi: Perangkat offline. Aktifkan mode online terlebih dahulu."
            return false
        }

        _syncStatus.value = "Sedang sinkronisasi data dari Mill Berau ke Server Jakarta..."
        delay(2000) // Realistic network delay simulation for remote Kalimantan mill

        try {
            // Mark all items across tables as synced
            ciltDao.markAllSynced()
            reliabilityPmDao.markAllSynced()
            abnormalityDao.markAllSynced()
            mentorPairingDao.markAllSynced()
            vibrationDao.markAllSynced()

            _syncStatus.value = "Sinkronisasi Berhasil! Seluruh data mill Berau telah terupdate di Server Jakarta."
            return true
        } catch (e: Exception) {
            Log.e("Repository", "Sync failed", e)
            _syncStatus.value = "Kesalahan teknis sinkronisasi: ${e.localizedMessage}"
            return false
        }
    }

    // Helper to insert and handle local database state
    suspend fun insertCiltCheck(check: CiltCheck) {
        val insertedId = ciltDao.insertCheck(check)
        if (_isOnline.value) {
            // Auto-sync single entry if online
            ciltDao.markSynced(insertedId)
        }
    }

    suspend fun insertReliabilityCheck(check: ReliabilityPmCheck) {
        val insertedId = reliabilityPmDao.insertCheck(check)
        if (_isOnline.value) {
            reliabilityPmDao.markSynced(insertedId)
        }
    }

    suspend fun insertAbnormalityReport(report: AbnormalityReport) {
        val insertedId = abnormalityDao.insertReport(report)
        if (_isOnline.value) {
            abnormalityDao.markSynced(insertedId)
        }
    }

    suspend fun deleteAbnormalityReport(report: AbnormalityReport) {
        abnormalityDao.deleteReport(report)
    }

    suspend fun insertMentorPairingLog(log: MentorPairingLog) {
        val insertedId = mentorPairingDao.insertLog(log)
        if (_isOnline.value) {
            mentorPairingDao.markSynced(insertedId)
        }
    }

    suspend fun insertVibrationLog(log: VibrationLog) {
        val insertedId = vibrationDao.insertLog(log)
        if (_isOnline.value) {
            vibrationDao.markSynced(insertedId)
        }
    }

    // Pre-populate realistic data if empty
    suspend fun prePopulateIfEmpty() {
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
                        motorVibration = 3.5f,
                        gearBoxVibration = 4.0f,
                        bearingTemp = bearingTemp,
                        motorTemp = 55f,
                        alarmState = alarm,
                        comments = "Historis sebelum perbaikan otonomous & PM",
                        isSynced = true
                    )
                )
            }

            // Post-Improvement Stable Trend (Jun-Jul 2026)
            for (i in 15 downTo 0) {
                val timestamp = now - i * dayMs
                val devVib = (2.5f + Math.random() * 1.5).toFloat() // 2.5 to 4.0 mm/s (normal < 4.5)
                val ndevVib = (2.0f + Math.random() * 1.0).toFloat()
                val bearingTemp = (55f + Math.random() * 5).toFloat() // 55 to 60 C
                
                vibrationDao.insertLog(
                    VibrationLog(
                        timestamp = timestamp,
                        driveEndVibration = devVib,
                        nonDriveEndVibration = ndevVib,
                        motorVibration = 1.8f,
                        gearBoxVibration = 2.1f,
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

            // 4. Pre-populate initial CILT & Reliability checks
            ciltDao.insertCheck(
                CiltCheck(
                    timestamp = now - 2 * dayMs,
                    operatorName = "Ari (Operator)",
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
                    comments = "Checklist lengkap CILT shift pagi. Mesin stabil.",
                    isSynced = true
                )
            )

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
        }
    }
}
