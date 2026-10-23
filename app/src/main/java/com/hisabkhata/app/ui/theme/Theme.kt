package com.hisabkhata.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CleanColorScheme = lightColorScheme(
    primary = PrimaryRed,
    onPrimary = SurfaceCard,
    primaryContainer = PrimaryRedLight,
    onPrimaryContainer = PrimaryRedDark,
    secondary = PrimaryGreen,
    onSecondary = SurfaceCard,
    background = SurfaceCanvas,
    surface = SurfaceCard,
    onBackground = TextHead,
    onSurface = TextHead
)

@Composable
fun HisabKhataTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CleanColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Ultra clean white status bar with dark slate icons
                @Suppress("DEPRECATION")
                window.statusBarColor = SurfaceCard.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
