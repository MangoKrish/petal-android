package com.petal.app.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.model.*
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
import com.petal.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Supported import/export formats for cycle data portability.
 */
enum class DataFormat(val display: String, val extension: String, val mimeType: String) {
    CSV("CSV", "csv", "text/csv"),
    JSON("JSON", "json", "application/json")
}

/**
 * Represents a parsed cycle record from an external source.
 */
data class ImportedCycleRecord(
    val startDate: String,
    val endDate: String,
    val cycleLength: Int,
    val flow: String = "Medium",
    val symptoms: Map<String, String> = emptyMap(),
    val source: String = "unknown"
)

/**
 * Data export and import screen with:
 * - Export to CSV, JSON
 * - Import from Health Connect
 * - Import from Flo/Clue format
 * - GDPR data request
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showGdprDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var importStatus by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }

    // File picker for import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            scope.launch {
                val result = importFromFile(context, uri)
                importStatus = result
                isImporting = false
            }
        }
    }

    // Health Connect import (CSV from exported Health Connect data)
    val healthConnectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isImporting = true
            scope.launch {
                val result = importFromHealthConnect(context, uri)
                importStatus = result
                isImporting = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Privacy") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Export Section ────────────────────────────────────────────
            SectionHeader(
                title = "Export Your Data",
                subtitle = "Download all your cycle data in portable formats"
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExportFormatCard(
                    format = DataFormat.CSV,
                    icon = Icons.Default.TableChart,
                    color = Teal500,
                    description = "Spreadsheet-compatible. Works with Excel, Google Sheets, and most analytics tools.",
                    isExporting = isExporting,
                    onClick = {
                        scope.launch {
                            isExporting = true
                            exportData(context, DataFormat.CSV)
                            isExporting = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                ExportFormatCard(
                    format = DataFormat.JSON,
                    icon = Icons.Default.Code,
                    color = Lavender500,
                    description = "Machine-readable. Ideal for developers or transferring to other apps.",
                    isExporting = isExporting,
                    onClick = {
                        scope.launch {
                            isExporting = true
                            exportData(context, DataFormat.JSON)
                            isExporting = false
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Import Section ───────────────────────────────────────────
            SectionHeader(
                title = "Import Data",
                subtitle = "Bring in data from other apps or Health Connect"
            )

            Spacer(modifier = Modifier.height(12.dp))

            ImportOptionCard(
                title = "Health Connect",
                subtitle = "Import MenstruationRecord data from Android Health Connect",
                icon = Icons.Default.HealthAndSafety,
                color = Teal500,
                isLoading = isImporting,
                onClick = {
                    healthConnectLauncher.launch(arrayOf("text/csv", "application/json", "*/*"))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ImportOptionCard(
                title = "Import from CSV/JSON",
                subtitle = "Import from Flo, Clue, or any standard cycle data file",
                icon = Icons.Default.UploadFile,
                color = Gold500,
                isLoading = isImporting,
                onClick = {
                    filePickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/json", "*/*"))
                }
            )

            // Import status message
            if (importStatus != null) {
                Spacer(modifier = Modifier.height(8.dp))
                PetalCard(
                    containerColor = if (importStatus!!.startsWith("Success"))
                        Teal100 else Rose100
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (importStatus!!.startsWith("Success"))
                                Icons.Default.CheckCircle
                            else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (importStatus!!.startsWith("Success")) Teal500 else Rose500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            importStatus!!,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── GDPR Section ─────────────────────────────────────────────
            SectionHeader(
                title = "Privacy & GDPR",
                subtitle = "Exercise your data rights"
            )

            Spacer(modifier = Modifier.height(12.dp))

            PetalCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    GdprActionRow(
                        title = "Request full data export",
                        subtitle = "We will email you a complete copy of all data Petal stores about you within 30 days",
                        icon = Icons.Default.Download,
                        color = Teal500,
                        onClick = { showGdprDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    GdprActionRow(
                        title = "Request data correction",
                        subtitle = "If any stored data is inaccurate, request a correction",
                        icon = Icons.Default.Edit,
                        color = Gold500,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:privacy@petal.health")
                                putExtra(Intent.EXTRA_SUBJECT, "GDPR Data Correction Request")
                                putExtra(Intent.EXTRA_TEXT, "I would like to request correction of my personal data.\n\nPlease describe what needs to be corrected:\n")
                            }
                            context.startActivity(intent)
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    GdprActionRow(
                        title = "Request data deletion",
                        subtitle = "Permanently delete all data associated with your account",
                        icon = Icons.Default.DeleteForever,
                        color = Rose500,
                        onClick = { showDeleteDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    GdprActionRow(
                        title = "Data processing info",
                        subtitle = "View details about how Petal processes your health data",
                        icon = Icons.Default.Policy,
                        color = Lavender500,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://petal.health/privacy"))
                            context.startActivity(intent)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data storage info
            PetalCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = Teal500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Data Storage",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your cycle data is stored locally on your device using encrypted storage. " +
                                "When synced, data is encrypted in transit (TLS 1.3) and at rest (AES-256). " +
                                "Petal never sells or shares your health data with third parties. " +
                                "Partner sharing only sends curated tips, never raw health data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // GDPR request dialog
        if (showGdprDialog) {
            AlertDialog(
                onDismissRequest = { showGdprDialog = false },
                title = { Text("Request Full Data Export") },
                text = {
                    Text(
                        "Under GDPR Article 20, you have the right to receive your personal data in a portable format. " +
                                "We will compile all data associated with your account and email it to you within 30 days.\n\n" +
                                "This will be sent to the email address associated with your account."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showGdprDialog = false
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:privacy@petal.health")
                            putExtra(Intent.EXTRA_SUBJECT, "GDPR Data Portability Request (Article 20)")
                            putExtra(Intent.EXTRA_TEXT, "I am exercising my right to data portability under GDPR Article 20.\n\nPlease provide a complete export of all personal data associated with my account.\n\nThank you.")
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Submit Request", color = Teal500)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGdprDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Delete confirmation dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Data?") },
                text = {
                    Text(
                        "This action is irreversible. All cycle entries, symptoms, preferences, and account data will be permanently deleted from our servers within 30 days.\n\n" +
                                "Local data will be deleted immediately. You will be signed out."
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:privacy@petal.health")
                            putExtra(Intent.EXTRA_SUBJECT, "GDPR Data Deletion Request (Article 17)")
                            putExtra(Intent.EXTRA_TEXT, "I am exercising my right to erasure under GDPR Article 17.\n\nPlease permanently delete all personal data associated with my account.\n\nI understand this action is irreversible.\n\nThank you.")
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Delete Everything", color = Rose500)
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
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ExportFormatCard(
    format: DataFormat,
    icon: ImageVector,
    color: Color,
    description: String,
    isExporting: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PetalCard(
        modifier = modifier,
        onClick = if (!isExporting) onClick else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                format.display,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else {
                FilledTonalButton(
                    onClick = onClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = color.copy(alpha = 0.1f),
                        contentColor = color
                    )
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export")
                }
            }
        }
    }
}

@Composable
private fun ImportOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    PetalCard(onClick = if (!isLoading) onClick else null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = color
                )
            } else {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GdprActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onClick) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Export logic ──────────────────────────────────────────────────────────────

private suspend fun exportData(context: Context, format: DataFormat) {
    withContext(Dispatchers.IO) {
        try {
            val database = com.petal.app.data.local.PetalDatabase.getInstance(context)
            val prefs = context.getSharedPreferences("petal_auth", Context.MODE_PRIVATE)
            val userId = prefs.getString("current_user_id", null) ?: return@withContext

            val entries = database.cycleEntryDao().getEntriesForUserOnce(userId)
            val sorted = entries.sortedBy { it.start }

            val content = when (format) {
                DataFormat.CSV -> {
                    val sb = StringBuilder()
                    sb.appendLine("Start Date,End Date,Cycle Length (days),Flow Intensity,Pain,Cramps,Cravings,Mood,Headaches,Synced,Created At")
                    sorted.forEach { entry ->
                        sb.appendLine(
                            "${entry.start},${entry.end},${entry.cycleLength},${entry.flowIntensity.display}," +
                                    "${entry.painLevel.display},${entry.crampsLevel.display}," +
                                    "${entry.cravingsLevel.display},${entry.moodLevel.display}," +
                                    "${entry.headachesLevel.display},${entry.isSynced},${entry.createdAt}"
                        )
                    }
                    sb.toString()
                }
                DataFormat.JSON -> {
                    val data = sorted.map { entry ->
                        mapOf(
                            "startDate" to entry.start,
                            "endDate" to entry.end,
                            "cycleLength" to entry.cycleLength.toString(),
                            "flowIntensity" to entry.flowIntensity.display,
                            "symptoms" to mapOf(
                                "pain" to entry.painLevel.display,
                                "cramps" to entry.crampsLevel.display,
                                "cravings" to entry.cravingsLevel.display,
                                "mood" to entry.moodLevel.display,
                                "headaches" to entry.headachesLevel.display
                            ).toString(),
                            "synced" to entry.isSynced.toString(),
                            "createdAt" to entry.createdAt,
                            "updatedAt" to entry.updatedAt
                        )
                    }
                    Json { prettyPrint = true }.encodeToString(data)
                }
            }

            val fileName = "petal_export_${LocalDate.now()}.${format.extension}"
            val file = File(context.cacheDir, fileName)
            file.writeText(content)

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = format.mimeType
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Export ${format.display}"))
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

// ── Import logic ─────────────────────────────────────────────────────────────

private suspend fun importFromFile(context: Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext "Error: Could not open file"

            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()
            reader.close()

            val records = parseImportData(content)
            if (records.isEmpty()) {
                return@withContext "Error: No valid cycle records found in file"
            }

            // Save imported records to database
            val database = com.petal.app.data.local.PetalDatabase.getInstance(context)
            val prefs = context.getSharedPreferences("petal_auth", Context.MODE_PRIVATE)
            val userId = prefs.getString("current_user_id", null)
                ?: return@withContext "Error: Not signed in"

            var importCount = 0
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            for (record in records) {
                try {
                    val entry = CycleEntry(
                        id = java.util.UUID.randomUUID().toString(),
                        userId = userId,
                        start = record.startDate,
                        end = record.endDate,
                        cycleLength = record.cycleLength.coerceIn(15, 45),
                        flowIntensity = FlowIntensity.fromString(record.flow),
                        painLevel = SymptomLevel.fromString(record.symptoms["pain"] ?: "None"),
                        crampsLevel = SymptomLevel.fromString(record.symptoms["cramps"] ?: "None"),
                        cravingsLevel = SymptomLevel.fromString(record.symptoms["cravings"] ?: "None"),
                        moodLevel = MoodLevel.fromString(record.symptoms["mood"] ?: "Calm"),
                        headachesLevel = SymptomLevel.fromString(record.symptoms["headaches"] ?: "None"),
                        createdAt = now,
                        updatedAt = now,
                        isSynced = false
                    )
                    database.cycleEntryDao().insertEntry(entry)
                    importCount++
                } catch (e: Exception) {
                    // Skip invalid records
                }
            }

            "Successfully imported $importCount of ${records.size} cycle records"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

private suspend fun importFromHealthConnect(context: Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext "Error: Could not open file"

            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()
            reader.close()

            val records = parseHealthConnectData(content)
            if (records.isEmpty()) {
                return@withContext "Error: No valid Health Connect records found"
            }

            val database = com.petal.app.data.local.PetalDatabase.getInstance(context)
            val prefs = context.getSharedPreferences("petal_auth", Context.MODE_PRIVATE)
            val userId = prefs.getString("current_user_id", null)
                ?: return@withContext "Error: Not signed in"

            var importCount = 0
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

            for (record in records) {
                try {
                    val entry = CycleEntry(
                        id = java.util.UUID.randomUUID().toString(),
                        userId = userId,
                        start = record.startDate,
                        end = record.endDate,
                        cycleLength = record.cycleLength.coerceIn(15, 45),
                        flowIntensity = FlowIntensity.fromString(record.flow),
                        createdAt = now,
                        updatedAt = now,
                        isSynced = false
                    )
                    database.cycleEntryDao().insertEntry(entry)
                    importCount++
                } catch (e: Exception) {
                    // Skip invalid records
                }
            }

            "Successfully imported $importCount Health Connect records"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

// ── Data parsers ─────────────────────────────────────────────────────────────

private fun parseImportData(content: String): List<ImportedCycleRecord> {
    val trimmed = content.trim()

    // Try JSON first
    if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
        return parseJsonImport(trimmed)
    }

    // Try CSV
    return parseCsvImport(trimmed)
}

private fun parseJsonImport(content: String): List<ImportedCycleRecord> {
    return try {
        val json = Json { ignoreUnknownKeys = true }
        val items = json.decodeFromString<List<Map<String, String>>>(content)

        items.mapNotNull { item ->
            val start = item["startDate"] ?: item["start_date"] ?: item["start"]
                ?: item["period_start"] ?: return@mapNotNull null
            val end = item["endDate"] ?: item["end_date"] ?: item["end"]
                ?: item["period_end"] ?: start

            val cycleLength = (item["cycleLength"] ?: item["cycle_length"]
                ?: item["cycle_days"] ?: "28").toIntOrNull() ?: 28

            val flow = item["flowIntensity"] ?: item["flow"] ?: item["flow_intensity"] ?: "Medium"

            ImportedCycleRecord(
                startDate = normalizeDate(start),
                endDate = normalizeDate(end),
                cycleLength = cycleLength,
                flow = flow,
                symptoms = mapOf(
                    "pain" to (item["pain"] ?: "None"),
                    "cramps" to (item["cramps"] ?: "None"),
                    "cravings" to (item["cravings"] ?: "None"),
                    "mood" to (item["mood"] ?: "Calm"),
                    "headaches" to (item["headaches"] ?: "None")
                ),
                source = item["source"] ?: "json_import"
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun parseCsvImport(content: String): List<ImportedCycleRecord> {
    val lines = content.lines().filter { it.isNotBlank() }
    if (lines.size < 2) return emptyList()

    val headerLine = lines.first().lowercase()
    val headers = headerLine.split(",", "\t", ";").map { it.trim() }

    // Detect column indices with flexible naming
    val startIdx = headers.indexOfFirst { it in listOf("start", "start_date", "startdate", "period_start", "start date") }
    val endIdx = headers.indexOfFirst { it in listOf("end", "end_date", "enddate", "period_end", "end date") }
    val lengthIdx = headers.indexOfFirst { it in listOf("cyclelength", "cycle_length", "cycle length", "length", "cycle_days", "cycle length (days)") }
    val flowIdx = headers.indexOfFirst { it in listOf("flow", "flowintensity", "flow_intensity", "flow intensity") }

    if (startIdx == -1) return emptyList()

    return lines.drop(1).mapNotNull { line ->
        try {
            val cols = line.split(",", "\t", ";").map { it.trim() }
            if (cols.size <= startIdx) return@mapNotNull null

            val start = normalizeDate(cols[startIdx])
            val end = if (endIdx >= 0 && endIdx < cols.size) normalizeDate(cols[endIdx]) else start
            val length = if (lengthIdx >= 0 && lengthIdx < cols.size) {
                cols[lengthIdx].toIntOrNull() ?: 28
            } else 28
            val flow = if (flowIdx >= 0 && flowIdx < cols.size) cols[flowIdx] else "Medium"

            ImportedCycleRecord(
                startDate = start,
                endDate = end,
                cycleLength = length,
                flow = flow,
                source = "csv_import"
            )
        } catch (e: Exception) {
            null
        }
    }
}

private fun parseHealthConnectData(content: String): List<ImportedCycleRecord> {
    // Health Connect exports can be JSON or CSV
    val trimmed = content.trim()
    if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val items = json.decodeFromString<List<Map<String, String>>>(trimmed)

            items.mapNotNull { item ->
                val start = item["startTime"] ?: item["start_time"] ?: item["date"]
                    ?: return@mapNotNull null
                val end = item["endTime"] ?: item["end_time"] ?: start
                val flow = item["flow"] ?: item["type"] ?: "Medium"

                // Health Connect menstruation records typically have type/flow fields
                val normalizedFlow = when (flow.lowercase()) {
                    "light", "1" -> "Light"
                    "medium", "2" -> "Medium"
                    "heavy", "3" -> "Heavy"
                    else -> "Medium"
                }

                ImportedCycleRecord(
                    startDate = normalizeDate(start.take(10)),
                    endDate = normalizeDate(end.take(10)),
                    cycleLength = 28,
                    flow = normalizedFlow,
                    source = "health_connect"
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    return parseCsvImport(trimmed)
}

/**
 * Smart date format detection and normalization.
 * Handles ISO 8601, US (MM/DD/YYYY), European (DD/MM/YYYY), and other common formats.
 */
private fun normalizeDate(input: String): String {
    val trimmed = input.trim().take(10) // Limit to date portion

    // Already ISO format
    if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return trimmed

    // MM/DD/YYYY or DD/MM/YYYY
    val slashMatch = Regex("(\\d{1,2})/(\\d{1,2})/(\\d{4})").find(trimmed)
    if (slashMatch != null) {
        val (a, b, year) = slashMatch.destructured
        val aInt = a.toInt()
        val bInt = b.toInt()

        // Heuristic: if first number > 12, it must be day (European format)
        return if (aInt > 12) {
            "${year}-${b.padStart(2, '0')}-${a.padStart(2, '0')}"
        } else {
            // Assume US format (MM/DD/YYYY)
            "${year}-${a.padStart(2, '0')}-${b.padStart(2, '0')}"
        }
    }

    // DD.MM.YYYY (European with dots)
    val dotMatch = Regex("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})").find(trimmed)
    if (dotMatch != null) {
        val (day, month, year) = dotMatch.destructured
        return "${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
    }

    // YYYY/MM/DD
    val ymdSlash = Regex("(\\d{4})/(\\d{1,2})/(\\d{1,2})").find(trimmed)
    if (ymdSlash != null) {
        val (year, month, day) = ymdSlash.destructured
        return "${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
    }

    // Fallback: return as-is (may cause parse errors downstream)
    return trimmed
}
