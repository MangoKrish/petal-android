package com.petal.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.local.UserDao
import com.petal.app.data.model.OnboardingData
import com.petal.app.data.repository.AuthRepository
import com.petal.app.ui.components.PetalButton
import com.petal.app.ui.theme.Rose500
import com.petal.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var step by remember { mutableIntStateOf(0) }
    var lastPeriodStart by remember { mutableStateOf(LocalDate.now().minusDays(14)) }
    var periodLength by remember { mutableIntStateOf(5) }
    var cycleLength by remember { mutableIntStateOf(28) }
    var selectedGoals by remember { mutableStateOf(setOf<String>()) }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }

    val scope = rememberCoroutineScope()

    val totalSteps = 4

    val goals = listOf(
        "Track my cycle", "Predict my period", "Monitor symptoms",
        "Share with partner", "Understand my body", "Plan pregnancy",
        "Avoid pregnancy", "Track for teen/child"
    )

    val symptoms = listOf(
        "Cramps", "Headaches", "Bloating", "Mood swings",
        "Fatigue", "Back pain", "Breast tenderness", "Acne",
        "Cravings", "Insomnia", "Nausea"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Progress indicator
        LinearProgressIndicator(
            progress = { (step + 1).toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(MaterialTheme.shapes.small),
            color = Rose500,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Step ${step + 1} of $totalSteps",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith
                        slideOutHorizontally { -it } + fadeOut()
            },
            label = "onboarding_step"
        ) { currentStep ->
            when (currentStep) {
                0 -> OnboardingCycleStep(
                    lastPeriodStart = lastPeriodStart,
                    onLastPeriodStartChange = { lastPeriodStart = it },
                    periodLength = periodLength,
                    onPeriodLengthChange = { periodLength = it },
                    cycleLength = cycleLength,
                    onCycleLengthChange = { cycleLength = it }
                )
                1 -> OnboardingGoalsStep(
                    goals = goals,
                    selectedGoals = selectedGoals,
                    onToggleGoal = {
                        selectedGoals = if (it in selectedGoals) selectedGoals - it
                        else selectedGoals + it
                    }
                )
                2 -> OnboardingSymptomsStep(
                    symptoms = symptoms,
                    selectedSymptoms = selectedSymptoms,
                    onToggleSymptom = {
                        selectedSymptoms = if (it in selectedSymptoms) selectedSymptoms - it
                        else selectedSymptoms + it
                    }
                )
                3 -> OnboardingCompleteStep()
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        if (step < totalSteps - 1) {
            PetalButton(
                text = "Continue",
                onClick = { step++ }
            )
        } else {
            PetalButton(
                text = "Get started",
                onClick = {
                    scope.launch {
                        authViewModel.run {
                            // Mark onboarding as complete
                        }
                    }
                    onComplete()
                }
            )
        }

        if (step > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = { step-- }) {
                Text("Back")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OnboardingCycleStep(
    lastPeriodStart: LocalDate,
    onLastPeriodStartChange: (LocalDate) -> Unit,
    periodLength: Int,
    onPeriodLengthChange: (Int) -> Unit,
    cycleLength: Int,
    onCycleLengthChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Let's learn about your cycle",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "This helps us give you accurate predictions. Don't worry if you're not sure -- estimates are fine!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text("When did your last period start?", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = lastPeriodStart.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Simple date adjustment buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(-7, -3, -1, 1).forEach { offset ->
                val label = if (offset < 0) "${-offset}d ago" else "+${offset}d"
                FilterChip(
                    selected = false,
                    onClick = { onLastPeriodStartChange(lastPeriodStart.plusDays(offset.toLong())) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("How long does your period usually last?", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledIconButton(
                onClick = { if (periodLength > 2) onPeriodLengthChange(periodLength - 1) },
                modifier = Modifier.size(40.dp)
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                "$periodLength days",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            FilledIconButton(
                onClick = { if (periodLength < 10) onPeriodLengthChange(periodLength + 1) },
                modifier = Modifier.size(40.dp)
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("How long is your full cycle? (period to period)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledIconButton(
                onClick = { if (cycleLength > 15) onCycleLengthChange(cycleLength - 1) },
                modifier = Modifier.size(40.dp)
            ) {
                Text("-", style = MaterialTheme.typography.titleLarge)
            }
            Text(
                "$cycleLength days",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            FilledIconButton(
                onClick = { if (cycleLength < 45) onCycleLengthChange(cycleLength + 1) },
                modifier = Modifier.size(40.dp)
            ) {
                Text("+", style = MaterialTheme.typography.titleLarge)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Not sure? 28 days is a common starting point.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OnboardingGoalsStep(
    goals: List<String>,
    selectedGoals: Set<String>,
    onToggleGoal: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "What are your goals?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Select all that apply. This helps us personalize your experience.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            goals.forEach { goal ->
                val isSelected = goal in selectedGoals
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleGoal(goal) },
                    label = { Text(goal) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun OnboardingSymptomsStep(
    symptoms: List<String>,
    selectedSymptoms: Set<String>,
    onToggleSymptom: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Which symptoms do you experience?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Select any that you typically notice. We'll track these for you.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            symptoms.forEach { symptom ->
                val isSelected = symptom in selectedSymptoms
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleSymptom(symptom) },
                    label = { Text(symptom) },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun OnboardingCompleteStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "You're all set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Petal is ready to help you understand your cycle. " +
                    "The more you log, the smarter your predictions get.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Tip: Share with your partner",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Petal's partner dashboard gives your partner or caregiver " +
                            "contextual advice based on where you are in your cycle. " +
                            "You can set this up in Settings any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
