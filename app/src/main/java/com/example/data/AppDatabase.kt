package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CiltCheck::class,
        ReliabilityPmCheck::class,
        AbnormalityReport::class,
        MentorPairingLog::class,
        VibrationLog::class,
        FlushingLog::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ciltDao(): CiltDao
    abstract fun reliabilityPmDao(): ReliabilityPmDao
    abstract fun abnormalityDao(): AbnormalityDao
    abstract fun mentorPairingDao(): MentorPairingDao
    abstract fun vibrationDao(): VibrationDao
    abstract fun flushingDao(): FlushingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vibration_logs ADD COLUMN greasingStatus TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN machineCleaned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN drainageCleaned INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN cleaningNotes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN vibrationSoundChecked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN temperatureChecked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN componentsConditionChecked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN inspectionNotes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN greasingBearingChecked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN lubricationNotes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN foundationBoltsTightened INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN noLooseBoltsChecked INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cilt_checks ADD COLUMN tighteningNotes TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "centrifuge_pm_database"
                )
                .addMigrations(MIGRATION_5_6, MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
