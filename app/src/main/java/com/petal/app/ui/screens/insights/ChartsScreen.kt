package com.petal.app.ui.screens.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
import com.petal.app.ui.viewmodel.InsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trends & Charts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.cycleLengths.isEmpty()) {
                PetalCard {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Not enough data yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Log a few cycles to see your trends visualized here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@Column
            }

            // Cycle Length Trend
            PetalCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Cycle Length Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Average: ${uiState.cycleLengthAvg} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LineChart(
                        data = uiState.cycleLengths.reversed().map { it.toFloat() },
                        lineColor = Rose500,
                        averageLine = uiState.cycleLengthAvg.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Period Length Trend
            PetalCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Period Length Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val avgPeriod = if (uiState.periodLengths.isNotEmpty()) {
                        uiState.periodLengths.average().toInt()
                    } else 0
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Average: $avgPeriod days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    BarChart(
                        data = uiState.periodLengths.reversed().map { it.toFloat() },
                        barColor = Teal500,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary stats
            PetalCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Cycle Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StatRow("Cycles logged", "${uiState.entries.size}")
                    StatRow("Average cycle", "${uiState.cycleLengthAvg} days")
                    if (uiState.cycleLengths.isNotEmpty()) {
                        StatRow("Shortest cycle", "${uiState.cycleLengths.min()} days")
                        StatRow("Longest cycle", "${uiState.cycleLengths.max()} days")
                        StatRow("Variation", "${uiState.cycleLengths.max() - uiState.cycleLengths.min()} days")
                    }
                    if (uiState.periodLengths.isNotEmpty()) {
                        StatRow("Average period", "${uiState.periodLengths.average().toInt()} days")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LineChart(
    data: List<Float>,
    lineColor: Color,
    averageLine: Float,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val avgLineColor = Neutral400

    Canvas(modifier = modifier) {
        val padding = 8.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        val minVal = (data.min() - 2).coerceAtLeast(0f)
        val maxVal = data.max() + 2
        val range = maxVal - minVal

        val stepX = if (data.size > 1) chartWidth / (data.size - 1) else chartWidth

        // Average line
        val avgY = padding + chartHeight * (1 - (averageLine - minVal) / range)
        drawLine(
            color = avgLineColor,
            start = Offset(padding, avgY),
            end = Offset(size.width - padding, avgY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                floatArrayOf(10f, 10f), 0f
            )
        )

        // Data line
        if (data.size >= 2) {
            val path = Path()
            data.forEachIndexed { index, value ->
                val x = padding + index * stepX
                val y = padding + chartHeight * (1 - (value - minVal) / range)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Data points
        data.forEachIndexed { index, value ->
            val x = padding + index * stepX
            val y = padding + chartHeight * (1 - (value - minVal) / range)
            drawCircle(Color.White, 5.dp.toPx(), Offset(x, y))
            drawCircle(lineColor, 3.5.dp.toPx(), Offset(x, y))
        }
    }
}

@Composable
private fun BarChart(
    data: List<Float>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    Canvas(modifier = modifier) {
        val padding = 8.dp.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        val maxVal = data.max() + 1
        val barWidth = (chartWidth / data.size) * 0.7f
        val gap = (chartWidth / data.size) * 0.3f

        data.forEachIndexed { index, value ->
            val barHeight = chartHeight * (value / maxVal)
            val x = padding + index * (barWidth + gap)
            val y = padding + chartHeight - barHeight

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
