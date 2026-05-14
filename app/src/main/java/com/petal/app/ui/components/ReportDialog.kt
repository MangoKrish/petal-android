package com.petal.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.ui.viewmodel.ModerationViewModel

/**
 * PHASE_6_7_PLAN.md §6B.1 — reusable report dialog. Mirrors
 * new-project/src/components/report-modal.tsx. Mounted on every Android
 * surface that shows a handle so reports always carry context.
 */
@Composable
fun ReportDialog(
    open: Boolean,
    context: String,
    subjectLabel: String,
    reportedUsername: String? = null,
    reportedUserId: String? = null,
    onDismiss: () -> Unit,
    viewModel: ModerationViewModel = hiltViewModel(),
) {
    if (!open) return

    val reasons = remember(context) { reasonsFor(context) }
    var reason by remember(open) { mutableStateOf<String?>(null) }
    var details by remember(open) { mutableStateOf("") }
    var error by remember(open) { mutableStateOf<String?>(null) }
    var success by remember(open) { mutableStateOf(false) }
    val ui by viewModel.ui.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report $subjectLabel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Your report goes to a small moderation queue. We don't share who reported what.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                reasons.forEach { (id, label) ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = reason == id, onClick = { reason = id })
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedTextField(
                    value = details,
                    onValueChange = { if (it.length <= 2000) details = it },
                    label = { Text("anything else? (optional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let { msg ->
                    Text(
                        msg,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (success) {
                    Text(
                        "Thanks — we've got it.",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val r = reason
                    if (r == null) {
                        error = "Pick a reason for the report."
                        return@TextButton
                    }
                    error = null
                    viewModel.submitReport(
                        context = context,
                        reason = r,
                        details = details.takeIf { it.isNotBlank() },
                        reportedUsername = reportedUsername,
                        reportedUserId = reportedUserId,
                    ) { ok, msg ->
                        if (ok) {
                            success = true
                            // Auto-close shortly after success.
                            onDismiss()
                        } else {
                            error = msg ?: "Couldn't file the report."
                        }
                    }
                },
                enabled = !ui.isSubmittingReport,
            ) { Text(if (ui.isSubmittingReport) "Sending…" else "Send report") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !ui.isSubmittingReport) { Text("Cancel") }
        },
    )
}

private fun reasonsFor(context: String): List<Pair<String, String>> = when (context) {
    "handle" -> listOf(
        "harassing_handle" to "Harassing or hateful handle",
        "impersonation" to "Impersonating someone",
        "spam" to "Spam or scam handle",
        "other" to "Other",
    )
    "partner_connection" -> listOf(
        "unwanted_contact" to "Unwanted contact",
        "harassment" to "Harassment",
        "impersonation" to "Impersonating someone I know",
        "other" to "Other",
    )
    "story_share" -> listOf(
        "factually_wrong" to "Factually wrong / misattributed",
        "right_of_publicity" to "Subject didn't consent to share",
        "harmful_framing" to "Harmful framing",
        "other" to "Other",
    )
    else -> listOf(
        "harassment" to "Harassment",
        "self_harm" to "Self-harm content",
        "spam" to "Spam",
        "other" to "Other",
    )
}
