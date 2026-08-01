package com.plakaneresi.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = PlateBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE5FA),
    onPrimaryContainer = Color(0xFF001A4D),
    secondary = Color(0xFF4A5B7C),
    background = Color(0xFFF6F7FB),
    onBackground = Color(0xFF14161C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF14161C),
    surfaceVariant = Color(0xFFE6E9F2),
    onSurfaceVariant = Color(0xFF4A4F5B),
    outline = Color(0xFFB9BFCC),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C0F5),
    onPrimary = Color(0xFF002074),
    primaryContainer = Color(0xFF0F2E70),
    onPrimaryContainer = Color(0xFFDCE5FA),
    secondary = Color(0xFFB9C4DE),
    background = Color(0xFF11131A),
    onBackground = Color(0xFFE3E5EC),
    surface = Color(0xFF1A1D26),
    onSurface = Color(0xFFE3E5EC),
    surfaceVariant = Color(0xFF2A2E3A),
    onSurfaceVariant = Color(0xFFC2C6D2),
    outline = Color(0xFF565B69),
)

/**
 * No dynamic colour on purpose: the palette is built around the plate blue, and letting
 * the wallpaper repaint it would fight with the plate graphic that carries the app.
 */
@Composable
fun PlakaNeresiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    // Without this the status bar icons stay dark when the user forces dark mode, which
    // reads as a bug rather than a theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
