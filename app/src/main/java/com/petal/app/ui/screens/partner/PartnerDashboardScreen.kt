package com.petal.app.ui.screens.partner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalButton
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.components.PhaseGradientBackground
import com.petal.app.ui.components.phaseColor
import com.petal.app.ui.screens.dashboard.CycleRing
import com.petal.app.ui.theme.*
import com.petal.app.ui.viewmodel.PartnerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerDashboardScreen(
    onNavigateToSetup: () -> Unit,
    onNavigateToCaregiver: () -> Unit,
    viewModel: PartnerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partner Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToSetup) {
                        Icon(Icons.Default.PersonAdd, "Invite partner")
                    }
                    IconButton(onClick = onNavigateToCaregiver) {
                        Icon(Icons.Default.ChildCare, "Caregiver mode")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Rose500)
            }
            return@Scaffold
        }

        PhaseGradientBackground(phase = uiState.currentPhase) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header
                Text(
                    "${uiState.trackerName}'s Cycle",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Cycle ring (smaller for partner view)
                CycleRing(
                    cycleDay = uiState.cycleDay,
                    cycleLength = uiState.cycleLength,
                    phase = uiState.currentPhase,
                    daysUntilPeriod = uiState.daysUntilPeriod,
                    modifier = Modifier.size(180.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Partner insight card - THE KEY FEATURE
                PartnerInsightCard(
                    phase = uiState.currentPhase,
                    cycleDay = uiState.cycleDay,
                    partnerNote = uiState.partnerNote
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Support Tips
                Text(
                    "How to support ${uiState.trackerName} today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                uiState.supportTips.forEachIndexed { index, tip ->
                    SupportTipCard(tip = tip, index = index)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Phase recommendations for partner
                uiState.recommendations?.let { recs ->
                    PetalCard {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "What ${uiState.trackerName} might need",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Diet tips (simplified for partner)
                            Text(
                                recs.diet.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = phaseColor(uiState.currentPhase),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            recs.diet.items.take(3).forEach { item ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("  ", style = MaterialTheme.typography.bodySmall)
                                    Text(item, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Exercise tips
                            Text(
                                recs.exercise.title,
                                style = MaterialTheme.typography.labelMedium,
                                color = phaseColor(uiState.currentPhase),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            recs.exercise.items.take(3).forEach { item ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("  ", style = MaterialTheme.typography.bodySmall)
                                    Text(item, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick context card
                PetalCard(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Understanding the cycle",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "The menstrual cycle has 4 phases, each with different hormonal influences on mood, energy, and physical symptoms. " +
                                    "Understanding these phases helps you be a better partner by knowing what to expect and how to help.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
