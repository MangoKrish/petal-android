package com.petal.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.SupportingConnection
import com.petal.app.ui.components.PetalCard

/**
 * PHASE_6_7_PLAN.md §6A.1 — primary users who are also supporting someone
 * else see them here in the Me/Settings tab. Mirrors
 * new-project/src/components/me-supporting-section.tsx.
 *
 * Renders nothing when the list is empty — keeps the Me tab clean for the
 * common case. The full dedicated supporter shell at SupporterDashboardScreen
 * is unchanged; this is a lighter row-style variant scoped to Settings.
 *
 * TODO: rows are intentionally non-interactive — a supporter-facing partner
 * dashboard surface doesn't exist yet, and routing into the primary-facing
 * partner view would just show the user their own data. Wire up tap →
 * "view this primary's shared data" once that surface lands.
 */
@Composable
fun MeSupportingSection(
    connections: List<SupportingConnection>,
) {
    if (connections.isEmpty()) return

    PetalCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "ALSO SUPPORTING",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (connections.size == 1) "1 person you check in on"
                else "${connections.size} people you check in on",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            connections.forEach { c ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Text(
                        c.primaryName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        c.roleLabel ?: "your person",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "You only see what each person has chosen to share.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
