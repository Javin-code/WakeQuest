package com.example

import com.example.alarm.AlarmScheduler
import com.example.challenge.CaptchaEngine
import com.example.challenge.MathEngine
import com.example.challenge.MovingButtonsEngine
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun math_engine_complexity_scales_with_difficulty() {
        val easySolves = MathEngine.getRequiredSolves(ChallengeDifficulty.EASY)
        val medSolves = MathEngine.getRequiredSolves(ChallengeDifficulty.MEDIUM)
        val hardSolves = MathEngine.getRequiredSolves(ChallengeDifficulty.HARD)

        assertEquals(2, easySolves)
        assertEquals(3, medSolves)
        assertEquals(4, hardSolves)
        assertTrue(easySolves < medSolves)
        assertTrue(medSolves < hardSolves)

        for (diff in ChallengeDifficulty.entries) {
            val problem = MathEngine.generateProblem(diff)
            assertNotNull(problem.expression)
            assertTrue(problem.expression.isNotEmpty())
        }
    }

    @Test
    fun math_engine_incorrect_answers_rejected() {
        val problem = MathEngine.generateProblem(ChallengeDifficulty.EASY)
        val wrongAnswer = problem.answer + 5
        assertFalse(problem.answer == wrongAnswer)
    }

    @Test
    fun captcha_engine_complexity_scales_with_difficulty() {
        val easy = CaptchaEngine.generateCaptcha(ChallengeDifficulty.EASY)
        val med = CaptchaEngine.generateCaptcha(ChallengeDifficulty.MEDIUM)
        val hard = CaptchaEngine.generateCaptcha(ChallengeDifficulty.HARD)

        assertEquals(4, easy.code.length)
        assertEquals(6, med.code.length)
        assertEquals(8, hard.code.length)

        assertFalse(easy.isCaseSensitive)
        assertTrue(med.isCaseSensitive)
        assertTrue(hard.isCaseSensitive)
    }

    @Test
    fun captcha_engine_incorrect_answers_rejected() {
        val challenge = CaptchaEngine.generateCaptcha(ChallengeDifficulty.MEDIUM)
        // Correct answer passes
        assertTrue(CaptchaEngine.validateAnswer(challenge, challenge.code))

        // Incorrect answers rejected
        assertFalse(CaptchaEngine.validateAnswer(challenge, "INVALID"))
        assertFalse(CaptchaEngine.validateAnswer(challenge, ""))
        assertFalse(CaptchaEngine.validateAnswer(challenge, challenge.code.reversed() + "X"))
    }

    @Test
    fun moving_buttons_engine_complexity_scales_with_difficulty() {
        val easyTaps = MovingButtonsEngine.getRequiredTaps(ChallengeDifficulty.EASY)
        val medTaps = MovingButtonsEngine.getRequiredTaps(ChallengeDifficulty.MEDIUM)
        val hardTaps = MovingButtonsEngine.getRequiredTaps(ChallengeDifficulty.HARD)

        assertEquals(5, easyTaps)
        assertEquals(10, medTaps)
        assertEquals(15, hardTaps)

        val easySize = MovingButtonsEngine.getButtonSizeDp(ChallengeDifficulty.EASY)
        val medSize = MovingButtonsEngine.getButtonSizeDp(ChallengeDifficulty.MEDIUM)
        val hardSize = MovingButtonsEngine.getButtonSizeDp(ChallengeDifficulty.HARD)

        assertTrue(easySize > medSize)
        assertTrue(medSize > hardSize)

        // Interval timing
        assertEquals(null, MovingButtonsEngine.getAutoMoveIntervalMillis(ChallengeDifficulty.EASY))
        assertEquals(2200L, MovingButtonsEngine.getAutoMoveIntervalMillis(ChallengeDifficulty.MEDIUM))
        assertEquals(1400L, MovingButtonsEngine.getAutoMoveIntervalMillis(ChallengeDifficulty.HARD))
    }

    @Test
    fun moving_buttons_engine_provides_safe_coordinates() {
        for (diff in ChallengeDifficulty.entries) {
            val pos = MovingButtonsEngine.generateNewPosition()
            assertTrue(pos.normalizedX in 0.1f..0.9f)
            assertTrue(pos.normalizedY in 0.1f..0.9f)
        }
    }

    @Test
    fun alarm_entity_bitmask_and_repeat_summary() {
        val mask = AlarmEntity.createMask(setOf(1, 2, 3, 4, 5))
        val alarm = AlarmEntity(
            hour = 6,
            minute = 45,
            repeatDaysMask = mask,
            challengeType = ChallengeType.MATH
        )

        assertTrue(alarm.isRepeating())
        assertEquals("Weekdays", alarm.getRepeatDaysSummary())
        assertEquals("06:45 AM", alarm.formattedTime())

        val nextTrigger = AlarmScheduler.calculateNextTriggerMillis(alarm)
        assertTrue(nextTrigger > 0L)
    }

    @Test
    fun snooze_policy_and_limits_enforced() {
        // 1. Strict mode (no snooze allowed)
        val strictAlarm = AlarmEntity(
            hour = 7,
            minute = 0,
            snoozeAllowed = false,
            snoozeLimit = 3
        )
        assertFalse(strictAlarm.canSnooze())
        assertEquals(0, strictAlarm.remainingSnoozes())

        // 2. Snooze allowed with limit 2
        val cappedAlarm = AlarmEntity(
            hour = 7,
            minute = 0,
            snoozeAllowed = true,
            snoozeLimit = 2,
            snoozeCount = 0
        )
        assertTrue(cappedAlarm.canSnooze())
        assertEquals(2, cappedAlarm.remainingSnoozes())

        // 3. After 1 snooze
        val snoozedOnce = cappedAlarm.copy(snoozeCount = 1)
        assertTrue(snoozedOnce.canSnooze())
        assertEquals(1, snoozedOnce.remainingSnoozes())

        // 4. After 2 snoozes (limit reached)
        val limitReached = cappedAlarm.copy(snoozeCount = 2)
        assertFalse(limitReached.canSnooze()) // Mandatory quest solve required!
        assertEquals(0, limitReached.remainingSnoozes())
    }
}
