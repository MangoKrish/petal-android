package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.*
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.CycleRepository
import com.petal.app.data.repository.FertilityLogRepository
import com.petal.app.domain.BayesianPredictor
import com.petal.app.domain.CycleCalculator
import com.petal.app.domain.DailyInsightsEngine
import com.petal.app.domain.FertilityFusion
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
    val entryCount: Int = 0,
    // PHASE_6_7_PLAN.md §6A.2 — hybrid predictor additions
    val cycleMode: CycleMode = CycleMode.Tracking,
    val fusion: FertilityFusion.FertilityEstimate? = null,
    val peakDay: LocalDate? = null,
    val fusionSource: FertilityFusion.OvulationSource? = null,
    val fusionConfidence: Double = 0.0,
    val phaseToday: FertilityFusion.FertilityPhaseToday? = null,
    val irregular: Boolean = false,
    val insufficientFertilityData: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository,
    private val cycleCalculator: CycleCalculator,
    private val bayesianPredictor: BayesianPredictor,
    private val insightsEngine: DailyInsightsEngine,
    private val notificationScheduler: NotificationScheduler,
    private val fertilityFusion: FertilityFusion,
    private val fertilityLogRepository: FertilityLogRepository
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

            // Hybrid: layer the symptom-fusion fertility estimate on top of
            // the Bayesian period prediction. PHASE_6_7_PLAN.md §6A.2.
            val cycleMode = authRepository.getCycleMode()
            val fertilityRows = fertilityLogRepository.getRecent(userId, days = 45)
            val fusionInput = FertilityFusion.FusionInput(
                today = LocalDate.now(),
                predictedNextPeriod = bayesResult.nextPeriodDate,
                cycleLengthMean = bayesResult.predictedLength.toInt(),
                cycleLengthVariance = bayesResult.posteriorStd * bayesResult.posteriorStd,
                baseConfidence = bayesResult.confidence,
                logs = fertilityLogRepository.toFusionLogs(fertilityRows),
                observedCycleCount = cycles.size
            )
            val fusion = fertilityFusion.predictFertileWindow(fusionInput)

            // Daily insights
            val insights = insightsEngine.getDailyInsights(cycleDay, avgLength, userName)

            // Cycle progress (0.0 to 1.0)
            val progress = cycleDay.toFloat() / avgLength.toFloat()

            // Schedule notifications
            notificationScheduler.schedulePeriodReminder(nextPeriod)
            notificationScheduler.scheduleSync()

            // Fusion takes over the fertile window when it has signals to work
            // with; otherwise we keep the calendar fallback already computed.
            val effectiveFertileStart = if (!fusion.insufficientData) fusion.fertileWindow.start else fertileStart
            val effectiveFertileEnd = if (!fusion.insufficientData) fusion.fertileWindow.end else fertileEnd
            val effectiveOvulation = if (!fusion.insufficientData)
                fusion.ovulationEstimate.date else ovulation

            _uiState.update {
                it.copy(
                    isLoading = false,
                    userName = userName,
                    currentPhase = phase,
                    cycleDay = cycleDay,
                    cycleLengthAvg = avgLength,
                    daysUntilNextPeriod = daysUntil,
                    nextPeriodDate = nextPeriod,
                    ovulationDate = effectiveOvulation,
                    fertileWindowStart = effectiveFertileStart,
                    fertileWindowEnd = effectiveFertileEnd,
                    confidence = confidence,
                    bayesianConfidence = bayesResult.confidence,
                    patternFlags = flags,
                    insights = insights,
                    cycleProgress = progress.coerceIn(0f, 1f),
                    entryCount = entries.size,
                    cycleMode = cycleMode,
                    fusion = fusion,
                    peakDay = if (!fusion.insufficientData) fusion.peakDay else null,
                    fusionSource = if (!fusion.insufficientData) fusion.ovulationEstimate.source else null,
                    fusionConfidence = if (!fusion.insufficientData) fusion.ovulationEstimate.confidence else 0.0,
                    phaseToday = if (!fusion.insufficientData) fusion.phaseToday else null,
                    irregular = fusion.irregular,
                    insufficientFertilityData = fusion.insufficientData
                )
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }

    fun logQuickPeriod(flowIntensity: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            try {
                val today = LocalDate.now()
                cycleRepository.saveEntry(
                    userId = userId,
                    start = today.toString(),
                    end = today.plusDays(4).toString(),
                    cycleLength = _uiState.value.cycleLengthAvg,
                    flowIntensity = com.petal.app.data.model.FlowIntensity.fromString(flowIntensity),
                )
                loadDashboard()
            } catch (_: Exception) {
                // Handle error silently for quick log
            }
        }
    }

    fun logQuickMoodSymptoms(moods: List<String>, symptoms: List<String>) {
        viewModelScope.launch {
            try {
                // Refresh dashboard to reflect the quick log
                loadDashboard()
            } catch (_: Exception) {
                // Handle error silently
            }
        }
    }
}
