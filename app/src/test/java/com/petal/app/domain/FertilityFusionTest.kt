package com.petal.app.domain

import com.petal.app.domain.FertilityFusion.FertilityLog
import com.petal.app.domain.FertilityFusion.FusionInput
import com.petal.app.domain.FertilityFusion.LhTestResult
import com.petal.app.domain.FertilityFusion.OvulationSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [FertilityFusion]. PHASE_6_7_PLAN.md §6A.2 — mirrors web
 * (fertility-fusion.test.ts) and API (fertilityFusionMath.test.ts) coverage
 * for the symptom-fusion fertility estimator.
 */
class FertilityFusionTest {

    private val fusion = FertilityFusion()

    private val today: LocalDate = LocalDate.of(2026, 5, 15)
    private val nextPeriod: LocalDate = today.plusDays(15)

    private fun baseInput(
        logs: List<FertilityLog> = emptyList(),
        observedCycleCount: Int = 6,
        cycleLengthVariance: Double = 4.0,
        baseConfidence: Double = 0.5,
    ): FusionInput = FusionInput(
        today = today,
        predictedNextPeriod = nextPeriod,
        cycleLengthMean = 28,
        cycleLengthVariance = cycleLengthVariance,
        baseConfidence = baseConfidence,
        logs = logs,
        observedCycleCount = observedCycleCount,
    )

    /* ─── insufficient data ───────────────────────────────────────── */

    @Test
    fun `predictFertileWindow flags insufficient data when fewer than 3 cycles observed`() {
        val r = fusion.predictFertileWindow(baseInput(observedCycleCount = 2))
        assertTrue(r.insufficientData)
        assertEquals(OvulationSource.CALENDAR, r.ovulationEstimate.source)
        assertTrue(r.evidence.isEmpty())
    }

    /* ─── signal detectors ────────────────────────────────────────── */

    @Test
    fun `detectBbtShift returns null with fewer than 6 readings`() {
        val logs = (0..4).map {
            FertilityLog(logDate = today.minusDays(it.toLong()), temperature = 36.5)
        }
        assertNull(fusion.detectBbtShift(logs))
    }

    @Test
    fun `detectBbtShift detects a sustained 3-day rise of 0_2C`() {
        // Six earlier readings at 36.4, then three at 36.7 (rise of 0.3°C)
        val logs = listOf(
            FertilityLog(today.minusDays(8), temperature = 36.4),
            FertilityLog(today.minusDays(7), temperature = 36.4),
            FertilityLog(today.minusDays(6), temperature = 36.4),
            FertilityLog(today.minusDays(5), temperature = 36.4),
            FertilityLog(today.minusDays(4), temperature = 36.4),
            FertilityLog(today.minusDays(3), temperature = 36.4),
            FertilityLog(today.minusDays(2), temperature = 36.7),
            FertilityLog(today.minusDays(1), temperature = 36.7),
            FertilityLog(today,             temperature = 36.7),
        )
        val shift = fusion.detectBbtShift(logs)
        assertNotNull(shift)
        assertEquals(today.minusDays(2), shift)
    }

    @Test
    fun `findEggWhiteMucus returns the most recent egg-white day`() {
        val logs = listOf(
            FertilityLog(today.minusDays(5), cervicalMucus = "egg_white"),
            FertilityLog(today.minusDays(2), cervicalMucus = "egg_white"),
            FertilityLog(today.minusDays(1), cervicalMucus = "watery"),
        )
        assertEquals(today.minusDays(2), fusion.findEggWhiteMucus(logs))
    }

    @Test
    fun `findRecentPositiveLh ignores positives outside the 2-day window`() {
        val recent = FertilityLog(today.minusDays(1), lhTestResult = LhTestResult.POSITIVE)
        val tooOld = FertilityLog(today.minusDays(5), lhTestResult = LhTestResult.POSITIVE)
        assertEquals(today.minusDays(1), fusion.findRecentPositiveLh(listOf(recent, tooOld), today))
        assertNull(fusion.findRecentPositiveLh(listOf(tooOld), today))
    }

    /* ─── priority order ──────────────────────────────────────────── */

    @Test
    fun `predictFertileWindow uses LH over BBT when both are present`() {
        val logs = listOf(
            FertilityLog(today.minusDays(8), temperature = 36.4),
            FertilityLog(today.minusDays(7), temperature = 36.4),
            FertilityLog(today.minusDays(6), temperature = 36.4),
            FertilityLog(today.minusDays(5), temperature = 36.4),
            FertilityLog(today.minusDays(4), temperature = 36.4),
            FertilityLog(today.minusDays(3), temperature = 36.4),
            FertilityLog(today.minusDays(2), temperature = 36.7),
            FertilityLog(today.minusDays(1), temperature = 36.7, lhTestResult = LhTestResult.POSITIVE),
            FertilityLog(today,             temperature = 36.7),
        )
        val r = fusion.predictFertileWindow(baseInput(logs = logs))
        assertEquals(OvulationSource.LH, r.ovulationEstimate.source)
        // Ovulation = day after the most recent positive LH = today
        assertEquals(today, r.ovulationEstimate.date)
    }

    @Test
    fun `predictFertileWindow uses BBT over mucus when LH is absent`() {
        val logs = listOf(
            FertilityLog(today.minusDays(8), temperature = 36.4),
            FertilityLog(today.minusDays(7), temperature = 36.4),
            FertilityLog(today.minusDays(6), temperature = 36.4),
            FertilityLog(today.minusDays(5), temperature = 36.4),
            FertilityLog(today.minusDays(4), temperature = 36.4, cervicalMucus = "egg_white"),
            FertilityLog(today.minusDays(3), temperature = 36.4),
            FertilityLog(today.minusDays(2), temperature = 36.7),
            FertilityLog(today.minusDays(1), temperature = 36.7),
            FertilityLog(today,             temperature = 36.7),
        )
        val r = fusion.predictFertileWindow(baseInput(logs = logs))
        assertEquals(OvulationSource.BBT, r.ovulationEstimate.source)
    }

    @Test
    fun `predictFertileWindow falls back to calendar when no signals are present`() {
        val r = fusion.predictFertileWindow(baseInput(logs = emptyList()))
        assertEquals(OvulationSource.CALENDAR, r.ovulationEstimate.source)
        assertEquals(nextPeriod.minusDays(14), r.ovulationEstimate.date)
        assertFalse("calendar-only path is still 'sufficient data' once 3+ cycles seen", r.insufficientData)
    }

    /* ─── confidence aggregation ──────────────────────────────────── */

    @Test
    fun `predictFertileWindow caps confidence at 0_95`() {
        // Stack every confidence-boosting signal: BBT, mucus, ovulation pain, LH.
        val logs = listOf(
            FertilityLog(today.minusDays(8), temperature = 36.4, cervicalMucus = "dry"),
            FertilityLog(today.minusDays(7), temperature = 36.4),
            FertilityLog(today.minusDays(6), temperature = 36.4),
            FertilityLog(today.minusDays(5), temperature = 36.4),
            FertilityLog(today.minusDays(4), temperature = 36.4),
            FertilityLog(today.minusDays(3), temperature = 36.4, cervicalMucus = "egg_white", ovulationPain = true),
            FertilityLog(today.minusDays(2), temperature = 36.7),
            FertilityLog(today.minusDays(1), temperature = 36.7, lhTestResult = LhTestResult.POSITIVE),
            FertilityLog(today,             temperature = 36.7),
        )
        val r = fusion.predictFertileWindow(baseInput(logs = logs, baseConfidence = 0.9))
        assertTrue("confidence should be capped", r.ovulationEstimate.confidence <= 0.95)
    }

    @Test
    fun `predictFertileWindow flags irregular cycles and widens the window`() {
        // Variance > 49 → irregular → window widens by 2 days on each side.
        val r = fusion.predictFertileWindow(baseInput(cycleLengthVariance = 60.0))
        assertTrue(r.irregular)
        val regular = fusion.predictFertileWindow(baseInput(cycleLengthVariance = 9.0))
        assertFalse(regular.irregular)
        val widenedWindow = java.time.temporal.ChronoUnit.DAYS.between(r.fertileWindow.start, r.fertileWindow.end)
        val normalWindow = java.time.temporal.ChronoUnit.DAYS.between(regular.fertileWindow.start, regular.fertileWindow.end)
        assertTrue("irregular window must be wider than regular", widenedWindow > normalWindow)
    }
}
