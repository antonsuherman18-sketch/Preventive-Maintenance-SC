package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cilt_checks")
data class CiltCheck(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val operatorName: String,
    val nozzleCleaned: Boolean = false,
    val bowlCleaned: Boolean = false,
    val areaCleaned: Boolean = false,
    val nozzleChecked: Boolean = false,
    val vibrationChecked: Boolean = false,
    val leakChecked: Boolean = false,
    val instrumentChecked: Boolean = false,
    val bearingGreased: Boolean = false,
    val couplingGreased: Boolean = false,
    val oilLevelChecked: Boolean = false,
    val nozzleBoltsTightened: Boolean = false,
    val fittingPipesTightened: Boolean = false,
    val beltTensionChecked: Boolean = false,
    val comments: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "reliability_pm_checks")
data class ReliabilityPmCheck(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val flushingIntervalMinutes: Int = 120, // default 2 hours
    val waterTempCelsius: Float = 92f, // standard hot water 90-95 C
    val hollowBearingChecked: Boolean = false,
    val couplingOilLeakChecked: Boolean = false,
    val vibrationChecked: Boolean = false,
    val bowlSpeedRpm: Float = 1450f, // normal range
    val bearingTempCelsius: Float = 62f, // normal < 70 C
    val unusualNoiseDetected: Boolean = false,
    val comments: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "abnormality_reports")
data class AbnormalityReport(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val factor: String, // MAN, METHOD, MACHINE, ENVIRONMENT
    val severityScore: Int = 1, // 1-10
    val occurrenceScore: Int = 1, // 1-10
    val detectionScore: Int = 1, // 1-10
    val rpn: Int = severityScore * occurrenceScore * detectionScore,
    val photoUri: String? = null, // Local photo path if captured
    val picName: String = "Anton Suherman",
    val tagType: String = "White Tag", // White Tag, Yellow Tag, None
    val isSynced: Boolean = false
)

@Entity(tableName = "mentor_pairing_logs")
data class MentorPairingLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val mentorName: String = "Anton Suherman",
    val menteeName: String,
    val activityName: String, // e.g. "Cek Kebocoran Nozzle", "Vibration Checking"
    val preTestScore: Int,
    val postTestScore: Int,
    val status: String, // Passed, Needs Practice
    val comments: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "vibration_logs")
data class VibrationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val operatorName: String = "",
    val shift: String = "Shift Siang", // Shift Siang atau Shift Malam
    val driveEndVibration: Float, // mm/s (normal < 4.5, critical > 8.8)
    val nonDriveEndVibration: Float,
    val motorBearingVibration: Float = 0f,
    val gearboxBearingVibration: Float = 0f,
    val bowlVibration: Float = 0f,
    val bearingTemp: Float, // C (normal < 70)
    val motorTemp: Float = 0f,
    val isGreased: Boolean = false,
    val soundState: String = "Normal", // Normal, Abnormal
    val hasLeakage: Boolean = false,
    val alarmState: String, // Normal, Warning, Critical
    val machineStatus: String = "Operasi", // Operasi, Standby, Rusak
    val comments: String = "",
    val isSynced: Boolean = false
)

@Entity(tableName = "flushing_logs")
data class FlushingLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val operatorName: String,
    val shift: String = "Shift Pagi", // Shift Pagi atau Shift Malam
    val unitName: String = "SC 01",
    val itemsJson: String = "",
    val completedCount: Int = 11,
    val totalCount: Int = 11,
    val notes: String = "",
    val isSynced: Boolean = false
)

data class FlushingTaskItem(
    val no: Int,
    val tahapanGroup: String, // "Persiapan" atau "Operation"
    val tahapanName: String,
    val specificAction: String,
    val criteria: String,
    var time: String = "08:00",
    var isDone: Boolean = true
)

fun defaultFlushingTasks(defaultTime: String = "08:00"): List<FlushingTaskItem> {
    return listOf(
        FlushingTaskItem(1, "Persiapan", "APD", "APD lengkap (helm, sarung tangan, sepatu safety, kacamata)", "Lengkap & baik", defaultTime, true),
        FlushingTaskItem(2, "Persiapan", "Cek Nozzle Holder", "Cek Nozzle holder terpasang baik", "Tidak ada kebocoran", defaultTime, true),
        FlushingTaskItem(3, "Persiapan", "Cek Area", "Pastika area sekitar centrifuge bersih", "Tidak ada gangguan", defaultTime, true),
        FlushingTaskItem(4, "Operation", "Buka Kran Air Panas", "Buka penuh kran air panas", "Terbuka penuh", defaultTime, true),
        FlushingTaskItem(5, "Operation", "Tutup Umpan Sludge", "Pastikan kran umpan sludge tertutup", "Tertutup rapat", defaultTime, true),
        FlushingTaskItem(6, "Operation", "Isi Bowl", "Biarkan bowl terisi air panas", "Getaran normal", defaultTime, true),
        FlushingTaskItem(7, "Operation", "Monitor Pengisian", "Buka top inspection cover, pastikan air keluar dari nozzle paling atas", "Air keluar", defaultTime, true),
        FlushingTaskItem(8, "Operation", "Tutup Cover", "Tutup kembali top inspection cover", "Tertutup rapat", defaultTime, true),
        FlushingTaskItem(9, "Operation", "Start Mesin", "Air keluar dari outlet light phase", "Air keluar", defaultTime, true),
        FlushingTaskItem(10, "Operation", "Stop Flushing", "Stop flushing ketika cairan dari outlet heavy phase berupa air bersih", "Air bersih", defaultTime, true),
        FlushingTaskItem(11, "Operation", "Siap Operasi", "Centrifuge siap mengolah sludge", "Normal", defaultTime, true)
    )
}

fun flushingTasksToJson(items: List<FlushingTaskItem>): String {
    val array = org.json.JSONArray()
    items.forEach { item ->
        val obj = org.json.JSONObject()
        obj.put("no", item.no)
        obj.put("tahapanGroup", item.tahapanGroup)
        obj.put("tahapanName", item.tahapanName)
        obj.put("specificAction", item.specificAction)
        obj.put("criteria", item.criteria)
        obj.put("time", item.time)
        obj.put("isDone", item.isDone)
        array.put(obj)
    }
    return array.toString()
}

fun jsonToFlushingTasks(jsonStr: String): List<FlushingTaskItem> {
    if (jsonStr.isBlank()) return defaultFlushingTasks()
    return try {
        val array = org.json.JSONArray(jsonStr)
        val list = mutableListOf<FlushingTaskItem>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                FlushingTaskItem(
                    no = obj.optInt("no", i + 1),
                    tahapanGroup = obj.optString("tahapanGroup", if (i < 3) "Persiapan" else "Operation"),
                    tahapanName = obj.optString("tahapanName", ""),
                    specificAction = obj.optString("specificAction", ""),
                    criteria = obj.optString("criteria", ""),
                    time = obj.optString("time", "08:00"),
                    isDone = obj.optBoolean("isDone", true)
                )
            )
        }
        list
    } catch (e: Exception) {
        defaultFlushingTasks()
    }
}
