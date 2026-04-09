package com.petal.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.CyclePhase
import com.petal.app.ui.components.phaseColor
import com.petal.app.ui.components.phaseGradientStart
import com.petal.app.ui.theme.*

@Composable
fun PhaseCard(
    phase: CyclePhase,
    hormoneNote: String,
    modifier: Modifier = Modifier
) {
    val color = phaseColor(phase)
    val gradientStart = phaseGradientStart(phase)

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(gradientStart, gradientStart.copy(alpha = 0.3f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${phase.display} Phase",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )

                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = phaseEmoji(phase),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = hormoneNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

private fun phaseEmoji(phase: CyclePhase): String = when (phase) {
    CyclePhase.Menstrual -> "Rest"
    CyclePhase.Follicular -> "Rise"
    CyclePhase.Ovulation -> "Peak"
    CyclePhase.Luteal -> "Calm"
}
