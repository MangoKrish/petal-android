package com.petal.app.ui.components.kawaii

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.FlowIntensity

/**
 * "Days like petal-rain ❀" — soft rounded pink bars showing flow intensity per day.
 * `flows` is a left-to-right ordered list (oldest → newest).
 * Bars: spotting/light = short, medium = mid, heavy = tall. Empty days are rendered
 * as faint placeholders so the rhythm of the period is visible.
 */
@Composable
fun DailyFlowChart(
    flows: List<FlowIntensity?>,
    modifier: Modifier = Modifier,
) {
    val pink = Color(0xFFE27B9C)
    val pinkLight = Color(0xFFFFD1DC)
    val pinkPale = Color(0xFFFFE9EE)
    val border = Color(0xFFF5D9DD)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Text(
            "flow trend ❀",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            if (flows.isEmpty()) "log a few days to see your rhythm bloom"
            else "your last ${flows.size} day${if (flows.size == 1) "" else "s"}, soft and quiet",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        ) {
            if (flows.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val n = flows.size
            val gap = 6.dp.toPx()
            val barW = ((w - gap * (n + 1)) / n).coerceAtLeast(2f)
            val maxH = h - 12.dp.toPx()

            for ((i, f) in flows.withIndex()) {
                val ratio = when (f) {
                    FlowIntensity.Heavy -> 1.0f
                    FlowIntensity.Medium -> 0.7f
                    FlowIntensity.Light -> 0.4f
                    null -> 0.08f
                }
                val barH = maxH * ratio
                val x = gap + i * (barW + gap)
                val y = h - barH
                val color = when (f) {
                    FlowIntensity.Heavy -> pink
                    FlowIntensity.Medium -> pinkLight
                    FlowIntensity.Light -> pinkLight.copy(alpha = 0.65f)
                    null -> pinkPale
                }
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(barW * 0.45f, barW * 0.45f),
                )
                if (f == null) {
                    drawRoundRect(
                        color = border,
                        topLeft = Offset(x, y),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(barW * 0.45f, barW * 0.45f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(pinkLight.copy(alpha = 0.65f), "light")
            Spacer(Modifier.width(10.dp))
            LegendDot(pinkLight, "medium")
            Spacer(Modifier.width(10.dp))
            LegendDot(pink, "heavy")
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(color),
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
