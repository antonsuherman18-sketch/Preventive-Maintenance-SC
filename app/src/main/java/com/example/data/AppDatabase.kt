package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CiltCheck::class,
        ReliabilityPmCheck::class,
        AbnormalityReport::class,
        MentorPairingLog::class,
        VibrationLog::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ciltDao(): CiltDao
    abstract fun reliabilityPmDao(): ReliabilityPmDao
    abstract fun abnormalityDao(): AbnormalityDao
    abstract fun mentorPairingDao(): MentorPairingDao
    abstract fun vibrationDao(): VibrationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "centrifuge_pm_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
