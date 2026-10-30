package com.example.challenge

import com.example.data.model.ChallengeDifficulty
import kotlin.random.Random

data class ButtonPosition(
    val normalizedX: Float, // 0.0f .. 1.0f
    val normalizedY: Float  // 0.0f .. 1.0f
)

object MovingButtonsEngine {

    fun getRequiredTaps(difficulty: ChallengeDifficulty): Int {
        return when (difficulty) {
            ChallengeDifficulty.EASY -> 5
            ChallengeDifficulty.MEDIUM -> 10
            ChallengeDifficulty.HARD -> 15
        }
    }

    fun getButtonSizeDp(difficulty: ChallengeDifficulty): Int {
        return when (difficulty) {
            ChallengeDifficulty.EASY -> 76
            ChallengeDifficulty.MEDIUM -> 64
            ChallengeDifficulty.HARD -> 54
        }
    }

    fun getAutoMoveIntervalMillis(difficulty: ChallengeDifficulty): Long? {
        return when (difficulty) {
            ChallengeDifficulty.EASY -> null // Only moves on tap
            ChallengeDifficulty.MEDIUM -> 2200L // Moves if not tapped in 2.2s
            ChallengeDifficulty.HARD -> 1400L // Moves rapidly in 1.4s
        }
    }

    fun generateNewPosition(previousPosition: ButtonPosition? = null): ButtonPosition {
        var x: Float
        var y: Float
        var attempts = 0
        do {
            // Keep target with safe margins inside arena
            x = 0.12f + Random.nextFloat() * 0.72f
            y = 0.12f + Random.nextFloat() * 0.72f
            attempts++
        } while (previousPosition != null &&
            attempts < 10 &&
            kotlin.math.abs(x - previousPosition.normalizedX) < 0.2f &&
            kotlin.math.abs(y - previousPosition.normalizedY) < 0.2f
        )

        return ButtonPosition(x, y)
    }
}
