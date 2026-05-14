package com.petal.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.User
import com.petal.app.data.model.UserRole
import com.petal.app.ui.components.PetalCard

/**
 * PHASE_6_7_PLAN.md §6A.1 — mirrors new-project/src/components/identity-settings-card.tsx.
 *   - shows the user's current role + handle + display name
 *   - lets them switch role (with a confirmation dialog)
 *   - lets them edit handle and display name
 */
@Composable
fun IdentitySettingsCard(
    user: User?,
    onSwitchRole: (UserRole, () -> Unit) -> Unit,
    onUpdateUsername: (String, (Boolean, String?) -> Unit) -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentRole = UserRole.fromString(user?.role)
    val originalHandle = user?.username ?: ""
    val originalName = user?.displayName ?: user?.name ?: ""

    var handle by remember(originalHandle) { mutableStateOf(originalHandle) }
    var displayName by remember(originalName) { mutableStateOf(originalName) }
    var handleError by remember { mutableStateOf<String?>(null) }
    var handleSavedTick by remember { mutableStateOf(false) }
    var nameSavedTick by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var pending by remember { mutableStateOf(false) }

    PetalCard(modifier = modifier) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text(
                    "YOUR IDENTITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "How you show up to anyone you connect with",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Role
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "YOUR ROLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (currentRole == UserRole.Primary) "Primary user" else "Supporter",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                TextButton(onClick = { showConfirm = true }, enabled = !pending) {
                    Text("Switch role")
                }
            }

            if (showConfirm) {
                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    title = {
                        Text(
                            if (currentRole == UserRole.Primary)
                                "Switch to supporter?"
                            else
                                "Switch to primary user?"
                        )
                    },
                    text = {
                        Text(
                            if (currentRole == UserRole.Primary)
                                "You'll lose access to cycle tracking. Your existing logs stay on your account but won't be visible from the supporter shell."
                            else
                                "You'll get access to cycle tracking, logs, and predictions."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            pending = true
                            val target = if (currentRole == UserRole.Primary) UserRole.Supporter else UserRole.Primary
                            onSwitchRole(target) {
                                pending = false
                                showConfirm = false
                            }
                        }) { Text("Yes, switch") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirm = false }) { Text("Cancel") }
                    }
                )
            }

            // Handle
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "YOUR HANDLE (OTHERS FIND YOU WITH THIS)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("@", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = handle,
                        onValueChange = { handle = it.lowercase() },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done
                        ),
                        placeholder = { Text("softpetal_4172") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            handleError = null
                            handleSavedTick = false
                            onUpdateUsername(handle) { ok, err ->
                                if (ok) {
                                    handleSavedTick = true
                                } else {
                                    handleError = err
                                }
                            }
                        },
                        enabled = handle != originalHandle && handle.isNotBlank()
                    ) { Text("Save") }
                }
                handleError?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                if (handleSavedTick) {
                    Text(
                        "Saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "3–32 lowercase letters, numbers, or underscores. Nothing identifying unless you want it.",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Display name
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "DISPLAY NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { if (it.length <= 50) displayName = it },
                        singleLine = true,
                        placeholder = { Text("How Petal greets you") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            onUpdateDisplayName(displayName)
                            nameSavedTick = true
                        },
                        enabled = displayName.trim() != originalName.trim() && displayName.isNotBlank()
                    ) { Text("Save") }
                }
                if (nameSavedTick) {
                    Text(
                        "Saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
