package com.example

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.AlarmScheduler
import com.example.data.db.WakeQuestDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import com.example.data.model.WakeUpRecordEntity
import com.example.receiver.AlarmReceiver
import com.example.receiver.BootReceiver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("WakeQuest", appName)
    }

    @Test
    fun `alarm scheduler schedules exact alarm in AlarmManager and cancels properly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager: ShadowAlarmManager = shadowOf(alarmManager)
        val scheduler = AlarmScheduler(context)

        val alarm = AlarmEntity(
            id = 42L,
            hour = 8,
            minute = 30,
            repeatDaysMask = 0, // Once
            isEnabled = true,
            challengeType = ChallengeType.MATH
        )

        // 1. Schedule alarm
        scheduler.schedule(alarm)

        val scheduledAlarm = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(scheduledAlarm)
        assertEquals(AlarmManager.RTC_WAKEUP, scheduledAlarm!!.type)
        assertTrue(scheduledAlarm.triggerAtTime > System.currentTimeMillis())

        // 2. Cancel alarm
        scheduler.cancel(alarm.id)
        val nextAfterCancel = shadowAlarmManager.nextScheduledAlarm
        assertNull(nextAfterCancel)
    }

    @Test
    fun `repeat scheduling calculates correct next trigger for specific days`() {
        // Reference time: Monday, Oct 26, 2026, 10:00 AM
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.OCTOBER, 26, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val referenceMillis = cal.timeInMillis

        // Alarm set for 07:00 AM on Wednesday and Friday (days 3 and 5)
        val alarm = AlarmEntity(
            hour = 7,
            minute = 0,
            repeatDaysMask = AlarmEntity.createMask(setOf(3, 5)),
            isEnabled = true
        )

        val nextTrigger = AlarmScheduler.calculateNextTriggerMillis(alarm, referenceMillis)
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextTrigger }

        // Next occurrence must be Wednesday (Calendar.WEDNESDAY = 4) at 07:00 AM
        assertEquals(Calendar.WEDNESDAY, resultCal.get(Calendar.DAY_OF_WEEK))
        assertEquals(7, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
        assertTrue(nextTrigger > referenceMillis)
    }

    @Test
    fun `boot receiver reschedules active alarms after reboot`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = WakeQuestDatabase.getDatabase(context)
        val dao = db.alarmDao()

        // Clean db
        for (a in dao.getAllAlarmsList()) {
            dao.deleteAlarm(a)
        }

        // Insert active alarm and inactive alarm
        val activeAlarmId = dao.insertAlarm(
            AlarmEntity(
                hour = 6,
                minute = 30,
                isEnabled = true,
                challengeType = ChallengeType.CAPTCHA
            )
        )
        dao.insertAlarm(
            AlarmEntity(
                hour = 9,
                minute = 0,
                isEnabled = false,
                challengeType = ChallengeType.MATH
            )
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager: ShadowAlarmManager = shadowOf(alarmManager)

        // Clear any previous scheduled alarms in shadow
        while (shadowAlarmManager.nextScheduledAlarm != null) {
            // Drain
        }

        // Send BOOT_COMPLETED broadcast
        val bootReceiver = BootReceiver()
        val bootIntent = Intent(Intent.ACTION_BOOT_COMPLETED)
        bootReceiver.onReceive(context, bootIntent)

        // Give coroutine brief moment to run on IO
        Thread.sleep(300)

        // Verify alarm was re-scheduled in AlarmManager
        val scheduled = shadowAlarmManager.nextScheduledAlarm
        assertNotNull(scheduled)
        assertEquals(AlarmManager.RTC_WAKEUP, scheduled!!.type)
    }

    @Test
    fun `alarm persistence survives application restart using Room`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Session 1: Create and save alarm into persistent database
        val db1 = Room.databaseBuilder(context, WakeQuestDatabase::class.java, "test_wakequest.db")
            .allowMainThreadQueries()
            .build()

        val dao1 = db1.alarmDao()
        // Ensure starting clean
        for (a in dao1.getAllAlarmsList()) {
            dao1.deleteAlarm(a)
        }

        val newAlarm = AlarmEntity(
            hour = 6,
            minute = 45,
            label = "Morning Workout Quest",
            repeatDaysMask = AlarmEntity.createMask(setOf(1, 3, 5)), // Mon, Wed, Fri
            isEnabled = true,
            challengeType = ChallengeType.MATH,
            difficulty = ChallengeDifficulty.HARD,
            vibrate = true,
            soundEnabled = true
        )

        val generatedId = dao1.insertAlarm(newAlarm)
        assertTrue(generatedId > 0L)

        // Close db1 to simulate app process termination
        db1.close()

        // 2. Session 2: Reopen database (simulating app relaunch)
        val db2 = Room.databaseBuilder(context, WakeQuestDatabase::class.java, "test_wakequest.db")
            .allowMainThreadQueries()
            .build()

        val dao2 = db2.alarmDao()
        val restoredAlarm = dao2.getAlarmById(generatedId)
        assertNotNull(restoredAlarm)
        assertEquals(generatedId, restoredAlarm!!.id)
        assertEquals(6, restoredAlarm.hour)
        assertEquals(45, restoredAlarm.minute)
        assertEquals("Morning Workout Quest", restoredAlarm.label)
        assertEquals(ChallengeType.MATH, restoredAlarm.challengeType)
        assertEquals(ChallengeDifficulty.HARD, restoredAlarm.difficulty)
        assertEquals(true, restoredAlarm.isEnabled)
        assertEquals(true, restoredAlarm.vibrate)
        assertEquals(true, restoredAlarm.soundEnabled)
        assertTrue(restoredAlarm.isDayEnabled(1)) // Monday
        assertTrue(restoredAlarm.isDayEnabled(3)) // Wednesday
        assertTrue(restoredAlarm.isDayEnabled(5)) // Friday
        assertEquals(false, restoredAlarm.isDayEnabled(2)) // Tuesday

        // 3. Edit alarm in session 2
        val editedAlarm = restoredAlarm.copy(
            hour = 7,
            minute = 15,
            label = "Updated Quest Time",
            challengeType = ChallengeType.MOVING_BUTTONS,
            difficulty = ChallengeDifficulty.MEDIUM
        )
        dao2.updateAlarm(editedAlarm)

        // Close db2 to simulate another restart
        db2.close()

        // 4. Session 3: Verify updated values survive restart
        val db3 = Room.databaseBuilder(context, WakeQuestDatabase::class.java, "test_wakequest.db")
            .allowMainThreadQueries()
            .build()
        val dao3 = db3.alarmDao()
        val updatedRestored = dao3.getAlarmById(generatedId)
        assertNotNull(updatedRestored)
        assertEquals(7, updatedRestored!!.hour)
        assertEquals(15, updatedRestored.minute)
        assertEquals("Updated Quest Time", updatedRestored.label)
        assertEquals(ChallengeType.MOVING_BUTTONS, updatedRestored.challengeType)
        assertEquals(ChallengeDifficulty.MEDIUM, updatedRestored.difficulty)

        // 5. Test Snooze count update and reset in Room
        dao3.updateSnoozeCount(generatedId, 2)
        val snoozedAlarm = dao3.getAlarmById(generatedId)
        assertNotNull(snoozedAlarm)
        assertEquals(2, snoozedAlarm!!.snoozeCount)

        dao3.resetSnoozeCount(generatedId)
        val resetAlarm = dao3.getAlarmById(generatedId)
        assertNotNull(resetAlarm)
        assertEquals(0, resetAlarm!!.snoozeCount)

        // 6. Test Enable/Disable toggle in Room
        dao3.setAlarmEnabled(generatedId, false)
        val toggled = dao3.getAlarmById(generatedId)
        assertNotNull(toggled)
        assertEquals(false, toggled!!.isEnabled)

        // 7. Test Deletion in Room
        dao3.deleteAlarm(toggled)
        val deleted = dao3.getAlarmById(generatedId)
        assertNull(deleted)

        db3.close()
    }

    @Test
    fun `wake_up_records_persist_and_calculate_trends_accurately`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, WakeQuestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val recordDao = db.wakeUpRecordDao()

        assertEquals(0, recordDao.getRecordCount())

        // Insert 3 records
        recordDao.insertRecord(
            WakeUpRecordEntity(
                alarmId = 1L,
                alarmLabel = "Early Bird",
                challengeType = ChallengeType.MATH,
                difficulty = ChallengeDifficulty.MEDIUM,
                timestampMillis = 1000L,
                solveDurationSeconds = 25,
                snoozesUsed = 0
            )
        )
        recordDao.insertRecord(
            WakeUpRecordEntity(
                alarmId = 1L,
                alarmLabel = "Early Bird",
                challengeType = ChallengeType.CAPTCHA,
                difficulty = ChallengeDifficulty.HARD,
                timestampMillis = 2000L,
                solveDurationSeconds = 35,
                snoozesUsed = 1
            )
        )
        recordDao.insertRecord(
            WakeUpRecordEntity(
                alarmId = 2L,
                alarmLabel = "Weekend Quest",
                challengeType = ChallengeType.MOVING_BUTTONS,
                difficulty = ChallengeDifficulty.EASY,
                timestampMillis = 3000L,
                solveDurationSeconds = 15,
                snoozesUsed = 0
            )
        )

        assertEquals(3, recordDao.getRecordCount())
        val records = recordDao.getAllRecordsList()
        assertEquals(3, records.size)

        // Verify statistical metrics
        val avgSolve = records.map { it.solveDurationSeconds }.average()
        assertEquals(25.0, avgSolve, 0.01)

        val fastest = records.minOf { it.solveDurationSeconds }
        assertEquals(15, fastest)

        val zeroSnoozes = records.count { it.snoozesUsed == 0 }
        assertEquals(2, zeroSnoozes)

        // Verify clear
        recordDao.clearAllRecords()
        assertEquals(0, recordDao.getRecordCount())

        db.close()
    }
}
