package com.petal.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.EducationAudience
import com.petal.app.data.model.EducationCards
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * PHASE_6_7_PLAN.md §6A.3 — replaces the pulse-only skeleton header during
 * loading states with a single short education tip. Rotates every 6 seconds
 * if the load takes longer.
 */
@Composable
fun LoadingTip(
    audience: EducationAudience = EducationAudience.All,
    modifier: Modifier = Modifier,
) {
    val tips = remember(audience) { EducationCards.loadingTips(audience) }
    if (tips.isEmpty()) return

    var index by remember(tips) { mutableStateOf(Random.nextInt(tips.size)) }

    LaunchedEffect(tips) {
        if (tips.size <= 1) return@LaunchedEffect
        while (true) {
            delay(6_000)
            index = (index + 1) % tips.size
        }
    }

    val tip = tips[index]

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "WHILE YOU WAIT · DID YOU KNOW?",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                tip.bodyShort,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
