package com.example.ariana.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PlumPrimary,
    onPrimary = InkDark,
    secondary = EmeraldAccent,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceDeepDark,
    onBackground = InkDark,
    onSurface = InkDark,
    outline = LineDark
)

private val LightColorScheme = lightColorScheme(
    primary = PlumPrimary,
    onPrimary = SurfaceLight,
    secondary = EmeraldAccent,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceDeepLight,
    onBackground = InkLight,
    onSurface = InkLight,
    outline = LineLight
)

@Composable
fun ArianaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
