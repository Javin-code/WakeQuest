package com.example.ui.challenge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.challenge.CaptchaEngine
import com.example.data.model.ChallengeDifficulty
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessNeonGreen
import kotlin.random.Random

@Composable
fun CaptchaChallenge(
    difficulty: ChallengeDifficulty,
    modifier: Modifier = Modifier,
    onChallengeSolved: () -> Unit
) {
    var challenge by remember(difficulty) {
        mutableStateOf(CaptchaEngine.generateCaptcha(difficulty))
    }
    var userInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attemptCount by remember { mutableIntStateOf(0) }
    var isSolved by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    fun verify() {
        if (userInput.isBlank()) {
            errorMessage = "Please enter the code shown above"
            return
        }

        if (CaptchaEngine.validateAnswer(challenge, userInput)) {
            errorMessage = null
            isSolved = true
            onChallengeSolved()
        } else {
            attemptCount++
            errorMessage = "Incorrect code! Generating new CAPTCHA..."
            userInput = ""
            // Generate a fresh CAPTCHA after incorrect attempt
            challenge = CaptchaEngine.generateCaptcha(difficulty)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        imageVector = Icons.Default.Password,
                        contentDescription = "Captcha Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CAPTCHA Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (challenge.isCaseSensitive) "Type the exact case-sensitive characters shown below" else "Type the characters shown below",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // CAPTCHA Display Card with Canvas Distortion
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkNavy),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A101D))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw noise wave lines
                    val rng = Random(challenge.noiseSeed)
                    for (i in 0..4) {
                        val path = Path()
                        path.moveTo(0f, rng.nextFloat() * height)
                        path.cubicTo(
                            width * 0.25f, rng.nextFloat() * height,
                            width * 0.75f, rng.nextFloat() * height,
                            width, rng.nextFloat() * height
                        )
                        val lineColor = if (i % 2 == 0) NeonCyan.copy(alpha = 0.4f) else ElectricAmber.copy(alpha = 0.4f)
                        drawPath(path, color = lineColor, style = Stroke(width = 2.5f))
                    }

                    // 2. Draw security noise dots
                    for (j in 0..30) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.15f),
                            radius = rng.nextFloat() * 3f + 1f,
                            center = Offset(rng.nextFloat() * width, rng.nextFloat() * height)
                        )
                    }

                    // 3. Draw stylized characters using native canvas
                    val chars = challenge.characters
                    val charSpacing = width / (chars.size + 1)
                    val basePaint = android.graphics.Paint().apply {
                        isAntiAlias = true
                        textSize = 80f
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.MONOSPACE,
                            android.graphics.Typeface.BOLD
                        )
                    }

                    drawContext.canvas.nativeCanvas.apply {
                        chars.forEachIndexed { index, captchaChar ->
                            save()
                            val posX = charSpacing * (index + 1)
                            val posY = (height / 2f) + captchaChar.offsetY + 24f

                            rotate(captchaChar.rotationDegrees, posX, posY)
                            scale(captchaChar.scale, captchaChar.scale, posX, posY)

                            // Alternate character hues
                            basePaint.color = when (index % 3) {
                                0 -> android.graphics.Color.rgb(0, 229, 255) // Cyan
                                1 -> android.graphics.Color.rgb(255, 145, 0) // Amber
                                else -> android.graphics.Color.WHITE
                            }

                            drawText(captchaChar.char.toString(), posX - 20f, posY, basePaint)
                            restore()
                        }
                    }
                }

                // Refresh Button
                IconButton(
                    onClick = {
                        challenge = CaptchaEngine.generateCaptcha(difficulty)
                        errorMessage = null
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "New CAPTCHA",
                        tint = NeonCyan
                    )
                }
            }
        }

        // Input Field
        OutlinedTextField(
            value = userInput,
            onValueChange = {
                userInput = it
                if (errorMessage != null) errorMessage = null
            },
            label = { Text("Enter verification code") },
            placeholder = { Text("Type code...") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("captcha_input"),
            keyboardOptions = KeyboardOptions(
                capitalization = if (challenge.isCaseSensitive) KeyboardCapitalization.None else KeyboardCapitalization.Characters,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                verify()
            }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Error Feedback
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = errorMessage.orEmpty(),
                color = AlertCrimson,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Action Buttons
        Button(
            onClick = {
                focusManager.clearFocus()
                verify()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("captcha_submit_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSolved) SuccessNeonGreen else NeonCyan,
                contentColor = DarkNavy
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isSolved) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Verified! Deactivating...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            } else {
                Text("Verify Code", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        OutlinedButton(
            onClick = {
                challenge = CaptchaEngine.generateCaptcha(difficulty)
                userInput = ""
                errorMessage = null
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate New Code")
        }
    }
}
