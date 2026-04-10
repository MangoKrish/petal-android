package com.petal.app.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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

    // Staggered arc draw animation with spring physics
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cycle_ring_progress"
    )

    // Per-segment reveal animation with stagger
    val segmentReveals = segments.mapIndexed { index, _ ->
        val animatable = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.65f,
                    stiffness = 200f,
                    visibilityThreshold = 0.001f
                ).let { spec ->
                    tween<Float>(
                        durationMillis = 900,
                        delayMillis = index * 120,
                        easing = FastOutSlowInEasing
                    )
                }
            )
        }
        animatable.value
    }

    // Marker entrance with spring
    val markerScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "marker_scale"
    )

    // Double-layer pulsing glow
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val outerGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "outer_glow_alpha"
    )

    // Animated day counter
    val animatedDay by animateIntAsState(
        targetValue = cycleDay,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "day_counter"
    )

    // Tap interaction for press feedback
    var isPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "press_scale"
    )

    val phaseColor = when (phase) {
        CyclePhase.Menstrual -> Rose500
        CyclePhase.Follicular -> Teal500
        CyclePhase.Ovulation -> Gold500
        CyclePhase.Luteal -> Lavender500
    }

    val phaseColorLight = when (phase) {
        CyclePhase.Menstrual -> Rose100
        CyclePhase.Follicular -> Teal100
        CyclePhase.Ovulation -> Gold100
        CyclePhase.Luteal -> Lavender100
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .size(260.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val strokeWidth = 14.dp.toPx()
            val glowStrokeWidth = 24.dp.toPx()
            val outerGlowWidth = 36.dp.toPx()
            val arcSize = Size(size.width - outerGlowWidth, size.height - outerGlowWidth)
            val topLeft = Offset(outerGlowWidth / 2, outerGlowWidth / 2)
            val gapDeg = 2.5f

            // Outer ambient glow for active phase
            drawArc(
                color = phaseColor.copy(alpha = outerGlowAlpha * 0.15f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = outerGlowWidth, cap = StrokeCap.Round)
            )

            // Draw background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth * 0.5f, cap = StrokeCap.Round)
            )

            // Draw each phase segment with enhanced glow
            segments.forEachIndexed { index, segment ->
                val startAngle = -90f + segment.startFraction * 360f + gapDeg / 2
                val sweepAngle = (segment.endFraction - segment.startFraction) * 360f - gapDeg
                val reveal = segmentReveals.getOrElse(index) { 1f }
                val isActive = segment.phase == phase

                // Double-layer glow for active segment
                if (isActive) {
                    drawArc(
                        color = segment.color.copy(alpha = glowAlpha * 0.2f),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * reveal,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = outerGlowWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = segment.color.copy(alpha = glowAlpha * 0.4f),
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
                    color = segment.color.copy(alpha = if (isActive) 1f else 0.25f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * reveal,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = if (isActive) strokeWidth * 1.1f else strokeWidth * 0.8f,
                        cap = StrokeCap.Round
                    )
                )
            }

            // Current position marker with enhanced glow
            val angle = Math.toRadians((-90 + animatedProgress * 360).toDouble())
            val ringRadius = (size.width - outerGlowWidth) / 2
            val centerX = size.width / 2
            val centerY = size.height / 2
            val dotX = centerX + ringRadius * Math.cos(angle).toFloat()
            val dotY = centerY + ringRadius * Math.sin(angle).toFloat()

            // Outer glow ring around marker
            drawCircle(
                color = phaseColor.copy(alpha = outerGlowAlpha * 0.5f),
                radius = 20.dp.toPx() * markerScale,
                center = Offset(dotX, dotY)
            )

            // Inner glow behind marker
            drawCircle(
                color = phaseColor.copy(alpha = glowAlpha * 0.5f),
                radius = 14.dp.toPx() * markerScale,
                center = Offset(dotX, dotY)
            )

            // White border marker
            drawCircle(
                color = Color.White,
                radius = 9.dp.toPx() * markerScale,
                center = Offset(dotX, dotY)
            )
            // Colored center dot
            drawCircle(
                color = phaseColor,
                radius = 7.dp.toPx() * markerScale,
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
