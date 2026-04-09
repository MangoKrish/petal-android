package com.petal.app.ui.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalButton
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.viewmodel.CycleLogViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogScreen(
    onNavigateBack: () -> Unit,
    viewModel: CycleLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.editingEntryId != null) "Edit Entry" else "Log Period") },
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
            // Error
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Dates
            PetalCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Period dates",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (uiState.editingEntryId != null) {
                            "Your saved details are loaded here so you can update them quickly."
                        } else {
                            "We remember your last logged details to save time. You can change anything below."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Start", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                uiState.startDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        // Date adjustment
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { viewModel.updateStartDate(uiState.startDate.minusDays(1)) },
                                modifier = Modifier.size(32.dp)
                            ) { Text("-") }
                            FilledTonalIconButton(
                                onClick = { viewModel.updateStartDate(uiState.startDate.plusDays(1)) },
                                modifier = Modifier.size(32.dp)
                            ) { Text("+") }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("End", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                uiState.endDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalIconButton(
                                onClick = { viewModel.updateEndDate(uiState.endDate.minusDays(1)) },
                                modifier = Modifier.size(32.dp)
                            ) { Text("-") }
                            FilledTonalIconButton(
                                onClick = { viewModel.updateEndDate(uiState.endDate.plusDays(1)) },
                                modifier = Modifier.size(32.dp)
                            ) { Text("+") }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cycle length
            PetalCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Cycle length",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledIconButton(
                            onClick = { viewModel.updateCycleLength(uiState.cycleLength - 1) },
                            modifier = Modifier.size(36.dp)
                        ) { Text("-") }
                        Text(
                            "${uiState.cycleLength} days",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        FilledIconButton(
                            onClick = { viewModel.updateCycleLength(uiState.cycleLength + 1) },
                            modifier = Modifier.size(36.dp)
                        ) { Text("+") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Flow
            FlowPicker(
                selected = uiState.flowIntensity,
                onSelect = { viewModel.updateFlowIntensity(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Symptoms
            Text(
                "Symptoms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            SymptomPicker(
                label = "Pain",
                selected = uiState.pain,
                onSelect = { viewModel.updatePain(it) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SymptomPicker(
                label = "Cramps",
                selected = uiState.cramps,
                onSelect = { viewModel.updateCramps(it) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SymptomPicker(
                label = "Cravings",
                selected = uiState.cravings,
                onSelect = { viewModel.updateCravings(it) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            SymptomPicker(
                label = "Headaches",
                selected = uiState.headaches,
                onSelect = { viewModel.updateHeadaches(it) }
            )
            Spacer(modifier = Modifier.height(12.dp))

            MoodPicker(
                selected = uiState.mood,
                onSelect = { viewModel.updateMood(it) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            PetalButton(
                text = if (uiState.editingEntryId != null) "Update entry" else "Save entry",
                onClick = { viewModel.save(onNavigateBack) },
                isLoading = uiState.isSaving
            )

            if (uiState.editingEntryId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                PetalButton(
                    text = "Delete entry",
                    onClick = { viewModel.deleteEntry(uiState.editingEntryId!!, onNavigateBack) },
                    isOutlined = true,
                    containerColor = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
