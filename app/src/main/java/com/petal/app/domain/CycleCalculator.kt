package com.petal.app.domain

import com.petal.app.data.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Complete port of the web app's cycle calculation engine.
 * All math exactly matches the Next.js/TypeScript implementation in utils/cycle.ts.
 */
@Singleton
class CycleCalculator @Inject constructor() {

    companion object {
        /** Default assumed cycle length when no data is available. */
        const val DEFAULT_CYCLE_LENGTH = 28

        /** Menstrual phase spans the first N days. */
        const val MENSTRUAL_PHASE_END_DAY = 5

        /** Follicular phase ends at approximately 46% of the cycle. */
        const val FOLLICULAR_PHASE_RATIO = 0.46

        /** Ovulation window lasts approximately 2 days after follicular ends. */
        const val OVULATION_WINDOW_DAYS = 2

        /** Ovulation typically occurs ~14 days before the next period. */
        const val LUTEAL_PHASE_LENGTH = 14

        /** Fertile window extends 3 days on either side of ovulation. */
        const val FERTILE_WINDOW_RADIUS = 3

        /** A spread of 7+ days between shortest/longest cycle indicates irregularity. */
        const val IRREGULARITY_THRESHOLD = 7

        /** Short cycle flag threshold. */
        const val SHORT_CYCLE_THRESHOLD = 21

        /** Long cycle flag threshold. */
        const val LONG_CYCLE_THRESHOLD = 35

        /** Long period flag threshold (days). */
        const val LONG_PERIOD_THRESHOLD = 8

        /** Minimum logged cycles before predictions are considered baseline. */
        const val BASELINE_CYCLE_COUNT = 3

        /** Maximum pattern flags shown to the user. */
        const val MAX_PATTERN_FLAGS = 4
    }

    private fun parseDate(date: String): LocalDate =
        LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)

    private fun addDays(date: LocalDate, days: Int): LocalDate =
        date.plusDays(days.toLong())

    /**
     * Gets the anchor date for cycle calculations.
     * Rolls forward from the last period start by complete cycles.
     */
    private fun getCycleAnchorDate(cycles: List<CycleLog>, referenceDate: LocalDate): LocalDate {
        val average = max(1, getAverageCycleLength(cycles))
        val lastStart = parseDate(cycles[0].start)
        val today = referenceDate
        val daysSinceStart = max(0, ChronoUnit.DAYS.between(lastStart, today).toInt())
        val completedCycles = floor(daysSinceStart.toDouble() / average).toInt()
        return addDays(lastStart, completedCycles * average)
    }

    /**
     * Returns the average cycle length rounded to the nearest integer.
     * Defaults to 28 if no cycles are logged.
     */
    fun getAverageCycleLength(cycles: List<CycleLog>): Int {
        if (cycles.isEmpty()) return DEFAULT_CYCLE_LENGTH
        val total = cycles.sumOf { it.cycleLength }
        return (total.toDouble() / cycles.size).roundToInt()
    }

    /**
     * Predicts the next period start date.
     * Uses anchor date + average cycle length.
     */
    fun getNextPeriodDate(cycles: List<CycleLog>): LocalDate {
        if (cycles.isEmpty()) return LocalDate.now().plusDays(DEFAULT_CYCLE_LENGTH.toLong())
        val average = getAverageCycleLength(cycles)
        val anchor = getCycleAnchorDate(cycles, LocalDate.now())
        return addDays(anchor, average)
    }

    /**
     * Predicts the next ovulation date.
     * Ovulation occurs approximately 14 days before the next period.
     * If the calculated date is in the past, rolls forward by one cycle.
     */
    fun getOvulationDate(cycles: List<CycleLog>): LocalDate {
        if (cycles.isEmpty()) return LocalDate.now().plusDays(LUTEAL_PHASE_LENGTH.toLong())
        val average = getAverageCycleLength(cycles)
        val today = LocalDate.now()
        var ovulation = addDays(getCycleAnchorDate(cycles, today), average - LUTEAL_PHASE_LENGTH)
        if (ovulation.isBefore(today)) {
            ovulation = addDays(ovulation, average)
        }
        return ovulation
    }

    /**
     * Returns the fertile window: 3 days before to 3 days after ovulation.
     */
    fun getFertileWindow(cycles: List<CycleLog>): Pair<LocalDate, LocalDate> {
        val ovulation = getOvulationDate(cycles)
        return Pair(
            ovulation.minusDays(FERTILE_WINDOW_RADIUS.toLong()),
            ovulation.plusDays(FERTILE_WINDOW_RADIUS.toLong())
        )
    }

    /**
     * Determines the current cycle phase based on the day within the cycle.
     * Phase boundaries:
     * - Menstrual: days 1-5
     * - Follicular: days 6 to ~46% of cycle length
     * - Ovulation: next 2 days after follicular
     * - Luteal: remaining days
     */
    fun getCurrentPhase(cycles: List<CycleLog>): CyclePhase {
        val average = getAverageCycleLength(cycles)
        val dayInCycle = getCurrentCycleDay(cycles)
        val follicularEnd = max(MENSTRUAL_PHASE_END_DAY + 1, (average * FOLLICULAR_PHASE_RATIO).roundToInt())
        val ovulationEnd = min(average, follicularEnd + OVULATION_WINDOW_DAYS)

        return when {
            dayInCycle <= MENSTRUAL_PHASE_END_DAY -> CyclePhase.Menstrual
            dayInCycle <= follicularEnd -> CyclePhase.Follicular
            dayInCycle <= ovulationEnd -> CyclePhase.Ovulation
            else -> CyclePhase.Luteal
        }
    }

    /**
     * Detects irregular cycles.
     * A spread of 7+ days between shortest and longest cycle is considered irregular.
     */
    fun detectIrregularity(cycles: List<CycleLog>): Boolean {
        if (cycles.size < 2) return false
        val lengths = cycles.map { it.cycleLength }
        return (lengths.max() - lengths.min()) >= IRREGULARITY_THRESHOLD
    }

    /**
     * Returns the current day within the cycle (1-indexed).
     * Uses roll-forward logic: (daysSinceStart % averageCycleLength) + 1.
     */
    fun getCurrentCycleDay(
        cycles: List<CycleLog>,
        referenceDate: LocalDate = LocalDate.now()
    ): Int {
        if (cycles.isEmpty()) return 1
        val average = max(1, getAverageCycleLength(cycles))
        val lastStart = parseDate(cycles[0].start)
        val today = referenceDate
        val daysSinceStart = max(0, ChronoUnit.DAYS.between(lastStart, today).toInt())
        return (daysSinceStart % average) + 1
    }

    /**
     * Calculates prediction confidence based on number of cycles,
     * spread in cycle lengths, and recency of logging.
     */
    fun getPredictionConfidence(cycles: List<CycleLog>): PredictionConfidence {
        if (cycles.size < 2) return PredictionConfidence.Low

        val average = max(1, getAverageCycleLength(cycles))
        val lengths = cycles.map { it.cycleLength }
        val spread = lengths.max() - lengths.min()
        val today = LocalDate.now()
        val lastStart = parseDate(cycles[0].start)
        val daysSinceLastStart = max(0, ChronoUnit.DAYS.between(lastStart, today).toInt())
        val hasRecentLogging = daysSinceLastStart <= average * 2 + 7

        return when {
            cycles.size >= 6 && spread <= 4 && hasRecentLogging -> PredictionConfidence.High
            cycles.size >= 3 && spread <= 7 && hasRecentLogging -> PredictionConfidence.Moderate
            cycles.size >= 4 && spread <= 4 -> PredictionConfidence.Moderate
            else -> PredictionConfidence.Low
        }
    }

    /**
     * Returns days between two date strings (inclusive).
     */
    fun getDaysBetween(start: String, end: String): Int {
        val startDate = parseDate(start)
        val endDate = parseDate(end)
        return max(1, ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1)
    }

    /**
     * Returns number of days until a target date from today.
     */
    fun getDaysUntil(date: LocalDate): Int {
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, date).toInt()
    }

    /**
     * Generates pattern flags based on cycle data analysis.
     * Exactly matches the 6 flag types from the web app.
     */
    fun getCyclePatternFlags(cycles: List<CycleLog>): List<CyclePatternFlag> {
        if (cycles.isEmpty()) return emptyList()

        val flags = mutableListOf<CyclePatternFlag>()
        val averageCycle = getAverageCycleLength(cycles)
        val lengths = cycles.map { it.cycleLength }
        val spread = lengths.max() - lengths.min()
        val averagePeriodLength = (cycles.sumOf { getDaysBetween(it.start, it.end) }.toDouble() / cycles.size).roundToInt()
        val daysSinceLastStart = max(
            0,
            ChronoUnit.DAYS.between(parseDate(cycles[0].start), LocalDate.now()).toInt()
        )

        // Flag 1: Baseline building
        if (cycles.size < BASELINE_CYCLE_COUNT) {
            flags.add(
                CyclePatternFlag(
                    id = "baseline-building",
                    title = "Prediction baseline is still forming",
                    detail = "Log a few more cycles to make timing signals more dependable.",
                    severity = FlagSeverity.Info
                )
            )
        }

        // Flag 2: Cycle variation
        if (spread >= IRREGULARITY_THRESHOLD) {
            flags.add(
                CyclePatternFlag(
                    id = "cycle-variation",
                    title = "Cycle lengths vary meaningfully",
                    detail = "Recent logs span $spread days, so predicted timing may shift month to month.",
                    severity = FlagSeverity.Watch
                )
            )
        }

        // Flag 3: Short cycles
        if (averageCycle < SHORT_CYCLE_THRESHOLD) {
            flags.add(
                CyclePatternFlag(
                    id = "short-cycles",
                    title = "Average cycle is on the short side",
                    detail = "Your recent average is $averageCycle days.",
                    severity = FlagSeverity.Care
                )
            )
        }

        // Flag 4: Long cycles
        if (averageCycle > LONG_CYCLE_THRESHOLD) {
            flags.add(
                CyclePatternFlag(
                    id = "long-cycles",
                    title = "Average cycle is on the long side",
                    detail = "Your recent average is $averageCycle days.",
                    severity = FlagSeverity.Care
                )
            )
        }

        // Flag 5: Long periods
        if (averagePeriodLength >= LONG_PERIOD_THRESHOLD) {
            flags.add(
                CyclePatternFlag(
                    id = "long-periods",
                    title = "Periods have been running longer",
                    detail = "Recent entries average about $averagePeriodLength bleeding days.",
                    severity = FlagSeverity.Watch
                )
            )
        }

        // Flag 6: Stale data
        if (daysSinceLastStart > averageCycle * 2 + 7) {
            flags.add(
                CyclePatternFlag(
                    id = "stale-data",
                    title = "Recent logs are overdue",
                    detail = "Adding a fresh period entry will improve predictions and summaries.",
                    severity = FlagSeverity.Info
                )
            )
        }

        return flags.take(MAX_PATTERN_FLAGS)
    }

    /**
     * Formats a LocalDate for display.
     */
    fun formatDate(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d")
        return date.format(formatter)
    }

    /**
     * Formats a LocalDate as ISO date string.
     */
    fun formatLocalISODate(date: LocalDate): String =
        date.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
