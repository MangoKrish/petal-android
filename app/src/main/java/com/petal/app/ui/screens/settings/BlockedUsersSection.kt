package com.petal.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.remote.dto.BlockedUserDto
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.viewmodel.ModerationViewModel

/**
 * PHASE_6_7_PLAN.md §6B.1 — inline settings section that lists blocked
 * handles and accepts a new handle to block. Mirrors
 * new-project/src/components/blocked-users-list.tsx.
 */
@Composable
fun BlockedUsersSection(
    viewModel: ModerationViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val ui by viewModel.ui.collectAsState()
    var pendingHandle by remember { mutableStateOf("") }
    var inlineError by remember { mutableStateOf<String?>(null) }

    PetalCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    "BLOCKED HANDLES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "People you don't want to hear from",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Blocked handles can't send you connection requests, see your group entries, or share stories with you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Block-a-handle form
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "BLOCK A HANDLE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("@", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = pendingHandle,
                        onValueChange = {
                            pendingHandle = it.lowercase()
                            inlineError = null
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done
                        ),
                        placeholder = { Text("handle to block") },
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            inlineError = null
                            viewModel.blockHandle(pendingHandle) { ok, err ->
                                if (ok) pendingHandle = ""
                                else inlineError = err
                            }
                        },
                        enabled = pendingHandle.length >= 3 && ui.savingBlockId == null
                    ) { Text("Block") }
                }
                inlineError?.let { msg ->
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
            }

            if (ui.isLoading && ui.blocks.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text("Loading blocks…", style = MaterialTheme.typography.bodySmall)
                }
            } else if (ui.blocks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "You haven't blocked anyone — that's a good thing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp).fillMaxWidth()
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ui.blocks.forEach { row -> BlockedRow(row, viewModel::removeBlock, ui.savingBlockId == row.id) }
                }
            }
        }
    }
}

@Composable
private fun BlockedRow(
    row: BlockedUserDto,
    onRemove: (String) -> Unit,
    isPending: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "@${row.username ?: "unknown"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (row.displayName != null) {
                    Text(
                        row.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(
                onClick = { onRemove(row.id) },
                enabled = !isPending
            ) { Text(if (isPending) "…" else "Unblock") }
            IconButton(onClick = { onRemove(row.id) }, enabled = !isPending) {
                Icon(Icons.Default.Close, contentDescription = "Remove block")
            }
        }
    }
}
