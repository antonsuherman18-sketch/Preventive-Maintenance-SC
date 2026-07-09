package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CiltDao {
    @Query("SELECT * FROM cilt_checks ORDER BY timestamp DESC")
    fun getAllChecks(): Flow<List<CiltCheck>>

    @Query("SELECT * FROM cilt_checks WHERE isSynced = 0")
    suspend fun getUnsyncedChecks(): List<CiltCheck>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: CiltCheck): Long

    @Query("UPDATE cilt_checks SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE cilt_checks SET isSynced = 1")
    suspend fun markAllSynced()
}

@Dao
interface ReliabilityPmDao {
    @Query("SELECT * FROM reliability_pm_checks ORDER BY timestamp DESC")
    fun getAllChecks(): Flow<List<ReliabilityPmCheck>>

    @Query("SELECT * FROM reliability_pm_checks WHERE isSynced = 0")
    suspend fun getUnsyncedChecks(): List<ReliabilityPmCheck>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheck(check: ReliabilityPmCheck): Long

    @Query("UPDATE reliability_pm_checks SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE reliability_pm_checks SET isSynced = 1")
    suspend fun markAllSynced()
}

@Dao
interface AbnormalityDao {
    @Query("SELECT * FROM abnormality_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<AbnormalityReport>>

    @Query("SELECT * FROM abnormality_reports WHERE isSynced = 0")
    suspend fun getUnsyncedReports(): List<AbnormalityReport>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: AbnormalityReport): Long

    @Query("UPDATE abnormality_reports SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE abnormality_reports SET isSynced = 1")
    suspend fun markAllSynced()
    
    @Delete
    suspend fun deleteReport(report: AbnormalityReport)
}

@Dao
interface MentorPairingDao {
    @Query("SELECT * FROM mentor_pairing_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<MentorPairingLog>>

    @Query("SELECT * FROM mentor_pairing_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<MentorPairingLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MentorPairingLog): Long

    @Query("UPDATE mentor_pairing_logs SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE mentor_pairing_logs SET isSynced = 1")
    suspend fun markAllSynced()
}

@Dao
interface VibrationDao {
    @Query("SELECT * FROM vibration_logs ORDER BY timestamp DESC LIMIT 100")
    fun getVibrationHistory(): Flow<List<VibrationLog>>

    @Query("SELECT * FROM vibration_logs WHERE isSynced = 0")
    suspend fun getUnsyncedLogs(): List<VibrationLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: VibrationLog): Long

    @Query("UPDATE vibration_logs SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE vibration_logs SET isSynced = 1")
    suspend fun markAllSynced()
}
