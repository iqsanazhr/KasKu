package com.example.kasku.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MonzoColorScheme = lightColorScheme(
    primary = MonzoTeal,
    onPrimary = Color.White,
    primaryContainer = MonzoTealLight,
    onPrimaryContainer = MonzoTealDark,
    
    secondary = MonzoCoral,
    onSecondary = Color.White,
    secondaryContainer = MonzoCoralPillLight,
    onSecondaryContainer = MonzoCoralDark,
    
    tertiary = MonzoNavy,
    onTertiary = Color.White,
    tertiaryContainer = MonzoElevated,
    onTertiaryContainer = MonzoTextPrimary,
    
    background = MonzoBackground,
    onBackground = MonzoTextPrimary,
    
    surface = MonzoSurface,
    onSurface = MonzoTextPrimary,
    surfaceVariant = MonzoElevated,
    onSurfaceVariant = MonzoTextSecondary,
    
    outline = MonzoBorder,
    outlineVariant = MonzoCardBorder
)

@Composable
fun KasKuTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = true // Dark status bar icons on light canvas
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = MonzoColorScheme,
        typography = Typography,
        content = content
    )
}