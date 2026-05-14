package com.petal.app.ui.screens.partner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.viewmodel.AuthViewModel

/**
 * Supporter shell home screen. PHASE_6_7_PLAN.md §6A.1.
 * Mirrors new-project/src/components/supporter-dashboard.tsx + supporter-empty-state.tsx.
 * Shows the supporter's handle so a primary can find them; once invited,
 * they'll see a list of "supporting [Name]" cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupporterDashboardScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val user by viewModel.currentUser.collectAsState()
    val displayName = user?.displayName ?: user?.name ?: "there"
    val username = user?.username

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Supporting") })
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

            Text(
                "Hi, ${displayName.lowercase()}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                "You're not yet linked to anyone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Empty-state card with copyable handle
            EmptyStateCard(username = username, displayName = user?.displayName)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "You only see what each person has chosen to share — nothing more, nothing by default.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun EmptyStateCard(username: String?, displayName: String?) {
    val ctx = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    PetalCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "WAITING FOR SOMEONE TO ADD YOU",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You're all set up — share your handle so they can find you",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "When a primary user invites you with this handle, you'll see what they've chosen to share — never anything more, never by default.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (username != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            "@$username",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("Petal handle", "@$username"))
                                copied = true
                            }
                        ) {
                            Text(if (copied) "Copied" else "Copy")
                        }
                    }
                }
            } else {
                Text(
                    "Your handle is being set up — check back in a moment.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (displayName != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "You'll show up as \"$displayName\" when you're added.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
