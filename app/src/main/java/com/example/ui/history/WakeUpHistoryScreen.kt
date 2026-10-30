package com.example.ui.history

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import com.example.data.model.WakeUpRecordEntity
import com.example.ui.components.WakeUpLineChart
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessNeonGreen
import com.example.ui.theme.SurfaceDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WakeUpHistoryScreen(
    records: List<WakeUpRecordEntity>,
    onNavigateBack: () -> Unit,
    onLogTestRecord: (ChallengeType, ChallengeDifficulty, Int, Int) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onNavigateBack)

    var showClearDialog by remember { mutableStateOf(false) }
    var showSimulateDialog by remember { mutableStateOf(false) }
    var selectedFilterType by remember { mutableStateOf<ChallengeType?>(null) }

    val filteredRecords = remember(records, selectedFilterType) {
        if (selectedFilterType == null) {
            records
        } else {
            records.filter { it.challengeType == selectedFilterType }
        }
    }

    // Statistical calculations
    val totalSessions = filteredRecords.size
    val averageSolveSec = if (filteredRecords.isNotEmpty()) {
        filteredRecords.map { it.solveDurationSeconds }.average().roundToInt()
    } else 0
    val fastestSolveSec = if (filteredRecords.isNotEmpty()) {
        filteredRecords.minOf { it.solveDurationSeconds }
    } else 0
    val zeroSnoozePercent = if (filteredRecords.isNotEmpty()) {
        val zeroCount = filteredRecords.count { it.snoozesUsed == 0 }
        ((zeroCount.toFloat() / filteredRecords.size) * 100).roundToInt()
    } else 0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wake-Up Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (records.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier.testTag("clear_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepObsidian
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary KPI Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiStatCard(
                        title = "Avg Solve Time",
                        value = "${averageSolveSec}s",
                        subtitle = if (averageSolveSec <= 20) "⚡ Sharp wake-up" else "⏰ Gradual wake-up",
                        icon = Icons.Default.HourglassBottom,
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Fastest Quest",
                        value = "${fastestSolveSec}s",
                        subtitle = "Personal best",
                        icon = Icons.Default.FlashOn,
                        accentColor = SuccessNeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiStatCard(
                        title = "Zero-Snooze Rate",
                        value = "$zeroSnoozePercent%",
                        subtitle = "Immediate rise",
                        icon = Icons.Default.CheckCircle,
                        accentColor = ElectricAmber,
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatCard(
                        title = "Total Wake-Ups",
                        value = "$totalSessions",
                        subtitle = "Quests conquered",
                        icon = Icons.Default.History,
                        accentColor = Color(0xFFB388FF),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Line Chart Visualization Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SOLVE DURATION TREND",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = "Time (seconds) to solve quest each morning",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Filter Chips Row
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilterType == null,
                                onClick = { selectedFilterType = null },
                                label = { Text("All (${records.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonCyan,
                                    selectedLabelColor = DarkNavy,
                                    containerColor = SurfaceDark,
                                    labelColor = Color.LightGray
                                )
                            )
                            ChallengeType.entries.forEach { type ->
                                val count = records.count { it.challengeType == type }
                                FilterChip(
                                    selected = selectedFilterType == type,
                                    onClick = { selectedFilterType = if (selectedFilterType == type) null else type },
                                    label = { Text("${type.displayName} ($count)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan,
                                        selectedLabelColor = DarkNavy,
                                        containerColor = SurfaceDark,
                                        labelColor = Color.LightGray
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // The Line Chart
                        WakeUpLineChart(
                            records = filteredRecords,
                            modifier = Modifier.testTag("wake_up_line_chart")
                        )
                    }
                }
            }

            // Quick Simulate & Test Wake-Up Button
            item {
                OutlinedButton(
                    onClick = { showSimulateDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("simulate_wakeup_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonCyan
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(NeonCyan.copy(alpha = 0.5f))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAlert,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simulate / Log Wake-Up Session",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Recent Sessions Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOGGED SESSIONS (${filteredRecords.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.2.sp
                    )
                }
            }

            if (filteredRecords.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No wake-up records match the filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                items(
                    items = filteredRecords.sortedByDescending { it.timestampMillis },
                    key = { it.id }
                ) { record ->
                    WakeUpRecordCard(record = record)
                }
            }
        }
    }

    // Confirmation dialog for clearing history
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Wake-Up History?") },
            text = { Text("This will permanently remove all logged wake-up and challenge duration records.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = AlertCrimson)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = DarkNavy,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }

    // Dialog for simulating a wake-up session
    if (showSimulateDialog) {
        SimulateWakeUpDialog(
            onDismiss = { showSimulateDialog = false },
            onConfirm = { type, diff, seconds, snoozes ->
                onLogTestRecord(type, diff, seconds, snoozes)
                showSimulateDialog = false
            }
        )
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun WakeUpRecordCard(
    record: WakeUpRecordEntity,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()) }
    val dateStr = remember(record.timestampMillis) { dateFormat.format(Date(record.timestampMillis)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (record.challengeType) {
                                ChallengeType.MATH -> NeonCyan.copy(alpha = 0.15f)
                                ChallengeType.CAPTCHA -> ElectricAmber.copy(alpha = 0.15f)
                                ChallengeType.MOVING_BUTTONS -> SuccessNeonGreen.copy(alpha = 0.15f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (record.challengeType) {
                            ChallengeType.MATH -> "∑"
                            ChallengeType.CAPTCHA -> "🔤"
                            ChallengeType.MOVING_BUTTONS -> "🎯"
                        },
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = record.alarmLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${record.challengeType.displayName} • ${record.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.solveDurationSeconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (record.solveDurationSeconds <= 20) SuccessNeonGreen else ElectricAmber,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        tint = if (record.snoozesUsed == 0) SuccessNeonGreen else Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (record.snoozesUsed == 0) "0 snoozes" else "${record.snoozesUsed} snooze(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (record.snoozesUsed == 0) SuccessNeonGreen else Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulateWakeUpDialog(
    onDismiss: () -> Unit,
    onConfirm: (ChallengeType, ChallengeDifficulty, Int, Int) -> Unit
) {
    var selectedType by remember { mutableStateOf(ChallengeType.MATH) }
    var selectedDiff by remember { mutableStateOf(ChallengeDifficulty.MEDIUM) }
    var solveSeconds by remember { mutableFloatStateOf(24f) }
    var snoozes by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Wake-Up Session") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add a simulated morning solve to preview how the chart visualizes wake-up duration trends.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )

                // Challenge Type Selector
                Text(
                    text = "Challenge Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChallengeType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = DarkNavy,
                                containerColor = SurfaceDark,
                                labelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Solve Duration Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Solve Duration",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${solveSeconds.roundToInt()} seconds",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = solveSeconds,
                    onValueChange = { solveSeconds = it },
                    valueRange = 5f..120f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = SurfaceDark
                    )
                )

                // Snooze Count Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Snoozes Used: $snoozes",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.LightGray
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0, 1, 2, 3).forEach { count ->
                            FilterChip(
                                selected = snoozes == count,
                                onClick = { snoozes = count },
                                label = { Text("$count") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricAmber,
                                    selectedLabelColor = DarkNavy,
                                    containerColor = SurfaceDark,
                                    labelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedType, selectedDiff, solveSeconds.roundToInt(), snoozes)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = DarkNavy
                )
            ) {
                Text("Add Record", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = DarkNavy,
        titleContentColor = Color.White
    )
}
