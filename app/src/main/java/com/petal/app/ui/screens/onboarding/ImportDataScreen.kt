package com.petal.app.ui.screens.onboarding

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.petal.app.data.model.*
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Import state tracking for the onboarding flow.
 */
data class ImportState(
    val isImporting: Boolean = false,
    val importComplete: Boolean = false,
    val importedCount: Int = 0,
    val errorMessage: String? = null,
    val detectedFormat: String? = null,
    val detectedDateFormat: String? = null,
    val previewRecords: List<ImportPreviewRecord> = emptyList()
)

/**
 * Preview of a parsed record before confirming import.
 */
data class ImportPreviewRecord(
    val startDate: String,
    val endDate: String,
    val cycleLength: Int,
    val flow: String,
    val source: String
)

/**
 * Onboarding screen for importing data from competitor apps or Health Connect.
 * Guides users through selecting a data source, picking a file, previewing records,
 * and confirming import.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportDataScreen(
    onNavigateBack: () -> Unit,
    onImportComplete: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var importState by remember { mutableStateOf(ImportState()) }
    var selectedSource by remember { mutableStateOf<String?>(null) }
    var showPreview by remember { mutableStateOf(false) }

    // File picker for CSV/JSON imports
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                importState = importState.copy(isImporting = true, errorMessage = null)
                val result = previewImportFile(context, uri)
                importState = result
                if (result.previewRecords.isNotEmpty()) {
                    showPreview = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Your Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        if (importState.importComplete) {
            // Success state
            ImportSuccessContent(
                importedCount = importState.importedCount,
                onContinue = onImportComplete,
                modifier = Modifier.padding(padding)
            )
        } else if (showPreview && importState.previewRecords.isNotEmpty()) {
            // Preview state
            ImportPreviewContent(
                state = importState,
                onConfirmImport = {
                    scope.launch {
                        importState = importState.copy(isImporting = true)
                        val count = confirmImport(context, importState.previewRecords)
                        importState = importState.copy(
                            isImporting = false,
                            importComplete = true,
                            importedCount = count
                        )
                    }
                },
                onCancel = {
                    showPreview = false
                    importState = ImportState()
                    selectedSource = null
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            // Source selection state
            SourceSelectionContent(
                selectedSource = selectedSource,
                importState = importState,
                onSelectHealthConnect = {
                    selectedSource = "health_connect"
                    filePickerLauncher.launch(arrayOf("text/csv", "application/json", "*/*"))
                },
                onSelectCsvFile = {
                    selectedSource = "csv"
                    filePickerLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                },
                onSelectJsonFile = {
                    selectedSource = "json"
                    filePickerLauncher.launch(arrayOf("application/json", "*/*"))
                },
                onSkip = onSkip,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

// ── Source Selection ──────────────────────────────────────────────────────────

@Composable
private fun SourceSelectionContent(
    selectedSource: String?,
    importState: ImportState,
    onSelectHealthConnect: () -> Unit,
    onSelectCsvFile: () -> Unit,
    onSelectJsonFile: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Illustration
        Surface(
            shape = CircleShape,
            color = Teal100,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Teal500,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "Bring your cycle history",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Import existing data so Petal can start with accurate predictions. " +
                    "Your data stays private and secure.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Import sources
        ImportSourceCard(
            title = "Health Connect",
            subtitle = "Import from Android's centralized health data platform",
            icon = Icons.Default.HealthAndSafety,
            color = Teal500,
            supportedApps = "Samsung Health, Google Fit, Fitbit, and more",
            isLoading = importState.isImporting && selectedSource == "health_connect",
            onClick = onSelectHealthConnect
        )

        Spacer(modifier = Modifier.height(12.dp))

        ImportSourceCard(
            title = "CSV File",
            subtitle = "Import from spreadsheet exports",
            icon = Icons.Default.TableChart,
            color = Gold500,
            supportedApps = "Flo, Clue, My Calendar, or any app that exports CSV",
            isLoading = importState.isImporting && selectedSource == "csv",
            onClick = onSelectCsvFile
        )

        Spacer(modifier = Modifier.height(12.dp))

        ImportSourceCard(
            title = "JSON File",
            subtitle = "Import from app data exports",
            icon = Icons.Default.Code,
            color = Lavender500,
            supportedApps = "Developer-friendly format, data backups",
            isLoading = importState.isImporting && selectedSource == "json",
            onClick = onSelectJsonFile
        )

        // Error message
        if (importState.errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            PetalCard(containerColor = Rose100) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Rose500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        importState.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = Rose700
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Smart format detection info
        PetalCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Gold500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Smart detection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Petal automatically detects date formats (MM/DD, DD/MM, ISO), " +
                            "column layouts, and data structures. Just select your file " +
                            "and we will handle the rest.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ImportSourceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    supportedApps: String,
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
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    supportedApps,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
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
                    contentDescription = "Select $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────────────────

@Composable
private fun ImportPreviewContent(
    state: ImportState,
    onConfirmImport: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Preview Import",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Detection info
        if (state.detectedFormat != null || state.detectedDateFormat != null) {
            PetalCard(containerColor = Teal50) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Teal500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        if (state.detectedFormat != null) {
                            Text(
                                "Format: ${state.detectedFormat}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (state.detectedDateFormat != null) {
                            Text(
                                "Date format: ${state.detectedDateFormat}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            "${state.previewRecords.size} records found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "Review a sample of detected records before importing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preview records (show first 5)
        state.previewRecords.take(5).forEachIndexed { index, record ->
            PetalCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Rose100,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Rose500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${record.startDate} to ${record.endDate}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Cycle: ${record.cycleLength} days | Flow: ${record.flow}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (index < minOf(4, state.previewRecords.size - 1)) {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (state.previewRecords.size > 5) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "... and ${state.previewRecords.size - 5} more records",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action buttons
        if (state.isImporting) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Teal500)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Importing records...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Button(
                onClick = onConfirmImport,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Teal500)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import ${state.previewRecords.size} Records")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Choose a Different File")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ── Success ──────────────────────────────────────────────────────────────────

@Composable
private fun ImportSuccessContent(
    importedCount: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = Teal100,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Teal500,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Import Complete",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "$importedCount cycle records imported successfully. " +
                    "Petal will use this data to generate more accurate predictions right away.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Rose500)
        ) {
            Text("Continue to Petal")
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

// ── Import logic ─────────────────────────────────────────────────────────────

private suspend fun previewImportFile(context: Context, uri: Uri): ImportState {
    return withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ImportState(
                    errorMessage = "Could not open file. Please try again."
                )

            val reader = BufferedReader(InputStreamReader(inputStream))
            val content = reader.readText()
            reader.close()

            val trimmed = content.trim()
            val isJson = trimmed.startsWith("[") || trimmed.startsWith("{")
            val detectedFormat = if (isJson) "JSON" else "CSV"

            val records = if (isJson) {
                parseJsonForPreview(trimmed)
            } else {
                parseCsvForPreview(trimmed)
            }

            if (records.isEmpty()) {
                return@withContext ImportState(
                    errorMessage = "No valid cycle records found in the file. " +
                            "Please ensure the file contains period start/end dates."
                )
            }

            // Detect date format
            val sampleDate = records.firstOrNull()?.startDate ?: ""
            val detectedDateFormat = detectDateFormat(sampleDate)

            ImportState(
                isImporting = false,
                previewRecords = records,
                detectedFormat = detectedFormat,
                detectedDateFormat = detectedDateFormat
            )
        } catch (e: Exception) {
            ImportState(
                errorMessage = "Error reading file: ${e.message}"
            )
        }
    }
}

private fun parseJsonForPreview(content: String): List<ImportPreviewRecord> {
    return try {
        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val items = json.decodeFromString<List<Map<String, String>>>(content)

        items.mapNotNull { item ->
            val start = item["startDate"] ?: item["start_date"] ?: item["start"]
                ?: item["period_start"] ?: item["startTime"]?.take(10) ?: return@mapNotNull null
            val end = item["endDate"] ?: item["end_date"] ?: item["end"]
                ?: item["period_end"] ?: item["endTime"]?.take(10) ?: start

            val cycleLength = (item["cycleLength"] ?: item["cycle_length"]
                ?: item["cycle_days"] ?: "28").toIntOrNull() ?: 28
            val flow = item["flowIntensity"] ?: item["flow"] ?: item["flow_intensity"] ?: "Medium"

            ImportPreviewRecord(
                startDate = normalizeImportDate(start),
                endDate = normalizeImportDate(end),
                cycleLength = cycleLength.coerceIn(15, 45),
                flow = normalizeFlowLabel(flow),
                source = item["source"] ?: "json"
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

private fun parseCsvForPreview(content: String): List<ImportPreviewRecord> {
    val lines = content.lines().filter { it.isNotBlank() }
    if (lines.size < 2) return emptyList()

    val headerLine = lines.first().lowercase()
    val separator = detectCsvSeparator(headerLine)
    val headers = headerLine.split(separator).map { it.trim() }

    val startIdx = headers.indexOfFirst {
        it in listOf("start", "start_date", "startdate", "period_start", "start date",
            "period start", "period_start_date")
    }
    val endIdx = headers.indexOfFirst {
        it in listOf("end", "end_date", "enddate", "period_end", "end date",
            "period end", "period_end_date")
    }
    val lengthIdx = headers.indexOfFirst {
        it in listOf("cyclelength", "cycle_length", "cycle length", "length", "cycle_days",
            "cycle length (days)", "days")
    }
    val flowIdx = headers.indexOfFirst {
        it in listOf("flow", "flowintensity", "flow_intensity", "flow intensity",
            "bleeding", "bleeding_intensity")
    }

    if (startIdx == -1) return emptyList()

    return lines.drop(1).mapNotNull { line ->
        try {
            val cols = line.split(separator).map { it.trim() }
            if (cols.size <= startIdx) return@mapNotNull null

            val start = normalizeImportDate(cols[startIdx])
            val end = if (endIdx >= 0 && endIdx < cols.size) {
                normalizeImportDate(cols[endIdx])
            } else start
            val length = if (lengthIdx >= 0 && lengthIdx < cols.size) {
                cols[lengthIdx].toIntOrNull() ?: 28
            } else 28
            val flow = if (flowIdx >= 0 && flowIdx < cols.size) {
                normalizeFlowLabel(cols[flowIdx])
            } else "Medium"

            ImportPreviewRecord(
                startDate = start,
                endDate = end,
                cycleLength = length.coerceIn(15, 45),
                flow = flow,
                source = "csv"
            )
        } catch (e: Exception) {
            null
        }
    }
}

private suspend fun confirmImport(
    context: Context,
    records: List<ImportPreviewRecord>
): Int {
    return withContext(Dispatchers.IO) {
        try {
            val database = com.petal.app.data.local.PetalDatabase.getInstance(context)
            val prefs = context.getSharedPreferences("petal_auth", Context.MODE_PRIVATE)
            val userId = prefs.getString("current_user_id", null) ?: return@withContext 0

            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            var count = 0

            for (record in records) {
                try {
                    val entry = CycleEntry(
                        id = java.util.UUID.randomUUID().toString(),
                        userId = userId,
                        start = record.startDate,
                        end = record.endDate,
                        cycleLength = record.cycleLength,
                        flowIntensity = FlowIntensity.fromString(record.flow),
                        createdAt = now,
                        updatedAt = now,
                        isSynced = false
                    )
                    database.cycleEntryDao().insertEntry(entry)
                    count++
                } catch (e: Exception) {
                    // Skip invalid records
                }
            }

            count
        } catch (e: Exception) {
            0
        }
    }
}

// ── Utility functions ────────────────────────────────────────────────────────

private fun detectCsvSeparator(headerLine: String): Regex {
    val commaCount = headerLine.count { it == ',' }
    val tabCount = headerLine.count { it == '\t' }
    val semicolonCount = headerLine.count { it == ';' }

    return when {
        tabCount > commaCount && tabCount > semicolonCount -> Regex("\t")
        semicolonCount > commaCount -> Regex(";")
        else -> Regex(",")
    }
}

private fun normalizeImportDate(input: String): String {
    val trimmed = input.trim().replace("\"", "").replace("'", "").take(10)

    // Already ISO format
    if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return trimmed

    // MM/DD/YYYY or DD/MM/YYYY
    val slashMatch = Regex("(\\d{1,2})/(\\d{1,2})/(\\d{2,4})").find(trimmed)
    if (slashMatch != null) {
        val (a, b, yearStr) = slashMatch.destructured
        val year = if (yearStr.length == 2) "20$yearStr" else yearStr
        val aInt = a.toInt()
        val bInt = b.toInt()

        return if (aInt > 12) {
            // Must be DD/MM/YYYY
            "${year}-${b.padStart(2, '0')}-${a.padStart(2, '0')}"
        } else if (bInt > 12) {
            // Must be MM/DD/YYYY
            "${year}-${a.padStart(2, '0')}-${b.padStart(2, '0')}"
        } else {
            // Ambiguous - assume US format MM/DD/YYYY
            "${year}-${a.padStart(2, '0')}-${b.padStart(2, '0')}"
        }
    }

    // DD.MM.YYYY
    val dotMatch = Regex("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{2,4})").find(trimmed)
    if (dotMatch != null) {
        val (day, month, yearStr) = dotMatch.destructured
        val year = if (yearStr.length == 2) "20$yearStr" else yearStr
        return "${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}"
    }

    // DD-MM-YYYY (non-ISO because day first)
    val dashMatch = Regex("(\\d{1,2})-(\\d{1,2})-(\\d{4})").find(trimmed)
    if (dashMatch != null) {
        val (a, b, year) = dashMatch.destructured
        val aInt = a.toInt()
        return if (aInt > 12) {
            "${year}-${b.padStart(2, '0')}-${a.padStart(2, '0')}"
        } else {
            // Ambiguous, assume ISO-like YYYY-MM-DD was already handled
            "${year}-${a.padStart(2, '0')}-${b.padStart(2, '0')}"
        }
    }

    return trimmed
}

private fun normalizeFlowLabel(flow: String): String {
    return when (flow.lowercase().trim()) {
        "light", "1", "spotting", "very light" -> "Light"
        "medium", "2", "normal", "moderate" -> "Medium"
        "heavy", "3", "very heavy", "super" -> "Heavy"
        else -> "Medium"
    }
}

private fun detectDateFormat(sample: String): String {
    return when {
        sample.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) -> "ISO 8601 (YYYY-MM-DD)"
        sample.matches(Regex("\\d{1,2}/\\d{1,2}/\\d{4}")) -> "US/EU (MM/DD/YYYY or DD/MM/YYYY)"
        sample.matches(Regex("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) -> "European (DD.MM.YYYY)"
        else -> "Auto-detected"
    }
}
