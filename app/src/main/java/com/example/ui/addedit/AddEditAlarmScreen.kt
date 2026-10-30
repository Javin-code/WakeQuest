package com.example.ui.addedit

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditAlarmScreen(
    alarmToEdit: AlarmEntity?,
    onSaveAlarm: (
        id: Long,
        hour: Int,
        minute: Int,
        label: String,
        repeatDaysMask: Int,
        isEnabled: Boolean,
        challengeType: ChallengeType,
        difficulty: ChallengeDifficulty,
        vibrate: Boolean,
        soundEnabled: Boolean,
        snoozeAllowed: Boolean,
        snoozeLimit: Int,
        snoozeDurationMinutes: Int
    ) -> Unit,
    onDeleteAlarm: (AlarmEntity) -> Unit,
    onNavigateBack: () -> Unit,
    onPreviewChallenge: (ChallengeType, ChallengeDifficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    // Initial state setup
    var hour by remember { mutableIntStateOf(alarmToEdit?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(alarmToEdit?.minute ?: 0) }
    var label by remember { mutableStateOf(alarmToEdit?.label ?: "Wake Up Quest") }
    var selectedDays by remember {
        mutableStateOf(
            if (alarmToEdit != null) AlarmEntity.getSelectedDays(alarmToEdit.repeatDaysMask)
            else setOf(1, 2, 3, 4, 5) // Default to weekdays
        )
    }
    var challengeType by remember {
        mutableStateOf(alarmToEdit?.challengeType ?: ChallengeType.MATH)
    }
    var difficulty by remember {
        mutableStateOf(alarmToEdit?.difficulty ?: ChallengeDifficulty.MEDIUM)
    }
    var soundEnabled by remember { mutableStateOf(alarmToEdit?.soundEnabled ?: true) }
    var vibrate by remember { mutableStateOf(alarmToEdit?.vibrate ?: true) }
    var isEnabled by remember { mutableStateOf(alarmToEdit?.isEnabled ?: true) }
    var snoozeAllowed by remember { mutableStateOf(alarmToEdit?.snoozeAllowed ?: true) }
    var snoozeLimit by remember { mutableIntStateOf(alarmToEdit?.snoozeLimit ?: 3) }
    var snoozeDurationMinutes by remember { mutableIntStateOf(alarmToEdit?.snoozeDurationMinutes ?: 5) }

    val scrollState = rememberScrollState()

    // 12-hour calculation helpers
    val isPm = hour >= 12
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (alarmToEdit == null) "New Alarm Quest" else "Edit Alarm Quest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val mask = AlarmEntity.createMask(selectedDays)
                            onSaveAlarm(
                                alarmToEdit?.id ?: 0L,
                                hour,
                                minute,
                                label,
                                mask,
                                isEnabled,
                                challengeType,
                                difficulty,
                                vibrate,
                                soundEnabled,
                                snoozeAllowed,
                                snoozeLimit,
                                snoozeDurationMinutes
                            )
                        },
                        modifier = Modifier.testTag("save_alarm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Alarm",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepObsidian
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Interactive Time Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkNavy),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SELECT ALARM TIME",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricAmber,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Time display & adjusters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    hour = if (hour == 23) 0 else hour + 1
                                },
                                modifier = Modifier.testTag("hour_plus_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Hour Plus", tint = NeonCyan)
                            }

                            Text(
                                text = String.format(Locale.getDefault(), "%02d", hour12),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color.White
                            )

                            IconButton(
                                onClick = {
                                    hour = if (hour == 0) 23 else hour - 1
                                },
                                modifier = Modifier.testTag("hour_minus_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Hour Minus", tint = NeonCyan)
                            }
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black
                            ),
                            color = NeonCyan,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Minute column
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(
                                onClick = {
                                    minute = if (minute >= 55) 0 else ((minute / 5) * 5) + 5
                                },
                                modifier = Modifier.testTag("minute_plus_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Minute Plus", tint = NeonCyan)
                            }

                            Text(
                                text = String.format(Locale.getDefault(), "%02d", minute),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                ),
                                color = Color.White
                            )

                            IconButton(
                                onClick = {
                                    minute = if (minute <= 4) 55 else ((minute / 5) * 5) - 5
                                },
                                modifier = Modifier.testTag("minute_minus_button")
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Minute Minus", tint = NeonCyan)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // AM / PM Toggle Column
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isPm) hour -= 12
                                    },
                                color = if (!isPm) NeonCyan else SurfaceDarkVariant
                            ) {
                                Text(
                                    text = "AM",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isPm) DarkNavy else Color.Gray
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (!isPm) hour += 12
                                    },
                                color = if (isPm) NeonCyan else SurfaceDarkVariant
                            ) {
                                Text(
                                    text = "PM",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPm) DarkNavy else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Alarm Label
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Alarm Label",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        placeholder = { Text("e.g. Work, Workout, Study...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alarm_label_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }

            // Section 3: Repeat Days Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Repeat Schedule",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            "Once" to emptySet<Int>(),
                            "Daily" to setOf(1, 2, 3, 4, 5, 6, 7),
                            "Weekdays" to setOf(1, 2, 3, 4, 5),
                            "Weekends" to setOf(6, 7)
                        )

                        for ((title, days) in presets) {
                            val isSelected = selectedDays == days
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDays = days },
                                label = { Text(title, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = DarkNavy
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Individual day circles (1..7: Mon..Sun)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf(
                            "Mon" to 1, "Tue" to 2, "Wed" to 3,
                            "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7
                        )

                        for ((name, id) in days) {
                            val isDaySelected = id in selectedDays
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (isDaySelected) NeonCyan else SurfaceDarkVariant)
                                    .border(
                                        width = 1.dp,
                                        color = if (isDaySelected) NeonCyan else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedDays = if (isDaySelected) {
                                            selectedDays - id
                                        } else {
                                            selectedDays + id
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.take(2),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDaySelected) DarkNavy else Color.LightGray
                                )
                            }
                        }
                    }
                }
            }

            // Section 4: Challenge Type Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wake-up Quest Challenge",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Must be solved to silence and dismiss this alarm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    for (type in ChallengeType.entries) {
                        val isCurrent = challengeType == type
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(
                                    width = if (isCurrent) 1.5.dp else 1.dp,
                                    color = if (isCurrent) NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { challengeType = type }
                                .testTag("challenge_option_${type.name}"),
                            color = if (isCurrent) DarkNavy else SurfaceDarkVariant,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = type.iconDesc, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) NeonCyan else Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = type.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preview Challenge Button
                    OutlinedButton(
                        onClick = { onPreviewChallenge(challengeType, difficulty) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("preview_challenge_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = "Preview",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Preview This Challenge Now")
                    }
                }
            }

            // Section 5: Challenge Difficulty Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Challenge Difficulty",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (diff in ChallengeDifficulty.entries) {
                            val isSelected = difficulty == diff
                            val diffColor = Color(diff.badgeColorHex)

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) diffColor else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { difficulty = diff }
                                    .testTag("difficulty_option_${diff.name}"),
                                color = if (isSelected) diffColor.copy(alpha = 0.2f) else SurfaceDarkVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = diff.displayName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) diffColor else Color.LightGray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (diff) {
                                            ChallengeDifficulty.EASY -> "Light wake"
                                            ChallengeDifficulty.MEDIUM -> "Normal alertness"
                                            ChallengeDifficulty.HARD -> "Heavy sleeper"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 6: Snooze Settings & Limits
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Allow Snoozing",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (snoozeAllowed) "Snooze enabled with strict cap" else "Strict mode: Zero snooze",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = snoozeAllowed,
                            onCheckedChange = { snoozeAllowed = it },
                            modifier = Modifier.testTag("snooze_allow_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = ElectricAmber
                            )
                        )
                    }

                    if (snoozeAllowed) {
                        Spacer(modifier = Modifier.height(14.dp))

                        // Snooze Limit Selector
                        Text(
                            text = "Snooze Limit (Mandatory challenge after cap)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricAmber
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val limits = listOf(1, 2, 3, 5)
                            for (limit in limits) {
                                val isSelected = snoozeLimit == limit
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { snoozeLimit = limit },
                                    label = { Text("$limit ${if (limit == 1) "snooze" else "snoozes"}", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ElectricAmber,
                                        selectedLabelColor = DarkNavy
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("snooze_limit_$limit")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Snooze Duration Selector
                        Text(
                            text = "Snooze Duration",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val durations = listOf(3, 5, 10, 15)
                            for (duration in durations) {
                                val isSelected = snoozeDurationMinutes == duration
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { snoozeDurationMinutes = duration },
                                    label = { Text("$duration min", fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = DarkNavy
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("snooze_duration_$duration")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 You can snooze up to $snoozeLimit times. After $snoozeLimit snoozes, the alarm locks into mandatory quest solve mode with no snooze option.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1528))
                                .border(1.dp, AlertCrimson.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🔒 Strict Wake-Up: Zero Snooze. You must solve the challenge to turn off the alarm when it rings.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Section 7: Sound & Vibration Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Sound",
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Alarm Audio Siren", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        }
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Vibration",
                                tint = ElectricAmber,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Continuous Vibration", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        }
                        Switch(
                            checked = vibrate,
                            onCheckedChange = { vibrate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = DeepObsidian,
                                checkedTrackColor = ElectricAmber
                            )
                        )
                    }
                }
            }

            // Save Alarm Button
            Button(
                onClick = {
                    val mask = AlarmEntity.createMask(selectedDays)
                    onSaveAlarm(
                        alarmToEdit?.id ?: 0L,
                        hour,
                        minute,
                        label,
                        mask,
                        isEnabled,
                        challengeType,
                        difficulty,
                        vibrate,
                        soundEnabled,
                        snoozeAllowed,
                        snoozeLimit,
                        snoozeDurationMinutes
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_alarm_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DarkNavy
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (alarmToEdit == null) "Create Alarm Quest" else "Save Changes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Delete Alarm Button (if editing existing alarm)
            if (alarmToEdit != null) {
                OutlinedButton(
                    onClick = { onDeleteAlarm(alarmToEdit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("delete_current_alarm_button"),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AlertCrimson
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AlertCrimson),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Alarm", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
