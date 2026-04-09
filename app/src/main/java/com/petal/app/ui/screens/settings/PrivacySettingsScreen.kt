package com.petal.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petal.app.ui.components.PetalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy") },
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

            PetalCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Your data, your control",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Petal takes your privacy seriously. Here's how we handle your data:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrivacyItem(
                icon = Icons.Default.Lock,
                title = "Encrypted storage",
                description = "All your cycle data is encrypted at rest on your device and in transit to our servers."
            )

            PrivacyItem(
                icon = Icons.Default.VisibilityOff,
                title = "No selling of data",
                description = "We never sell your health data to third parties. Period."
            )

            PrivacyItem(
                icon = Icons.Default.PersonOff,
                title = "Partner access is limited",
                description = "Partners only see what you explicitly share. They never have access to your raw data or detailed symptom logs unless you allow it."
            )

            PrivacyItem(
                icon = Icons.Default.PhoneAndroid,
                title = "Offline-first",
                description = "Your data is stored locally on your device first. Cloud sync is optional and can be disabled."
            )

            PrivacyItem(
                icon = Icons.Default.Delete,
                title = "Easy data deletion",
                description = "You can delete all your data at any time from the Settings page. This is permanent and irreversible."
            )

            PrivacyItem(
                icon = Icons.Default.Security,
                title = "Security question recovery",
                description = "Password recovery uses a security question instead of email, so we don't need to send emails that could reveal you use a period tracker."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacyItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    PetalCard(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
