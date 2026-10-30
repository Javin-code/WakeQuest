package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTestNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardAlpha = if (alarm.isEnabled) 1.0f else 0.6f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("alarm_card_${alarm.id}")
            .clickable { onEdit() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (alarm.isEnabled) NeonCyan.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alarm.isEnabled) 4.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Time and Enable/Disable Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = alarm.formattedTime(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = if (alarm.isEnabled) Color.White else Color.Gray
                    )

                    Text(
                        text = alarm.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (alarm.isEnabled) NeonCyan else Color.Gray
                    )
                }

                // Switch
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.testTag("alarm_switch_${alarm.id}"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepObsidian,
                        checkedTrackColor = NeonCyan,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = SurfaceDarkVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Repeat Days Indicator Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                for (i in 1..7) {
                    val isDayActive = alarm.isDayEnabled(i)
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    !alarm.isEnabled -> Color.Transparent
                                    isDayActive -> NeonCyan.copy(alpha = 0.25f)
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    !alarm.isEnabled -> Color.DarkGray
                                    isDayActive -> NeonCyan
                                    else -> Color.DarkGray
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNames[i - 1],
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isDayActive) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                !alarm.isEnabled -> Color.DarkGray
                                isDayActive -> Color.White
                                else -> Color.Gray
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Summary text
                Text(
                    text = alarm.getRepeatDaysSummary(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quest Badges: Challenge Type & Difficulty
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Challenge Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1B2E))
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = alarm.challengeType.iconDesc, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = alarm.challengeType.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Difficulty Badge
                val diffColor = Color(alarm.difficulty.badgeColorHex)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(diffColor.copy(alpha = 0.15f))
                        .border(1.dp, diffColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "● ${alarm.difficulty.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = diffColor
                    )
                }

                // Vibration / Sound Indicator
                if (alarm.vibrate) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDarkVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = "Vibration enabled",
                                tint = Color.LightGray,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Snooze Policy Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (alarm.snoozeAllowed) ElectricAmber.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.3f))
                        .border(
                            1.dp,
                            if (alarm.snoozeAllowed) ElectricAmber.copy(alpha = 0.5f) else Color.DarkGray,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (alarm.snoozeAllowed) "Snooze: Max ${alarm.snoozeLimit}x (${alarm.snoozeDurationMinutes}m)" else "No Snooze (Strict)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.snoozeAllowed) ElectricAmber else Color.LightGray,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Row: "Test Quest Now", Edit & Delete Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Test Alarm Now Button
                OutlinedButton(
                    onClick = onTestNow,
                    modifier = Modifier.testTag("test_alarm_${alarm.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = ElectricAmber
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricAmber.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Test Quest",
                        tint = ElectricAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Test Alarm",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_alarm_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit alarm",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_alarm_${alarm.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete alarm",
                            tint = AlertCrimson,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
