package com.petal.app.ui.screens.insights

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.model.*
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
import com.petal.app.ui.viewmodel.InsightsViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Advanced analytics screen with:
 * - Cycle length over time (Canvas line chart)
 * - Symptom frequency by phase (grouped bar chart)
 * - Prediction accuracy tracking
 * - Monthly summary with export
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleTrendsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Length", "Symptoms", "Accuracy", "Summary")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cycle Trends") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        exportMonthlySummary(
                            context = context,
                            entries = uiState.entries,
                            format = "csv"
                        )
                    }) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export data")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = Rose500
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> CycleLengthChart(entries = uiState.entries)
                1 -> SymptomFrequencyChart(entries = uiState.entries)
                2 -> PredictionAccuracySection(entries = uiState.entries)
                3 -> MonthlySummarySection(
                    entries = uiState.entries,
                    onExport = { format ->
                        exportMonthlySummary(context, uiState.entries, format)
                    }
                )
            }
        }
    }
}

// ── Cycle Length Line Chart ──────────────────────────────────────────────────

@Composable
private fun CycleLengthChart(
    entries: List<CycleEntry>,
    modifier: Modifier = Modifier
) {
    val cycleLengths = entries
        .sortedBy { it.start }
        .map { it.cycleLength }
        .takeLast(12)

    if (cycleLengths.size < 2) {
        EmptyChartPlaceholder(message = "Log at least 2 cycles to see length trends")
        return
    }

    val average = cycleLengths.average().toFloat()
    val minLength = (cycleLengths.min() - 3).coerceAtLeast(18)
    val maxLength = (cycleLengths.max() + 3).coerceAtMost(50)
    val range = (maxLength - minLength).toFloat().coerceAtLeast(1f)

    var selectedIndex by remember { mutableIntStateOf(-1) }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "chart_progress"
    )

    val accessibilityLabel = buildString {
        append("Cycle length chart showing ${cycleLengths.size} cycles. ")
        append("Average cycle length is ${average.toInt()} days. ")
        append("Shortest cycle: ${cycleLengths.min()} days. ")
        append("Longest cycle: ${cycleLengths.max()} days.")
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PetalCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Cycle Length Over Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Last ${cycleLengths.size} cycles",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip(label = "Average", value = "${average.toInt()}d", color = Teal500)
                    StatChip(label = "Shortest", value = "${cycleLengths.min()}d", color = Rose500)
                    StatChip(label = "Longest", value = "${cycleLengths.max()}d", color = Lavender500)
                    StatChip(
                        label = "Spread",
                        value = "${cycleLengths.max() - cycleLengths.min()}d",
                        color = Gold500
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Line chart
                val density = LocalDensity.current
                val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .semantics { contentDescription = accessibilityLabel }
                        .pointerInput(cycleLengths) {
                            detectTapGestures { offset ->
                                val chartWidth = size.width.toFloat()
                                val stepX = chartWidth / (cycleLengths.size - 1).coerceAtLeast(1)
                                val tappedIndex = ((offset.x / stepX) + 0.5f).toInt()
                                    .coerceIn(0, cycleLengths.size - 1)
                                selectedIndex = if (selectedIndex == tappedIndex) -1 else tappedIndex
                            }
                        }
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height
                    val paddingBottom = 30f
                    val paddingTop = 10f
                    val usableHeight = chartHeight - paddingBottom - paddingTop

                    // Grid lines
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = paddingTop + (usableHeight / gridSteps) * i
                        drawLine(
                            color = Color(0xFFE5E5E5),
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = 1f
                        )
                        // Label
                        val value = maxLength - ((maxLength - minLength).toFloat() / gridSteps * i).toInt()
                        drawContext.canvas.nativeCanvas.drawText(
                            "${value}d",
                            chartWidth - 28f,
                            y - 4f,
                            android.graphics.Paint().apply {
                                color = 0xFF737373.toInt()
                                textSize = with(density) { 9.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.RIGHT
                            }
                        )
                    }

                    // Average line (dashed)
                    val avgY = paddingTop + usableHeight * (1f - (average - minLength) / range)
                    drawLine(
                        color = Teal500.copy(alpha = 0.5f),
                        start = Offset(0f, avgY * animatedProgress.coerceIn(0f, 1f)),
                        end = Offset(chartWidth * animatedProgress, avgY),
                        strokeWidth = 2f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(10f, 6f)
                        )
                    )

                    // Line path
                    if (cycleLengths.size >= 2) {
                        val stepX = chartWidth / (cycleLengths.size - 1).coerceAtLeast(1)
                        val points = cycleLengths.mapIndexed { index, length ->
                            val x = index * stepX
                            val normalizedY = (length - minLength).toFloat() / range
                            val y = paddingTop + usableHeight * (1f - normalizedY)
                            Offset(x, y)
                        }

                        // Draw smooth line
                        val path = Path()
                        val visibleCount = (points.size * animatedProgress).toInt().coerceAtLeast(1)

                        for (i in 0 until visibleCount.coerceAtMost(points.size)) {
                            if (i == 0) {
                                path.moveTo(points[i].x, points[i].y)
                            } else {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val cpx = (prev.x + curr.x) / 2f
                                path.cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = Rose500,
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )

                        // Data points
                        for (i in 0 until visibleCount.coerceAtMost(points.size)) {
                            val isSelected = i == selectedIndex
                            val radius = if (isSelected) 8f else 5f

                            drawCircle(
                                color = Color.White,
                                radius = radius + 2f,
                                center = points[i]
                            )
                            drawCircle(
                                color = if (isSelected) Gold500 else Rose500,
                                radius = radius,
                                center = points[i]
                            )
                        }

                        // Selected tooltip
                        if (selectedIndex in cycleLengths.indices) {
                            val point = points[selectedIndex]
                            val label = "${cycleLengths[selectedIndex]} days"
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                point.x,
                                point.y - 16f,
                                android.graphics.Paint().apply {
                                    color = 0xFF262626.toInt()
                                    textSize = with(density) { 12.sp.toPx() }
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Trend analysis card
        val trend = analyzeCycleTrend(cycleLengths)
        PetalCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Trend Analysis",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trend,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Symptom Frequency Grouped Bar Chart ──────────────────────────────────────

@Composable
private fun SymptomFrequencyChart(
    entries: List<CycleEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.size < 2) {
        EmptyChartPlaceholder(message = "Log more cycles with symptoms to see frequency patterns")
        return
    }

    // Group entries by estimated phase
    val phaseEntries = entries.groupBy { entry ->
        estimatePhaseForEntry(entry)
    }

    val symptomsByPhase = CyclePhase.entries.associateWith { phase ->
        val phaseData = phaseEntries[phase] ?: emptyList()
        mapOf(
            "Pain" to phaseData.count { it.painLevel != SymptomLevel.None },
            "Cramps" to phaseData.count { it.crampsLevel != SymptomLevel.None },
            "Cravings" to phaseData.count { it.cravingsLevel != SymptomLevel.None },
            "Headaches" to phaseData.count { it.headachesLevel != SymptomLevel.None },
            "Mood" to phaseData.count { it.moodLevel != MoodLevel.Calm }
        )
    }

    val symptoms = listOf("Pain", "Cramps", "Cravings", "Headaches", "Mood")
    val phaseColors = mapOf(
        CyclePhase.Menstrual to Rose500,
        CyclePhase.Follicular to Teal500,
        CyclePhase.Ovulation to Gold500,
        CyclePhase.Luteal to Lavender500
    )

    val maxCount = symptomsByPhase.values.flatMap { it.values }.maxOrNull()?.coerceAtLeast(1) ?: 1

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "bar_chart_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        PetalCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Symptom Frequency by Phase",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "How often each symptom appears per phase",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CyclePhase.entries.forEach { phase ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(phaseColors[phase] ?: Rose500)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                phase.display,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Grouped bar chart
                val density = LocalDensity.current

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .semantics {
                            contentDescription = "Grouped bar chart showing symptom frequency by cycle phase"
                        }
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height
                    val paddingBottom = 60f
                    val paddingLeft = 0f
                    val usableWidth = chartWidth - paddingLeft
                    val usableHeight = chartHeight - paddingBottom

                    val groupWidth = usableWidth / symptoms.size
                    val barWidth = (groupWidth * 0.7f) / CyclePhase.entries.size
                    val barSpacing = (groupWidth * 0.3f) / (CyclePhase.entries.size + 1)

                    symptoms.forEachIndexed { symptomIdx, symptom ->
                        val groupStartX = paddingLeft + symptomIdx * groupWidth

                        CyclePhase.entries.forEachIndexed { phaseIdx, phase ->
                            val count = symptomsByPhase[phase]?.get(symptom) ?: 0
                            val barHeight = (count.toFloat() / maxCount) * usableHeight * animatedProgress

                            val x = groupStartX + barSpacing * (phaseIdx + 1) + barWidth * phaseIdx
                            val y = usableHeight - barHeight

                            drawRoundRect(
                                color = phaseColors[phase] ?: Rose500,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            // Count label above bar
                            if (count > 0 && animatedProgress > 0.9f) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    "$count",
                                    x + barWidth / 2,
                                    y - 6f,
                                    android.graphics.Paint().apply {
                                        color = 0xFF737373.toInt()
                                        textSize = with(density) { 9.sp.toPx() }
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                )
                            }
                        }

                        // Symptom label
                        drawContext.canvas.nativeCanvas.drawText(
                            symptom,
                            groupStartX + groupWidth / 2,
                            chartHeight - 10f,
                            android.graphics.Paint().apply {
                                color = 0xFF525252.toInt()
                                textSize = with(density) { 11.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Symptom insights
        val topSymptom = symptoms.maxByOrNull { symptom ->
            symptomsByPhase.values.sumOf { it[symptom] ?: 0 }
        }
        val menstrualTopSymptom = symptoms.maxByOrNull { symptom ->
            symptomsByPhase[CyclePhase.Menstrual]?.get(symptom) ?: 0
        }

        PetalCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Symptom Insights",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (topSymptom != null) {
                    Text(
                        text = "Your most common symptom across all phases is $topSymptom.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (menstrualTopSymptom != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "During your menstrual phase, $menstrualTopSymptom is most frequent.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Prediction Accuracy ──────────────────────────────────────────────────────

@Composable
private fun PredictionAccuracySection(
    entries: List<CycleEntry>,
    modifier: Modifier = Modifier
) {
    if (entries.size < 3) {
        EmptyChartPlaceholder(message = "Log at least 3 cycles to track prediction accuracy")
        return
    }

    val sortedEntries = entries.sortedBy { it.start }
    val accuracyData = mutableListOf<Pair<Int, Int>>() // (cycle index, days off)

    for (i in 2 until sortedEntries.size) {
        val prev1 = sortedEntries[i - 2]
        val prev2 = sortedEntries[i - 1]
        val actual = sortedEntries[i]

        // Simple prediction: average of last two cycle lengths
        val predictedLength = (prev1.cycleLength + prev2.cycleLength) / 2
        val predictedStart = LocalDate.parse(prev2.start).plusDays(predictedLength.toLong())
        val actualStart = LocalDate.parse(actual.start)

        val daysOff = java.time.temporal.ChronoUnit.DAYS.between(predictedStart, actualStart)
            .toInt()
        val absDaysOff = kotlin.math.abs(daysOff)

        accuracyData.add(Pair(i - 2, absDaysOff))
    }

    val avgAccuracy = if (accuracyData.isNotEmpty()) {
        accuracyData.map { it.second }.average()
    } else 0.0

    val withinOneDay = accuracyData.count { it.second <= 1 }
    val withinThreeDays = accuracyData.count { it.second <= 3 }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "accuracy_progress"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Accuracy overview
        PetalCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Prediction Accuracy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "How close our predictions were to actual period starts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AccuracyStatCard(
                        value = String.format("%.1f", avgAccuracy),
                        unit = "days",
                        label = "Avg. offset",
                        color = if (avgAccuracy <= 2) Teal500 else Gold500
                    )
                    AccuracyStatCard(
                        value = "$withinOneDay",
                        unit = "/${accuracyData.size}",
                        label = "Within 1 day",
                        color = Teal500
                    )
                    AccuracyStatCard(
                        value = "$withinThreeDays",
                        unit = "/${accuracyData.size}",
                        label = "Within 3 days",
                        color = Lavender500
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Accuracy bar chart
                val density = LocalDensity.current
                val maxDaysOff = accuracyData.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .semantics {
                            contentDescription = "Bar chart showing prediction accuracy across cycles"
                        }
                ) {
                    val chartWidth = size.width
                    val chartHeight = size.height
                    val paddingBottom = 30f
                    val usableHeight = chartHeight - paddingBottom

                    val barWidth = (chartWidth / accuracyData.size) * 0.6f
                    val barSpacing = (chartWidth / accuracyData.size) * 0.4f

                    // 3-day threshold line
                    val thresholdY = usableHeight * (1f - 3f / maxDaysOff)
                    drawLine(
                        color = Teal500.copy(alpha = 0.3f),
                        start = Offset(0f, thresholdY),
                        end = Offset(chartWidth, thresholdY),
                        strokeWidth = 1f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(8f, 4f)
                        )
                    )

                    accuracyData.forEachIndexed { index, (_, daysOff) ->
                        val barHeight = (daysOff.toFloat() / maxDaysOff) * usableHeight * animatedProgress
                        val x = index * (barWidth + barSpacing) + barSpacing / 2

                        val barColor = when {
                            daysOff <= 1 -> Teal500
                            daysOff <= 3 -> Gold500
                            else -> Rose500
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, usableHeight - barHeight),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Day offset label
                        if (animatedProgress > 0.9f) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "${daysOff}d",
                                x + barWidth / 2,
                                usableHeight - barHeight - 6f,
                                android.graphics.Paint().apply {
                                    color = 0xFF525252.toInt()
                                    textSize = with(density) { 9.sp.toPx() }
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                            )
                        }

                        // Cycle label
                        drawContext.canvas.nativeCanvas.drawText(
                            "C${index + 3}",
                            x + barWidth / 2,
                            chartHeight - 6f,
                            android.graphics.Paint().apply {
                                color = 0xFF737373.toInt()
                                textSize = with(density) { 9.sp.toPx() }
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Accuracy explanation
        PetalCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "About Accuracy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = buildString {
                        append("Predictions improve as you log more cycles. ")
                        if (avgAccuracy <= 2) {
                            append("Your predictions are highly accurate, averaging within ${String.format("%.1f", avgAccuracy)} days.")
                        } else if (avgAccuracy <= 4) {
                            append("Your predictions are moderately accurate. Continue logging to improve precision.")
                        } else {
                            append("Your cycle has some variability. Bayesian analysis helps account for this by weighting recent cycles more heavily.")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AccuracyStatCard(
    value: String,
    unit: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = color.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Monthly Summary ──────────────────────────────────────────────────────────

@Composable
private fun MonthlySummarySection(
    entries: List<CycleEntry>,
    onExport: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedEntries = entries.sortedByDescending { it.start }
    val groupedByMonth = sortedEntries.groupBy { entry ->
        YearMonth.from(LocalDate.parse(entry.start))
    }.toSortedMap(compareByDescending { it })

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Export buttons
        PetalCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "Export Data",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onExport("csv") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV")
                    }
                    OutlinedButton(
                        onClick = { onExport("json") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export JSON")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly cards
        groupedByMonth.forEach { (yearMonth, monthEntries) ->
            val monthName = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            val year = yearMonth.year
            val avgLength = monthEntries.map { it.cycleLength }.average().toInt()
            val symptomCount = monthEntries.count { entry ->
                entry.painLevel != SymptomLevel.None ||
                        entry.crampsLevel != SymptomLevel.None ||
                        entry.headachesLevel != SymptomLevel.None
            }

            PetalCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$monthName $year",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                "${monthEntries.size} entries",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${avgLength}d",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Teal500
                            )
                            Text(
                                "Avg. cycle",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "$symptomCount",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Rose500
                            )
                            Text(
                                "Symptom days",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val dominantMood = monthEntries
                                .groupBy { it.moodLevel }
                                .maxByOrNull { it.value.size }?.key
                            Text(
                                dominantMood?.display ?: "N/A",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Lavender500
                            )
                            Text(
                                "Top mood",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// ── Shared helpers ───────────────────────────────────────────────────────────

@Composable
private fun StatChip(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ShowChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Neutral300
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun estimatePhaseForEntry(entry: CycleEntry): CyclePhase {
    val cycleLength = entry.cycleLength
    val startDate = LocalDate.parse(entry.start)
    val endDate = LocalDate.parse(entry.end)
    val periodLength = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1

    // Use the midpoint of the period as the cycle day for classification
    val midDay = periodLength / 2 + 1
    val follicularEnd = maxOf(6, (cycleLength * 0.46).toInt())
    val ovulationEnd = minOf(cycleLength, follicularEnd + 2)

    return when {
        midDay <= 5 -> CyclePhase.Menstrual
        midDay <= follicularEnd -> CyclePhase.Follicular
        midDay <= ovulationEnd -> CyclePhase.Ovulation
        else -> CyclePhase.Luteal
    }
}

private fun analyzeCycleTrend(lengths: List<Int>): String {
    if (lengths.size < 3) return "Not enough data for trend analysis."

    val recentHalf = lengths.takeLast(lengths.size / 2)
    val olderHalf = lengths.take(lengths.size / 2)

    val recentAvg = recentHalf.average()
    val olderAvg = olderHalf.average()
    val diff = recentAvg - olderAvg

    val spread = lengths.max() - lengths.min()

    return buildString {
        when {
            diff > 2 -> append("Your cycles are trending longer recently (averaging ${recentAvg.toInt()} days vs. ${olderAvg.toInt()} days earlier). ")
            diff < -2 -> append("Your cycles are trending shorter recently (averaging ${recentAvg.toInt()} days vs. ${olderAvg.toInt()} days earlier). ")
            else -> append("Your cycle length has been relatively stable (averaging around ${lengths.average().toInt()} days). ")
        }

        when {
            spread <= 3 -> append("With a spread of only $spread days, your cycles are highly regular.")
            spread <= 6 -> append("A $spread-day spread is normal. Most cycles vary by a few days.")
            else -> append("A $spread-day spread suggests some variability. Factors like stress, sleep, and exercise can influence cycle length.")
        }
    }
}

private fun exportMonthlySummary(
    context: Context,
    entries: List<CycleEntry>,
    format: String
) {
    val sorted = entries.sortedBy { it.start }

    val content = when (format) {
        "csv" -> {
            val header = "Start,End,Cycle Length,Flow,Pain,Cramps,Cravings,Mood,Headaches"
            val rows = sorted.joinToString("\n") { entry ->
                "${entry.start},${entry.end},${entry.cycleLength},${entry.flowIntensity.display}," +
                        "${entry.painLevel.display},${entry.crampsLevel.display}," +
                        "${entry.cravingsLevel.display},${entry.moodLevel.display}," +
                        "${entry.headachesLevel.display}"
            }
            "$header\n$rows"
        }
        else -> {
            val jsonEntries = sorted.map { entry ->
                mapOf(
                    "start" to entry.start,
                    "end" to entry.end,
                    "cycleLength" to entry.cycleLength.toString(),
                    "flow" to entry.flowIntensity.display,
                    "pain" to entry.painLevel.display,
                    "cramps" to entry.crampsLevel.display,
                    "cravings" to entry.cravingsLevel.display,
                    "mood" to entry.moodLevel.display,
                    "headaches" to entry.headachesLevel.display
                )
            }
            Json { prettyPrint = true }.encodeToString(jsonEntries)
        }
    }

    try {
        val fileName = "petal_cycle_data_${LocalDate.now()}.${format}"
        val file = File(context.cacheDir, fileName)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = if (format == "csv") "text/csv" else "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Export Cycle Data"))
    } catch (e: Exception) {
        android.util.Log.w("CycleTrends", "Export failed", e)
    }
}
