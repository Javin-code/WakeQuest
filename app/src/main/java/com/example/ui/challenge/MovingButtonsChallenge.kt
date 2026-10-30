package com.example.ui.challenge

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.challenge.ButtonPosition
import com.example.challenge.MovingButtonsEngine
import com.example.data.model.ChallengeDifficulty
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessNeonGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MovingButtonsChallenge(
    difficulty: ChallengeDifficulty,
    modifier: Modifier = Modifier,
    onChallengeSolved: () -> Unit
) {
    val totalTapsRequired = remember(difficulty) {
        MovingButtonsEngine.getRequiredTaps(difficulty)
    }
    val buttonSizeDp = remember(difficulty) {
        MovingButtonsEngine.getButtonSizeDp(difficulty).dp
    }
    val autoMoveInterval = remember(difficulty) {
        MovingButtonsEngine.getAutoMoveIntervalMillis(difficulty)
    }

    var tapsCompleted by remember { mutableIntStateOf(0) }
    var currentPosition by remember {
        mutableStateOf(MovingButtonsEngine.generateNewPosition())
    }
    var isSolved by remember { mutableStateOf(false) }

    val buttonScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    // Auto-movement for medium and hard difficulties
    LaunchedEffect(currentPosition, isSolved) {
        if (!isSolved && autoMoveInterval != null) {
            delay(autoMoveInterval)
            currentPosition = MovingButtonsEngine.generateNewPosition(currentPosition)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quest Header & Progress Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdsClick,
                        contentDescription = "Target Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Moving Target Challenge",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isSolved) "Challenge Complete! Awakening..." else "Tap the moving target $totalTapsRequired times to deactivate",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { tapsCompleted.toFloat() / totalTapsRequired },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isSolved) SuccessNeonGreen else NeonCyan,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Taps: $tapsCompleted / $totalTapsRequired",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSolved) SuccessNeonGreen else NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Target Arena
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.5.dp,
                    color = if (isSolved) SuccessNeonGreen.copy(alpha = 0.6f) else NeonCyan.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            val arenaWidthPx = constraints.maxWidth
            val arenaHeightPx = constraints.maxHeight
            val buttonSizePx = with(androidx.compose.ui.platform.LocalDensity.current) {
                buttonSizeDp.toPx()
            }

            if (!isSolved) {
                // Calculate pixel offset from normalized position
                val maxOffsetX = (arenaWidthPx - buttonSizePx).coerceAtLeast(0f)
                val maxOffsetY = (arenaHeightPx - buttonSizePx).coerceAtLeast(0f)

                val targetX = (currentPosition.normalizedX * maxOffsetX).roundToInt()
                val targetY = (currentPosition.normalizedY * maxOffsetY).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(targetX, targetY) }
                        .size(buttonSizeDp)
                        .scale(buttonScale.value)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(NeonCyan, ElectricAmber)
                            )
                        )
                        .border(2.dp, Color.White, CircleShape)
                        .testTag("moving_target_button")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                buttonScale.animateTo(0.8f, tween(50))
                                buttonScale.animateTo(1f, tween(100, easing = FastOutSlowInEasing))
                            }

                            val newCount = tapsCompleted + 1
                            tapsCompleted = newCount

                            if (newCount >= totalTapsRequired) {
                                isSolved = true
                                onChallengeSolved()
                            } else {
                                currentPosition = MovingButtonsEngine.generateNewPosition(currentPosition)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Target Target",
                        tint = Color.White,
                        modifier = Modifier.size(buttonSizeDp * 0.55f)
                    )
                }
            } else {
                // Victory inside arena
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = SuccessNeonGreen,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Target Neutralized!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SuccessNeonGreen
                        )
                    }
                }
            }
        }
    }
}
