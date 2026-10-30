package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.alarm.AudioAndVibrationManager
import com.example.ui.alarm.AlarmActivity

class AlarmService : Service() {

    private var audioAndVibrationManager: AudioAndVibrationManager? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        audioAndVibrationManager = AudioAndVibrationManager(this)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        @Suppress("DEPRECATION")
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "wakequest:AlarmServiceWakeLock"
        ).apply {
            setReferenceCounted(false)
            acquire(10 * 60 * 1000L) // 10 minutes max safety limit
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_ALARM) {
            Log.d(TAG, "Stopping AlarmService")
            stopAlarmPlayback()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(EXTRA_LABEL) ?: "Wake Up Quest!"
        val soundEnabled = intent?.getBooleanExtra(EXTRA_SOUND, true) ?: true
        val vibrateEnabled = intent?.getBooleanExtra(EXTRA_VIBRATE, true) ?: true
        val challengeType = intent?.getStringExtra(EXTRA_CHALLENGE_TYPE) ?: "MATH"
        val difficulty = intent?.getStringExtra(EXTRA_DIFFICULTY) ?: "MEDIUM"
        val snoozeAllowed = intent?.getBooleanExtra(EXTRA_SNOOZE_ALLOWED, true) ?: true
        val snoozeLimit = intent?.getIntExtra(EXTRA_SNOOZE_LIMIT, 3) ?: 3
        val snoozeCount = intent?.getIntExtra(EXTRA_SNOOZE_COUNT, 0) ?: 0
        val snoozeDuration = intent?.getIntExtra(EXTRA_SNOOZE_DURATION, 5) ?: 5

        currentActiveAlarmId = alarmId

        createNotificationChannel()
        val notification = createNotification(
            alarmId,
            label,
            challengeType,
            difficulty,
            snoozeAllowed,
            snoozeLimit,
            snoozeCount,
            snoozeDuration
        )

        try {
            startForeground(NOTIFICATION_ID, notification)
        } catch (e: Exception) {
            Log.e(TAG, "Failed startForeground", e)
        }

        audioAndVibrationManager?.start(enableSound = soundEnabled, enableVibration = vibrateEnabled)

        return START_STICKY
    }

    private fun stopAlarmPlayback() {
        audioAndVibrationManager?.stop()
        currentActiveAlarmId = null
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wakeLock", e)
        }
    }

    override fun onDestroy() {
        stopAlarmPlayback()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.alarm_service_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.alarm_service_channel_desc)
                enableVibration(true)
                setSound(null, null) // Sound is managed via AudioAndVibrationManager for looping
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(
        alarmId: Long,
        label: String,
        challengeType: String,
        difficulty: String,
        snoozeAllowed: Boolean,
        snoozeLimit: Int,
        snoozeCount: Int,
        snoozeDuration: Int
    ): Notification {
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            putExtra(EXTRA_LABEL, label)
            putExtra(EXTRA_CHALLENGE_TYPE, challengeType)
            putExtra(EXTRA_DIFFICULTY, difficulty)
            putExtra(EXTRA_SNOOZE_ALLOWED, snoozeAllowed)
            putExtra(EXTRA_SNOOZE_LIMIT, snoozeLimit)
            putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
            putExtra(EXTRA_SNOOZE_DURATION, snoozeDuration)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ $label")
            .setContentText("Solve the $challengeType challenge to dismiss!")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()
    }

    companion object {
        const val TAG = "AlarmService"
        const val CHANNEL_ID = "wakequest_alarm_channel"
        const val NOTIFICATION_ID = 2026

        const val ACTION_START_ALARM = "com.example.wakequest.START_ALARM"
        const val ACTION_STOP_ALARM = "com.example.wakequest.STOP_ALARM"

        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_CHALLENGE_TYPE = "extra_challenge_type"
        const val EXTRA_DIFFICULTY = "extra_difficulty"
        const val EXTRA_SOUND = "extra_sound"
        const val EXTRA_VIBRATE = "extra_vibrate"
        const val EXTRA_SNOOZE_ALLOWED = "extra_snooze_allowed"
        const val EXTRA_SNOOZE_LIMIT = "extra_snooze_limit"
        const val EXTRA_SNOOZE_COUNT = "extra_snooze_count"
        const val EXTRA_SNOOZE_DURATION = "extra_snooze_duration"

        @Volatile
        var currentActiveAlarmId: Long? = null

        fun startAlarm(
            context: Context,
            alarmId: Long,
            label: String,
            challengeType: String,
            difficulty: String,
            sound: Boolean,
            vibrate: Boolean,
            snoozeAllowed: Boolean = true,
            snoozeLimit: Int = 3,
            snoozeCount: Int = 0,
            snoozeDuration: Int = 5
        ) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_START_ALARM
                putExtra(EXTRA_ALARM_ID, alarmId)
                putExtra(EXTRA_LABEL, label)
                putExtra(EXTRA_CHALLENGE_TYPE, challengeType)
                putExtra(EXTRA_DIFFICULTY, difficulty)
                putExtra(EXTRA_SOUND, sound)
                putExtra(EXTRA_VIBRATE, vibrate)
                putExtra(EXTRA_SNOOZE_ALLOWED, snoozeAllowed)
                putExtra(EXTRA_SNOOZE_LIMIT, snoozeLimit)
                putExtra(EXTRA_SNOOZE_COUNT, snoozeCount)
                putExtra(EXTRA_SNOOZE_DURATION, snoozeDuration)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAlarm(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(intent)
        }
    }
}
