package com.petal.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ════════════════════════════════════════════════════════
// Extended color system for phase-aware, art-inspired theming
// ════════════════════════════════════════════════════════

data class PetalExtendedColors(
    val phaseAccent: Color,
    val phaseGlow: Color,
    val phaseGradientStart: Color,
    val phaseSurface: Color,
    val warmSurface: Color,      // Warm card background
    val textMuted: Color,
    val subtleBorder: Color,
    val isDark: Boolean,
)

val LocalPetalColors = staticCompositionLocalOf {
    PetalExtendedColors(
        phaseAccent = Rose500,
        phaseGlow = Color(0x15F43F5E),
        phaseGradientStart = MenstrualGradientStart,
        phaseSurface = Rose50,
        warmSurface = Color.White,
        textMuted = Neutral500,
        subtleBorder = Neutral200,
        isDark = false,
    )
}

// ════════════════════════════════════════════════════════
// Light Color Scheme — soft watercolor garden
// ════════════════════════════════════════════════════════

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
    background = Color(0xFFFBF7F4),       // Warm petal cream
    onBackground = Color(0xFF2D2D3A),     // Soft dark — not harsh black
    surface = Color(0xFFFFFCFD),
    onSurface = Color(0xFF2D2D3A),
    surfaceVariant = Rose50,
    onSurfaceVariant = Neutral600,
    outline = Neutral300,
    outlineVariant = Neutral200,
    inverseSurface = Neutral800,
    inverseOnSurface = Neutral100,
    inversePrimary = Rose200,
    surfaceTint = Rose500,
)

// ════════════════════════════════════════════════════════
// Dark Color Scheme — "Candlelit Study"
//
// Human, warm, art-inspired. Like an evening room with:
//  - Warm wooden furniture (brown-charcoal backgrounds)
//  - Dried flowers (muted rose, sage, lavender accents)
//  - Candlelight (warm gold highlights)
//  - Old books and parchment (warm off-white text)
//
// NOT cold. NOT sterile. NOT neon.
// Every surface should feel touchable and warm.
// ════════════════════════════════════════════════════════

private val DarkColorScheme = darkColorScheme(
    primary = DarkRoseSoft,                   // Dried rose — warm, not screaming
    onPrimary = Color(0xFF3D1520),            // Deep rose contrast
    primaryContainer = Color(0xFF4A2030),      // Warm dark rose
    onPrimaryContainer = Rose200,
    secondary = DarkTealSoft,                  // Sage green — herbal warmth
    onSecondary = Color(0xFF1A3A30),
    secondaryContainer = Color(0xFF254A40),
    onSecondaryContainer = Teal200,
    tertiary = DarkLavenderSoft,               // Evening lavender
    onTertiary = Color(0xFF2E1E3E),
    tertiaryContainer = Color(0xFF3E2E55),
    onTertiaryContainer = Lavender200,
    error = Color(0xFFE8A0A0),                 // Soft error — not alarming
    errorContainer = Color(0xFF5C2020),
    onError = Color(0xFF4A1515),
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkBg,                       // Deep warm brown
    onBackground = DarkTextPrimary,            // Warm parchment
    surface = DarkSurface,                     // Old book cover
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,      // Leather card
    onSurfaceVariant = DarkTextSecondary,      // Aged paper
    outline = DarkOutline,                     // Pencil line
    outlineVariant = DarkOutlineSubtle,        // Whisper line
    inverseSurface = DarkTextPrimary,
    inverseOnSurface = DarkBg,
    inversePrimary = Rose600,
    surfaceTint = DarkRoseSoft,
)

@Composable
fun PetalTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Our palette is curated — don't override
    phaseName: String = "Menstrual",
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Phase-aware extended colors
    val extendedColors = if (darkTheme) {
        when (phaseName.lowercase()) {
            "menstrual" -> PetalExtendedColors(
                phaseAccent = DarkRoseSoft,
                phaseGlow = MenstrualGlow,
                phaseGradientStart = MenstrualDarkTint,
                phaseSurface = MenstrualDarkTint,
                warmSurface = DarkSurfaceElevated,
                textMuted = DarkTextMuted,
                subtleBorder = DarkOutlineSubtle,
                isDark = true,
            )
            "follicular" -> PetalExtendedColors(
                phaseAccent = DarkTealSoft,
                phaseGlow = FollicularGlow,
                phaseGradientStart = FollicularDarkTint,
                phaseSurface = FollicularDarkTint,
                warmSurface = DarkSurfaceElevated,
                textMuted = DarkTextMuted,
                subtleBorder = DarkOutlineSubtle,
                isDark = true,
            )
            "ovulation" -> PetalExtendedColors(
                phaseAccent = DarkGoldSoft,
                phaseGlow = OvulationGlow,
                phaseGradientStart = OvulationDarkTint,
                phaseSurface = OvulationDarkTint,
                warmSurface = DarkSurfaceElevated,
                textMuted = DarkTextMuted,
                subtleBorder = DarkOutlineSubtle,
                isDark = true,
            )
            else -> PetalExtendedColors(
                phaseAccent = DarkLavenderSoft,
                phaseGlow = LutealGlow,
                phaseGradientStart = LutealDarkTint,
                phaseSurface = LutealDarkTint,
                warmSurface = DarkSurfaceElevated,
                textMuted = DarkTextMuted,
                subtleBorder = DarkOutlineSubtle,
                isDark = true,
            )
        }
    } else {
        when (phaseName.lowercase()) {
            "menstrual" -> PetalExtendedColors(
                phaseAccent = Rose500,
                phaseGlow = Color(0x15F43F5E),
                phaseGradientStart = Rose100,
                phaseSurface = Rose50,
                warmSurface = Color.White,
                textMuted = Neutral500,
                subtleBorder = Neutral200,
                isDark = false,
            )
            "follicular" -> PetalExtendedColors(
                phaseAccent = Teal500,
                phaseGlow = Color(0x1514B8A6),
                phaseGradientStart = Teal100,
                phaseSurface = Teal50,
                warmSurface = Color.White,
                textMuted = Neutral500,
                subtleBorder = Neutral200,
                isDark = false,
            )
            "ovulation" -> PetalExtendedColors(
                phaseAccent = Gold500,
                phaseGlow = Color(0x15F59E0B),
                phaseGradientStart = Gold100,
                phaseSurface = Gold50,
                warmSurface = Color.White,
                textMuted = Neutral500,
                subtleBorder = Neutral200,
                isDark = false,
            )
            else -> PetalExtendedColors(
                phaseAccent = Lavender500,
                phaseGlow = Color(0x15A855F7),
                phaseGradientStart = Lavender100,
                phaseSurface = Lavender50,
                warmSurface = Color.White,
                textMuted = Neutral500,
                subtleBorder = Neutral200,
                isDark = false,
            )
        }
    }

    // Status bar and nav bar
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

    CompositionLocalProvider(LocalPetalColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PetalTypography,
            shapes = PetalShapes,
            content = content
        )
    }
}
