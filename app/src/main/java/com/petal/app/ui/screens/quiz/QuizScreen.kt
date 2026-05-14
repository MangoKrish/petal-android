package com.petal.app.ui.screens.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petal.app.data.remote.dto.DailyQuizQuestionDto
import com.petal.app.data.remote.dto.QuizStatsDto
import com.petal.app.ui.components.PetalCard
import com.petal.app.ui.viewmodel.QuizViewModel

/**
 * PHASE_6_7_PLAN.md §6B.4 — Daily quiz screen.
 * Mirrors new-project/src/components/quiz-tab.tsx in flow:
 *   stats strip → one-question-at-a-time → progress dots → review on completion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel = hiltViewModel(),
) {
    val state by viewModel.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily quiz") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "5 questions · take it slow · explanation after each",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            val set = state.set
            val questions = set?.questions ?: emptyList()
            val total = questions.size
            val answered = questions.count { it.attempt != null }
            val correctSoFar = questions.count { it.attempt?.correct == true }
            val allDone = total > 0 && answered == total

            QuizStatsStrip(
                stats = state.stats,
                todayCorrect = correctSoFar,
                todayAnswered = answered,
                todayTotal = total,
            )

            when {
                state.isLoading -> {
                    PetalCard {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                state.error != null && set == null -> {
                    PetalCard {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                state.error ?: "Couldn't load.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(12.dp))
                            FilledTonalButton(onClick = { viewModel.refresh() }) {
                                Text("try again")
                            }
                        }
                    }
                }

                total == 0 -> {
                    PetalCard {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "no quiz today — content lands soon ♡",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                allDone -> {
                    QuizResultsCard(
                        correct = correctSoFar,
                        total = total,
                        questions = questions,
                        onReview = { viewModel.setActiveIndex(it) },
                    )
                }

                else -> {
                    val idx = state.activeIndex.coerceIn(0, total - 1)
                    val question = questions[idx]
                    QuizQuestionCard(
                        question = question,
                        index = idx,
                        total = total,
                        pendingKey = state.pendingAnswerKey,
                        onAnswer = { key -> viewModel.answer(question, key) },
                        onNext = { viewModel.next() },
                        onPrev = { viewModel.prev() },
                    )
                }
            }

            if (total > 0) {
                ProgressDots(
                    questions = questions,
                    activeIndex = if (allDone) -1 else state.activeIndex,
                    onTap = { viewModel.setActiveIndex(it) },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuizStatsStrip(
    stats: QuizStatsDto?,
    todayCorrect: Int,
    todayAnswered: Int,
    todayTotal: Int,
) {
    val lifetimePct = stats?.let {
        if (it.lifetimeAnswered > 0) ((it.lifetimeCorrect.toDouble() / it.lifetimeAnswered) * 100).toInt() else null
    }
    PetalCard {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuizStatItem(
                label = "today",
                value = "$todayCorrect/$todayTotal",
                hint = "$todayAnswered answered",
            )
            QuizStatItem(
                label = "streak",
                value = "${stats?.currentStreakDays ?: 0}d",
                hint = stats?.let { if (it.longestStreakDays > 0) "best ${it.longestStreakDays}d" else "" } ?: "",
            )
            QuizStatItem(
                label = "lifetime",
                value = lifetimePct?.let { "$it%" } ?: "—",
                hint = stats?.let { "${it.lifetimeCorrect}/${it.lifetimeAnswered}" } ?: "",
            )
        }
    }
}

@Composable
private fun QuizStatItem(label: String, value: String, hint: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        )
        if (hint.isNotEmpty()) {
            Spacer(Modifier.height(2.dp))
            Text(
                hint,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuizQuestionCard(
    question: DailyQuizQuestionDto,
    index: Int,
    total: Int,
    pendingKey: String?,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
) {
    val attempt = question.attempt
    val answered = attempt != null
    PetalCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            "${categoryLabel(question.category)} · ${question.difficulty}",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                )
                Text(
                    "${index + 1} of $total",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                question.prompt,
                style = MaterialTheme.typography.bodyLarge,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEach { opt ->
                    val isSelected = attempt?.selectedKey == opt.key
                    val isCorrect = answered && opt.key == attempt?.correctKey
                    val isWrongPick = answered && isSelected && !isCorrect
                    val isPending = pendingKey == opt.key && !answered

                    val containerColor = when {
                        isCorrect -> Color(0xFFE6F4EC)
                        isWrongPick -> Color(0xFFFAE3EA)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val borderColor = when {
                        isCorrect -> Color(0xFF4AA275)
                        isWrongPick -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = containerColor,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (!answered) Modifier.clickable(enabled = pendingKey == null) { onAnswer(opt.key) } else Modifier)
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "${opt.key.uppercase()}.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(24.dp),
                            )
                            Text(
                                opt.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            when {
                                isCorrect -> Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4AA275))
                                isWrongPick -> Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                isPending -> CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                else -> Spacer(Modifier.width(0.dp))
                            }
                        }
                    }
                }
            }

            if (answered && attempt != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            (if (attempt.correct) "Yes — " else "Not quite. ") + attempt.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (!question.source.isNullOrBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "source · ${question.source}",
                                style = MaterialTheme.typography.labelSmall.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FilledTonalButton(
                    onClick = onPrev,
                    enabled = index > 0,
                ) { Text("‹ prev") }
                FilledTonalButton(
                    onClick = onNext,
                    enabled = answered && index < total - 1,
                ) {
                    Text(if (answered) "next ›" else "answer to continue")
                }
            }
        }
    }
}

@Composable
private fun QuizResultsCard(
    correct: Int,
    total: Int,
    questions: List<DailyQuizQuestionDto>,
    onReview: (Int) -> Unit,
) {
    val ratio = if (total > 0) correct.toDouble() / total else 0.0
    val headline = when {
        ratio >= 1.0 -> "All five — kind work today ♡"
        ratio >= 0.6 -> "Solid showing — quiet learning every day."
        else -> "Showing up is the streak. Tomorrow's a fresh set."
    }
    PetalCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "TODAY'S QUIZ",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "$correct of $total correct",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                headline,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            questions.forEachIndexed { i, q ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReview(i) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (q.attempt?.correct == true) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4AA275))
                        } else {
                            Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            q.prompt,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "review ›",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "a fresh set lands at midnight ♡",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.labelMedium.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProgressDots(
    questions: List<DailyQuizQuestionDto>,
    activeIndex: Int,
    onTap: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        questions.forEachIndexed { i, q ->
            val isActive = i == activeIndex
            val isAnswered = q.attempt != null
            val color = when {
                isAnswered && q.attempt?.correct == true -> Color(0xFF4AA275)
                isAnswered -> MaterialTheme.colorScheme.onSurfaceVariant
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(if (isActive) 18.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isActive) MaterialTheme.colorScheme.onSurfaceVariant else color)
                    .clickable { onTap(i) }
            )
        }
    }
}

private fun categoryLabel(c: String): String = when (c) {
    "period" -> "period"
    "pcos" -> "PCOS"
    "pcod" -> "PCOD"
    "fertility" -> "fertility"
    "myth" -> "myths"
    "body" -> "body"
    "partner" -> "for partners"
    else -> c
}
