package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.AlarmScheduler
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import com.example.data.repository.AlarmRepository
import com.example.service.AlarmService
import com.example.ui.alarm.AlarmActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import com.example.data.model.WakeUpRecordEntity
import kotlinx.coroutines.launch

sealed interface ScreenState {
    data object Home : ScreenState
    data class AddEdit(val alarmToEdit: AlarmEntity? = null) : ScreenState
    data class ChallengePreview(
        val challengeType: ChallengeType,
        val difficulty: ChallengeDifficulty
    ) : ScreenState
    data object History : ScreenState
}

class AlarmViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AlarmRepository(application)
    private val scheduler = AlarmScheduler(application)

    val alarms: StateFlow<List<AlarmEntity>> = repository.allAlarms
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val wakeUpRecords: StateFlow<List<WakeUpRecordEntity>> = repository.allWakeUpRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentScreen = MutableStateFlow<ScreenState>(ScreenState.Home)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedHistoryIfEmpty()
        }
    }

    fun navigateToHome() {
        _currentScreen.value = ScreenState.Home
    }

    fun navigateToAddAlarm() {
        _currentScreen.value = ScreenState.AddEdit(null)
    }

    fun navigateToEditAlarm(alarm: AlarmEntity) {
        _currentScreen.value = ScreenState.AddEdit(alarm)
    }

    fun navigateToHistory() {
        _currentScreen.value = ScreenState.History
    }

    fun navigateToChallengePreview(challengeType: ChallengeType, difficulty: ChallengeDifficulty) {
        _currentScreen.value = ScreenState.ChallengePreview(challengeType, difficulty)
    }

    fun logTestWakeUp(
        challengeType: ChallengeType,
        difficulty: ChallengeDifficulty,
        solveDurationSeconds: Int,
        snoozesUsed: Int
    ) {
        viewModelScope.launch {
            val record = WakeUpRecordEntity(
                alarmId = 0L,
                alarmLabel = "Morning Quest",
                challengeType = challengeType,
                difficulty = difficulty,
                timestampMillis = System.currentTimeMillis(),
                solveDurationSeconds = solveDurationSeconds,
                snoozesUsed = snoozesUsed
            )
            repository.logWakeUpRecord(record)
            _statusMessage.value = "Logged wake-up record: ${solveDurationSeconds}s"
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _statusMessage.value = "Wake-up history cleared"
        }
    }

    fun saveAlarm(
        id: Long = 0,
        hour: Int,
        minute: Int,
        label: String,
        repeatDaysMask: Int,
        isEnabled: Boolean,
        challengeType: ChallengeType,
        difficulty: ChallengeDifficulty,
        vibrate: Boolean,
        soundEnabled: Boolean,
        snoozeAllowed: Boolean = true,
        snoozeLimit: Int = 3,
        snoozeDurationMinutes: Int = 5
    ) {
        val safeHour = hour.coerceIn(0, 23)
        val safeMinute = minute.coerceIn(0, 59)
        val safeLabel = label.trim().ifEmpty { "Wake Up Quest" }

        viewModelScope.launch {
            // Check for duplicate alarm
            val duplicate = repository.findDuplicateAlarm(safeHour, safeMinute, repeatDaysMask, excludeId = id)
            if (duplicate != null) {
                val timeStr = duplicate.formattedTime()
                val scheduleStr = duplicate.getRepeatDaysSummary()
                _statusMessage.value = "Duplicate alarm: An alarm for $timeStr ($scheduleStr) already exists."
                return@launch
            }

            val entity = AlarmEntity(
                id = id,
                hour = safeHour,
                minute = safeMinute,
                label = safeLabel,
                repeatDaysMask = repeatDaysMask,
                isEnabled = isEnabled,
                challengeType = challengeType,
                difficulty = difficulty,
                vibrate = vibrate,
                soundEnabled = soundEnabled,
                snoozeAllowed = snoozeAllowed,
                snoozeLimit = snoozeLimit,
                snoozeDurationMinutes = snoozeDurationMinutes
            )

            repository.saveAlarm(entity)
            _statusMessage.value = "Alarm saved (${entity.formattedTime()})"
            _currentScreen.value = ScreenState.Home
        }
    }

    fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAlarm(alarm, isEnabled)
            if (isEnabled) {
                val nextMillis = AlarmScheduler.calculateNextTriggerMillis(alarm.copy(isEnabled = true))
                val timeUntil = AlarmScheduler.getTimeUntil(nextMillis)
                _statusMessage.value = "Alarm set for $timeUntil"
            } else {
                _statusMessage.value = "Alarm turned off"
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
            _statusMessage.value = "Alarm deleted"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun canScheduleExactAlarms(): Boolean = repository.canScheduleExactAlarms()

    fun testAlarmNow(context: Context, alarm: AlarmEntity) {
        // Starts alarm service and launches full screen alarm directly
        AlarmService.startAlarm(
            context = context,
            alarmId = alarm.id,
            label = "[TEST] " + alarm.label,
            challengeType = alarm.challengeType.name,
            difficulty = alarm.difficulty.name,
            sound = alarm.soundEnabled,
            vibrate = alarm.vibrate,
            snoozeAllowed = alarm.snoozeAllowed,
            snoozeLimit = alarm.snoozeLimit,
            snoozeCount = alarm.snoozeCount,
            snoozeDuration = alarm.snoozeDurationMinutes
        )

        val intent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(AlarmService.EXTRA_ALARM_ID, alarm.id)
            putExtra(AlarmService.EXTRA_LABEL, "[TEST] " + alarm.label)
            putExtra(AlarmService.EXTRA_CHALLENGE_TYPE, alarm.challengeType.name)
            putExtra(AlarmService.EXTRA_DIFFICULTY, alarm.difficulty.name)
            putExtra(AlarmService.EXTRA_SNOOZE_ALLOWED, alarm.snoozeAllowed)
            putExtra(AlarmService.EXTRA_SNOOZE_LIMIT, alarm.snoozeLimit)
            putExtra(AlarmService.EXTRA_SNOOZE_COUNT, alarm.snoozeCount)
            putExtra(AlarmService.EXTRA_SNOOZE_DURATION, alarm.snoozeDurationMinutes)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    }
}
