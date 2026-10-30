package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarm.AlarmScheduler
import com.example.data.db.WakeQuestDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d(TAG, "Reboot/package replace detected. Rescheduling alarms...")
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = WakeQuestDatabase.getDatabase(context)
                    val scheduler = AlarmScheduler(context)
                    val enabledAlarms = db.alarmDao().getEnabledAlarms()

                    for (alarm in enabledAlarms) {
                        scheduler.schedule(alarm)
                        Log.d(TAG, "Rescheduled alarm ${alarm.id} (${alarm.formattedTime()})")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule alarms after boot", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
