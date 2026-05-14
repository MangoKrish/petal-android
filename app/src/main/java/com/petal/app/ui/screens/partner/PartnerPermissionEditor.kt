package com.petal.app.ui.screens.partner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.SharePermissions
import com.petal.app.ui.components.PetalCard

/**
 * PHASE_6_7_PLAN.md §6B.2 — mirrors new-project/src/components/partner-permission-editor.tsx.
 *
 * NOTE on the catalog mismatch: web/API support a 7-permission catalog
 * (`view_phase`, `view_cycle_dates`, `view_predictions`, `view_symptoms`,
 * `view_mood`, `view_wellness`, `view_unwell_pings`). Android's existing
 * `SharePermissions` model carries 4 booleans. We render the four available
 * controls; the broader catalog will land here when the partner API contract
 * is reconciled in a follow-up. The starter / standard / deep / custom
 * profile presets still apply within those 4.
 */
@Composable
fun PartnerPermissionEditor(
    permissions: SharePermissions,
    onChange: (SharePermissions) -> Unit,
    onSave: (() -> Unit)? = null,
    isSaving: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val activeProfile = remember(permissions) { profileFor(permissions) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "WHAT THIS PERSON SEES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "You can change this anytime — nothing's shared by default.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Profile presets
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Profile.entries.forEach { p ->
                val active = activeProfile == p
                Surface(
                    onClick = { p.permissions?.let(onChange) },
                    enabled = p.permissions != null,
                    shape = RoundedCornerShape(12.dp),
                    color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(p.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            p.hint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Per-permission checkboxes
        PetalCard {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PermissionCheckbox(
                    label = "Cycle dates",
                    hint = "Period start, next period prediction",
                    checked = permissions.cycleLength,
                    onChange = { onChange(permissions.copy(cycleLength = it)) }
                )
                PermissionCheckbox(
                    label = "Predictions",
                    hint = "Fertile window, ovulation estimate",
                    checked = permissions.predictions,
                    onChange = { onChange(permissions.copy(predictions = it)) }
                )
                PermissionCheckbox(
                    label = "Latest period",
                    hint = "Most recent period start date",
                    checked = permissions.latestPeriod,
                    onChange = { onChange(permissions.copy(latestPeriod = it)) }
                )
                PermissionCheckbox(
                    label = "Symptoms",
                    hint = "Last 7 days of symptoms",
                    checked = permissions.symptoms,
                    onChange = { onChange(permissions.copy(symptoms = it)) }
                )
            }
        }

        if (onSave != null) {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSaving) "Saving…" else "Save changes")
            }
        }
    }
}

@Composable
private fun PermissionCheckbox(
    label: String,
    hint: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(checked = checked, onCheckedChange = onChange, modifier = Modifier.padding(top = 0.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private enum class Profile(
    val label: String,
    val hint: String,
    val permissions: SharePermissions?,
) {
    Starter(
        label = "Starter",
        hint = "Predictions only — phase-style guidance, no dates",
        permissions = SharePermissions(
            latestPeriod = false,
            cycleLength = false,
            symptoms = false,
            predictions = true,
        ),
    ),
    Standard(
        label = "Standard (recommended)",
        hint = "Cycle dates + predictions — what most partners want",
        permissions = SharePermissions(
            latestPeriod = true,
            cycleLength = true,
            symptoms = false,
            predictions = true,
        ),
    ),
    Deep(
        label = "Deep",
        hint = "+ symptoms — for partners helping closely",
        permissions = SharePermissions(
            latestPeriod = true,
            cycleLength = true,
            symptoms = true,
            predictions = true,
        ),
    ),
    Custom(
        label = "Custom",
        hint = "Pick exactly what's shared",
        permissions = null,
    );
}

private fun profileFor(p: SharePermissions): Profile {
    Profile.entries.forEach { profile ->
        if (profile.permissions != null && profile.permissions == p) return profile
    }
    return Profile.Custom
}
