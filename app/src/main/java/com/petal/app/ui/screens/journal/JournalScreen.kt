package com.petal.app.ui.screens.journal

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.petal.app.ui.components.GlassCard
import com.petal.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class JournalEntryData(
    val id: String = "",
    val date: LocalDate = LocalDate.now(),
    val content: String = "",
    val mood: String? = null,
    val sentiment: String = "neutral",
    val tags: List<String> = emptyList(),
    val phase: String? = null,
    val cycleDay: Int? = null,
)

private val MOODS = listOf(
    "\uD83D\uDE0A" to "Happy",
    "\uD83D\uDE0C" to "Calm",
    "\uD83D\uDE22" to "Sad",
    "\uD83D\uDE24" to "Frustrated",
    "\uD83D\uDE30" to "Anxious",
    "\uD83D\uDE34" to "Tired",
    "\uD83E\uDD70" to "Loved",
    "\uD83D\uDCAA" to "Strong",
    "\uD83D\uDE36" to "Numb",
    "\uD83E\uDD14" to "Reflective",
)

private val SENTIMENT_DISPLAY = mapOf(
    "positive" to ("\uD83C\uDF38" to SuccessColor),
    "neutral" to ("\uD83C\uDF3F" to Neutral500),
    "negative" to ("\uD83C\uDF27\uFE0F" to Rose500),
)

private val PROMPTS = listOf(
    "How are you feeling right now?",
    "What was the highlight of your day?",
    "What's on your mind tonight?",
    "How is your body feeling today?",
    "What are you grateful for?",
    "What would make tomorrow better?",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalScreen(
    onNavigateBack: () -> Unit,
    currentPhase: String? = null,
    currentCycleDay: Int? = null,
) {
    var isWriting by remember { mutableStateOf(false) }
    var content by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf(listOf<JournalEntryData>()) }
    val prompt = remember { PROMPTS.random() }

    val today = LocalDate.now()
    val todayEntry = entries.find { it.date == today }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Journal") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
            )
        },
        floatingActionButton = {
            if (!isWriting && todayEntry == null) {
                ExtendedFloatingActionButton(
                    onClick = { isWriting = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Write")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Writing interface
            if (isWriting) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = DarkLavenderSoft,
                    ) {
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mood picker
                        Text(
                            text = "How are you feeling?",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            MOODS.forEach { (emoji, label) ->
                                FilterChip(
                                    selected = selectedMood == label,
                                    onClick = {
                                        selectedMood = if (selectedMood == label) null else label
                                    },
                                    label = { Text("$emoji $label", fontSize = 12.sp) },
                                    shape = RoundedCornerShape(20.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp),
                            placeholder = { Text("Write freely... this is your private space.") },
                            shape = RoundedCornerShape(12.dp),
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = {
                                    if (content.isNotBlank()) {
                                        val newEntry = JournalEntryData(
                                            id = System.currentTimeMillis().toString(),
                                            date = today,
                                            content = content,
                                            mood = selectedMood,
                                            sentiment = analyzeSentiment(content),
                                            phase = currentPhase,
                                            cycleDay = currentCycleDay,
                                        )
                                        entries = listOf(newEntry) + entries
                                        content = ""
                                        selectedMood = null
                                        isWriting = false
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                enabled = content.isNotBlank(),
                            ) {
                                Text("Save entry")
                            }
                            OutlinedButton(
                                onClick = {
                                    isWriting = false
                                    content = ""
                                    selectedMood = null
                                },
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }

            // Today's entry
            if (todayEntry != null && !isWriting) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowColor = SuccessColor,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val (emoji, _) = SENTIMENT_DISPLAY[todayEntry.sentiment] ?: ("\uD83C\uDF3F" to Neutral500)
                            Text(emoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Today's entry",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (todayEntry.mood != null) {
                                    Text(
                                        todayEntry.mood,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = todayEntry.content,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Empty state
            if (entries.isEmpty() && !isWriting) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("\uD83D\uDCDD", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Your private space",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            "Write freely. Your journal is encrypted and only visible to you.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }

            // Past entries
            if (entries.isNotEmpty()) {
                item {
                    Text(
                        "Recent entries",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                items(entries.filter { it.date != today }) { entry ->
                    JournalEntryCard(entry)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun JournalEntryCard(entry: JournalEntryData) {
    val (emoji, color) = SENTIMENT_DISPLAY[entry.sentiment] ?: ("\uD83C\uDF3F" to Neutral500)

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        showGlow = false,
        cornerRadius = 16.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = entry.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (entry.phase != null) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(entry.phase, fontSize = 10.sp) },
                    shape = RoundedCornerShape(20.dp),
                )
            }
        }
        if (entry.mood != null) {
            Text(
                entry.mood,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Text(
            text = entry.content,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

private fun analyzeSentiment(text: String): String {
    val positive = setOf("happy", "great", "wonderful", "amazing", "good", "love", "beautiful",
        "grateful", "joy", "excited", "peaceful", "calm", "energetic", "strong", "confident")
    val negative = setOf("sad", "tired", "pain", "angry", "frustrated", "anxious", "worried",
        "stressed", "terrible", "horrible", "bad", "hate", "crying", "lonely", "depressed")

    val words = text.lowercase().split(Regex("\\W+"))
    var score = 0
    for (word in words) {
        if (word in positive) score++
        if (word in negative) score--
    }
    return when {
        score >= 2 -> "positive"
        score <= -2 -> "negative"
        else -> "neutral"
    }
}
