package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.*
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.CycleRepository
import com.petal.app.domain.CycleCalculator
import com.petal.app.domain.DailyInsightsEngine
import com.petal.app.domain.RecommendationsEngine
import com.petal.app.domain.PhaseRecommendations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val isLoading: Boolean = true,
    val dayInsights: DayInsights? = null,
    val recommendations: PhaseRecommendations? = null,
    val currentPhase: CyclePhase = CyclePhase.Follicular,
    val cycleDay: Int = 1,
    val cycleLengthAvg: Int = 28,
    val entries: List<CycleEntry> = emptyList(),
    val cycleLengths: List<Int> = emptyList(),
    val periodLengths: List<Int> = emptyList()
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository,
    private val cycleCalculator: CycleCalculator,
    private val insightsEngine: DailyInsightsEngine,
    private val recommendationsEngine: RecommendationsEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = authRepository.getCurrentUserId() ?: return@launch
            val user = authRepository.getCurrentUser()
            val entries = cycleRepository.getEntries(userId)
            val cycles = entries.map { it.toCycleLog() }

            val userName = user?.name ?: "there"
            val avgLength = cycleCalculator.getAverageCycleLength(cycles)
            val cycleDay = cycleCalculator.getCurrentCycleDay(cycles)
            val phase = cycleCalculator.getCurrentPhase(cycles)

            val insights = insightsEngine.getDailyInsights(cycleDay, avgLength, userName)
            val recs = recommendationsEngine.getRecommendations(phase.display)

            val cycleLengths = entries.map { it.cycleLength }
            val periodLengths = entries.map { cycleCalculator.getDaysBetween(it.start, it.end) }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    dayInsights = insights,
                    recommendations = recs,
                    currentPhase = phase,
                    cycleDay = cycleDay,
                    cycleLengthAvg = avgLength,
                    entries = entries,
                    cycleLengths = cycleLengths,
                    periodLengths = periodLengths
                )
            }
        }
    }
}
