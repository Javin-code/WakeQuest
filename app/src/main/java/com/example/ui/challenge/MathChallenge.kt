package com.example.ui.challenge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.challenge.MathEngine
import com.example.challenge.MathProblem
import com.example.data.model.ChallengeDifficulty
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessNeonGreen

@Composable
fun MathChallenge(
    difficulty: ChallengeDifficulty,
    modifier: Modifier = Modifier,
    onChallengeSolved: () -> Unit
) {
    val totalRequired = remember(difficulty) { MathEngine.getRequiredSolves(difficulty) }
    var solvedCount by remember { mutableIntStateOf(0) }
    var currentProblem by remember(difficulty) {
        mutableStateOf(MathEngine.generateProblem(difficulty))
    }
    var currentInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSolved by remember { mutableStateOf(false) }

    fun submitAnswer() {
        val entered = currentInput.toIntOrNull()
        if (entered == null) {
            errorMessage = "Please enter a valid number"
            return
        }

        if (entered == currentProblem.answer) {
            errorMessage = null
            val newSolved = solvedCount + 1
            solvedCount = newSolved
            currentInput = ""

            if (newSolved >= totalRequired) {
                isSolved = true
                onChallengeSolved()
            } else {
                currentProblem = MathEngine.generateProblem(difficulty)
            }
        } else {
            errorMessage = "Incorrect! Answer was ${currentProblem.answer}. Try this new problem."
            currentInput = ""
            currentProblem = MathEngine.generateProblem(difficulty)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quest Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Math Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Arithmetic Quest",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { solvedCount.toFloat() / totalRequired },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (isSolved) SuccessNeonGreen else NeonCyan,
                    trackColor = MaterialTheme.colorScheme.surface
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Solved: $solvedCount / $totalRequired",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSolved) SuccessNeonGreen else NeonCyan
                )
            }
        }

        // Equation Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavy),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CALCULATE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricAmber,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${currentProblem.expression} = ?",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // User Input Display Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0A101D))
                        .border(1.dp, if (errorMessage != null) AlertCrimson else NeonCyan, RoundedCornerShape(10.dp))
                        .testTag("math_input"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentInput.isEmpty()) "Tap keypad..." else currentInput,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (currentInput.isEmpty()) Color.Gray else NeonCyan
                    )
                }
            }
        }

        // Error message
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = errorMessage.orEmpty(),
                color = AlertCrimson,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        // Dedicated Responsive On-Screen Keypad
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("-", "0", "DEL")
            )

            for (row in keyRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (key in row) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    errorMessage = null
                                    when (key) {
                                        "DEL" -> {
                                            if (currentInput.isNotEmpty()) {
                                                currentInput = currentInput.dropLast(1)
                                            }
                                        }
                                        "-" -> {
                                            if (currentInput.isEmpty()) {
                                                currentInput = "-"
                                            }
                                        }
                                        else -> {
                                            if (currentInput.length < 7) {
                                                currentInput += key
                                            }
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (key == "DEL") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (key == "DEL") {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Delete",
                                        tint = AlertCrimson
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = { submitAnswer() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("math_submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSolved) SuccessNeonGreen else NeonCyan,
                contentColor = DarkNavy
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isSolved) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quest Completed! Deactivating...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Answer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
