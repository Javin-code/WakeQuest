package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.AlarmScheduler
import com.example.data.model.AlarmEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SurfaceDark
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.ShowChart

@Composable
fun LiveClockHeader(
    alarms: List<AlarmEntity>,
    onOpenChallengeSandbox: () -> Unit,
    onOpenHistory: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }
    var secondsString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
        val secFormat = SimpleDateFormat(":ss a", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())

        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            secondsString = secFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    // Find next upcoming enabled alarm
    val nextAlarmInfo = remember(alarms, currentTimeString) {
        val enabledAlarms = alarms.filter { it.isEnabled }
        if (enabledAlarms.isEmpty()) {
            null
        } else {
            val nowMillis = System.currentTimeMillis()
            val upcoming = enabledAlarms.map { alarm ->
                val triggerMillis = AlarmScheduler.calculateNextTriggerMillis(alarm, nowMillis)
                alarm to triggerMillis
            }.minByOrNull { it.second }

            upcoming?.let { (alarm, triggerMillis) ->
                val timeUntil = AlarmScheduler.getTimeUntil(triggerMillis, nowMillis)
                "${alarm.formattedTime()} ($timeUntil)"
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceDark,
                            DarkNavy
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Current Date
                Text(
                    text = currentDateString.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = ElectricAmber
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Big Digital Clock
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currentTimeString.ifEmpty { "00:00" },
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        ),
                        color = Color.White
                    )

                    Text(
                        text = secondsString.ifEmpty { ":00 AM" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = NeonCyan,
                        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Upcoming Alarm Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0D1726))
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = "Next Alarm",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (nextAlarmInfo != null) "Next: $nextAlarmInfo" else "No active alarms",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (nextAlarmInfo != null) NeonCyan else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onOpenChallengeSandbox,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_sandbox_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = "Sandbox",
                            tint = ElectricAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Challenges",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = ElectricAmber
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_trends_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Trends",
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trends",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
            }
        }
    }
}
