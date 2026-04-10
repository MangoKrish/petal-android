package com.petal.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.petal.app.R
import com.petal.app.ui.theme.*

data class QuickLogData(
    val flow: String = "",
    val moods: List<String> = emptyList(),
    val symptoms: List<String> = emptyList()
)

private val MOODS = listOf(
    "Happy" to "\uD83D\uDE0A",
    "Calm" to "\uD83D\uDE0C",
    "Sad" to "\uD83D\uDE22",
    "Irritable" to "\uD83D\uDE24",
    "Anxious" to "\uD83D\uDE30",
    "Tired" to "\uD83E\uDD71",
    "Loving" to "\uD83E\uDD70",
    "Meh" to "\uD83D\uDE10"
)

private val SYMPTOMS = listOf(
    "Cramps" to "\uD83D\uDD25",
    "Headache" to "\uD83E\uDD15",
    "Bloating" to "\uD83D\uDE2E\u200D\uD83D\uDCA8",
    "Fatigue" to "\uD83D\uDCA4",
    "Cravings" to "\uD83C\uDF6B",
    "Back pain" to "\uD83D\uDE23",
    "Mood swings" to "\uD83D\uDE36",
    "Insomnia" to "\uD83C\uDF19"
)

private val FLOWS = listOf("Spotting", "Light", "Medium", "Heavy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickLogBottomSheet(
    isVisible: Boolean,
    currentDay: Int,
    phase: String,
    onDismiss: () -> Unit,
    onLogPeriod: (String) -> Unit,
    onLogMoodSymptoms: (List<String>, List<String>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mode by remember { mutableStateOf("idle") } // idle, period, mood, done
    var selectedFlow by remember { mutableStateOf("") }
    var selectedMoods by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedSymptoms by remember { mutableStateOf<Set<String>>(emptySet()) }

    if (!isVisible) return

    ModalBottomSheet(
        onDismissRequest = {
            mode = "idle"
            selectedFlow = ""
            selectedMoods = emptySet()
            selectedSymptoms = emptySet()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        AnimatedContent(
            targetState = mode,
            transitionSpec = {
                fadeIn(tween(250)) + slideInHorizontally { it / 3 } togetherWith
                    fadeOut(tween(200)) + slideOutHorizontally { -it / 3 }
            },
            label = "sheet_mode"
        ) { currentMode ->
            when (currentMode) {
                "done" -> {
                    // Success state with Lottie celebration
                    val composition by rememberLottieComposition(
                        LottieCompositionSpec.RawRes(R.raw.celebration)
                    )
                    val lottieProgress by animateLottieCompositionAsState(
                        composition = composition,
                        iterations = 1,
                        speed = 1.2f
                    )
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        mode = "idle"
                        onDismiss()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Lottie celebration behind
                        LottieAnimation(
                            composition = composition,
                            progress = { lottieProgress },
                            modifier = Modifier.size(200.dp)
                        )
                        // Text overlay
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Saved",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Logged!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Thanks for checking in today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                "period" -> {
                    // Flow selector
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "How's your flow?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { mode = "idle" }) {
                                Icon(Icons.Default.Close, "Cancel")
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FLOWS.forEach { flow ->
                                val selected = selectedFlow == flow
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedFlow = flow },
                                    label = { Text(flow) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                onLogPeriod(selectedFlow.ifEmpty { "Medium" })
                                mode = "done"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            enabled = selectedFlow.isNotEmpty()
                        ) {
                            Text("Log period", modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                "mood" -> {
                    // Mood & symptom selector
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "How do you feel?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { mode = "idle" }) {
                                Icon(Icons.Default.Close, "Cancel")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "MOOD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MOODS.forEach { (label, emoji) ->
                                val selected = label in selectedMoods
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedMoods = if (selected)
                                            selectedMoods - label
                                        else
                                            selectedMoods + label
                                    },
                                    label = { Text("$emoji $label") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "SYMPTOMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SYMPTOMS.forEach { (label, emoji) ->
                                val selected = label in selectedSymptoms
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedSymptoms = if (selected)
                                            selectedSymptoms - label
                                        else
                                            selectedSymptoms + label
                                    },
                                    label = { Text("$emoji $label") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                onLogMoodSymptoms(selectedMoods.toList(), selectedSymptoms.toList())
                                mode = "done"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            enabled = selectedMoods.isNotEmpty() || selectedSymptoms.isNotEmpty()
                        ) {
                            Text("Save check-in", modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }

                else -> {
                    // Idle — show quick action choices
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            "Day $currentDay \u00B7 $phase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "How are you feeling today?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { mode = "period" },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Period started", modifier = Modifier.padding(vertical = 4.dp))
                            }
                            OutlinedButton(
                                onClick = { mode = "mood" },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Text("Log mood", modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
