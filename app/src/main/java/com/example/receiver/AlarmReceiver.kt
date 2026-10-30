package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.AlarmScheduler
import com.example.data.db.WakeQuestDatabase
import com.example.service.AlarmService
import com.example.ui.alarm.AlarmActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, -1L)
        Log.d(TAG, "AlarmReceiver triggered for alarm ID: $alarmId")

        if (alarmId == -1L) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = WakeQuestDatabase.getDatabase(context)
                val alarm = db.alarmDao().getAlarmById(alarmId)

                if (alarm != null && alarm.isEnabled) {
                    // Start Alarm Audio & Vibration Service
                    AlarmService.startAlarm(
                        context = context,
                        alarmId = alarm.id,
                        label = alarm.label,
                        challengeType = alarm.challengeType.name,
                        difficulty = alarm.difficulty.name,
                        sound = alarm.soundEnabled,
                        vibrate = alarm.vibrate,
                        snoozeAllowed = alarm.snoozeAllowed,
                        snoozeLimit = alarm.snoozeLimit,
                        snoozeCount = alarm.snoozeCount,
                        snoozeDuration = alarm.snoozeDurationMinutes
                    )

                    // Also attempt direct Activity launch
                    val activityIntent = Intent(context, AlarmActivity::class.java).apply {
                        putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
                        putExtra(AlarmService.EXTRA_LABEL, alarm.label)
                        putExtra(AlarmService.EXTRA_CHALLENGE_TYPE, alarm.challengeType.name)
                        putExtra(AlarmService.EXTRA_DIFFICULTY, alarm.difficulty.name)
                        putExtra(AlarmService.EXTRA_SNOOZE_ALLOWED, alarm.snoozeAllowed)
                        putExtra(AlarmService.EXTRA_SNOOZE_LIMIT, alarm.snoozeLimit)
                        putExtra(AlarmService.EXTRA_SNOOZE_COUNT, alarm.snoozeCount)
                        putExtra(AlarmService.EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMinutes)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    try {
                        context.startActivity(activityIntent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Direct activity start failed (full screen intent in service will handle it)", e)
                    }

                    // Schedule next repeat or disable if one-time
                    val scheduler = AlarmScheduler(context)
                    if (alarm.isRepeating()) {
                        // Calculate next trigger after current moment
                        scheduler.schedule(alarm, referenceTimeMillis = System.currentTimeMillis() + 1000L)
                    } else {
                        // Mark as disabled in DB
                        db.alarmDao().setAlarmEnabled(alarm.id, false)
                    }
                } else {
                    Log.d(TAG, "Alarm $alarmId is either not found or already disabled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling alarm trigger", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AlarmReceiver"
    }
}
