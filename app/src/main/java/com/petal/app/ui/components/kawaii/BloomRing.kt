package com.petal.app.ui.components.kawaii

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.ui.theme.SakuraAccentGold2
import com.petal.app.ui.theme.SakuraAccentPink
import com.petal.app.ui.theme.SakuraAccentPink2
import com.petal.app.ui.theme.SakuraAccentPink3

/**
 * Kawaii bloom ring — circular cycle progress with a 5-petal flower
 * whose state reflects the current cycle phase.
 */
@Composable
fun BloomRing(
    currentDay: Int,
    cycleLength: Int,
    phase: String,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "bloom-breathe")
    val breathe by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val (petalFill, petalCore, openness, shimmer) = remember(phase) { palette(phase) }
    val progress = (currentDay.toFloat() / cycleLength.toFloat()).coerceIn(0f, 1f)

    Box(modifier = modifier.size(230.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = (w.coerceAtMost(h) / 2f) - 8.dp.toPx()

            // Track ring
            drawCircle(
                color = Color(0xFFFFE0E8),
                radius = r,
                style = Stroke(width = 6.dp.toPx())
            )

            // Progress arc
            val sweep = 360f * progress
            drawArc(
                color = petalCore,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = androidx.compose.ui.geometry.Size(2 * r, 2 * r),
                style = Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )

            // 5 petals
            val petalDist = (18.dp.toPx() + 14.dp.toPx() * openness) * breathe
            val petalRy = (30.dp.toPx() + 12.dp.toPx() * openness) * breathe
            val petalRx = petalRy * 0.38f
            for (angle in listOf(0f, 72f, 144f, 216f, 288f)) {
                rotate(degrees = angle, pivot = Offset(cx, cy)) {
                    drawOval(
                        color = petalFill.copy(alpha = 0.92f),
                        topLeft = Offset(cx - petalRx, cy - petalDist - petalRy),
                        size = androidx.compose.ui.geometry.Size(petalRx * 2f, petalRy * 2f)
                    )
                }
            }
            // Center
            drawCircle(petalCore, radius = 14.dp.toPx() * breathe, center = Offset(cx, cy))
            drawCircle(Color.White.copy(alpha = 0.55f), radius = 6.dp.toPx() * breathe, center = Offset(cx, cy))
            if (shimmer) {
                drawCircle(SakuraAccentGold2, radius = 3.dp.toPx(), center = Offset(cx + 5.dp.toPx(), cy - 3.dp.toPx()))
                drawCircle(Color.White, radius = 2.dp.toPx(), center = Offset(cx - 5.dp.toPx(), cy + 2.dp.toPx()))
            }
        }
    }
}

private data class Palette(
    val petalFill: Color,
    val petalCore: Color,
    val openness: Float,
    val shimmer: Boolean,
)

private fun palette(phase: String): Palette = when (phase.lowercase()) {
    "menstrual" -> Palette(SakuraAccentPink3, Color(0xFFC66285), 0.55f, false)
    "follicular" -> Palette(SakuraAccentPink, SakuraAccentPink3, 0.85f, false)
    "ovulation" -> Palette(SakuraAccentPink2, SakuraAccentGold2, 1.0f, true)
    else -> Palette(Color(0xFFF0D6DD), Color(0xFFC7B1D8), 0.7f, false)
}

@Composable
fun BloomCard(
    currentDay: Int,
    cycleLength: Int,
    phase: String,
    daysUntilNext: Int?,
    modifier: Modifier = Modifier
) {
    val phaseLabel = when (phase.lowercase()) {
        "menstrual" -> "⊹ menstrual day ⊹"
        "follicular" -> "⊹ follicular day ⊹"
        "ovulation" -> "⊹ ovulation day ⊹"
        else -> "⊹ luteal day ⊹"
    }
    val subtitle = when (phase.lowercase()) {
        "menstrual" -> "rest is your superpower today"
        "follicular" -> "fresh energy is rising softly"
        "ovulation" -> "your body's in full bloom today"
        else -> "winding down with grace"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        BloomRing(currentDay = currentDay, cycleLength = cycleLength, phase = phase)
        Text(
            text = phaseLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(top = 8.dp)
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = if (daysUntilNext != null && daysUntilNext >= 0)
                "next cycle in $daysUntilNext ${if (daysUntilNext == 1) "day" else "days"} ♡"
            else "cycle day $currentDay of $cycleLength",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
