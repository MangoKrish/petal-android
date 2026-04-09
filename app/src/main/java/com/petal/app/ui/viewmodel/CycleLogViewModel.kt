package com.petal.app.ui.viewmodel

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
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CycleLogUiState())
    val uiState: StateFlow<CycleLogUiState> = _uiState.asStateFlow()

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

    fun loadEntry(entryId: String) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val entry = cycleRepository.getEntry(userId, entryId) ?: return@launch

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
                    editingEntryId = entry.id
                )
            }
        }
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
