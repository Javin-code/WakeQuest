package com.example.data.repository

import android.content.Context
import com.example.alarm.AlarmScheduler
import com.example.data.db.AlarmDao
import com.example.data.db.WakeQuestDatabase
import com.example.data.db.WakeUpRecordDao
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import com.example.data.model.WakeUpRecordEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class AlarmRepository(private val context: Context) {

    private val db = WakeQuestDatabase.getDatabase(context)
    private val dao: AlarmDao = db.alarmDao()
    private val recordDao: WakeUpRecordDao = db.wakeUpRecordDao()
    private val scheduler = AlarmScheduler(context)

    val allAlarms: Flow<List<AlarmEntity>> = dao.getAllAlarmsFlow()
    val allWakeUpRecords: Flow<List<WakeUpRecordEntity>> = recordDao.getAllRecordsFlow()

    suspend fun getAlarmById(id: Long): AlarmEntity? = dao.getAlarmById(id)

    suspend fun findDuplicateAlarm(hour: Int, minute: Int, repeatDaysMask: Int, excludeId: Long = 0L): AlarmEntity? {
        val match = dao.findDuplicateAlarm(hour, minute, repeatDaysMask)
        return if (match != null && match.id != excludeId) match else null
    }

    suspend fun saveAlarm(alarm: AlarmEntity): Long {
        return if (alarm.id == 0L) {
            val newId = dao.insertAlarm(alarm)
            val insertedAlarm = alarm.copy(id = newId)
            if (insertedAlarm.isEnabled) {
                scheduler.schedule(insertedAlarm)
            }
            newId
        } else {
            dao.updateAlarm(alarm)
            if (alarm.isEnabled) {
                scheduler.schedule(alarm)
            } else {
                scheduler.cancel(alarm.id)
            }
            alarm.id
        }
    }

    suspend fun toggleAlarm(alarm: AlarmEntity, isEnabled: Boolean) {
        val updated = alarm.copy(isEnabled = isEnabled)
        dao.updateAlarm(updated)
        if (isEnabled) {
            scheduler.schedule(updated)
        } else {
            scheduler.cancel(updated.id)
        }
    }

    suspend fun deleteAlarm(alarm: AlarmEntity) {
        scheduler.cancel(alarm.id)
        dao.deleteAlarm(alarm)
    }

    suspend fun snoozeAlarm(alarmId: Long, snoozeDurationMinutes: Int) {
        val alarm = dao.getAlarmById(alarmId)
        val currentCount = alarm?.snoozeCount ?: 0
        dao.updateSnoozeCount(alarmId, currentCount + 1)
        scheduler.scheduleSnooze(alarmId, snoozeDurationMinutes)
    }

    suspend fun resetSnooze(alarmId: Long) {
        dao.resetSnoozeCount(alarmId)
    }

    suspend fun logWakeUpRecord(record: WakeUpRecordEntity): Long {
        return recordDao.insertRecord(record)
    }

    suspend fun clearHistory() {
        recordDao.clearAllRecords()
    }

    suspend fun seedHistoryIfEmpty() {
        val existing = recordDao.getAllRecordsList()
        if (existing.isEmpty()) {
            val now = Calendar.getInstance()
            val sampleData = listOf(
                Pair(6, 42) to (ChallengeType.MATH to ChallengeDifficulty.MEDIUM),
                Pair(5, 35) to (ChallengeType.MOVING_BUTTONS to ChallengeDifficulty.EASY),
                Pair(4, 52) to (ChallengeType.CAPTCHA to ChallengeDifficulty.MEDIUM),
                Pair(3, 28) to (ChallengeType.MATH to ChallengeDifficulty.HARD),
                Pair(2, 31) to (ChallengeType.MOVING_BUTTONS to ChallengeDifficulty.MEDIUM),
                Pair(1, 24) to (ChallengeType.CAPTCHA to ChallengeDifficulty.HARD),
                Pair(0, 18) to (ChallengeType.MATH to ChallengeDifficulty.MEDIUM)
            )

            for ((dayOffsetAndDuration, challengeInfo) in sampleData) {
                val (daysAgo, duration) = dayOffsetAndDuration
                val (type, diff) = challengeInfo
                val cal = (now.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -daysAgo)
                    set(Calendar.HOUR_OF_DAY, 7)
                    set(Calendar.MINUTE, 15)
                }
                recordDao.insertRecord(
                    WakeUpRecordEntity(
                        alarmId = 1L,
                        alarmLabel = "Morning Wakeup",
                        challengeType = type,
                        difficulty = diff,
                        timestampMillis = cal.timeInMillis,
                        solveDurationSeconds = duration,
                        snoozesUsed = if (daysAgo == 4) 1 else 0
                    )
                )
            }
        }
    }

    fun canScheduleExactAlarms(): Boolean = scheduler.canScheduleExactAlarms()
}
