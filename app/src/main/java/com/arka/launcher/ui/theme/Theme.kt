package com.arka.launcher.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class ArkaThemeColors(
    val name: String,
    val bg0: Color,
    val bg1: Color,
    val bg2: Color,
    val carve: Color,
    val copper: Color,
    val copperLight: Color,
    val ember: Color,
    val ink: Color,
    val inkMute: Color,
    val isDark: Boolean
)

val ArkaThemes = mapOf(
    "sandstone" to ArkaThemeColors(
        name = "Dark Sandstone",
        bg0 = SandstoneBg0, bg1 = SandstoneBg1, bg2 = SandstoneBg2,
        carve = SandstoneCarve, copper = SandstoneCopper, copperLight = SandstoneCopperLight,
        ember = SandstoneEmber, ink = SandstoneInk, inkMute = SandstoneInkMute,
        isDark = true
    ),
    "sunrise" to ArkaThemeColors(
        name = "Warm Sunrise",
        bg0 = SunriseBg0, bg1 = SunriseBg1, bg2 = SunriseBg2,
        carve = SunriseCarve, copper = SunriseCopper, copperLight = SunriseCopperLight,
        ember = SunriseEmber, ink = SunriseInk, inkMute = SunriseInkMute,
        isDark = false
    ),
    "warli" to ArkaThemeColors(
        name = "Warli Canvas",
        bg0 = WarliBg0, bg1 = WarliBg1, bg2 = WarliBg2,
        carve = WarliCarve, copper = WarliCopper, copperLight = WarliCopperLight,
        ember = WarliEmber, ink = WarliInk, inkMute = WarliInkMute,
        isDark = false
    ),
    "pattachitra" to ArkaThemeColors(
        name = "Pattachitra",
        bg0 = PattachitraBg0, bg1 = PattachitraBg1, bg2 = PattachitraBg2,
        carve = PattachitraCarve, copper = PattachitraCopper, copperLight = PattachitraCopperLight,
        ember = PattachitraEmber, ink = PattachitraInk, inkMute = PattachitraInkMute,
        isDark = true
    )
)

val THEME_KEYS = ArkaThemes.keys.toList()

fun ArkaThemeColors.toColorScheme(): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = copper,
            secondary = copperLight,
            tertiary = ember,
            background = bg0,
            surface = bg1,
            onPrimary = bg0,
            onSecondary = bg0,
            onTertiary = bg0,
            onBackground = ink,
            onSurface = ink,
            surfaceVariant = bg2,
            onSurfaceVariant = inkMute,
            outline = carve
        )
    } else {
        lightColorScheme(
            primary = copper,
            secondary = copperLight,
            tertiary = ember,
            background = bg0,
            surface = bg1,
            onPrimary = bg0,
            onSecondary = bg0,
            onTertiary = bg0,
            onBackground = ink,
            onSurface = ink,
            surfaceVariant = bg2,
            onSurfaceVariant = inkMute,
            outline = carve
        )
    }
}

@Composable
fun ArkaTheme(
    themeKey: String = "sandstone",
    content: @Composable () -> Unit
) {
    val theme = ArkaThemes[themeKey] ?: ArkaThemes["sandstone"]!!
    val colorScheme = theme.toColorScheme()
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !theme.isDark
            controller.isAppearanceLightNavigationBars = !theme.isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
