package com.petal.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.PredictionConfidence
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PredictionCard(
    nextPeriodDate: LocalDate,
    ovulationDate: LocalDate,
    fertileWindowStart: LocalDate,
    fertileWindowEnd: LocalDate,
    confidence: PredictionConfidence,
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

            // Next period
            PredictionRow(
                color = Rose500,
                label = "Next period",
                value = nextPeriodDate.format(dateFormatter),
                icon = Icons.Default.FiberManualRecord
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Ovulation
            PredictionRow(
                color = Gold500,
                label = "Ovulation",
                value = ovulationDate.format(dateFormatter),
                icon = Icons.Default.FiberManualRecord
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Fertile window
            PredictionRow(
                color = Teal500,
                label = "Fertile window",
                value = "${fertileWindowStart.format(dateFormatter)} - ${fertileWindowEnd.format(dateFormatter)}",
                icon = Icons.Default.CalendarMonth
            )
        }
    }
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
