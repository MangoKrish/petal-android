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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.CyclePhase
import com.petal.app.ui.theme.*

private data class PhaseSegment(
    val phase: CyclePhase,
    val startFraction: Float,
    val endFraction: Float,
    val color: Color,
    val trackColor: Color
)

private fun getPhaseSegments(cycleLength: Int): List<PhaseSegment> {
    val menstrualEnd = 5f / cycleLength
    val follicularEnd = (cycleLength * 0.46f) / cycleLength
    val ovulationEnd = ((cycleLength * 0.46f) + 2f) / cycleLength
    return listOf(
        PhaseSegment(CyclePhase.Menstrual, 0f, menstrualEnd, Rose500, Rose100),
        PhaseSegment(CyclePhase.Follicular, menstrualEnd, follicularEnd, Teal500, Teal100),
        PhaseSegment(CyclePhase.Ovulation, follicularEnd, ovulationEnd, Gold500, Gold100),
        PhaseSegment(CyclePhase.Luteal, ovulationEnd, 1f, Lavender500, Lavender100),
    )
}

@Composable
fun CycleRing(
    cycleDay: Int,
    cycleLength: Int,
    phase: CyclePhase,
    daysUntilPeriod: Int,
    modifier: Modifier = Modifier
) {
    val progress = cycleDay.toFloat() / cycleLength.toFloat()
    val segments = remember(cycleLength) { getPhaseSegments(cycleLength) }

    // Staggered arc draw animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "cycle_ring_progress"
    )

    // Per-segment reveal animation
    val segmentReveals = segments.mapIndexed { index, _ ->
        val animatable = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 800,
                    delayMillis = index * 150,
                    easing = FastOutSlowInEasing
                )
            )
        }
        animatable.value
    }

    // Marker scale animation
    val markerScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "marker_scale"
    )

    // Pulsing glow for current phase
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Animated day counter
    val animatedDay by animateIntAsState(
        targetValue = cycleDay,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "day_counter"
    )

    val phaseColor = when (phase) {
        CyclePhase.Menstrual -> Rose500
        CyclePhase.Follicular -> Teal500
        CyclePhase.Ovulation -> Gold500
        CyclePhase.Luteal -> Lavender500
    }

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            val strokeWidth = 14.dp.toPx()
            val glowStrokeWidth = 22.dp.toPx()
            val arcSize = Size(size.width - glowStrokeWidth, size.height - glowStrokeWidth)
            val topLeft = Offset(glowStrokeWidth / 2, glowStrokeWidth / 2)
            val gapDeg = 2f // gap between segments

            // Draw background track
            drawArc(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth * 0.6f, cap = StrokeCap.Round)
            )

            // Draw each phase segment
            segments.forEachIndexed { index, segment ->
                val startAngle = -90f + segment.startFraction * 360f + gapDeg / 2
                val sweepAngle = (segment.endFraction - segment.startFraction) * 360f - gapDeg
                val reveal = segmentReveals.getOrElse(index) { 1f }
                val isActive = segment.phase == phase

                // Glow for active segment
                if (isActive) {
                    drawArc(
                        color = segment.color.copy(alpha = glowAlpha * 0.3f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * reveal,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = glowStrokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Main arc
                drawArc(
                    color = segment.color.copy(alpha = if (isActive) 1f else 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * reveal,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = if (isActive) strokeWidth else strokeWidth * 0.85f, cap = StrokeCap.Round)
                )
            }

            // Current position marker
            val angle = Math.toRadians((-90 + animatedProgress * 360).toDouble())
            val ringRadius = (size.width - glowStrokeWidth) / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            val dotX = centerX + ringRadius * Math.cos(angle).toFloat()
            val dotY = centerY + ringRadius * Math.sin(angle).toFloat()

            // Pulsing glow behind marker
            drawCircle(
                color = phaseColor.copy(alpha = glowAlpha * 0.4f),
                radius = 16.dp.toPx() * markerScale,
                center = Offset(dotX, dotY)
            )

            // White border
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx() * markerScale,
                center = Offset(dotX, dotY)
            )
            // Colored dot
            drawCircle(
                color = phaseColor,
                radius = 6.dp.toPx() * markerScale,
                center = Offset(dotX, dotY)
            )
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Day $animatedDay",
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
