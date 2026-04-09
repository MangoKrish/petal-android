package com.petal.app.ui.screens.partner

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
import com.petal.app.ui.viewmodel.PartnerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerSetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: PartnerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Partner") },
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
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            PetalCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Share with your partner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your partner will see a simplified dashboard with contextual advice based on your cycle phase. " +
                                "They won't see your raw data -- just helpful insights about how to support you.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        uiState.error!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            PetalTextField(
                value = uiState.inviteName,
                onValueChange = { viewModel.updateInviteName(it) },
                label = "Partner's name",
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PetalTextField(
                value = uiState.inviteEmail,
                onValueChange = { viewModel.updateInviteEmail(it) },
                label = "Partner's email",
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            PetalTextField(
                value = uiState.inviteNote,
                onValueChange = { viewModel.updateInviteNote(it) },
                label = "Personal note (optional)",
                singleLine = false,
                maxLines = 3,
                leadingIcon = { Icon(Icons.Default.Note, null) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Caregiver mode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "For parents/guardians of teens. Shows age-appropriate content.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = uiState.isCaregiver,
                    onCheckedChange = { viewModel.updateIsCaregiver(it) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            PetalButton(
                text = "Send invitation",
                onClick = { viewModel.sendInvite(onNavigateBack) },
                isLoading = uiState.isSending,
                enabled = uiState.inviteName.isNotBlank() && uiState.inviteEmail.isNotBlank()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Existing connections
            if (uiState.partnerConnections.isNotEmpty()) {
                Text(
                    "Connected Partners",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                uiState.partnerConnections.forEach { connection ->
                    PetalCard {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    connection.partnerName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    connection.partnerEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${connection.status.display}${if (connection.isCaregiver) " (Caregiver)" else ""}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            IconButton(onClick = { viewModel.removePartner(connection.id) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
