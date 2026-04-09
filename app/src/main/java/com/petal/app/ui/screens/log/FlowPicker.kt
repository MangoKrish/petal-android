package com.petal.app.ui.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.FlowIntensity
import com.petal.app.ui.theme.Rose200
import com.petal.app.ui.theme.Rose400
import com.petal.app.ui.theme.Rose600

@Composable
fun FlowPicker(
    selected: FlowIntensity,
    onSelect: (FlowIntensity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "Flow intensity",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlowIntensity.entries.forEach { flow ->
                val isSelected = flow == selected
                val color = when (flow) {
                    FlowIntensity.Light -> Rose200
                    FlowIntensity.Medium -> Rose400
                    FlowIntensity.Heavy -> Rose600
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { onSelect(flow) },
                    label = { Text(flow.display) },
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color,
                        selectedLabelColor = if (flow == FlowIntensity.Heavy) androidx.compose.ui.graphics.Color.White
                        else MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    }
}
