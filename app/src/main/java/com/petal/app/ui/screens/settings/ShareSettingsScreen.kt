package com.petal.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalButton
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.components.PetalTextField
import com.petal.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var label by remember { mutableStateOf("My shared summary") }
    var showCycleLength by remember { mutableStateOf(true) }
    var showNextPeriod by remember { mutableStateOf(true) }
    var showSymptoms by remember { mutableStateOf(false) }
    var showPhase by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Links") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, "Create link")
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Share a read-only summary of your cycle with someone you trust.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.shareLinks.isEmpty()) {
                PetalCard {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No share links yet",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Create a link to share a summary with your partner or caregiver.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                uiState.shareLinks.forEach { link ->
                    PetalCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        link.label,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        if (link.active) "Active" else "Revoked",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (link.active) MaterialTheme.colorScheme.secondary
                                        else MaterialTheme.colorScheme.error
                                    )
                                }
                                if (link.active) {
                                    IconButton(onClick = { viewModel.revokeShareLink(link.id) }) {
                                        Icon(
                                            Icons.Default.LinkOff,
                                            contentDescription = "Revoke",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (link.showCycleLength) ShareBadge("Cycle")
                                if (link.showNextPeriod) ShareBadge("Period")
                                if (link.showSymptoms) ShareBadge("Symptoms")
                                if (link.showPhase) ShareBadge("Phase")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PetalButton(
                text = "Create new share link",
                onClick = { showCreateDialog = true },
                isOutlined = true
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create share link") },
            text = {
                Column {
                    PetalTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = "Link label"
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("What to share:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    CheckboxItem("Cycle length", showCycleLength) { showCycleLength = it }
                    CheckboxItem("Next period date", showNextPeriod) { showNextPeriod = it }
                    CheckboxItem("Symptoms", showSymptoms) { showSymptoms = it }
                    CheckboxItem("Current phase", showPhase) { showPhase = it }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createShareLink(label, showCycleLength, showNextPeriod, showSymptoms, showPhase)
                    showCreateDialog = false
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ShareBadge(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun CheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}
