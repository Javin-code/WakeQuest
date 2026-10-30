package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "wakeup_records")
data class WakeUpRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val alarmId: Long,
    val alarmLabel: String = "Wake Up Quest",
    val challengeType: ChallengeType = ChallengeType.MATH,
    val difficulty: ChallengeDifficulty = ChallengeDifficulty.MEDIUM,
    val timestampMillis: Long = System.currentTimeMillis(),
    val solveDurationSeconds: Int, // How long it took to solve the challenge
    val snoozesUsed: Int = 0
) {
    fun formattedDate(): String {
        val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }

    fun formattedDayOfWeek(): String {
        val sdf = SimpleDateFormat("EEE", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }

    fun formattedTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestampMillis))
    }
}
