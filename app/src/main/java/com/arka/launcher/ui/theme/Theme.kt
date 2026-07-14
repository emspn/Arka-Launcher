package com.arka.launcher.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Copper,
    secondary = CopperLight,
    tertiary = Ember,
    background = Bg0,
    surface = Bg1,
    onPrimary = Bg0,
    onSecondary = Bg0,
    onTertiary = Bg0,
    onBackground = Ink,
    onSurface = Ink,
    surfaceVariant = Bg2,
    onSurfaceVariant = InkMute,
    outline = Carve
)

@Composable
fun ArkaTheme(
    darkTheme: Boolean = true, // Force dark theme for sandstone by default
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
