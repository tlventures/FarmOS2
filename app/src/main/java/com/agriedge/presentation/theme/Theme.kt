package com.agriedge.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenDark,
    secondary = GreenSecondary,
    onSecondary = Color.White,
    tertiary = GreenTertiary,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = ErrorColor
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenSecondary,
    onPrimary = GreenDark,
    primaryContainer = GreenPrimaryVariant,
    onPrimaryContainer = GreenLight,
    secondary = GreenTertiary,
    onSecondary = GreenDark,
    tertiary = GreenLight,
    background = BackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorColor
)

@Composable
fun AgriEdgeLinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
