package com.petal.app.ui.screens.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.domain.Recommendation
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.components.phaseColor
import com.petal.app.ui.viewmodel.InsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recommendations") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        val recs = uiState.recommendations ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "${recs.phase} Phase",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = phaseColor(uiState.currentPhase)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                recs.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            RecommendationSection(
                recommendation = recs.diet,
                icon = Icons.Default.Restaurant,
                color = phaseColor(uiState.currentPhase)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RecommendationSection(
                recommendation = recs.exercise,
                icon = Icons.Default.FitnessCenter,
                color = phaseColor(uiState.currentPhase)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RecommendationSection(
                recommendation = recs.sleep,
                icon = Icons.Default.Bedtime,
                color = phaseColor(uiState.currentPhase)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RecommendationSection(
                recommendation = recs.selfCare,
                icon = Icons.Default.Spa,
                color = phaseColor(uiState.currentPhase)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RecommendationSection(
    recommendation: Recommendation,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    PetalCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    recommendation.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            recommendation.items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = color.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(8.dp)
                            .padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        item,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
