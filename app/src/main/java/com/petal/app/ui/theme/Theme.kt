package com.petal.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Rose500,
    onPrimary = Color.White,
    primaryContainer = Rose100,
    onPrimaryContainer = Rose900,
    secondary = Teal500,
    onSecondary = Color.White,
    secondaryContainer = Teal100,
    onSecondaryContainer = Teal900,
    tertiary = Lavender500,
    onTertiary = Color.White,
    tertiaryContainer = Lavender100,
    onTertiaryContainer = Lavender700,
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFF),
    onBackground = Neutral900,
    surface = Color(0xFFFFFBFF),
    onSurface = Neutral900,
    surfaceVariant = Rose50,
    onSurfaceVariant = Neutral600,
    outline = Neutral300,
    outlineVariant = Neutral200,
    inverseSurface = Neutral800,
    inverseOnSurface = Neutral100,
    inversePrimary = Rose200,
    surfaceTint = Rose500
)

private val DarkColorScheme = darkColorScheme(
    primary = Rose300,
    onPrimary = Rose900,
    primaryContainer = Rose800,
    onPrimaryContainer = Rose100,
    secondary = Teal300,
    onSecondary = Teal900,
    secondaryContainer = Teal800,
    onSecondaryContainer = Teal100,
    tertiary = Lavender300,
    onTertiary = Lavender700,
    tertiaryContainer = Lavender700,
    onTertiaryContainer = Lavender100,
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1A1A),
    onBackground = Neutral100,
    surface = Color(0xFF1A1A1A),
    onSurface = Neutral100,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral300,
    outline = Neutral600,
    outlineVariant = Neutral700,
    inverseSurface = Neutral200,
    inverseOnSurface = Neutral800,
    inversePrimary = Rose600,
    surfaceTint = Rose300
)

@Composable
fun PetalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PetalTypography,
        shapes = PetalShapes,
        content = content
    )
}
