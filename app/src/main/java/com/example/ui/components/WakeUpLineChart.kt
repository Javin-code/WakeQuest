package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WakeUpRecordEntity
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessNeonGreen
import com.example.ui.theme.SurfaceDark
import kotlin.math.roundToInt

@Composable
fun WakeUpLineChart(
    records: List<WakeUpRecordEntity>,
    modifier: Modifier = Modifier
) {
    if (records.isEmpty()) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavy)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No wake-up data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
        return
    }

    var selectedIndex by remember(records) {
        mutableIntStateOf(records.lastIndex)
    }

    val selectedRecord = records.getOrNull(selectedIndex) ?: records.last()

    // Smooth animation on data load
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(records) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, tween(600))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wake_up_line_chart"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Chart Title & Selected Point Tooltip Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WAKE-UP RESPONSE TIME",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricAmber,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Seconds to solve challenge",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Interactive Tooltip Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F2338))
                        .border(1.dp, NeonCyan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${selectedRecord.solveDurationSeconds}s",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${selectedRecord.formattedDate()})",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line Chart (Recharts-inspired smooth gradient curve with gridlines)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(records) {
                            detectTapGestures { offset ->
                                val pointSpacing = size.width / (records.size + 1)
                                val tappedIndex = ((offset.x / pointSpacing) - 0.5f)
                                    .roundToInt()
                                    .coerceIn(0, records.lastIndex)
                                selectedIndex = tappedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 35f
                    val paddingRight = 35f
                    val paddingTop = 25f
                    val paddingBottom = 45f

                    val usableWidth = width - paddingLeft - paddingRight
                    val usableHeight = height - paddingTop - paddingBottom

                    val maxSeconds = (records.maxOfOrNull { it.solveDurationSeconds } ?: 60)
                        .coerceAtLeast(40)
                    val yMax = ((maxSeconds + 10) / 10) * 10f // Rounded to nearest 10

                    // 1. Draw horizontal gridlines and Y-axis labels
                    val ySteps = 4
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        isAntiAlias = true
                    }

                    for (i in 0..ySteps) {
                        val yRatio = i.toFloat() / ySteps
                        val yPos = paddingTop + (usableHeight * (1f - yRatio))
                        val value = (yMax * yRatio).toInt()

                        // Dotted grid line
                        drawLine(
                            color = Color(0xFF1E2C42),
                            start = Offset(paddingLeft, yPos),
                            end = Offset(width - paddingRight, yPos),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )

                        // Y-axis text
                        drawContext.canvas.nativeCanvas.drawText(
                            "${value}s",
                            4f,
                            yPos + 8f,
                            textPaint
                        )
                    }

                    // 2. Compute point coordinates
                    val pointSpacing = usableWidth / (records.size - 1).coerceAtLeast(1)
                    val points = records.mapIndexed { index, record ->
                        val x = paddingLeft + (index * pointSpacing)
                        val normalizedY = (record.solveDurationSeconds / yMax).coerceIn(0f, 1f)
                        val y = paddingTop + usableHeight * (1f - (normalizedY * animationProgress.value))
                        Offset(x, y)
                    }

                    // 3. Build smooth cubic bezier curve
                    if (points.isNotEmpty()) {
                        val path = Path()
                        val fillPath = Path()

                        path.moveTo(points.first().x, points.first().y)
                        fillPath.moveTo(points.first().x, paddingTop + usableHeight)
                        fillPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                            path.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                            fillPath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                        }

                        fillPath.lineTo(points.last().x, paddingTop + usableHeight)
                        fillPath.close()

                        // Gradient Area Fill (Recharts style)
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                startY = paddingTop,
                                endY = paddingTop + usableHeight
                            )
                        )

                        // Glowing Line Stroke
                        drawPath(
                            path = path,
                            color = NeonCyan,
                            style = Stroke(width = 3.5f)
                        )

                        // 4. Draw interactive circular markers
                        points.forEachIndexed { idx, point ->
                            val isCurrent = idx == selectedIndex
                            val record = records[idx]

                            // Outer glow circle
                            drawCircle(
                                color = if (isCurrent) NeonCyan else Color.White,
                                radius = if (isCurrent) 7.dp.toPx() else 4.dp.toPx(),
                                center = point
                            )

                            // Inner dot
                            drawCircle(
                                color = if (isCurrent) ElectricAmber else DarkNavy,
                                radius = if (isCurrent) 4.dp.toPx() else 2.5f.dp.toPx(),
                                center = point
                            )

                            // X-axis day label
                            drawContext.canvas.nativeCanvas.drawText(
                                record.formattedDayOfWeek(),
                                point.x - 18f,
                                height - 8f,
                                textPaint
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Point Detail Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceDark
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedRecord.challengeType.iconDesc,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "${selectedRecord.challengeType.displayName} (${selectedRecord.difficulty.displayName})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${selectedRecord.formattedDate()} at ${selectedRecord.formattedTime()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${selectedRecord.solveDurationSeconds} sec",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selectedRecord.solveDurationSeconds <= 30) SuccessNeonGreen else ElectricAmber
                        )
                        Text(
                            text = if (selectedRecord.snoozesUsed == 0) "Zero Snooze ✨" else "${selectedRecord.snoozesUsed} snooze(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedRecord.snoozesUsed == 0) SuccessNeonGreen else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
