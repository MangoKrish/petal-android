package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.*
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.CycleRepository
import com.petal.app.domain.BayesianPredictor
import com.petal.app.domain.CycleCalculator
import com.petal.app.domain.DailyInsightsEngine
import com.petal.app.domain.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val currentPhase: CyclePhase = CyclePhase.Follicular,
    val cycleDay: Int = 1,
    val cycleLengthAvg: Int = 28,
    val daysUntilNextPeriod: Int = 0,
    val nextPeriodDate: LocalDate = LocalDate.now(),
    val ovulationDate: LocalDate = LocalDate.now(),
    val fertileWindowStart: LocalDate = LocalDate.now(),
    val fertileWindowEnd: LocalDate = LocalDate.now(),
    val confidence: PredictionConfidence = PredictionConfidence.Low,
    val bayesianConfidence: Double = 0.0,
    val patternFlags: List<CyclePatternFlag> = emptyList(),
    val insights: DayInsights? = null,
    val cycleProgress: Float = 0f,
    val entryCount: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository,
    private val cycleCalculator: CycleCalculator,
    private val bayesianPredictor: BayesianPredictor,
    private val insightsEngine: DailyInsightsEngine,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
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
            val nextPeriod = cycleCalculator.getNextPeriodDate(cycles)
            val daysUntil = cycleCalculator.getDaysUntil(nextPeriod)
            val ovulation = cycleCalculator.getOvulationDate(cycles)
            val (fertileStart, fertileEnd) = cycleCalculator.getFertileWindow(cycles)
            val confidence = cycleCalculator.getPredictionConfidence(cycles)
            val flags = cycleCalculator.getCyclePatternFlags(cycles)

            // Bayesian prediction
            val lastSymptoms = entries.firstOrNull()?.symptoms
            val bayesResult = bayesianPredictor.predict(
                cycles = cycles,
                symptoms = lastSymptoms,
                lastPeriodStart = if (cycles.isNotEmpty()) LocalDate.parse(cycles[0].start) else null
            )

            // Daily insights
            val insights = insightsEngine.getDailyInsights(cycleDay, avgLength, userName)

            // Cycle progress (0.0 to 1.0)
            val progress = cycleDay.toFloat() / avgLength.toFloat()

            // Schedule notifications
            notificationScheduler.schedulePeriodReminder(nextPeriod)
            notificationScheduler.scheduleSync()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userName = userName,
                    currentPhase = phase,
                    cycleDay = cycleDay,
                    cycleLengthAvg = avgLength,
                    daysUntilNextPeriod = daysUntil,
                    nextPeriodDate = nextPeriod,
                    ovulationDate = ovulation,
                    fertileWindowStart = fertileStart,
                    fertileWindowEnd = fertileEnd,
                    confidence = confidence,
                    bayesianConfidence = bayesResult.confidence,
                    patternFlags = flags,
                    insights = insights,
                    cycleProgress = progress.coerceIn(0f, 1f),
                    entryCount = entries.size
                )
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }
}
