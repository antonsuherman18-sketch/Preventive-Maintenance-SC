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
    val driveEndVibration: Float, // mm/s (normal < 4.5, critical > 8.8)
    val nonDriveEndVibration: Float,
    val motorVibration: Float,
    val gearBoxVibration: Float,
    val bearingTemp: Float, // C (normal < 70)
    val motorTemp: Float,
    val alarmState: String, // Normal, Warning, Critical
    val comments: String = "",
    val isSynced: Boolean = false
)
