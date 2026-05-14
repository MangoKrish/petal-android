package com.petal.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.CycleMode
import com.petal.app.ui.components.PetalCard

/**
 * PHASE_6_7_PLAN.md §6A.2 — mirrors new-project/src/components/cycle-mode-toggle.tsx.
 * Changes the wording around the fertile window without changing the math.
 */
@Composable
fun CycleModeToggle(
    selected: CycleMode,
    onSelected: (CycleMode) -> Unit,
    modifier: Modifier = Modifier
) {
    PetalCard(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Cycle mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "How should Petal frame your fertile window? This changes the wording, never the math.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            CycleMode.entries.forEach { mode ->
                ModeOption(
                    mode = mode,
                    selected = mode == selected,
                    onClick = { onSelected(mode) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (selected == CycleMode.AvoidingPregnancy) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Text(
                        "Petal isn't contraception. If you're trying to avoid pregnancy, please use a reliable method alongside cycle tracking.",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeOption(
    mode: CycleMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    val (label, hint) = when (mode) {
        CycleMode.Tracking -> "Tracking" to "Just keeping an eye on my body"
        CycleMode.TryingToConceive -> "Trying to conceive" to "Highlight the days most likely for conception"
        CycleMode.AvoidingPregnancy -> "Avoiding pregnancy" to "Wider caution band; this isn't contraception"
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
        ),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
