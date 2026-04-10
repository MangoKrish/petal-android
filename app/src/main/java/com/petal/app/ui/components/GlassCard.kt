package com.petal.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.petal.app.ui.theme.LocalPetalColors

/**
 * Warm, tactile card that feels like holding a piece of art.
 *
 * In dark mode: Warm brown surface with soft phase-tinted border,
 *   like a card pressed from handmade paper under candlelight.
 * In light mode: Clean white with soft shadow and warmth.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    glowColor: Color? = null,
    cornerRadius: Dp = 20.dp,
    showGlow: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val petalColors = LocalPetalColors.current
    val isDark = petalColors.isDark

    val shape = RoundedCornerShape(cornerRadius)

    val bgColor = if (isDark) {
        petalColors.warmSurface
    } else {
        Color.White
    }

    val borderColor = if (isDark) {
        val accent = glowColor ?: petalColors.phaseAccent
        if (showGlow) accent.copy(alpha = 0.15f) else petalColors.subtleBorder
    } else {
        Color(0x0A000000)
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (isDark) 1.dp else 0.5.dp,
                color = borderColor,
                shape = shape,
            )
            .padding(16.dp),
        content = content,
    )
}

/**
 * A soft warm glow — like a candle illuminating a dried flower.
 * Used as background decoration, NOT as a tech-style neon glow.
 */
@Composable
fun WarmGlow(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                ),
                shape = RoundedCornerShape(50),
            )
    )
}
