package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WakeQuestDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DeepObsidian,
    primaryContainer = SurfaceDarkVariant,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricAmber,
    onSecondary = DeepObsidian,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = ElectricAmber,
    tertiary = ElectricPurple,
    onTertiary = DeepObsidian,
    background = DeepObsidian,
    onBackground = TextPrimary,
    surface = DarkNavy,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = BorderDark,
    error = AlertCrimson,
    onError = TextPrimary
)

// Clean high-contrast dark theme prioritized for alarm clock clarity
@Composable
fun WakeQuestTheme(
    darkTheme: Boolean = true, // Default to sleek dark theme for alarm clock
    content: @Composable () -> Unit
) {
    val colorScheme = WakeQuestDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DeepObsidian.toArgb()
                window.navigationBarColor = DeepObsidian.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Retain alias for backwards compatibility with template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    WakeQuestTheme(darkTheme = true, content = content)
}
