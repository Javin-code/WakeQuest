package com.example.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.AlarmEntity
import com.example.receiver.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(alarm: AlarmEntity, referenceTimeMillis: Long = System.currentTimeMillis()) {
        // Cancel any previous intent first to strictly prevent duplicate triggers
        cancel(alarm.id)

        if (!alarm.isEnabled) {
            return
        }

        val triggerTimeMillis = calculateNextTriggerMillis(alarm, referenceTimeMillis)
        val pendingIntent = createPendingIntent(alarm.id)

        try {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm ${alarm.id} for $triggerTimeMillis (${getTimeUntil(triggerTimeMillis)})")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm ${alarm.id}, falling back", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Failed fallback scheduling", fallbackEx)
            }
        }
    }

    fun cancel(alarmId: Long) {
        val pendingIntent = createPendingIntent(alarmId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Cancelled alarm $alarmId")
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleSnooze(alarmId: Long, snoozeDurationMinutes: Int) {
        cancel(alarmId)
        val triggerTimeMillis = System.currentTimeMillis() + (snoozeDurationMinutes * 60 * 1000L)
        val pendingIntent = createPendingIntent(alarmId)

        try {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Snoozed alarm $alarmId for $snoozeDurationMinutes minutes until $triggerTimeMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling snooze", e)
        }
    }

    private fun createPendingIntent(alarmId: Long): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            action = "com.example.wakequest.ALARM_TRIGGER_$alarmId"
        }
        return PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val TAG = "AlarmScheduler"
        const val EXTRA_ALARM_ID = "extra_alarm_id"

        fun calculateNextTriggerMillis(alarm: AlarmEntity, referenceTimeMillis: Long = System.currentTimeMillis()): Long {
            val now = Calendar.getInstance().apply { timeInMillis = referenceTimeMillis }

            val target = Calendar.getInstance().apply {
                timeInMillis = referenceTimeMillis
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (!alarm.isRepeating()) {
                // One-time alarm
                if (target.timeInMillis <= now.timeInMillis) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
                return target.timeInMillis
            }

            // Repeating alarm: check up to 8 days ahead to find next matching day of week
            for (offset in 0..7) {
                val candidate = (target.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, offset)
                }
                val isoDay = toIsoDayOfWeek(candidate.get(Calendar.DAY_OF_WEEK))
                if (alarm.isDayEnabled(isoDay) && candidate.timeInMillis > now.timeInMillis) {
                    return candidate.timeInMillis
                }
            }

            // Fallback: 1 day later
            if (target.timeInMillis <= now.timeInMillis) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            return target.timeInMillis
        }

        private fun toIsoDayOfWeek(calendarDayOfWeek: Int): Int {
            return when (calendarDayOfWeek) {
                Calendar.MONDAY -> AlarmEntity.MONDAY
                Calendar.TUESDAY -> AlarmEntity.TUESDAY
                Calendar.WEDNESDAY -> AlarmEntity.WEDNESDAY
                Calendar.THURSDAY -> AlarmEntity.THURSDAY
                Calendar.FRIDAY -> AlarmEntity.FRIDAY
                Calendar.SATURDAY -> AlarmEntity.SATURDAY
                Calendar.SUNDAY -> AlarmEntity.SUNDAY
                else -> AlarmEntity.MONDAY
            }
        }

        fun getTimeUntil(triggerMillis: Long, currentMillis: Long = System.currentTimeMillis()): String {
            val diff = triggerMillis - currentMillis
            if (diff <= 0) return "Right now"

            val totalMinutes = diff / (1000 * 60)
            val days = totalMinutes / (60 * 24)
            val hours = (totalMinutes % (60 * 24)) / 60
            val minutes = totalMinutes % 60

            val parts = mutableListOf<String>()
            if (days > 0) parts.add("$days ${if (days == 1L) "day" else "days"}")
            if (hours > 0) parts.add("$hours ${if (hours == 1L) "hr" else "hrs"}")
            if (minutes > 0 || parts.isEmpty()) parts.add("$minutes ${if (minutes == 1L) "min" else "mins"}")

            return "in " + parts.joinToString(" ")
        }
    }
}
