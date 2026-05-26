package com.example.pdfviewer.ui.theme

import android.app.Activity
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
    primary = Color(0xFF1E5A98),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Color(0xFF001C38),
    secondary = Color(0xFF51606F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD5E4F7),
    onSecondaryContainer = Color(0xFF0E1D2A),
    tertiary = Color(0xFF3E6B5C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC1F1E0),
    onTertiaryContainer = Color(0xFF002118),
    background = Color(0xFFFAF9FC),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFAF9FC),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E2E8),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6CC),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA7C8FF),
    onPrimary = Color(0xFF00315E),
    primaryContainer = Color(0xFF004784),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFFBAC8DB),
    onSecondary = Color(0xFF23313F),
    secondaryContainer = Color(0xFF3A4857),
    onSecondaryContainer = Color(0xFFD5E4F7),
    tertiary = Color(0xFFA6D6C4),
    onTertiary = Color(0xFF07372B),
    tertiaryContainer = Color(0xFF224E41),
    onTertiaryContainer = Color(0xFFC1F1E0),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF111318),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC4C6CC),
    outline = Color(0xFF8E9199),
    outlineVariant = Color(0xFF43474E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun PdfViewerTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
