package com.petal.app.ui.screens.log

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalButton
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
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
    var showSuccess by remember { mutableStateOf(false) }

    // Success animation
    if (showSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            showSuccess = false
            onNavigateBack()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSymptomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.editingEntryId != null) "Edit Entry" else "Log Period",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                // Error
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Dates card with animation
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { -40 }
                ) {
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
                                    "We remember your last logged details to save time."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Start date
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
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilledTonalIconButton(
                                        onClick = { viewModel.updateStartDate(uiState.startDate.minusDays(1)) },
                                        modifier = Modifier.size(36.dp),
                                        shape = MaterialTheme.shapes.medium
                                    ) { Text("-", fontWeight = FontWeight.Bold) }
                                    FilledTonalIconButton(
                                        onClick = { viewModel.updateStartDate(uiState.startDate.plusDays(1)) },
                                        modifier = Modifier.size(36.dp),
                                        shape = MaterialTheme.shapes.medium
                                    ) { Text("+", fontWeight = FontWeight.Bold) }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                            // End date
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
                                        modifier = Modifier.size(36.dp),
                                        shape = MaterialTheme.shapes.medium
                                    ) { Text("-", fontWeight = FontWeight.Bold) }
                                    FilledTonalIconButton(
                                        onClick = { viewModel.updateEndDate(uiState.endDate.plusDays(1)) },
                                        modifier = Modifier.size(36.dp),
                                        shape = MaterialTheme.shapes.medium
                                    ) { Text("+", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cycle length with animated counter
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            FilledIconButton(
                                onClick = { viewModel.updateCycleLength(uiState.cycleLength - 1) },
                                modifier = Modifier.size(40.dp),
                                shape = MaterialTheme.shapes.medium
                            ) { Text("-", fontWeight = FontWeight.Bold) }

                            AnimatedContent(
                                targetState = uiState.cycleLength,
                                transitionSpec = {
                                    if (targetState > initialState) {
                                        slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
                                    } else {
                                        slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                                    }.using(SizeTransform(clip = false))
                                },
                                label = "cycle_length"
                            ) { length ->
                                Text(
                                    "$length days",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            FilledIconButton(
                                onClick = { viewModel.updateCycleLength(uiState.cycleLength + 1) },
                                modifier = Modifier.size(40.dp),
                                shape = MaterialTheme.shapes.medium
                            ) { Text("+", fontWeight = FontWeight.Bold) }
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
                    onClick = {
                        showSuccess = true
                        viewModel.save(onNavigateBack)
                    },
                    isLoading = uiState.isSaving
                )

                val entryIdToDelete = uiState.editingEntryId
                if (entryIdToDelete != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PetalButton(
                        text = "Delete entry",
                        onClick = { viewModel.deleteEntry(entryIdToDelete, onNavigateBack) },
                        isOutlined = true,
                        containerColor = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Success overlay
            AnimatedVisibility(
                visible = showSuccess,
                enter = fadeIn(tween(300)) + scaleIn(tween(300)),
                exit = fadeOut(tween(300)) + scaleOut(tween(300)),
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Saved",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Logged!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Thanks for checking in today.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
