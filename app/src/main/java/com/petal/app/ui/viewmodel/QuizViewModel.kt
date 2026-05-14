package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.remote.dto.DailyQuizQuestionDto
import com.petal.app.data.remote.dto.DailyQuizSetDto
import com.petal.app.data.remote.dto.QuizAttemptDto
import com.petal.app.data.remote.dto.QuizHistoryEntryDto
import com.petal.app.data.remote.dto.QuizStatsDto
import com.petal.app.data.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PHASE_6_7_PLAN.md §6B.4 — daily quiz VM.
 * Tracks today's set, lifetime stats, current question index, and pending answer state.
 */
data class QuizUiState(
    val isLoading: Boolean = true,
    val set: DailyQuizSetDto? = null,
    val stats: QuizStatsDto? = null,
    val history: List<QuizHistoryEntryDto> = emptyList(),
    val activeIndex: Int = 0,
    val pendingAnswerKey: String? = null,
    val error: String? = null,
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(QuizUiState())
    val ui: StateFlow<QuizUiState> = _ui.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val setRes = repository.fetchToday()
            val statsRes = repository.stats()
            val historyRes = repository.history(14)
            val set = setRes.getOrNull()
            val stats = statsRes.getOrNull()
            val history = historyRes.getOrDefault(emptyList())
            val initialIdx = set?.questions?.indexOfFirst { it.attempt == null }
                ?.let { if (it < 0) (set.questions.size - 1).coerceAtLeast(0) else it }
                ?: 0
            _ui.update {
                it.copy(
                    isLoading = false,
                    set = set,
                    stats = stats,
                    history = history,
                    activeIndex = initialIdx,
                    error = setRes.exceptionOrNull()?.message ?: statsRes.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun setActiveIndex(idx: Int) {
        val total = _ui.value.set?.questions?.size ?: return
        if (idx in 0 until total) _ui.update { it.copy(activeIndex = idx) }
    }

    fun next() {
        val total = _ui.value.set?.questions?.size ?: return
        _ui.update { it.copy(activeIndex = (it.activeIndex + 1).coerceAtMost(total - 1)) }
    }

    fun prev() {
        _ui.update { it.copy(activeIndex = (it.activeIndex - 1).coerceAtLeast(0)) }
    }

    fun answer(question: DailyQuizQuestionDto, key: String) {
        if (question.attempt != null) return
        if (_ui.value.pendingAnswerKey != null) return
        viewModelScope.launch {
            _ui.update { it.copy(pendingAnswerKey = key) }
            val r = repository.answer(question.id, key)
            r.fold(
                onSuccess = { resp ->
                    _ui.update { state ->
                        val current = state.set ?: return@update state.copy(pendingAnswerKey = null)
                        val updatedQuestions = current.questions.map { q ->
                            if (q.id == question.id) {
                                q.copy(
                                    attempt = QuizAttemptDto(
                                        selectedKey = key,
                                        correct = resp.correct,
                                        correctKey = resp.correctKey,
                                        explanation = resp.explanation,
                                    )
                                )
                            } else q
                        }
                        state.copy(
                            set = current.copy(
                                questions = updatedQuestions,
                                completed = resp.setCompleted,
                                answeredCount = updatedQuestions.count { it.attempt != null },
                            ),
                            pendingAnswerKey = null,
                        )
                    }
                    if (resp.setCompleted) {
                        // Refresh stats once the set completes so the streak ticks.
                        repository.stats().getOrNull()?.let { s -> _ui.update { it.copy(stats = s) } }
                    }
                    // Refresh history so the just-answered question shows up in
                    // the footer review list (PHASE_6_7_PLAN.md §6B.4).
                    repository.history(14).getOrNull()?.let { h -> _ui.update { it.copy(history = h) } }
                },
                onFailure = { e -> _ui.update { it.copy(pendingAnswerKey = null, error = e.message) } },
            )
        }
    }

    fun clearError() { _ui.update { it.copy(error = null) } }
}
