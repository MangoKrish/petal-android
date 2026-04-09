package com.petal.app.ui.screens.dashboard

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.model.FlagSeverity
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.components.PhaseGradientBackground
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

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Rose500)
        }
        return
    }

    PhaseGradientBackground(phase = uiState.currentPhase) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Greeting
            Text(
                text = uiState.insights?.greeting ?: "Hello, ${uiState.userName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cycle Ring
            CycleRing(
                cycleDay = uiState.cycleDay,
                cycleLength = uiState.cycleLengthAvg,
                phase = uiState.currentPhase,
                daysUntilPeriod = uiState.daysUntilNextPeriod
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Phase Card
            PhaseCard(
                phase = uiState.currentPhase,
                hormoneNote = uiState.insights?.hormoneNote ?: ""
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Prediction Card
            PredictionCard(
                nextPeriodDate = uiState.nextPeriodDate,
                ovulationDate = uiState.ovulationDate,
                fertileWindowStart = uiState.fertileWindowStart,
                fertileWindowEnd = uiState.fertileWindowEnd,
                confidence = uiState.confidence
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pattern Flags
            if (uiState.patternFlags.isNotEmpty()) {
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Quick Actions
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
                    label = "Log",
                    color = Rose500,
                    onClick = onNavigateToQuickLog,
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

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Insight Preview
            uiState.insights?.cards?.firstOrNull()?.let { card ->
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
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
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
}
