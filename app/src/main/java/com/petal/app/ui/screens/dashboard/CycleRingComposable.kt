package com.petal.app.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.CyclePhase
import com.petal.app.ui.theme.*

@Composable
fun CycleRing(
    cycleDay: Int,
    cycleLength: Int,
    phase: CyclePhase,
    daysUntilPeriod: Int,
    modifier: Modifier = Modifier
) {
    val progress = cycleDay.toFloat() / cycleLength.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "cycle_ring_progress"
    )

    val phaseColor = when (phase) {
        CyclePhase.Menstrual -> Rose500
        CyclePhase.Follicular -> Teal500
        CyclePhase.Ovulation -> Gold500
        CyclePhase.Luteal -> Lavender500
    }

    val trackColor = when (phase) {
        CyclePhase.Menstrual -> Rose100
        CyclePhase.Follicular -> Teal100
        CyclePhase.Ovulation -> Gold100
        CyclePhase.Luteal -> Lavender100
    }

    Box(
        modifier = modifier.size(220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val strokeWidth = 16.dp.toPx()
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

            // Track (background ring)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = phaseColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Phase segment markers
            val menstrualEnd = 5f / cycleLength * 360f
            val follicularEnd = (cycleLength * 0.46f) / cycleLength * 360f
            val ovulationEnd = ((cycleLength * 0.46f) + 2f) / cycleLength * 360f

            // Small dot at current position
            val angle = Math.toRadians((-90 + animatedProgress * 360).toDouble())
            val dotRadius = 6.dp.toPx()
            val ringRadius = (size.width - strokeWidth) / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            val dotX = centerX + ringRadius * Math.cos(angle).toFloat()
            val dotY = centerY + ringRadius * Math.sin(angle).toFloat()

            drawCircle(
                color = Color.White,
                radius = dotRadius + 2.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            drawCircle(
                color = phaseColor,
                radius = dotRadius,
                center = Offset(dotX, dotY)
            )
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Day $cycleDay",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = phaseColor
            )
            Text(
                text = phase.display,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (daysUntilPeriod > 0) {
                Text(
                    text = "$daysUntilPeriod day${if (daysUntilPeriod != 1) "s" else ""}\nuntil period",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Period\nexpected",
                    style = MaterialTheme.typography.bodySmall,
                    color = Rose500,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
