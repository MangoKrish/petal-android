package com.petal.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.CycleMode
import com.petal.app.ui.components.PetalCard

/**
 * PHASE_6_7_PLAN.md §6A.2 — mirrors
 * new-project/src/components/prediction-transparency-panel.tsx.
 * Pure presentation; the parent passes pre-formatted text so we don't have to
 * recompute the Bayesian numbers here.
 */
@Composable
fun PredictionTransparencyPanel(
    cyclesUsed: Int,
    averageCycleLength: Int,
    minCycle: Int?,
    maxCycle: Int?,
    nextPeriodSummary: String,
    fertilityWindowSummary: String?,
    cycleMode: CycleMode,
    modifier: Modifier = Modifier
) {
    PetalCard(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "How Petal predicts your cycle",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Petal uses a Bayesian model for next-period dates. Each cycle you log makes the next prediction a little more accurate.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            FactBox(
                text = buildString {
                    append("Right now Petal is using ")
                    append(cyclesUsed)
                    append(" of your cycles.")
                    if (minCycle != null && maxCycle != null) {
                        append(" Your average cycle is ")
                        append(averageCycleLength)
                        append(" days (range: ")
                        append(minCycle)
                        append("–")
                        append(maxCycle)
                        append(").")
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                nextPeriodSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                "How Petal estimates your fertile window",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "For the fertile window, Petal combines your cycle length with any signals you've tracked:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            BulletLine("Basal body temperature — a sustained rise of about 0.2°C tells Petal ovulation has already happened.")
            BulletLine("Cervical mucus — egg-white texture is peak fertility.")
            BulletLine("Ovulation pain — a sharp twinge mid-cycle (mittelschmerz) can pin ovulation to a specific day.")
            BulletLine("LH tests — a positive test usually means ovulation in 12–36 hours.")

            Spacer(modifier = Modifier.height(12.dp))

            if (fertilityWindowSummary != null) {
                FactBox(text = fertilityWindowSummary)
            } else {
                FactBox(text = "Log a few more cycles for a clearer fertile-window estimate.")
            }

            if (cycleMode == CycleMode.AvoidingPregnancy) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        "Petal isn't contraception. If you're trying to avoid pregnancy, please use a reliable method alongside cycle tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FactBox(text: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun BulletLine(text: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            "• ",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
