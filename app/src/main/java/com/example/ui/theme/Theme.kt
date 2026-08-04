package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF6366f1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFeef0ff),
    onPrimaryContainer = Color(0xFF1a1a2e),
    secondary = Color(0xFF4a4a6a),
    onSecondary = Color.White,
    background = Color(0xFFf0f2f5),
    onBackground = Color(0xFF1a1a2e),
    surface = Color.White,
    onSurface = Color(0xFF1a1a2e),
    surfaceVariant = Color(0xFFe8eaef),
    onSurfaceVariant = Color(0xFF4a4a6a)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF818cf8),
    onPrimary = Color(0xFF1a1b2e),
    primaryContainer = Color(0xFF1e2040),
    onPrimaryContainer = Color(0xFFe8e9f0),
    secondary = Color(0xFFb8b9cc),
    onSecondary = Color(0xFF1a1b2e),
    background = Color(0xFF1a1b2e),
    onBackground = Color(0xFFe8e9f0),
    surface = Color(0xFF222338),
    onSurface = Color(0xFFe8e9f0),
    surfaceVariant = Color(0xFF2a2c44),
    onSurfaceVariant = Color(0xFFb8b9cc)
)

private val SepiaColors = lightColorScheme(
    primary = Color(0xFFb8860b),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFfdf3d0),
    onPrimaryContainer = Color(0xFF4a3828),
    secondary = Color(0xFF6b5540),
    background = Color(0xFFf4ecd8),
    onBackground = Color(0xFF4a3828),
    surface = Color(0xFFfaf3e4),
    onSurface = Color(0xFF4a3828),
    surfaceVariant = Color(0xFFebe0c8),
    onSurfaceVariant = Color(0xFF6b5540)
)

private val AmoledColors = darkColorScheme(
    primary = Color(0xFFbb86fc),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF1a1028),
    onPrimaryContainer = Color(0xFFf0f0f0),
    secondary = Color(0xFFb0b0b0),
    background = Color.Black,
    onBackground = Color(0xFFf0f0f0),
    surface = Color(0xFF0a0a0a),
    onSurface = Color(0xFFf0f0f0),
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFFb0b0b0)
)

@Composable
fun MyApplicationTheme(
    themeIndex: Int = 0,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeIndex) {
        1 -> DarkColors
        2 -> SepiaColors
        3 -> AmoledColors
        else -> if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        } else {
            LightColors
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
