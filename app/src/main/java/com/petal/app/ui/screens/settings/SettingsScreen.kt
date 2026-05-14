package com.petal.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.kawaii.PetalStyle
import com.petal.app.ui.components.kawaii.PetalStylePicker
import com.petal.app.ui.theme.ThemeMode
import com.petal.app.ui.viewmodel.SettingsViewModel
import com.petal.app.ui.viewmodel.PetalStyleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToNotifications: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToSharing: () -> Unit,
    onNavigateToPremium: () -> Unit = {},
    onNavigateToReferral: () -> Unit = {},
    onNavigateToJournal: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // User profile card
            uiState.user?.let { user ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    user.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                user.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance
            Text(
                "Appearance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            ThemeModeSelector(
                selectedMode = uiState.themeMode,
                onSelected = viewModel::updateThemeMode
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kawaii: petal-style picker
            run {
                val styleVm: PetalStyleViewModel = hiltViewModel()
                val style by styleVm.style.collectAsState(initial = PetalStyle.SAKURA)
                val enabled by styleVm.enabled.collectAsState(initial = true)
                PetalStylePicker(
                    current = style,
                    enabled = enabled,
                    onSelectStyle = styleVm::setStyle,
                    onToggleEnabled = styleVm::setEnabled,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Identity (PHASE_6_7_PLAN.md §6A.1) — role, handle, display name
            Text(
                "Identity",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            IdentitySettingsCard(
                user = uiState.user,
                onSwitchRole = viewModel::switchRole,
                onUpdateUsername = viewModel::updateUsername,
                onUpdateDisplayName = viewModel::updateDisplayName
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PHASE_6_7_PLAN.md §6B.1 — block list
            BlockedUsersSection()

            Spacer(modifier = Modifier.height(24.dp))

            // Cycle predictions (PHASE_6_7_PLAN.md §6A.2)
            Text(
                "Cycle predictions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            CycleModeToggle(
                selected = uiState.cycleMode,
                onSelected = viewModel::updateCycleMode
            )

            Spacer(modifier = Modifier.height(12.dp))

            PredictionTransparencyPanel(
                cyclesUsed = uiState.cyclesUsed,
                averageCycleLength = uiState.averageCycleLength,
                minCycle = uiState.minCycle,
                maxCycle = uiState.maxCycle,
                nextPeriodSummary = uiState.nextPeriodSummary,
                fertilityWindowSummary = uiState.fertilityWindowSummary,
                cycleMode = uiState.cycleMode
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications & Sharing
            Text(
                "Notifications & Sharing",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsNavItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage reminders and alerts",
                onClick = onNavigateToNotifications
            )

            SettingsNavItem(
                icon = Icons.Default.Share,
                title = "Sharing",
                subtitle = "Manage share links",
                onClick = onNavigateToSharing
            )

            SettingsNavItem(
                icon = Icons.Default.Security,
                title = "Privacy",
                subtitle = "Data and privacy settings",
                onClick = onNavigateToPrivacy
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Features
            Text(
                "Features",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsNavItem(
                icon = Icons.Default.Diamond,
                title = "Petal Premium",
                subtitle = "Unlock unlimited features",
                onClick = onNavigateToPremium
            )

            SettingsNavItem(
                icon = Icons.Default.CardGiftcard,
                title = "Refer Friends",
                subtitle = "Share Petal, earn achievements",
                onClick = onNavigateToReferral
            )

            // PHASE_6_7_PLAN.md §6B.3 — friend groups + wellness scoreboard
            SettingsNavItem(
                icon = Icons.Default.Groups,
                title = "Friend groups",
                subtitle = "Wellness scoreboard with people you trust",
                onClick = onNavigateToGroups
            )

            SettingsNavItem(
                icon = Icons.Default.Book,
                title = "Wellness Journal",
                subtitle = "Your private reflection space",
                onClick = onNavigateToJournal
            )

            SettingsNavItem(
                icon = Icons.Default.EmojiEvents,
                title = "Achievements",
                subtitle = "Track your milestones",
                onClick = onNavigateToAchievements
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Account
            Text(
                "Account",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            SettingsNavItem(
                icon = Icons.AutoMirrored.Filled.Logout,
                title = "Sign out",
                subtitle = "Sign out of your account",
                onClick = { viewModel.logout(onLogout) }
            )

            SettingsNavItem(
                icon = Icons.Default.DeleteForever,
                title = "Delete account",
                subtitle = "Permanently delete all your data",
                onClick = { showDeleteDialog = true },
                isDestructive = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App info
            Text(
                "Petal v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                "The first period tracker built for couples & caregivers",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete account?") },
            text = { Text("This will permanently delete all your data, including cycle entries, settings, and partner connections. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(onLogout)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("App theme", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Choose light, dark, or follow your phone setting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                ThemeMode.values().toList().forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = selectedMode == mode,
                        onClick = { onSelected(mode) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ThemeMode.values().size
                        )
                    ) {
                        Text(
                            when (mode) {
                                ThemeMode.SYSTEM -> "System"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
