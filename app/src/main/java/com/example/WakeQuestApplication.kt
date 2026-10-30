package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.db.WakeQuestDatabase
import com.example.service.AlarmService

class WakeQuestApplication : Application() {

    lateinit var database: WakeQuestDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = WakeQuestDatabase.getDatabase(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AlarmService.CHANNEL_ID,
                getString(R.string.alarm_service_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_service_channel_desc)
                enableVibration(true)
                setSound(null, null)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
