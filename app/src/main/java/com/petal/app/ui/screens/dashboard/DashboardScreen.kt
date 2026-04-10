package com.petal.app.ui.screens.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.model.FlagSeverity
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.components.PhaseGradientBackground
import com.petal.app.ui.components.QuickLogBottomSheet
import com.petal.app.ui.theme.*
import com.petal.app.ui.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToInsights: () -> Unit,
    onNavigateToRecommendations: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToEducation: () -> Unit,
    onNavigateToQuickLog: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQuickLogSheet by remember { mutableStateOf(false) }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    // Stagger animation state
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Bottom sheet for quick log
    QuickLogBottomSheet(
        isVisible = showQuickLogSheet,
        currentDay = uiState.cycleDay,
        phase = uiState.currentPhase.display,
        onDismiss = { showQuickLogSheet = false },
        onLogPeriod = { flow ->
            viewModel.logQuickPeriod(flow)
            showQuickLogSheet = false
        },
        onLogMoodSymptoms = { moods, symptoms ->
            viewModel.logQuickMoodSymptoms(moods, symptoms)
            showQuickLogSheet = false
        }
    )

    PhaseGradientBackground(phase = uiState.currentPhase) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Greeting with fade
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -20 }
            ) {
                Text(
                    text = uiState.insights?.greeting ?: "Hello, ${uiState.userName}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cycle Ring with scale-in
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(800, delayMillis = 200)) + scaleIn(
                    tween(800, delayMillis = 200),
                    initialScale = 0.8f
                )
            ) {
                CycleRing(
                    cycleDay = uiState.cycleDay,
                    cycleLength = uiState.cycleLengthAvg,
                    phase = uiState.currentPhase,
                    daysUntilPeriod = uiState.daysUntilNextPeriod
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Phase Card with slide
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 400)) + slideInVertically(tween(500, delayMillis = 400)) { 30 }
            ) {
                PhaseCard(
                    phase = uiState.currentPhase,
                    hormoneNote = uiState.insights?.hormoneNote ?: ""
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Prediction Card
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 500)) + slideInVertically(tween(500, delayMillis = 500)) { 30 }
            ) {
                PredictionCard(
                    nextPeriodDate = uiState.nextPeriodDate,
                    ovulationDate = uiState.ovulationDate,
                    fertileWindowStart = uiState.fertileWindowStart,
                    fertileWindowEnd = uiState.fertileWindowEnd,
                    confidence = uiState.confidence
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pattern Flags
            if (uiState.patternFlags.isNotEmpty()) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, delayMillis = 600)) + slideInVertically(tween(500, delayMillis = 600)) { 30 }
                ) {
                    PetalCard {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Pattern Insights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            uiState.patternFlags.forEach { flag ->
                                val flagColor = when (flag.severity) {
                                    FlagSeverity.Info -> Teal500
                                    FlagSeverity.Watch -> Gold500
                                    FlagSeverity.Care -> Rose500
                                }
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.FiberManualRecord,
                                        contentDescription = null,
                                        tint = flagColor,
                                        modifier = Modifier
                                            .size(10.dp)
                                            .padding(top = 6.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            flag.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            flag.detail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Actions
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(500, delayMillis = 700)) + slideInVertically(tween(500, delayMillis = 700)) { 30 }
            ) {
                Column {
                    Text(
                        "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Add,
                            label = "Quick Log",
                            color = Rose500,
                            onClick = { showQuickLogSheet = true },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.Lightbulb,
                            label = "Insights",
                            color = Teal500,
                            onClick = onNavigateToInsights,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            label = "Trends",
                            color = Gold500,
                            onClick = onNavigateToCharts,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.School,
                            label = "Learn",
                            color = Lavender500,
                            onClick = onNavigateToEducation,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Insight Preview
            uiState.insights?.cards?.firstOrNull()?.let { card ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500, delayMillis = 800)) + slideInVertically(tween(500, delayMillis = 800)) { 30 }
                ) {
                    PetalCard(onClick = onNavigateToInsights) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Today's Tip",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                card.headline,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                card.tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "press_scale"
    )

    ElevatedCard(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier.scale(scale),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    LaunchedEffect(pressed) {
        if (pressed) {
            kotlinx.coroutines.delay(200)
            pressed = false
        }
    }
}
