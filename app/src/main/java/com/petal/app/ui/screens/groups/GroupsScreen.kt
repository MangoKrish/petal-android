package com.petal.app.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.viewmodel.GroupsViewModel

/**
 * PHASE_6_7_PLAN.md §6B.3 — top-level Groups list. Mirrors the web
 * GroupsTab. Tapping a group routes to GroupDetailScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onNavigateBack: () -> Unit,
    onOpenGroup: (String) -> Unit,
    viewModel: GroupsViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var showJoin by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Groups") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Small circles · wellness only by default · cycle data is always opt-in.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        showCreate = !showCreate
                        showJoin = false
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(if (showCreate) "Cancel" else "Create a group") }
                OutlinedButton(
                    onClick = {
                        showJoin = !showJoin
                        showCreate = false
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(if (showJoin) "Cancel" else "Join with a code") }
            }
            if (showCreate) {
                CreateGroupForm(
                    onCreate = { name, emoji ->
                        viewModel.createGroup(name, emoji) { ok, err ->
                            if (ok) showCreate = false
                            // Errors surface via ui.error
                        }
                    },
                )
            }
            if (showJoin) {
                JoinGroupForm(
                    onJoin = { code ->
                        viewModel.joinGroup(code) { ok, err ->
                            if (ok) showJoin = false
                        }
                    },
                )
            }

            ui.error?.let { msg ->
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                    Text(msg, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }

            if (ui.isLoading && ui.groups.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Text("Loading groups…")
                }
            } else if (ui.groups.isEmpty()) {
                PetalCard {
                    Text(
                        "No groups yet — start one above, or join with a code from a friend.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                ui.groups.forEach { group ->
                    PetalCard(onClick = { onOpenGroup(group.id) }) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = (if (group.emoji != null) "${group.emoji} " else "") + group.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    "${group.memberCount} of ${group.maxMembers}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "sharing: ${shareSummary(group.myShareLevel)}" +
                                    (if (!group.myReceiveUnwellPings) " · unwell pings off" else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateGroupForm(onCreate: (name: String, emoji: String?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    PetalCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("New group", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 50) name = it },
                singleLine = true,
                placeholder = { Text("Group name (e.g. quiet club)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = emoji,
                onValueChange = { if (it.length <= 8) emoji = it },
                singleLine = true,
                placeholder = { Text("Emoji (optional)") },
                modifier = Modifier.fillMaxWidth(0.5f)
            )
            Button(
                onClick = { onCreate(name, emoji.ifBlank { null }) },
                enabled = name.isNotBlank()
            ) { Text("Create group") }
        }
    }
}

@Composable
private fun JoinGroupForm(onJoin: (code: String) -> Unit) {
    var code by remember { mutableStateOf("") }
    PetalCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Join with code", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 12) code = it.uppercase() },
                singleLine = true,
                placeholder = { Text("ABCD1234") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onJoin(code) },
                enabled = code.isNotBlank()
            ) { Text("Join group") }
        }
    }
}

private fun shareSummary(level: String): String = when (level) {
    "nothing" -> "nothing"
    "wellness_only" -> "wellness only"
    "phase" -> "wellness + phase"
    "cycle_dates" -> "wellness + cycle dates"
    "symptoms" -> "wellness + symptoms"
    else -> level
}
