package com.petal.app.ui.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.SymptomLevel

@Composable
fun SymptomPicker(
    label: String,
    selected: SymptomLevel,
    onSelect: (SymptomLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SymptomLevel.entries.forEach { level ->
                val isSelected = level == selected
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(level) },
                    label = {
                        Text(
                            level.display,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
