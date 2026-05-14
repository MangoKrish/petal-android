package com.petal.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.CycleMode
import com.petal.app.data.model.PredictionConfidence
import com.petal.app.domain.FertilityFusion
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Hybrid predictor card. PHASE_6_7_PLAN.md §6A.2.
 * Mirrors new-project/src/components/prediction-card.tsx so web and Android
 * surface the same information with the same mode-aware framing.
 */
@Composable
fun PredictionCard(
    nextPeriodDate: LocalDate,
    ovulationDate: LocalDate,
    fertileWindowStart: LocalDate,
    fertileWindowEnd: LocalDate,
    confidence: PredictionConfidence,
    cycleMode: CycleMode = CycleMode.Tracking,
    peakDay: LocalDate? = null,
    fusionSource: FertilityFusion.OvulationSource? = null,
    phaseToday: FertilityFusion.FertilityPhaseToday? = null,
    irregular: Boolean = false,
    insufficientFertilityData: Boolean = false,
    onHowDoesThisWorkClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d")

    PetalCard(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Predictions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                ConfidenceBadge(confidence)
            }

            Spacer(modifier = Modifier.height(16.dp))

            PredictionRow(
                color = Rose500,
                label = "Next period",
                value = nextPeriodDate.format(dateFormatter),
                icon = Icons.Default.FiberManualRecord
            )

            Spacer(modifier = Modifier.height(12.dp))

            PredictionRow(
                color = Gold500,
                label = "Ovulation",
                value = ovulationDate.format(dateFormatter),
                icon = Icons.Default.FiberManualRecord
            )

            if (peakDay != null) {
                Spacer(modifier = Modifier.height(12.dp))
                PredictionRow(
                    color = Gold700,
                    label = "Peak day",
                    value = peakDay.format(dateFormatter),
                    icon = Icons.Default.Star
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val fertileLabel = when (cycleMode) {
                CycleMode.TryingToConceive -> "Best days to try"
                CycleMode.AvoidingPregnancy -> "Avoid these days"
                CycleMode.Tracking -> "Fertile window"
            }
            PredictionRow(
                color = Teal500,
                label = fertileLabel,
                value = "${fertileWindowStart.format(dateFormatter)} - ${fertileWindowEnd.format(dateFormatter)}",
                icon = Icons.Default.CalendarMonth
            )

            if (insufficientFertilityData) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Log a few more cycles for a clearer fertile-window estimate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                fusionSource?.let { src ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        sourceCopy(src) + if (irregular) " · your cycles vary, this is an estimate" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                phaseToday?.let { phase ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        phaseCopy(phase),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (cycleMode == CycleMode.AvoidingPregnancy) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Petal isn't contraception. If you're trying to avoid pregnancy, please use a reliable method alongside cycle tracking.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (onHowDoesThisWorkClick != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = onHowDoesThisWorkClick, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        "How does Petal predict this?",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun sourceCopy(source: FertilityFusion.OvulationSource): String = when (source) {
    FertilityFusion.OvulationSource.LH -> "Based on your LH test"
    FertilityFusion.OvulationSource.BBT -> "Based on your temperature shift"
    FertilityFusion.OvulationSource.PAIN -> "Based on mucus + ovulation pain"
    FertilityFusion.OvulationSource.MUCUS -> "Based on cervical mucus"
    FertilityFusion.OvulationSource.CALENDAR -> "Based on your cycle history"
}

private fun phaseCopy(phase: FertilityFusion.FertilityPhaseToday): String = when (phase) {
    FertilityFusion.FertilityPhaseToday.LOW -> "Low fertility today"
    FertilityFusion.FertilityPhaseToday.RISING -> "Approaching ovulation"
    FertilityFusion.FertilityPhaseToday.PEAK -> "Ovulation peak"
    FertilityFusion.FertilityPhaseToday.POST_OVULATION -> "Post-ovulation"
}

@Composable
private fun PredictionRow(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ConfidenceBadge(confidence: PredictionConfidence) {
    val (color, bgColor) = when (confidence) {
        PredictionConfidence.High -> Teal700 to Teal100
        PredictionConfidence.Moderate -> Gold700 to Gold100
        PredictionConfidence.Low -> Rose700 to Rose100
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = bgColor
    ) {
        Text(
            text = "${confidence.display} confidence",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
