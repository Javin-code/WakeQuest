package com.example.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.data.db.WakeQuestDatabase
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import com.example.data.model.WakeUpRecordEntity
import com.example.service.AlarmService
import com.example.ui.challenge.CaptchaChallenge
import com.example.ui.challenge.MathChallenge
import com.example.ui.challenge.MovingButtonsChallenge
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessNeonGreen
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.WakeQuestTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure system window flags to show over lockscreen and keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val alarmId = intent.getLongExtra(AlarmService.EXTRA_ALARM_ID, -1L)
        val label = intent.getStringExtra(AlarmService.EXTRA_LABEL) ?: "Wake Up Quest!"
        val challengeTypeStr = intent.getStringExtra(AlarmService.EXTRA_CHALLENGE_TYPE) ?: ChallengeType.MATH.name
        val difficultyStr = intent.getStringExtra(AlarmService.EXTRA_DIFFICULTY) ?: ChallengeDifficulty.MEDIUM.name
        val snoozeAllowed = intent.getBooleanExtra(AlarmService.EXTRA_SNOOZE_ALLOWED, true)
        val snoozeLimit = intent.getIntExtra(AlarmService.EXTRA_SNOOZE_LIMIT, 3)
        val initialSnoozeCount = intent.getIntExtra(AlarmService.EXTRA_SNOOZE_COUNT, 0)
        val snoozeDuration = intent.getIntExtra(AlarmService.EXTRA_SNOOZE_DURATION, 5)

        val challengeType = try {
            ChallengeType.valueOf(challengeTypeStr)
        } catch (_: Exception) {
            ChallengeType.MATH
        }

        val difficulty = try {
            ChallengeDifficulty.valueOf(difficultyStr)
        } catch (_: Exception) {
            ChallengeDifficulty.MEDIUM
        }

        val startTimeMillis = System.currentTimeMillis()

        setContent {
            WakeQuestTheme(darkTheme = true) {
                AlarmScreenContent(
                    alarmId = alarmId,
                    label = label,
                    challengeType = challengeType,
                    difficulty = difficulty,
                    snoozeAllowed = snoozeAllowed,
                    snoozeLimit = snoozeLimit,
                    snoozeCount = initialSnoozeCount,
                    snoozeDuration = snoozeDuration,
                    onSnoozeAlarm = {
                        // Handle snooze
                        AlarmService.stopAlarm(applicationContext)
                        val db = WakeQuestDatabase.getDatabase(applicationContext)
                        val scheduler = AlarmScheduler(applicationContext)
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            db.alarmDao().updateSnoozeCount(alarmId, initialSnoozeCount + 1)
                            scheduler.scheduleSnooze(alarmId, snoozeDuration)
                        }
                        finish()
                    },
                    onQuestSolved = {
                        // Stop sound and vibration immediately, reset snooze count, and log wake-up duration
                        val solveDurationSec = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt().coerceAtLeast(1)
                        AlarmService.stopAlarm(applicationContext)
                        val db = WakeQuestDatabase.getDatabase(applicationContext)
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            db.alarmDao().resetSnoozeCount(alarmId)
                            val record = WakeUpRecordEntity(
                                alarmId = alarmId,
                                alarmLabel = label,
                                challengeType = challengeType,
                                difficulty = difficulty,
                                timestampMillis = System.currentTimeMillis(),
                                solveDurationSeconds = solveDurationSec,
                                snoozesUsed = initialSnoozeCount
                            )
                            db.wakeUpRecordDao().insertRecord(record)
                        }
                        finish()
                    }
                )
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("Unit"))
    override fun onBackPressed() {
        // Physical back button press is ignored until the quest is solved or snoozed
    }
}

@Composable
fun AlarmScreenContent(
    alarmId: Long,
    label: String,
    challengeType: ChallengeType,
    difficulty: ChallengeDifficulty,
    snoozeAllowed: Boolean,
    snoozeLimit: Int,
    snoozeCount: Int,
    snoozeDuration: Int,
    onSnoozeAlarm: () -> Unit,
    onQuestSolved: () -> Unit
) {
    var isSolved by remember { mutableStateOf(false) }
    var currentTimeString by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val canSnooze = snoozeAllowed && snoozeCount < snoozeLimit
    val snoozesRemaining = (snoozeLimit - snoozeCount).coerceAtLeast(0)

    // Clock ticker
    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        while (true) {
            currentTimeString = timeFormat.format(Date())
            delay(1000)
        }
    }

    // Intercept back button - dismissal is locked until challenge is solved or snoozed!
    BackHandler(enabled = !isSolved) {
        // Back press intentionally ignored
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepObsidian
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Alarm Status Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSolved) SuccessNeonGreen.copy(alpha = 0.15f) else AlertCrimson.copy(alpha = 0.18f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (isSolved) SuccessNeonGreen else AlertCrimson
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isSolved) Icons.Default.CheckCircle else Icons.Default.Alarm,
                            contentDescription = "Alarm status",
                            tint = if (isSolved) SuccessNeonGreen else AlertCrimson,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSolved) "ALARM DISMISSED" else "WAKE UP QUEST ACTIVE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSolved) SuccessNeonGreen else AlertCrimson,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (currentTimeString.isNotEmpty()) currentTimeString else "07:00 AM",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )

                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Snooze Status Badge / Warning
                    if (snoozeAllowed) {
                        if (canSnooze) {
                            Text(
                                text = "Snooze available: $snoozesRemaining of $snoozeLimit remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricAmber,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = AlertCrimson,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SNOOZE LIMIT REACHED ($snoozeLimit/$snoozeLimit) — Mandatory Quest Solve!",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AlertCrimson
                                )
                            }
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Strict Mode",
                                tint = Color.LightGray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STRICT MODE: Snooze disabled. Solve quest to turn off.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            // Snooze Action Button (Visible only when allowed and limit not reached)
            if (!isSolved && canSnooze) {
                Button(
                    onClick = onSnoozeAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .height(46.dp)
                        .testTag("snooze_alarm_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricAmber,
                        contentColor = DarkNavy
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Snooze $snoozeDuration min ($snoozesRemaining left)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            if (isSolved) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Solved",
                            tint = SuccessNeonGreen,
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "🎉 Quest Completed!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessNeonGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Shutting down alarm...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Challenge View Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    when (challengeType) {
                        ChallengeType.MOVING_BUTTONS -> {
                            MovingButtonsChallenge(
                                difficulty = difficulty,
                                onChallengeSolved = {
                                    isSolved = true
                                    coroutineScope.launch {
                                        delay(1200)
                                        onQuestSolved()
                                    }
                                }
                            )
                        }
                        ChallengeType.CAPTCHA -> {
                            CaptchaChallenge(
                                difficulty = difficulty,
                                onChallengeSolved = {
                                    isSolved = true
                                    coroutineScope.launch {
                                        delay(1200)
                                        onQuestSolved()
                                    }
                                }
                            )
                        }
                        ChallengeType.MATH -> {
                            MathChallenge(
                                difficulty = difficulty,
                                onChallengeSolved = {
                                    isSolved = true
                                    coroutineScope.launch {
                                        delay(1200)
                                        onQuestSolved()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
