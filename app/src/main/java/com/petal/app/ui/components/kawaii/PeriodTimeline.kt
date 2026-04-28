package com.petal.app.ui.components.kawaii

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.FlowIntensity

data class PeriodTimelineEntry(
    val startLabel: String,
    val days: Int,
    val flowIntensity: FlowIntensity,
    val topMood: String,
)

/**
 * "your story so far ❀" — horizontal strip of past periods with a small
 * "days like this" callout if there's a repeating pattern.
 */
@Composable
fun PeriodTimeline(
    entries: List<PeriodTimelineEntry>,
    modifier: Modifier = Modifier,
) {
    val pink = Color(0xFFE27B9C)
    val pinkLight = Color(0xFFFFD1DC)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
            .padding(20.dp),
    ) {
        Text(
            "your story so far ❀",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            if (entries.isEmpty()) "log a few cycles and watch your patterns bloom"
            else "the rhythm of your last ${entries.size} cycle${if (entries.size == 1) "" else "s"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (entries.isEmpty()) return@Column

        Spacer(Modifier.height(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            entries.forEach { e ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        e.startLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(86.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        repeat(e.days) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(when (e.flowIntensity) {
                                        FlowIntensity.Heavy -> 22.dp
                                        FlowIntensity.Medium -> 16.dp
                                        FlowIntensity.Light -> 12.dp
                                    })
                                    .clip(CircleShape)
                                    .background(when (e.flowIntensity) {
                                        FlowIntensity.Heavy -> pink
                                        FlowIntensity.Medium -> pinkLight
                                        FlowIntensity.Light -> pinkLight.copy(alpha = 0.65f)
                                    }),
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${e.days}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // "days like this" pattern callout (very light heuristic)
        val avg = entries.map { it.days }.average()
        val variance = entries.map { Math.abs(it.days - avg) }.average()
        val callout = when {
            entries.size < 3 -> null
            variance < 1.2 -> "your periods land like clockwork ⊹ — about ${"%.0f".format(avg)} days each."
            entries.first().days > avg + 1 -> "this cycle ran a touch longer than usual — gentle pace recommended ⌒"
            entries.first().days < avg - 1 -> "this cycle was shorter than your average ❀ — keep an eye on it"
            else -> null
        }
        if (callout != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    callout,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
