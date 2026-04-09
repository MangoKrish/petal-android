package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.CycleEntry
import com.petal.app.data.model.CycleLog
import com.petal.app.data.model.CyclePhase
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.CycleRepository
import com.petal.app.domain.CycleCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class CalendarDayInfo(
    val date: LocalDate,
    val isPeriodDay: Boolean = false,
    val isPredictedPeriod: Boolean = false,
    val isFertileDay: Boolean = false,
    val isOvulationDay: Boolean = false,
    val isToday: Boolean = false,
    val phase: CyclePhase? = null,
    val hasEntry: Boolean = false
)

data class CalendarUiState(
    val isLoading: Boolean = true,
    val currentMonth: YearMonth = YearMonth.now(),
    val days: List<CalendarDayInfo> = emptyList(),
    val entries: List<CycleEntry> = emptyList(),
    val selectedDate: LocalDate? = null,
    val selectedEntry: CycleEntry? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository,
    private val cycleCalculator: CycleCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCalendar()
    }

    fun loadCalendar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val entries = cycleRepository.getEntries(userId)
            val cycles = entries.map { it.toCycleLog() }

            buildCalendarDays(cycles, entries, _uiState.value.currentMonth)
        }
    }

    fun navigateMonth(offset: Int) {
        viewModelScope.launch {
            val newMonth = _uiState.value.currentMonth.plusMonths(offset.toLong())
            _uiState.update { it.copy(currentMonth = newMonth) }

            val userId = authRepository.getCurrentUserId() ?: return@launch
            val entries = cycleRepository.getEntries(userId)
            val cycles = entries.map { it.toCycleLog() }

            buildCalendarDays(cycles, entries, newMonth)
        }
    }

    fun selectDate(date: LocalDate) {
        val entry = _uiState.value.entries.firstOrNull { entry ->
            val start = LocalDate.parse(entry.start)
            val end = LocalDate.parse(entry.end)
            !date.isBefore(start) && !date.isAfter(end)
        }
        _uiState.update { it.copy(selectedDate = date, selectedEntry = entry) }
    }

    private fun buildCalendarDays(
        cycles: List<CycleLog>,
        entries: List<CycleEntry>,
        month: YearMonth
    ) {
        val today = LocalDate.now()
        val firstDay = month.atDay(1)
        val lastDay = month.atEndOfMonth()

        // Build period day set from actual entries
        val periodDays = mutableSetOf<LocalDate>()
        for (entry in entries) {
            val start = LocalDate.parse(entry.start)
            val end = LocalDate.parse(entry.end)
            var day = start
            while (!day.isAfter(end)) {
                periodDays.add(day)
                day = day.plusDays(1)
            }
        }

        // Predicted dates
        val nextPeriod = cycleCalculator.getNextPeriodDate(cycles)
        val avgLength = cycleCalculator.getAverageCycleLength(cycles)
        val predictedPeriodDays = mutableSetOf<LocalDate>()
        for (i in 0..4) { // Predict 5-day period
            predictedPeriodDays.add(nextPeriod.plusDays(i.toLong()))
        }

        val (fertileStart, fertileEnd) = cycleCalculator.getFertileWindow(cycles)
        val ovulationDate = cycleCalculator.getOvulationDate(cycles)

        val days = mutableListOf<CalendarDayInfo>()
        var current = firstDay
        while (!current.isAfter(lastDay)) {
            val phase = if (cycles.isNotEmpty()) {
                cycleCalculator.getCurrentPhase(
                    cycles.map { CycleLog(it.start, it.end, it.cycleLength) }
                )
            } else null

            val isFertile = !current.isBefore(fertileStart) && !current.isAfter(fertileEnd)

            days.add(
                CalendarDayInfo(
                    date = current,
                    isPeriodDay = current in periodDays,
                    isPredictedPeriod = current in predictedPeriodDays && current !in periodDays,
                    isFertileDay = isFertile,
                    isOvulationDay = current == ovulationDate,
                    isToday = current == today,
                    phase = phase,
                    hasEntry = entries.any { entry ->
                        val s = LocalDate.parse(entry.start)
                        val e = LocalDate.parse(entry.end)
                        !current.isBefore(s) && !current.isAfter(e)
                    }
                )
            )
            current = current.plusDays(1)
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                days = days,
                entries = entries,
                currentMonth = month
            )
        }
    }
}
