package com.example.ui.challenge

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChallengeDifficulty
import com.example.data.model.ChallengeType
import com.example.ui.theme.DarkNavy
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.ElectricAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SuccessNeonGreen
import com.example.ui.theme.SurfaceDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengeSandboxScreen(
    initialType: ChallengeType = ChallengeType.MATH,
    initialDifficulty: ChallengeDifficulty = ChallengeDifficulty.MEDIUM,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onNavigateBack() }

    var selectedType by remember { mutableStateOf(initialType) }
    var selectedDifficulty by remember { mutableStateOf(initialDifficulty) }
    var refreshKey by remember { mutableStateOf(0) }
    var solvedState by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Challenge Practice Lab",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("sandbox_back_button")
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
                            solvedState = false
                            refreshKey++
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Challenge Type selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (type in ChallengeType.entries) {
                    val isSelected = selectedType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedType = type
                            solvedState = false
                            refreshKey++
                        },
                        label = { Text("${type.iconDesc} ${type.displayName}", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = DarkNavy
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Difficulty selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (diff in ChallengeDifficulty.entries) {
                    val isSelected = selectedDifficulty == diff
                    val diffColor = Color(diff.badgeColorHex)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedDifficulty = diff
                            solvedState = false
                            refreshKey++
                        },
                        label = { Text(diff.displayName, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = diffColor,
                            selectedLabelColor = DarkNavy
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Challenge Interactive Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (solvedState) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        color = SurfaceDark
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Solved",
                                tint = SuccessNeonGreen,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Challenge Passed!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuccessNeonGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "You proved your cognitive alertness.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedButton(
                                onClick = {
                                    solvedState = false
                                    refreshKey++
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Try Again")
                            }
                        }
                    }
                } else {
                    androidx.compose.runtime.key(selectedType, selectedDifficulty, refreshKey) {
                        when (selectedType) {
                            ChallengeType.MOVING_BUTTONS -> {
                                MovingButtonsChallenge(
                                    difficulty = selectedDifficulty,
                                    onChallengeSolved = { solvedState = true }
                                )
                            }
                            ChallengeType.CAPTCHA -> {
                                CaptchaChallenge(
                                    difficulty = selectedDifficulty,
                                    onChallengeSolved = { solvedState = true }
                                )
                            }
                            ChallengeType.MATH -> {
                                MathChallenge(
                                    difficulty = selectedDifficulty,
                                    onChallengeSolved = { solvedState = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
