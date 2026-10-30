package com.example.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class AudioAndVibrationManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var fallbackAudioTrack: AudioTrack? = null
    private var fallbackJob: Job? = null
    private var isFallbackPlaying = false

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var isPlaying = false

    fun start(enableSound: Boolean = true, enableVibration: Boolean = true) {
        if (isPlaying) return
        isPlaying = true

        if (enableSound) {
            startAlarmSound()
        }
        if (enableVibration) {
            startVibration()
        }
    }

    fun stop() {
        if (!isPlaying) return
        isPlaying = false

        stopAlarmSound()
        stopVibration()
    }

    private fun startAlarmSound() {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                val curVol = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                if (curVol < maxVol * 0.4f) {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, (maxVol * 0.7f).toInt(), 0)
                }
            }

            var alarmUri: Uri? = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            if (alarmUri == null) {
                alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            }

            if (alarmUri != null) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    isLooping = true
                    setOnPreparedListener { it.start() }
                    setOnErrorListener { _, what, extra ->
                        Log.e(TAG, "MediaPlayer error ($what, $extra), switching to fallback tone")
                        playSynthesizedTone()
                        true
                    }
                    prepareAsync()
                }
            } else {
                playSynthesizedTone()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start default ringtone, playing fallback synthesized alarm tone", e)
            playSynthesizedTone()
        }
    }

    private fun playSynthesizedTone() {
        if (isFallbackPlaying) return
        isFallbackPlaying = true

        fallbackJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                val sampleRate = 44100
                val durationSeconds = 0.5
                val numSamples = (durationSeconds * sampleRate).toInt()
                val buffer = ShortArray(numSamples)
                val freqLow = 880.0 // A5 tone
                val freqHigh = 1200.0 // High pitch alert

                val minBufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(maxOf(minBufferSize, numSamples * 2))
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                fallbackAudioTrack = audioTrack
                audioTrack.play()

                var toggle = false
                while (isActive && isPlaying) {
                    val freq = if (toggle) freqHigh else freqLow
                    toggle = !toggle
                    for (i in 0 until numSamples) {
                        val angle = 2.0 * PI * i / (sampleRate / freq)
                        buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.8).toInt().toShort()
                    }
                    audioTrack.write(buffer, 0, numSamples)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Synthesized tone error", e)
            }
        }
    }

    private fun stopAlarmSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        } finally {
            mediaPlayer = null
        }

        try {
            fallbackJob?.cancel()
            fallbackAudioTrack?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping fallback audio track", e)
        } finally {
            fallbackJob = null
            fallbackAudioTrack = null
            isFallbackPlaying = false
        }
    }

    private fun startVibration() {
        try {
            val pattern = longArrayOf(0, 500, 300, 500, 300)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(pattern, 0) // Repeat from index 0
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting vibration", e)
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping vibration", e)
        }
    }

    companion object {
        private const val TAG = "AudioAndVibration"
    }
}
