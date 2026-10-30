package com.example.data.model

enum class ChallengeType(val displayName: String, val iconDesc: String, val description: String) {
    MOVING_BUTTONS(
        displayName = "Moving Target",
        iconDesc = "🎯",
        description = "Catch and tap the moving button multiple times before it moves away."
    ),
    CAPTCHA(
        displayName = "CAPTCHA Code",
        iconDesc = "🔤",
        description = "Decipher and type the stylized alphanumeric verification code."
    ),
    MATH(
        displayName = "Math Puzzle",
        iconDesc = "🧮",
        description = "Solve mental arithmetic problems to prove cognitive alertness."
    )
}
