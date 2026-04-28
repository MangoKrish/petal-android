package com.petal.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════════════
// PETAL — KAWAII / SAKURA REDESIGN
// "digital hanami companion"
//
// Light theme: Sakura — warm cream + sakura pinks
// Dark theme:  Yozakura — deep warm indigo + plum
// ════════════════════════════════════════════════════════

// Sakura (light) palette
val SakuraBgPrimary = Color(0xFFFFF6F0)
val SakuraBgCard = Color(0xFFFFFBF6)
val SakuraAccentPink = Color(0xFFFFD1DC)      // primary
val SakuraAccentPink2 = Color(0xFFFFC2D1)     // active
val SakuraAccentPink3 = Color(0xFFE27B9C)     // emphasis
val SakuraAccentGold = Color(0xFFF7E9C3)      // ovulation / highlight
val SakuraAccentGold2 = Color(0xFFF4D58A)
val SakuraAccentMint = Color(0xFFCDE4C4)      // fertility / health
val SakuraAccentSky = Color(0xFFCAE4F0)       // info
val SakuraTextPrimary = Color(0xFF5A3947)
val SakuraTextSecondary = Color(0xFFA07686)
val SakuraTextTertiary = Color(0xFFB07A8A)
val SakuraBorderSoft = Color(0xFFF5D9DD)

// Yozakura (dark) palette
val YozakuraBgPrimary = Color(0xFF1E1A24)
val YozakuraBgCard = Color(0xFF2E2338)
val YozakuraAccentPink = Color(0xFFE8A0B4)
val YozakuraAccentPink2 = Color(0xFFD38198)
val YozakuraAccentGold = Color(0xFFEDD9A3)
val YozakuraAccentMint = Color(0xFFB4D4A8)
val YozakuraAccentSky = Color(0xFFA0BBD4)
val YozakuraTextPrimary = Color(0xFFF5E6E9)
val YozakuraTextSecondary = Color(0xFFC9A8B5)
val YozakuraBorderSoft = Color(0x33E8A0B4)

val KawaiiLightColorScheme = lightColorScheme(
    primary = SakuraAccentPink3,
    onPrimary = Color.White,
    primaryContainer = SakuraAccentPink,
    onPrimaryContainer = SakuraTextPrimary,
    secondary = SakuraAccentSky,
    onSecondary = SakuraTextPrimary,
    secondaryContainer = SakuraAccentSky,
    onSecondaryContainer = SakuraTextPrimary,
    tertiary = SakuraAccentGold2,
    onTertiary = SakuraTextPrimary,
    tertiaryContainer = SakuraAccentGold,
    onTertiaryContainer = SakuraTextPrimary,
    error = Color(0xFFD08CA0),
    errorContainer = Color(0xFFFFE2E8),
    onError = Color.White,
    onErrorContainer = SakuraTextPrimary,
    background = SakuraBgPrimary,
    onBackground = SakuraTextPrimary,
    surface = SakuraBgCard,
    onSurface = SakuraTextPrimary,
    surfaceVariant = Color(0xFFFFEDE5),
    onSurfaceVariant = SakuraTextSecondary,
    outline = SakuraBorderSoft,
    outlineVariant = Color(0xFFF8E5E9),
    inverseSurface = SakuraTextPrimary,
    inverseOnSurface = SakuraBgCard,
    inversePrimary = SakuraAccentPink2,
    surfaceTint = SakuraAccentPink3,
)

val KawaiiDarkColorScheme = darkColorScheme(
    primary = YozakuraAccentPink,
    onPrimary = Color(0xFF3D1520),
    primaryContainer = Color(0xFF4A2030),
    onPrimaryContainer = YozakuraAccentPink,
    secondary = YozakuraAccentSky,
    onSecondary = YozakuraBgPrimary,
    secondaryContainer = Color(0xFF2A3548),
    onSecondaryContainer = YozakuraAccentSky,
    tertiary = YozakuraAccentGold,
    onTertiary = YozakuraBgPrimary,
    tertiaryContainer = Color(0xFF453620),
    onTertiaryContainer = YozakuraAccentGold,
    error = Color(0xFFE8A0A0),
    errorContainer = Color(0xFF5C2020),
    onError = Color(0xFF4A1515),
    onErrorContainer = Color(0xFFFFDAD6),
    background = YozakuraBgPrimary,
    onBackground = YozakuraTextPrimary,
    surface = YozakuraBgCard,
    onSurface = YozakuraTextPrimary,
    surfaceVariant = Color(0xFF382A44),
    onSurfaceVariant = YozakuraTextSecondary,
    outline = YozakuraBorderSoft,
    outlineVariant = Color(0x22E8A0B4),
    inverseSurface = YozakuraTextPrimary,
    inverseOnSurface = YozakuraBgPrimary,
    inversePrimary = YozakuraAccentPink2,
    surfaceTint = YozakuraAccentPink,
)
