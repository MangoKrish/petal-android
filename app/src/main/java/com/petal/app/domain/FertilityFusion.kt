package com.petal.app.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android port of new-project/src/utils/fertility-fusion.ts and
 * PetalAPI/src/services/fertilityFusionMath.ts.
 *
 * Pure math — same algorithm runs on web, API, and Android per
 * PHASE_6_7_PLAN.md §6A.2. If you change priority order, window math, or
 * confidence weights, change all three together.
 *
 * NOTE: data-layer wiring (a Room entity for per-day fertility logs, repo
 * fetch, ViewModel integration) is intentionally deferred to a dedicated
 * Android session. This module is the math the wiring will call into.
 */
@Singleton
class FertilityFusion @Inject constructor() {

    enum class OvulationSource { LH, PAIN, MUCUS, BBT, CALENDAR }
    enum class FertilityPhaseToday { LOW, RISING, PEAK, POST_OVULATION }
    enum class LhTestResult { POSITIVE, NEGATIVE, INCONCLUSIVE }

    data class FertilityLog(
        val logDate: LocalDate,
        val temperature: Double? = null,
        val cervicalMucus: String? = null,
        val ovulationPain: Boolean = false,
        val lhTestResult: LhTestResult? = null
    )

    data class FertilityEvidence(
        val signal: String,
        val day: LocalDate,
        val weight: Double
    )

    data class OvulationEstimate(
        val date: LocalDate,
        val source: OvulationSource,
        val confidence: Double
    )

    data class FertilityWindow(val start: LocalDate, val end: LocalDate)

    data class FertilityEstimate(
        val ovulationEstimate: OvulationEstimate,
        val fertileWindow: FertilityWindow,
        val peakDay: LocalDate,
        val phaseToday: FertilityPhaseToday,
        val evidence: List<FertilityEvidence>,
        val irregular: Boolean,
        val insufficientData: Boolean
    )

    data class FusionInput(
        val today: LocalDate,
        val predictedNextPeriod: LocalDate,
        val cycleLengthMean: Int,
        val cycleLengthVariance: Double,
        val baseConfidence: Double,
        val logs: List<FertilityLog>,
        val observedCycleCount: Int
    )

    /* ─── Signal detectors ──────────────────────────────────────────── */

    /**
     * Sustained BBT rise of ≥ 0.2°C over 3+ consecutive days. Returns the
     * date of the first elevated reading, or null if no shift detected.
     */
    fun detectBbtShift(logs: List<FertilityLog>): LocalDate? {
        val temps = logs
            .filter { it.temperature != null }
            .sortedBy { it.logDate }
            .map { it.logDate to it.temperature!! }
        if (temps.size < 6) return null

        val midpoint = temps.size / 2
        val baseline = temps.take(midpoint).sumOf { it.second } / midpoint

        var consecutive = 0
        var firstShift: LocalDate? = null
        for (i in midpoint until temps.size) {
            val (date, temp) = temps[i]
            if (temp >= baseline + 0.2) {
                consecutive += 1
                if (consecutive == 1) firstShift = date
                if (consecutive >= 3 && firstShift != null) return firstShift
            } else {
                consecutive = 0
                firstShift = null
            }
        }
        return null
    }

    fun findEggWhiteMucus(logs: List<FertilityLog>): LocalDate? =
        logs.sortedByDescending { it.logDate }
            .firstOrNull { it.cervicalMucus == "egg_white" }
            ?.logDate

    fun findOvulationPain(logs: List<FertilityLog>): LocalDate? =
        logs.sortedByDescending { it.logDate }
            .firstOrNull { it.ovulationPain }
            ?.logDate

    fun findRecentPositiveLh(
        logs: List<FertilityLog>,
        today: LocalDate,
        withinDays: Long = 2
    ): LocalDate? = logs.sortedByDescending { it.logDate }
        .firstOrNull {
            it.lhTestResult == LhTestResult.POSITIVE &&
                ChronoUnit.DAYS.between(it.logDate, today) in 0..withinDays
        }
        ?.logDate

    /* ─── Phase classification ─────────────────────────────────────── */

    private fun classifyPhase(
        today: LocalDate,
        ovulation: LocalDate,
        windowStart: LocalDate,
        peak: LocalDate
    ): FertilityPhaseToday = when {
        today.isBefore(windowStart) -> FertilityPhaseToday.LOW
        today.isAfter(ovulation) -> FertilityPhaseToday.POST_OVULATION
        today == peak -> FertilityPhaseToday.PEAK
        else -> FertilityPhaseToday.RISING
    }

    /* ─── Main estimator ────────────────────────────────────────────── */

    fun predictFertileWindow(input: FusionInput): FertilityEstimate {
        val today = input.today

        if (input.observedCycleCount < 3) {
            return FertilityEstimate(
                ovulationEstimate = OvulationEstimate(
                    date = input.predictedNextPeriod.minusDays(14),
                    source = OvulationSource.CALENDAR,
                    confidence = 0.1
                ),
                fertileWindow = FertilityWindow(
                    start = input.predictedNextPeriod.minusDays(19),
                    end = input.predictedNextPeriod.minusDays(13)
                ),
                peakDay = input.predictedNextPeriod.minusDays(15),
                phaseToday = FertilityPhaseToday.LOW,
                evidence = emptyList(),
                irregular = false,
                insufficientData = true
            )
        }

        val irregular = input.cycleLengthVariance > 49.0
        val widenDays = if (irregular) 2L else 0L
        val evidence = mutableListOf<FertilityEvidence>()

        val lh = findRecentPositiveLh(input.logs, today)
        val bbt = detectBbtShift(input.logs)
        val eggWhite = findEggWhiteMucus(input.logs)
        val pain = findOvulationPain(input.logs)

        val (ovulationDate, source) = when {
            lh != null -> {
                evidence += FertilityEvidence("lh_positive", lh, 0.9)
                lh.plusDays(1) to OvulationSource.LH
            }
            bbt != null -> {
                evidence += FertilityEvidence("bbt_shift", bbt, 0.7)
                bbt.minusDays(1) to OvulationSource.BBT
            }
            eggWhite != null && pain != null -> {
                evidence += FertilityEvidence("egg_white_mucus", eggWhite, 0.5)
                evidence += FertilityEvidence("ovulation_pain", pain, 0.5)
                eggWhite to OvulationSource.PAIN
            }
            eggWhite != null -> {
                evidence += FertilityEvidence("egg_white_mucus", eggWhite, 0.5)
                eggWhite.plusDays(1) to OvulationSource.MUCUS
            }
            else -> {
                val cal = input.predictedNextPeriod.minusDays(14)
                evidence += FertilityEvidence("calendar_estimate", cal, 0.3)
                cal to OvulationSource.CALENDAR
            }
        }

        val windowStart = ovulationDate.minusDays(5 + widenDays)
        val windowEnd = ovulationDate.plusDays(1 + widenDays)
        val peakDay = ovulationDate.minusDays(1)

        val hasRecentBbt = bbt != null
        val hasRecentMucus = input.logs.any { it.cervicalMucus != null }
        val hasOvulationPainThisCycle = pain != null
        val hasLhThisCycle = input.logs.any { it.lhTestResult != null }

        var confidence = input.baseConfidence
        if (hasRecentBbt) confidence += 0.15
        if (hasRecentMucus) confidence += 0.10
        if (hasOvulationPainThisCycle) confidence += 0.10
        if (hasLhThisCycle) confidence += 0.20
        if (irregular) confidence -= 0.10
        confidence = confidence.coerceIn(0.05, 0.95)
        confidence = (confidence * 100).toInt() / 100.0

        return FertilityEstimate(
            ovulationEstimate = OvulationEstimate(ovulationDate, source, confidence),
            fertileWindow = FertilityWindow(windowStart, windowEnd),
            peakDay = peakDay,
            phaseToday = classifyPhase(today, ovulationDate, windowStart, peakDay),
            evidence = evidence,
            irregular = irregular,
            insufficientData = false
        )
    }
}
