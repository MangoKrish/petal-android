package com.petal.app.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.FlowIntensity
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val FLOW_OPTIONS = listOf(FlowIntensity.Light, FlowIntensity.Medium, FlowIntensity.Heavy)
private val SYMPTOM_CHIPS = listOf(
    "🌸 cramps", "🍵 fatigue", "✨ bloating", "☁ headache", "❀ tender", "⌒ moody", "💧 acne"
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun CalendarQuickLogSheet(
    visible: Boolean,
    targetDate: LocalDate?,
    initialFlow: FlowIntensity? = null,
    initialSymptoms: List<String> = emptyList(),
    onSave: (flow: FlowIntensity?, symptoms: List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!visible || targetDate == null) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var flow by remember(targetDate) { mutableStateOf(initialFlow) }
    val selectedSymptoms = remember(targetDate) {
        mutableStateListOf<String>().apply { addAll(initialSymptoms) }
    }

    val niceDate = remember(targetDate) {
        val day = targetDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).lowercase()
        val date = targetDate.format(DateTimeFormatter.ofPattern("MMMM d", Locale.getDefault())).lowercase()
        "$day, $date"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                "quick log ♡",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                niceDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))

            Text("flow today ⋄", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FlowOptionPill(label = "spotting", selected = flow == null) { flow = null }
                FLOW_OPTIONS.forEach { f ->
                    FlowOptionPill(label = f.display.lowercase(), selected = flow == f) { flow = f }
                }
            }

            Spacer(Modifier.height(18.dp))
            Text("how does your body feel?", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SYMPTOM_CHIPS.forEach { s ->
                    val active = s in selectedSymptoms
                    SymptomChip(label = s, active = active) {
                        if (active) selectedSymptoms.remove(s) else selectedSymptoms.add(s)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onSave(flow, selectedSymptoms.toList()); onDismiss() },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            ) {
                Text("save with care ♡", fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun FlowOptionPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        )
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SymptomChip(label: String, active: Boolean, onClick: () -> Unit) {
    val bg = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val border = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
    }
}
