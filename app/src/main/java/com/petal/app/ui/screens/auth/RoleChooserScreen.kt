package com.petal.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.model.UserRole
import com.petal.app.ui.viewmodel.AuthViewModel

/**
 * Pre-auth landing screen. PHASE_6_7_PLAN.md §6A.1.
 * Mirrors new-project/src/components/role-chooser.tsx — two cards, big touch
 * targets, warm copy. Tap selects a role and continues to signup.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleChooserScreen(
    onContinueToSignup: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Petal",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                "Before we begin — what brings you here?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            RoleCard(
                title = "I'm tracking my cycle",
                body = "Period dates, symptoms, mood, and gentle predictions — built around you.",
                onClick = {
                    viewModel.setPendingRole(UserRole.Primary)
                    onContinueToSignup()
                },
                accent = MaterialTheme.colorScheme.primaryContainer
            )

            RoleCard(
                title = "I'm supporting someone",
                body = "See what helps, learn the rhythms, and show up softly when it matters.",
                onClick = {
                    viewModel.setPendingRole(UserRole.Supporter)
                    onContinueToSignup()
                },
                accent = MaterialTheme.colorScheme.secondaryContainer
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "You can switch later in settings — no pressure either way.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RoleCard(
    title: String,
    body: String,
    onClick: () -> Unit,
    accent: androidx.compose.ui.graphics.Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = accent,
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 140.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Choose your space".uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Continue ›",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
