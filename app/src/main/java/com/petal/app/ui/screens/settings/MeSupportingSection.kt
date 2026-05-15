package com.petal.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
 */
@Composable
fun MeSupportingSection(
    connections: List<SupportingConnection>,
    onOpenConnection: (connectionId: String) -> Unit = {},
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenConnection(c.connectionId) }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    Text(
                        "open ›",
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
