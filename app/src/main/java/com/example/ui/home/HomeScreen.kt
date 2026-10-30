package com.example.ui.home

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.ui.components.AlarmCard
import com.example.ui.components.LiveClockHeader
import com.example.ui.components.PermissionWarningBanner
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    alarms: List<AlarmEntity>,
    canScheduleExact: Boolean,
    statusMessage: String?,
    onClearStatusMessage: () -> Unit,
    onToggleAlarm: (AlarmEntity, Boolean) -> Unit,
    onEditAlarm: (AlarmEntity) -> Unit,
    onDeleteAlarm: (AlarmEntity) -> Unit,
    onAddAlarm: () -> Unit,
    onTestAlarmNow: (Context, AlarmEntity) -> Unit,
    onOpenChallengeSandbox: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var alarmToDelete by remember { mutableStateOf<AlarmEntity?>(null) }

    LaunchedEffect(statusMessage) {
        if (!statusMessage.isNullOrEmpty()) {
            snackbarHostState.showSnackbar(statusMessage)
            onClearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Logo",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WakeQuest",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Wake-Up Trends",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepObsidian
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAlarm,
                modifier = Modifier.testTag("add_alarm_button"),
                containerColor = NeonCyan,
                contentColor = DarkNavy,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Alarm",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Item 1: Live Clock & Next Alarm Header
            item {
                LiveClockHeader(
                    alarms = alarms,
                    onOpenChallengeSandbox = onOpenChallengeSandbox,
                    onOpenHistory = onOpenHistory
                )
            }

            // Item 2: Permission Banner (if exact alarm disabled on Android 12+)
            if (!canScheduleExact) {
                item {
                    PermissionWarningBanner(canScheduleExact = canScheduleExact)
                }
            }

            // Item 3: Alarms Section Title & Counter
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCHEDULED QUESTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.5.sp
                    )

                    Text(
                        text = "${alarms.count { it.isEnabled }} active / ${alarms.size} total",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = NeonCyan
                    )
                }
            }

            // Alarms List or Empty State
            if (alarms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AlarmOff,
                                contentDescription = "No Alarms",
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No Alarms Created Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button below to set your first wake-up quest",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = alarms,
                    key = { it.id }
                ) { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { isEnabled -> onToggleAlarm(alarm, isEnabled) },
                        onEdit = { onEditAlarm(alarm) },
                        onDelete = { alarmToDelete = alarm },
                        onTestNow = { onTestAlarmNow(context, alarm) }
                    )
                }
            }

            // Bottom Spacer for Floating Action Button
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (alarmToDelete != null) {
        val target = alarmToDelete!!
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
            title = {
                Text("Delete Alarm?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Are you sure you want to delete ${target.formattedTime()} (${target.label})?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAlarm(target)
                        alarmToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete", color = AlertCrimson, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { alarmToDelete = null }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}
