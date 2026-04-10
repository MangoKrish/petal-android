package com.petal.app.ui.theme

import androidx.compose.ui.graphics.Color

// ════════════════════════════════════════════════════════
// PETAL COLOR SYSTEM
// Art-inspired, humane, warm — like a beloved painting.
//
// Light mode: Soft petal cream, watercolor washes
// Dark mode: Warm candlelit study, dried flowers,
//            old book pages, golden hour shadows
//
// NOT vibecody. NOT neon. NOT tech-trendy.
// Think: Josephine Wall, botanical illustrations,
//        Renaissance warmth, cozy evening light.
// ════════════════════════════════════════════════════════

// ─── Rose palette (primary brand) ───
val Rose50 = Color(0xFFFFF1F2)
val Rose100 = Color(0xFFFFE4E6)
val Rose200 = Color(0xFFFECDD3)
val Rose300 = Color(0xFFFDA4AF)
val Rose400 = Color(0xFFFB7185)
val Rose500 = Color(0xFFF43F5E)
val Rose600 = Color(0xFFE11D48)
val Rose700 = Color(0xFFBE123C)
val Rose800 = Color(0xFF9F1239)
val Rose900 = Color(0xFF881337)

// ─── Teal palette (follicular / partner) ───
val Teal50 = Color(0xFFF0FDFA)
val Teal100 = Color(0xFFCCFBF1)
val Teal200 = Color(0xFF99F6E4)
val Teal300 = Color(0xFF5EEAD4)
val Teal400 = Color(0xFF2DD4BF)
val Teal500 = Color(0xFF14B8A6)
val Teal600 = Color(0xFF0D9488)
val Teal700 = Color(0xFF0F766E)
val Teal800 = Color(0xFF115E59)
val Teal900 = Color(0xFF134E4A)

// ─── Gold palette (ovulation) ───
val Gold50 = Color(0xFFFFFBEB)
val Gold100 = Color(0xFFFEF3C7)
val Gold200 = Color(0xFFFDE68A)
val Gold300 = Color(0xFFFCD34D)
val Gold400 = Color(0xFFFBBF24)
val Gold500 = Color(0xFFF59E0B)
val Gold600 = Color(0xFFD97706)
val Gold700 = Color(0xFFB45309)

// ─── Lavender palette (luteal / calm) ───
val Lavender50 = Color(0xFFFAF5FF)
val Lavender100 = Color(0xFFF3E8FF)
val Lavender200 = Color(0xFFE9D5FF)
val Lavender300 = Color(0xFFD8B4FE)
val Lavender400 = Color(0xFFC084FC)
val Lavender500 = Color(0xFFA855F7)
val Lavender600 = Color(0xFF9333EA)
val Lavender700 = Color(0xFF7C3AED)

// ─── Neutral palette ───
val Neutral50 = Color(0xFFFAFAFA)
val Neutral100 = Color(0xFFF5F5F5)
val Neutral200 = Color(0xFFE5E5E5)
val Neutral300 = Color(0xFFD4D4D4)
val Neutral400 = Color(0xFFA3A3A3)
val Neutral500 = Color(0xFF737373)
val Neutral600 = Color(0xFF525252)
val Neutral700 = Color(0xFF404040)
val Neutral800 = Color(0xFF262626)
val Neutral900 = Color(0xFF171717)

// ════════════════════════════════════════════════════════
// DARK THEME — "Candlelit Study"
//
// Warm, human, art-inspired.
// Like reading by candlelight in a room full of
// dried roses, old paintings, and warm wooden shelves.
//
// Backgrounds: Deep warm browns and charcoals
//   (not cold gray, not sterile black)
// Accents: Soft, muted — like watercolor on dark paper
// Text: Warm off-white, like parchment
// ════════════════════════════════════════════════════════

// Dark backgrounds — warm charcoal/chocolate tones
val DarkBg = Color(0xFF1A1612)              // Deep warm brown (like dark wood)
val DarkSurface = Color(0xFF231F1B)          // Warm charcoal (like an old book cover)
val DarkSurfaceElevated = Color(0xFF2C2722)  // Cards (like leather)
val DarkSurfaceHighlight = Color(0xFF35302A) // Active/selected — fireside warmth

// Soft, painterly accents for dark mode
// (muted, not neon — like watercolor on brown paper)
val DarkRoseSoft = Color(0xFFE8839A)         // Dried rose petal pink
val DarkTealSoft = Color(0xFF6CB8AD)         // Sage green / herbal
val DarkGoldSoft = Color(0xFFD4A85C)         // Honeyed gold / candlelight
val DarkLavenderSoft = Color(0xFFB89AD4)     // Twilight lavender

// Dark text — parchment-like warmth
val DarkTextPrimary = Color(0xFFF0EBE3)      // Warm parchment white
val DarkTextSecondary = Color(0xFFBDB5A8)    // Aged paper
val DarkTextMuted = Color(0xFF8A8278)         // Faded ink

// Dark borders & separators — subtle, organic
val DarkOutline = Color(0xFF4A433B)          // Like a pencil line
val DarkOutlineSubtle = Color(0xFF3A342D)    // Whisper of separation

// Phase tints for dark mode (warm, subtle — not glowing)
val MenstrualDarkTint = Color(0xFF2D1E1E)    // Warm dark rose
val FollicularDarkTint = Color(0xFF1C2A25)   // Deep forest green
val OvulationDarkTint = Color(0xFF2D2718)    // Warm amber shadow
val LutealDarkTint = Color(0xFF241E2D)       // Evening purple

// Soft glows (very subtle — like candlelight on a wall)
val MenstrualGlow = Color(0x15E8839A)
val FollicularGlow = Color(0x156CB8AD)
val OvulationGlow = Color(0x15D4A85C)
val LutealGlow = Color(0x15B89AD4)

// ════════════════════════════════════════════════════════
// Phase-specific colors (shared)
// ════════════════════════════════════════════════════════
val MenstrualColor = Rose500
val FollicularColor = Teal500
val OvulationColor = Gold500
val LutealColor = Lavender500

// Phase gradient start colors (light mode)
val MenstrualGradientStart = Rose100
val FollicularGradientStart = Teal100
val OvulationGradientStart = Gold100
val LutealGradientStart = Lavender100

// Severity colors
val InfoColor = Teal500
val WatchColor = Gold500
val CareColor = Rose500

// Success / warning / error
val SuccessColor = Color(0xFF22C55E)
val WarningColor = Color(0xFFF59E0B)
val ErrorColor = Color(0xFFEF4444)
