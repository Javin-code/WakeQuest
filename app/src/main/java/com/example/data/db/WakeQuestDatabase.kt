package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import com.example.data.model.WakeUpRecordEntity

class Converters {
    @TypeConverter
    fun fromChallengeType(value: ChallengeType?): String = value?.name ?: ChallengeType.MATH.name

    @TypeConverter
    fun toChallengeType(value: String?): ChallengeType =
        try {
            if (value != null) ChallengeType.valueOf(value) else ChallengeType.MATH
        } catch (_: Exception) {
            ChallengeType.MATH
        }

    @TypeConverter
    fun fromDifficulty(value: ChallengeDifficulty?): String = value?.name ?: ChallengeDifficulty.MEDIUM.name

    @TypeConverter
    fun toDifficulty(value: String?): ChallengeDifficulty =
        try {
            if (value != null) ChallengeDifficulty.valueOf(value) else ChallengeDifficulty.MEDIUM
        } catch (_: Exception) {
            ChallengeDifficulty.MEDIUM
        }
}

@Database(
    entities = [AlarmEntity::class, WakeUpRecordEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WakeQuestDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun wakeUpRecordDao(): WakeUpRecordDao

    companion object {
        @Volatile
        private var INSTANCE: WakeQuestDatabase? = null

        fun getDatabase(context: Context): WakeQuestDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WakeQuestDatabase::class.java,
                    "wakequest_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
