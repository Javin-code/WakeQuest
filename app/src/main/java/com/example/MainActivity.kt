package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.addedit.AddEditAlarmScreen
import com.example.ui.challenge.ChallengeSandboxScreen
import com.example.ui.history.WakeUpHistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.WakeQuestTheme
import com.example.ui.viewmodel.AlarmViewModel
import com.example.ui.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WakeQuestTheme(darkTheme = true) {
                // Request Notification Permission on Android 13+
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { _ -> }

                    LaunchedEffect(Unit) {
                        val hasNotificationPermission = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasNotificationPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepObsidian
                ) {
                    WakeQuestApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun WakeQuestApp(viewModel: AlarmViewModel) {
    val screenState by viewModel.currentScreen.collectAsStateWithLifecycle()
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val wakeUpRecords by viewModel.wakeUpRecords.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val canScheduleExact = viewModel.canScheduleExactAlarms()

    when (val state = screenState) {
        is ScreenState.Home -> {
            HomeScreen(
                alarms = alarms,
                canScheduleExact = canScheduleExact,
                statusMessage = statusMessage,
                onClearStatusMessage = { viewModel.clearStatusMessage() },
                onToggleAlarm = { alarm, isEnabled -> viewModel.toggleAlarm(alarm, isEnabled) },
                onEditAlarm = { alarm -> viewModel.navigateToEditAlarm(alarm) },
                onDeleteAlarm = { alarm -> viewModel.deleteAlarm(alarm) },
                onAddAlarm = { viewModel.navigateToAddAlarm() },
                onTestAlarmNow = { context, alarm -> viewModel.testAlarmNow(context, alarm) },
                onOpenChallengeSandbox = {
                    viewModel.navigateToChallengePreview(
                        com.example.data.model.ChallengeType.MATH,
                        com.example.data.model.ChallengeDifficulty.MEDIUM
                    )
                },
                onOpenHistory = { viewModel.navigateToHistory() }
            )
        }

        is ScreenState.AddEdit -> {
            AddEditAlarmScreen(
                alarmToEdit = state.alarmToEdit,
                onSaveAlarm = { id, hour, minute, label, repeatMask, isEnabled, type, diff, vibrate, sound, snoozeAllowed, snoozeLimit, snoozeDuration ->
                    viewModel.saveAlarm(
                        id = id,
                        hour = hour,
                        minute = minute,
                        label = label,
                        repeatDaysMask = repeatMask,
                        isEnabled = isEnabled,
                        challengeType = type,
                        difficulty = diff,
                        vibrate = vibrate,
                        soundEnabled = sound,
                        snoozeAllowed = snoozeAllowed,
                        snoozeLimit = snoozeLimit,
                        snoozeDurationMinutes = snoozeDuration
                    )
                },
                onDeleteAlarm = { alarm ->
                    viewModel.deleteAlarm(alarm)
                    viewModel.navigateToHome()
                },
                onNavigateBack = { viewModel.navigateToHome() },
                onPreviewChallenge = { type, diff ->
                    viewModel.navigateToChallengePreview(type, diff)
                }
            )
        }

        is ScreenState.ChallengePreview -> {
            ChallengeSandboxScreen(
                initialType = state.challengeType,
                initialDifficulty = state.difficulty,
                onNavigateBack = { viewModel.navigateToHome() }
            )
        }

        is ScreenState.History -> {
            WakeUpHistoryScreen(
                records = wakeUpRecords,
                onNavigateBack = { viewModel.navigateToHome() },
                onLogTestRecord = { type, diff, seconds, snoozes ->
                    viewModel.logTestWakeUp(type, diff, seconds, snoozes)
                },
                onClearHistory = { viewModel.clearHistory() }
            )
        }
    }
}
