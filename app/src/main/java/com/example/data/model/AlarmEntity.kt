package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int, // 0..23
    val minute: Int, // 0..59
    val label: String = "Wake Up Quest",
    val repeatDaysMask: Int = 0, // Bitmask: bit 0 = Mon, ..., bit 6 = Sun
    val isEnabled: Boolean = true,
    val challengeType: ChallengeType = ChallengeType.MATH,
    val difficulty: ChallengeDifficulty = ChallengeDifficulty.MEDIUM,
    val vibrate: Boolean = true,
    val soundEnabled: Boolean = true,
    val snoozeAllowed: Boolean = true,
    val snoozeLimit: Int = 3, // Maximum snooze attempts before mandatory quest solve
    val snoozeDurationMinutes: Int = 5,
    val snoozeCount: Int = 0 // Number of times snoozed during current session
) {
    fun isRepeating(): Boolean = repeatDaysMask > 0

    fun canSnooze(): Boolean = snoozeAllowed && snoozeCount < snoozeLimit

    fun remainingSnoozes(): Int = if (snoozeAllowed) (snoozeLimit - snoozeCount).coerceAtLeast(0) else 0

    fun isDayEnabled(dayOfWeek1To7: Int): Boolean {
        if (dayOfWeek1To7 !in 1..7) return false
        val bit = 1 shl (dayOfWeek1To7 - 1)
        return (repeatDaysMask and bit) != 0
    }

    fun formattedTime(is24Hour: Boolean = false): String {
        return if (is24Hour) {
            String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        } else {
            val hourIn12 = when {
                hour == 0 -> 12
                hour > 12 -> hour - 12
                else -> hour
            }
            val amPm = if (hour < 12) "AM" else "PM"
            String.format(Locale.getDefault(), "%02d:%02d %s", hourIn12, minute, amPm)
        }
    }

    fun getRepeatDaysSummary(): String {
        if (repeatDaysMask == 0) return "Once"
        if (repeatDaysMask == 127) return "Every day"
        if (repeatDaysMask == 31) return "Weekdays"
        if (repeatDaysMask == 96) return "Weekends"

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val active = mutableListOf<String>()
        for (i in 0..6) {
            if ((repeatDaysMask and (1 shl i)) != 0) {
                active.add(days[i])
            }
        }
        return active.joinToString(", ")
    }

    companion object {
        const val MONDAY = 1
        const val TUESDAY = 2
        const val WEDNESDAY = 3
        const val THURSDAY = 4
        const val FRIDAY = 5
        const val SATURDAY = 6
        const val SUNDAY = 7

        fun createMask(days: Set<Int>): Int {
            var mask = 0
            for (day in days) {
                if (day in 1..7) {
                    mask = mask or (1 shl (day - 1))
                }
            }
            return mask
        }

        fun getSelectedDays(mask: Int): Set<Int> {
            val result = mutableSetOf<Int>()
            for (day in 1..7) {
                if ((mask and (1 shl (day - 1))) != 0) {
                    result.add(day)
                }
            }
            return result
        }
    }
}
