package com.petal.app.ui.screens.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.viewmodel.GroupsViewModel

private val SHARE_LEVELS = listOf(
    "nothing" to ("Nothing" to "Presence only — no scoreboard contribution"),
    "wellness_only" to ("Wellness only" to "Default — water, sleep, exercise summaries"),
    "phase" to ("+ Phase" to "Your current cycle phase, no dates"),
    "cycle_dates" to ("+ Cycle dates" to "Period start, next-period prediction"),
    "symptoms" to ("+ Symptoms" to "Last 7 days of symptoms"),
)

private val RANGES = listOf("day" to "Today", "week" to "This week", "month" to "This month")

private val RANK_COPY = listOf("leading", "right behind", "warming up", "consistent", "showing up")

/**
 * PHASE_6_7_PLAN.md §6B.3 — group detail. Scoreboard + sharing controls +
 * unwell-ping + members + leave/disband. Mirrors web group-detail.tsx with
 * the same kind framing for ranks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onNavigateBack: () -> Unit,
    viewModel: GroupsViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsState()
    LaunchedEffect(groupId) { viewModel.openGroup(groupId) }

    var unwellOpen by remember { mutableStateOf(false) }
    var unwellMessage by remember { mutableStateOf("") }
    var showLeave by remember { mutableStateOf(false) }
    var showDisband by remember { mutableStateOf(false) }

    val group = ui.groups.firstOrNull { it.id == groupId }
    val isOwner = group?.createdBy != null && group.createdBy == ui.currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(group?.name ?: "Group") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.closeGroup()
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (group == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header card with join code
            PetalCard {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            (if (group.emoji != null) "${group.emoji} " else "") + group.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${group.memberCount} of ${group.maxMembers}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "JOIN CODE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                group.joinCode,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Scoreboard
            PetalCard {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SCOREBOARD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Wellness · ${RANGES.firstOrNull { it.first == ui.range }?.second ?: "This week"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RANGES.forEach { (id, label) ->
                            FilterChip(
                                selected = ui.range == id,
                                onClick = { viewModel.changeRange(id) },
                                label = { Text(label) },
                                shape = RoundedCornerShape(999.dp)
                            )
                        }
                    }

                    if (ui.isLoadingDetail) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else if (ui.scoreboard.isEmpty()) {
                        Text(
                            "No logs yet — get a few in and the scoreboard fills up softly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        ui.scoreboard.forEachIndexed { idx, entry ->
                            val isMe = entry.userId == ui.currentUserId
                            val rankCopy = RANK_COPY[idx.coerceAtMost(RANK_COPY.size - 1)]
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            buildString {
                                                append(entry.displayName ?: entry.username?.let { "@$it" } ?: "Member")
                                                if (isMe) append(" (you)")
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            entry.totalScore.toString(),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        buildString {
                                            append("$rankCopy · hydration ${entry.hydrationStreakDays}d · sleep ${"%.1f".format(entry.avgSleepHours)}h · move ${entry.totalExerciseMinutes}m")
                                            if (entry.quizAnswered > 0) {
                                                append(" · quiz ${entry.quizCorrect}/${entry.quizAnswered}")
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Share controls + unwell ping
            PetalCard {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("WHAT YOU SHARE WITH THIS GROUP", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Sharing settings · just for ${group.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Cycle data is opt-in — wellness scoreboard contributions only need 'wellness only' or higher.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    SHARE_LEVELS.forEach { (id, labels) ->
                        val (label, hint) = labels
                        val active = group.myShareLevel == id
                        Surface(
                            onClick = { viewModel.updateShareLevel(group.id, id) },
                            enabled = ui.pendingActionId != group.id,
                            shape = RoundedCornerShape(12.dp),
                            color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = group.myReceiveUnwellPings,
                                onCheckedChange = { viewModel.toggleReceivePings(group.id, it) }
                            )
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Receive 'I'm not feeling great' pings", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "Soft notifications when a friend in this group needs gentleness",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (unwellOpen) {
                        OutlinedTextField(
                            value = unwellMessage,
                            onValueChange = { if (it.length <= 300) unwellMessage = it },
                            placeholder = { Text("add a quick note (optional)") },
                            minLines = 2, maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.fireUnwell(group.id, unwellMessage.ifBlank { null })
                                    unwellMessage = ""
                                    unwellOpen = false
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Send to opted-in friends") }
                            OutlinedButton(
                                onClick = { unwellOpen = false },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                        }
                    } else {
                        OutlinedButton(
                            onClick = { unwellOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("I'm not feeling great today") }
                    }

                    ui.pingResultMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                msg,
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Members
            PetalCard {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MEMBERS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "${group.memberCount} ${if (group.memberCount == 1) "person" else "people"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (ui.members.isEmpty()) {
                        Text("Loading members…", style = MaterialTheme.typography.bodySmall)
                    } else {
                        ui.members.forEach { m ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            m.displayName ?: m.username?.let { "@$it" } ?: "member",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (m.userId == group.createdBy) {
                                            Text("CREATOR", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else if (m.userId == ui.currentUserId) {
                                            Text("(you)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (isOwner && m.userId != ui.currentUserId) {
                                        IconButton(onClick = { viewModel.removeMember(group.id, m.userId) }) {
                                            Icon(Icons.Default.Close, contentDescription = "Remove")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showLeave = true },
                    modifier = Modifier.weight(1f)
                ) { Text("Leave group") }
                if (isOwner) {
                    OutlinedButton(
                        onClick = { showDisband = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("Disband") }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showLeave) {
        AlertDialog(
            onDismissRequest = { showLeave = false },
            title = { Text("Leave this group?") },
            text = { Text("You'll stop seeing the scoreboard and unwell pings. You can rejoin later with the code.") },
            confirmButton = {
                TextButton(onClick = {
                    showLeave = false
                    viewModel.leaveGroup(groupId, onNavigateBack)
                }) { Text("Leave", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showLeave = false }) { Text("Cancel") } }
        )
    }

    if (showDisband) {
        AlertDialog(
            onDismissRequest = { showDisband = false },
            title = { Text("Disband this group?") },
            text = { Text("Everyone is removed and the group is deleted. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDisband = false
                    viewModel.disbandGroup(groupId, onNavigateBack)
                }) { Text("Disband", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDisband = false }) { Text("Cancel") } }
        )
    }
}
