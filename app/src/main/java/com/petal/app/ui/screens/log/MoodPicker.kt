package com.petal.app.ui.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.MoodLevel

@Composable
fun MoodPicker(
    selected: MoodLevel,
    onSelect: (MoodLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Mood",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MoodLevel.entries.forEach { mood ->
                val isSelected = mood == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(mood) },
                    label = { Text(mood.display) }
                )
            }
        }
    }
}
