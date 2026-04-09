package com.petal.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.*
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.CycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class CycleLogUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now(),
    val cycleLength: Int = 28,
    val flowIntensity: FlowIntensity = FlowIntensity.Medium,
    val pain: SymptomLevel = SymptomLevel.None,
    val cramps: SymptomLevel = SymptomLevel.None,
    val cravings: SymptomLevel = SymptomLevel.None,
    val mood: MoodLevel = MoodLevel.Calm,
    val headaches: SymptomLevel = SymptomLevel.None,
    val editingEntryId: String? = null
)

@HiltViewModel
class CycleLogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CycleLogUiState())
    val uiState: StateFlow<CycleLogUiState> = _uiState.asStateFlow()

    private val requestedDate = savedStateHandle.get<String>("date")
    private val requestedEntryId = savedStateHandle.get<String>("entryId")

    init {
        initializeForm()
    }

    private fun initializeForm() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val latestEntry = cycleRepository.getLatestEntry(userId)
            val initialDate = requestedDate?.let(LocalDate::parse) ?: LocalDate.now()

            if (requestedEntryId != null) {
                val entry = cycleRepository.getEntry(userId, requestedEntryId) ?: latestEntry
                if (entry != null) {
                    applyEntry(entry)
                } else {
                    applyRememberedDefaults(initialDate, latestEntry)
                }
                return@launch
            }

            val matchingEntry = cycleRepository.getEntries(userId).firstOrNull { entry ->
                val start = LocalDate.parse(entry.start)
                val end = LocalDate.parse(entry.end)
                !initialDate.isBefore(start) && !initialDate.isAfter(end)
            }

            if (matchingEntry != null) {
                applyEntry(matchingEntry)
            } else {
                applyRememberedDefaults(initialDate, latestEntry)
            }
        }
    }

    private fun applyEntry(entry: CycleEntry) {
        _uiState.update {
            it.copy(
                startDate = LocalDate.parse(entry.start),
                endDate = LocalDate.parse(entry.end),
                cycleLength = entry.cycleLength,
                flowIntensity = entry.flowIntensity,
                pain = entry.painLevel,
                cramps = entry.crampsLevel,
                cravings = entry.cravingsLevel,
                mood = entry.moodLevel,
                headaches = entry.headachesLevel,
                editingEntryId = entry.id,
                error = null
            )
        }
    }

    private fun applyRememberedDefaults(date: LocalDate, latestEntry: CycleEntry?) {
        val start = date
        val end = latestEntry?.let { remembered ->
            val previousDuration = (
                LocalDate.parse(remembered.end).toEpochDay() -
                    LocalDate.parse(remembered.start).toEpochDay()
                ).toInt().coerceAtLeast(0)
            start.plusDays(previousDuration.toLong())
        } ?: date

        _uiState.update {
            it.copy(
                startDate = start,
                endDate = end,
                cycleLength = latestEntry?.cycleLength ?: it.cycleLength,
                flowIntensity = latestEntry?.flowIntensity ?: it.flowIntensity,
                pain = latestEntry?.painLevel ?: it.pain,
                cramps = latestEntry?.crampsLevel ?: it.cramps,
                cravings = latestEntry?.cravingsLevel ?: it.cravings,
                mood = latestEntry?.moodLevel ?: it.mood,
                headaches = latestEntry?.headachesLevel ?: it.headaches,
                editingEntryId = null,
                error = null
            )
        }
    }

    fun updateStartDate(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun updateEndDate(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun updateCycleLength(length: Int) {
        _uiState.update { it.copy(cycleLength = length.coerceIn(15, 45)) }
    }

    fun updateFlowIntensity(flow: FlowIntensity) {
        _uiState.update { it.copy(flowIntensity = flow) }
    }

    fun updatePain(level: SymptomLevel) {
        _uiState.update { it.copy(pain = level) }
    }

    fun updateCramps(level: SymptomLevel) {
        _uiState.update { it.copy(cramps = level) }
    }

    fun updateCravings(level: SymptomLevel) {
        _uiState.update { it.copy(cravings = level) }
    }

    fun updateMood(mood: MoodLevel) {
        _uiState.update { it.copy(mood = mood) }
    }

    fun updateHeadaches(level: SymptomLevel) {
        _uiState.update { it.copy(headaches = level) }
    }

    fun save(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSaving = true, error = null) }

            try {
                val userId = authRepository.getCurrentUserId()
                    ?: throw IllegalStateException("Not logged in")

                cycleRepository.saveEntry(
                    userId = userId,
                    entryId = state.editingEntryId,
                    start = state.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    end = state.endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    cycleLength = state.cycleLength,
                    flowIntensity = state.flowIntensity,
                    pain = state.pain,
                    cramps = state.cramps,
                    cravings = state.cravings,
                    mood = state.mood,
                    headaches = state.headaches
                )

                _uiState.update { it.copy(isSaving = false, success = true) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = e.message ?: "Failed to save entry")
                }
            }
        }
    }

    fun deleteEntry(entryId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId() ?: return@launch
                cycleRepository.deleteEntry(userId, entryId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
