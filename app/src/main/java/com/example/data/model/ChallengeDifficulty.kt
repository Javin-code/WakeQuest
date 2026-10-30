package com.example.data.model

enum class ChallengeDifficulty(val displayName: String, val badgeColorHex: Long) {
    EASY("Easy", 0xFF00E676),      // Bright green
    MEDIUM("Medium", 0xFFFFB300),  // Bright amber
    HARD("Hard", 0xFFFF1744)       // Vibrant red
}
