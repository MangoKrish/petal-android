package com.petal.app.ui.screens.log

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.petal.app.ui.components.PetalCard

/**
 * Collapsible "Today's fertility signals" section for QuickLogScreen.
 * PHASE_6_7_PLAN.md §6A.2. Mirrors the web QuickLogSheet additions.
 */
@Composable
fun FertilitySection(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    temperature: String,
    cervicalMucus: String?,
    ovulationPain: Boolean,
    lhTestResult: String?,
    sexualActivity: Boolean,
    onTemperatureChange: (String) -> Unit,
    onMucusChange: (String?) -> Unit,
    onOvulationPainChange: (Boolean) -> Unit,
    onLhTestChange: (String?) -> Unit,
    onSexualActivityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    PetalCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Today's fertility signals",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Optional — sharper fertile-window estimate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggleExpanded) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    // Temperature
                    Text(
                        "Waking temperature (°C)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = temperature,
                        onValueChange = onTemperatureChange,
                        placeholder = { Text("36.50") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Cervical mucus
                    Text(
                        "Cervical mucus",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ChipRow(
                        options = listOf("dry", "sticky", "creamy", "watery", "egg_white"),
                        labels = mapOf("egg_white" to "egg-white"),
                        selected = cervicalMucus,
                        onSelect = onMucusChange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // LH test
                    Text(
                        "LH test (if you took one)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    ChipRow(
                        options = listOf("positive", "negative", "inconclusive"),
                        labels = emptyMap(),
                        selected = lhTestResult,
                        onSelect = onLhTestChange
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = ovulationPain, onCheckedChange = onOvulationPainChange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ovulation twinge today", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(checked = sexualActivity, onCheckedChange = onSexualActivityChange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sexual activity today", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipRow(
    options: List<String>,
    labels: Map<String, String>,
    selected: String?,
    onSelect: (String?) -> Unit
) {
    FlowRowSpaced(
        items = options,
        spacing = 6.dp
    ) { option ->
        FilterChip(
            selected = option == selected,
            onClick = { onSelect(if (option == selected) null else option) },
            label = { Text(labels[option] ?: option) },
            shape = RoundedCornerShape(999.dp)
        )
    }
}

/**
 * Light-weight wrap row. Compose Foundation's FlowRow exists but requires an
 * extra import; this keeps deps minimal.
 */
@Composable
private fun <T> FlowRowSpaced(
    items: List<T>,
    spacing: androidx.compose.ui.unit.Dp,
    content: @Composable (T) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items.forEach { content(it) }
    }
}
