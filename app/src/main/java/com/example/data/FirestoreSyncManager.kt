package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Manages two-way real-time synchronization between local Room SQLite and Firebase Cloud Firestore.
 * Supports offline-first operation: works offline and pushes/pulls data seamlessly when online.
 */
class FirestoreSyncManager(
    private val db: AppDatabase,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            // Enable offline persistence in Firestore SDK
            firestoreSettings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        }
    }

    private val listeners = mutableListOf<ListenerRegistration>()
    private var isListening = false

    // Collections
    private val CILT_COLLECTION = "cilt_checks"
    private val RELIABILITY_COLLECTION = "reliability_pm_checks"
    private val ABNORMALITY_COLLECTION = "abnormality_reports"
    private val MENTOR_COLLECTION = "mentor_pairing_logs"
    private val VIBRATION_COLLECTION = "vibration_logs"
    private val FLUSHING_COLLECTION = "flushing_logs"

    /**
     * Start real-time listeners for all 6 collections so updates made by ANY phone
     * are automatically pulled and stored into this phone's Room database.
     */
    fun startRealtimeSync() {
        if (isListening) return
        isListening = true

        try {
            // 1. CILT Checks Listener
            val ciltReg = firestore.collection(CILT_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "CILT listener error", error)
                        return@addSnapshotListener
                    }
                    snapshot?.let { snap ->
                        scope.launch {
                            for (doc in snap.documents) {
                                try {
                                    val timestamp = doc.getLong("timestamp") ?: continue
                                    val operatorName = doc.getString("operatorName") ?: ""
                                    // Check if existing
                                    val existing = db.ciltDao().findByTimestampAndOperator(timestamp, operatorName)
                                    val id = existing?.id ?: 0L
                                    val item = CiltCheck(
                                        id = id,
                                        timestamp = timestamp,
                                        operatorName = operatorName,
                                        // 1. Cleaning
                                        areaCleaned = doc.getBoolean("areaCleaned") ?: false,
                                        machineCleaned = doc.getBoolean("machineCleaned") ?: (doc.getBoolean("bowlCleaned") ?: false),
                                        drainageCleaned = doc.getBoolean("drainageCleaned") ?: (doc.getBoolean("nozzleCleaned") ?: false),
                                        cleaningNotes = doc.getString("cleaningNotes") ?: "",
                                        // 2. Inspection
                                        vibrationSoundChecked = doc.getBoolean("vibrationSoundChecked") ?: (doc.getBoolean("vibrationChecked") ?: false),
                                        temperatureChecked = doc.getBoolean("temperatureChecked") ?: (doc.getBoolean("instrumentChecked") ?: false),
                                        leakChecked = doc.getBoolean("leakChecked") ?: false,
                                        componentsConditionChecked = doc.getBoolean("componentsConditionChecked") ?: (doc.getBoolean("nozzleChecked") ?: false),
                                        inspectionNotes = doc.getString("inspectionNotes") ?: "",
                                        // 3. Lubrication
                                        greasingBearingChecked = doc.getBoolean("greasingBearingChecked") ?: (doc.getBoolean("bearingGreased") ?: false),
                                        oilLevelChecked = doc.getBoolean("oilLevelChecked") ?: false,
                                        lubricationNotes = doc.getString("lubricationNotes") ?: "",
                                        // 4. Tightening
                                        foundationBoltsTightened = doc.getBoolean("foundationBoltsTightened") ?: (doc.getBoolean("nozzleBoltsTightened") ?: false),
                                        noLooseBoltsChecked = doc.getBoolean("noLooseBoltsChecked") ?: (doc.getBoolean("fittingPipesTightened") ?: false),
                                        tighteningNotes = doc.getString("tighteningNotes") ?: "",
                                        // Legacy
                                        nozzleCleaned = doc.getBoolean("nozzleCleaned") ?: false,
                                        bowlCleaned = doc.getBoolean("bowlCleaned") ?: false,
                                        nozzleChecked = doc.getBoolean("nozzleChecked") ?: false,
                                        vibrationChecked = doc.getBoolean("vibrationChecked") ?: false,
                                        instrumentChecked = doc.getBoolean("instrumentChecked") ?: false,
                                        bearingGreased = doc.getBoolean("bearingGreased") ?: false,
                                        couplingGreased = doc.getBoolean("couplingGreased") ?: false,
                                        nozzleBoltsTightened = doc.getBoolean("nozzleBoltsTightened") ?: false,
                                        fittingPipesTightened = doc.getBoolean("fittingPipesTightened") ?: false,
                                        beltTensionChecked = doc.getBoolean("beltTensionChecked") ?: false,
                                        comments = doc.getString("comments") ?: "",
                                        isSynced = true
                                    )
                                    db.ciltDao().insertCheck(item)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error inserting remote CILT", e)
                                }
                            }
                        }
                    }
                }
            listeners.add(ciltReg)

            // 2. Reliability PM Listener
            val relReg = firestore.collection(RELIABILITY_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "Reliability listener error", error)
                        return@addSnapshotListener
                    }
                    snapshot?.let { snap ->
                        scope.launch {
                            for (doc in snap.documents) {
                                try {
                                    val timestamp = doc.getLong("timestamp") ?: continue
                                    val existing = db.reliabilityPmDao().findByTimestamp(timestamp)
                                    val id = existing?.id ?: 0L
                                    val item = ReliabilityPmCheck(
                                        id = id,
                                        timestamp = timestamp,
                                        flushingIntervalMinutes = doc.getLong("flushingIntervalMinutes")?.toInt() ?: 120,
                                        waterTempCelsius = doc.getDouble("waterTempCelsius")?.toFloat() ?: 92f,
                                        hollowBearingChecked = doc.getBoolean("hollowBearingChecked") ?: false,
                                        couplingOilLeakChecked = doc.getBoolean("couplingOilLeakChecked") ?: false,
                                        vibrationChecked = doc.getBoolean("vibrationChecked") ?: false,
                                        bowlSpeedRpm = doc.getDouble("bowlSpeedRpm")?.toFloat() ?: 1450f,
                                        bearingTempCelsius = doc.getDouble("bearingTempCelsius")?.toFloat() ?: 62f,
                                        unusualNoiseDetected = doc.getBoolean("unusualNoiseDetected") ?: false,
                                        comments = doc.getString("comments") ?: "",
                                        isSynced = true
                                    )
                                    db.reliabilityPmDao().insertCheck(item)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error inserting remote Reliability", e)
                                }
                            }
                        }
                    }
                }
            listeners.add(relReg)

            // 3. Abnormality Reports Listener
            val abReg = firestore.collection(ABNORMALITY_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "Abnormality listener error", error)
                        return@addSnapshotListener
                    }
                    snapshot?.let { snap ->
                        scope.launch {
                            for (doc in snap.documents) {
                                try {
                                    val timestamp = doc.getLong("timestamp") ?: continue
                                    val title = doc.getString("title") ?: ""
                                    val existing = db.abnormalityDao().findByTimestampAndTitle(timestamp, title)
                                    val id = existing?.id ?: 0L
                                    val sev = doc.getLong("severityScore")?.toInt() ?: 1
                                    val occ = doc.getLong("occurrenceScore")?.toInt() ?: 1
                                    val det = doc.getLong("detectionScore")?.toInt() ?: 1
                                    val rpn = doc.getLong("rpn")?.toInt() ?: (sev * occ * det)
                                    val item = AbnormalityReport(
                                        id = id,
                                        timestamp = timestamp,
                                        title = title,
                                        description = doc.getString("description") ?: "",
                                        factor = doc.getString("factor") ?: "MACHINE",
                                        severityScore = sev,
                                        occurrenceScore = occ,
                                        detectionScore = det,
                                        rpn = rpn,
                                        photoUri = doc.getString("photoUri"),
                                        picName = doc.getString("picName") ?: "Anton Suherman",
                                        tagType = doc.getString("tagType") ?: "",
                                        status = doc.getString("status") ?: "Open",
                                        mechanicName = doc.getString("mechanicName") ?: "",
                                        repairNotes = doc.getString("repairNotes") ?: "",
                                        resolvedTimestamp = doc.getLong("resolvedTimestamp") ?: 0L,
                                        isSynced = true
                                    )
                                    db.abnormalityDao().insertReport(item)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error inserting remote Abnormality", e)
                                }
                            }
                        }
                    }
                }
            listeners.add(abReg)

            // 4. Mentor Pairing Logs Listener
            val mentorReg = firestore.collection(MENTOR_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "Mentor listener error", error)
                        return@addSnapshotListener
                    }
                    snapshot?.let { snap ->
                        scope.launch {
                            for (doc in snap.documents) {
                                try {
                                    val timestamp = doc.getLong("timestamp") ?: continue
                                    val menteeName = doc.getString("menteeName") ?: ""
                                    val existing = db.mentorPairingDao().findByTimestampAndMentee(timestamp, menteeName)
                                    val id = existing?.id ?: 0L
                                    val item = MentorPairingLog(
                                        id = id,
                                        timestamp = timestamp,
                                        mentorName = doc.getString("mentorName") ?: "Anton Suherman",
                                        menteeName = menteeName,
                                        activityName = doc.getString("activityName") ?: "",
                                        preTestScore = doc.getLong("preTestScore")?.toInt() ?: 0,
                                        postTestScore = doc.getLong("postTestScore")?.toInt() ?: 0,
                                        status = doc.getString("status") ?: "Passed",
                                        comments = doc.getString("comments") ?: "",
                                        isSynced = true
                                    )
                                    db.mentorPairingDao().insertLog(item)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error inserting remote Mentor log", e)
                                }
                            }
                        }
                    }
                }
            listeners.add(mentorReg)

            // 5. Vibration Logs Listener
            val vibReg = firestore.collection(VIBRATION_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "Vibration listener error", error)
                        return@addSnapshotListener
                    }
                    snapshot?.let { snap ->
                        scope.launch {
                            for (doc in snap.documents) {
                                try {
                                    val timestamp = doc.getLong("timestamp") ?: continue
                                    val opName = doc.getString("operatorName") ?: ""
                                    val existing = db.vibrationDao().findByTimestampAndOperator(timestamp, opName)
                                    val id = existing?.id ?: 0L
                                    val item = VibrationLog(
                                        id = id,
                                        timestamp = timestamp,
                                        operatorName = opName,
                                        shift = doc.getString("shift") ?: "Shift Siang",
                                        driveEndVibration = doc.getDouble("driveEndVibration")?.toFloat() ?: 0f,
                                        nonDriveEndVibration = doc.getDouble("nonDriveEndVibration")?.toFloat() ?: 0f,
                                        motorBearingVibration = doc.getDouble("motorBearingVibration")?.toFloat() ?: 0f,
                                        gearboxBearingVibration = doc.getDouble("gearboxBearingVibration")?.toFloat() ?: 0f,
                                        bowlVibration = doc.getDouble("bowlVibration")?.toFloat() ?: 0f,
                                        bearingTemp = doc.getDouble("bearingTemp")?.toFloat() ?: 0f,
                                        motorTemp = doc.getDouble("motorTemp")?.toFloat() ?: 0f,
                                        isGreased = doc.getBoolean("isGreased") ?: false,
                                        greasingStatus = doc.getString("greasingStatus") ?: (if (doc.getBoolean("isGreased") == true) "Ya" else "Tidak"),
                                        soundState = doc.getString("soundState") ?: "Normal",
                                        hasLeakage = doc.getBoolean("hasLeakage") ?: false,
                                        alarmState = doc.getString("alarmState") ?: "Normal",
                                        machineStatus = doc.getString("machineStatus") ?: "Operasi",
                                        comments = doc.getString("comments") ?: "",
                                        isSynced = true
                                    )
                                    db.vibrationDao().insertLog(item)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error inserting remote Vibration log", e)
                                }
                            }
                        }
                    }
                }
            listeners.add(vibReg)

            // 6. Flushing Logs Listener
            val flushReg = firestore.collection(FLUSHING_COLLECTION)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("FirestoreSync", "Flushing listener error", error)
                        return@addSnapshotListener
                    }
                    snapshot?.let { snap ->
                        scope.launch {
                            for (doc in snap.documents) {
                                try {
                                    val timestamp = doc.getLong("timestamp") ?: continue
                                    val unitName = doc.getString("unitName") ?: "SC 01"
                                    val existing = db.flushingDao().findByTimestampAndUnit(timestamp, unitName)
                                    val id = existing?.id ?: 0L
                                    val item = FlushingLog(
                                        id = id,
                                        timestamp = timestamp,
                                        operatorName = doc.getString("operatorName") ?: "",
                                        shift = doc.getString("shift") ?: "Shift Pagi",
                                        unitName = unitName,
                                        itemsJson = doc.getString("itemsJson") ?: "",
                                        completedCount = doc.getLong("completedCount")?.toInt() ?: 11,
                                        totalCount = doc.getLong("totalCount")?.toInt() ?: 11,
                                        notes = doc.getString("notes") ?: "",
                                        isSynced = true
                                    )
                                    db.flushingDao().insertLog(item)
                                } catch (e: Exception) {
                                    Log.e("FirestoreSync", "Error inserting remote Flushing log", e)
                                }
                            }
                        }
                    }
                }
            listeners.add(flushReg)

            Log.i("FirestoreSync", "All 6 Firestore realtime listeners attached successfully.")
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Failed to start realtime listeners", e)
        }
    }

    /**
     * Push all local data (or unsynced data) to Cloud Firestore.
     */
    suspend fun pushAllLocalDataToFirestore(): Boolean {
        return try {
            // Push CILT
            val cilts = db.ciltDao().getAllChecksList()
            for (c in cilts) {
                pushCilt(c)
            }

            // Push Reliability PM
            val rels = db.reliabilityPmDao().getAllChecksList()
            for (r in rels) {
                pushReliability(r)
            }

            // Push Abnormality Reports
            val abs = db.abnormalityDao().getAllReportsList()
            for (a in abs) {
                pushAbnormality(a)
            }

            // Push Mentor Pairing
            val mentors = db.mentorPairingDao().getAllLogsList()
            for (m in mentors) {
                pushMentorPairing(m)
            }

            // Push Vibration Logs
            val vibs = db.vibrationDao().getAllLogsList()
            for (v in vibs) {
                pushVibration(v)
            }

            // Push Flushing Logs
            val flushes = db.flushingDao().getAllLogsList()
            for (f in flushes) {
                pushFlushing(f)
            }

            // Mark all local items as synced
            db.ciltDao().markAllSynced()
            db.reliabilityPmDao().markAllSynced()
            db.abnormalityDao().markAllSynced()
            db.mentorPairingDao().markAllSynced()
            db.vibrationDao().markAllSynced()
            db.flushingDao().markAllSynced()

            true
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error pushing data to Firestore", e)
            false
        }
    }

    // Individual uploaders with deterministic document IDs so re-uploading doesn't create duplicates
    suspend fun pushCilt(check: CiltCheck) {
        val docId = "cilt_${check.timestamp}_${check.operatorName.replace(" ", "_")}"
        val data = hashMapOf<String, Any>(
            "timestamp" to check.timestamp,
            "operatorName" to check.operatorName,
            // 1. Cleaning
            "areaCleaned" to check.areaCleaned,
            "machineCleaned" to check.machineCleaned,
            "drainageCleaned" to check.drainageCleaned,
            "cleaningNotes" to check.cleaningNotes,
            // 2. Inspection
            "vibrationSoundChecked" to check.vibrationSoundChecked,
            "temperatureChecked" to check.temperatureChecked,
            "leakChecked" to check.leakChecked,
            "componentsConditionChecked" to check.componentsConditionChecked,
            "inspectionNotes" to check.inspectionNotes,
            // 3. Lubrication
            "greasingBearingChecked" to check.greasingBearingChecked,
            "oilLevelChecked" to check.oilLevelChecked,
            "lubricationNotes" to check.lubricationNotes,
            // 4. Tightening
            "foundationBoltsTightened" to check.foundationBoltsTightened,
            "noLooseBoltsChecked" to check.noLooseBoltsChecked,
            "tighteningNotes" to check.tighteningNotes,
            // Legacy
            "nozzleCleaned" to check.nozzleCleaned,
            "bowlCleaned" to check.bowlCleaned,
            "nozzleChecked" to check.nozzleChecked,
            "vibrationChecked" to check.vibrationChecked,
            "instrumentChecked" to check.instrumentChecked,
            "bearingGreased" to check.bearingGreased,
            "couplingGreased" to check.couplingGreased,
            "nozzleBoltsTightened" to check.nozzleBoltsTightened,
            "fittingPipesTightened" to check.fittingPipesTightened,
            "beltTensionChecked" to check.beltTensionChecked,
            "comments" to check.comments,
            "lastUpdated" to System.currentTimeMillis()
        )
        firestore.collection(CILT_COLLECTION).document(docId).set(data, SetOptions.merge()).await()
    }

    suspend fun pushReliability(check: ReliabilityPmCheck) {
        val docId = "rel_${check.timestamp}"
        val data = hashMapOf<String, Any>(
            "timestamp" to check.timestamp,
            "flushingIntervalMinutes" to check.flushingIntervalMinutes,
            "waterTempCelsius" to check.waterTempCelsius,
            "hollowBearingChecked" to check.hollowBearingChecked,
            "couplingOilLeakChecked" to check.couplingOilLeakChecked,
            "vibrationChecked" to check.vibrationChecked,
            "bowlSpeedRpm" to check.bowlSpeedRpm,
            "bearingTempCelsius" to check.bearingTempCelsius,
            "unusualNoiseDetected" to check.unusualNoiseDetected,
            "comments" to check.comments,
            "lastUpdated" to System.currentTimeMillis()
        )
        firestore.collection(RELIABILITY_COLLECTION).document(docId).set(data, SetOptions.merge()).await()
    }

    suspend fun pushAbnormality(report: AbnormalityReport) {
        val docId = "ab_${report.timestamp}_${report.title.take(15).replace(" ", "_")}"
        val data = hashMapOf<String, Any>(
            "timestamp" to report.timestamp,
            "title" to report.title,
            "description" to report.description,
            "factor" to report.factor,
            "severityScore" to report.severityScore,
            "occurrenceScore" to report.occurrenceScore,
            "detectionScore" to report.detectionScore,
            "rpn" to report.rpn,
            "picName" to report.picName,
            "tagType" to report.tagType,
            "status" to report.status,
            "mechanicName" to report.mechanicName,
            "repairNotes" to report.repairNotes,
            "resolvedTimestamp" to report.resolvedTimestamp,
            "lastUpdated" to System.currentTimeMillis()
        )
        report.photoUri?.let { data["photoUri"] = it }
        firestore.collection(ABNORMALITY_COLLECTION).document(docId).set(data, SetOptions.merge()).await()
    }

    suspend fun pushMentorPairing(log: MentorPairingLog) {
        val docId = "mentor_${log.timestamp}_${log.menteeName.replace(" ", "_")}"
        val data = hashMapOf<String, Any>(
            "timestamp" to log.timestamp,
            "mentorName" to log.mentorName,
            "menteeName" to log.menteeName,
            "activityName" to log.activityName,
            "preTestScore" to log.preTestScore,
            "postTestScore" to log.postTestScore,
            "status" to log.status,
            "comments" to log.comments,
            "lastUpdated" to System.currentTimeMillis()
        )
        firestore.collection(MENTOR_COLLECTION).document(docId).set(data, SetOptions.merge()).await()
    }

    suspend fun pushVibration(log: VibrationLog) {
        val docId = "vib_${log.timestamp}_${log.operatorName.replace(" ", "_")}"
        val data = hashMapOf<String, Any>(
            "timestamp" to log.timestamp,
            "operatorName" to log.operatorName,
            "shift" to log.shift,
            "driveEndVibration" to log.driveEndVibration,
            "nonDriveEndVibration" to log.nonDriveEndVibration,
            "motorBearingVibration" to log.motorBearingVibration,
            "gearboxBearingVibration" to log.gearboxBearingVibration,
            "bowlVibration" to log.bowlVibration,
            "bearingTemp" to log.bearingTemp,
            "motorTemp" to log.motorTemp,
            "isGreased" to log.isGreased,
            "greasingStatus" to log.greasingStatus,
            "soundState" to log.soundState,
            "hasLeakage" to log.hasLeakage,
            "alarmState" to log.alarmState,
            "machineStatus" to log.machineStatus,
            "comments" to log.comments,
            "lastUpdated" to System.currentTimeMillis()
        )
        firestore.collection(VIBRATION_COLLECTION).document(docId).set(data, SetOptions.merge()).await()
    }

    suspend fun pushFlushing(log: FlushingLog) {
        val docId = "flush_${log.timestamp}_${log.unitName.replace(" ", "_")}"
        val data = hashMapOf<String, Any>(
            "timestamp" to log.timestamp,
            "operatorName" to log.operatorName,
            "shift" to log.shift,
            "unitName" to log.unitName,
            "itemsJson" to log.itemsJson,
            "completedCount" to log.completedCount,
            "totalCount" to log.totalCount,
            "notes" to log.notes,
            "lastUpdated" to System.currentTimeMillis()
        )
        firestore.collection(FLUSHING_COLLECTION).document(docId).set(data, SetOptions.merge()).await()
    }

    suspend fun deleteAbnormality(report: AbnormalityReport) {
        try {
            val docId = "ab_${report.timestamp}_${report.title.take(15).replace(" ", "_")}"
            firestore.collection(ABNORMALITY_COLLECTION).document(docId).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting abnormality from cloud", e)
        }
    }

    suspend fun deleteFlushing(log: FlushingLog) {
        try {
            val docId = "flush_${log.timestamp}_${log.unitName.replace(" ", "_")}"
            firestore.collection(FLUSHING_COLLECTION).document(docId).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error deleting flushing from cloud", e)
        }
    }
}
