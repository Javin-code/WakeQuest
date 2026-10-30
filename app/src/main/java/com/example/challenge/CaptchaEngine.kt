package com.example.challenge

import com.example.data.model.ChallengeDifficulty
import kotlin.random.Random

data class CaptchaChar(
    val char: Char,
    val rotationDegrees: Float,
    val offsetY: Float,
    val scale: Float
)

data class CaptchaChallenge(
    val code: String,
    val characters: List<CaptchaChar>,
    val noiseSeed: Int,
    val isCaseSensitive: Boolean
)

object CaptchaEngine {

    // Characters that are distinct and unambiguous (excluding 0/O, 1/I/l)
    private const val EASY_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val MEDIUM_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz"
    private const val HARD_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz"

    fun generateCaptcha(difficulty: ChallengeDifficulty): CaptchaChallenge {
        val length = when (difficulty) {
            ChallengeDifficulty.EASY -> 4
            ChallengeDifficulty.MEDIUM -> 6
            ChallengeDifficulty.HARD -> 8
        }
        val pool = when (difficulty) {
            ChallengeDifficulty.EASY -> EASY_CHARS
            ChallengeDifficulty.MEDIUM -> MEDIUM_CHARS
            ChallengeDifficulty.HARD -> HARD_CHARS
        }
        val isCaseSensitive = difficulty != ChallengeDifficulty.EASY

        val sb = StringBuilder()
        val charsList = mutableListOf<CaptchaChar>()

        for (i in 0 until length) {
            val char = pool[Random.nextInt(pool.length)]
            sb.append(char)

            val rotation = Random.nextFloat() * 36f - 18f // -18 to +18 deg
            val offsetY = Random.nextFloat() * 16f - 8f // -8 to +8 dp
            val scale = 0.9f + Random.nextFloat() * 0.25f // 0.9 to 1.15

            charsList.add(CaptchaChar(char, rotation, offsetY, scale))
        }

        return CaptchaChallenge(
            code = sb.toString(),
            characters = charsList,
            noiseSeed = Random.nextInt(1000, 9999),
            isCaseSensitive = isCaseSensitive
        )
    }

    fun validateAnswer(challenge: CaptchaChallenge, input: String): Boolean {
        val trimmedInput = input.trim()
        return if (challenge.isCaseSensitive) {
            trimmedInput == challenge.code
        } else {
            trimmedInput.equals(challenge.code, ignoreCase = true)
        }
    }
}
