package com.petal.app.domain

import com.petal.app.data.model.CycleLog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * Unit tests for [BayesianPredictor]. PHASE_6_7_PLAN.md §6A.2 — brings Android
 * under the same table-driven coverage as the web (bayesian.test.ts) and API
 * (predictionService.test.ts). The shared confidence formula is the most
 * important cross-platform contract; if the cases at the end of this file
 * fail, web/API/Android have drifted.
 */
class BayesianPredictorTest {

    private val predictor = BayesianPredictor()

    private fun cycle(length: Int): CycleLog =
        CycleLog(start = "2026-01-01", end = "2026-01-05", cycleLength = length)

    /* ─── computePosterior ─────────────────────────────────────────── */

    @Test
    fun `computePosterior returns the prior with no observations`() {
        val (mean, variance) = predictor.computePosterior(emptyList())
        assertEquals(BayesianPredictor.PRIOR_MEAN, mean, 1e-9)
        assertEquals(BayesianPredictor.PRIOR_VARIANCE, variance, 1e-9)
    }

    @Test
    fun `computePosterior nudges toward a single observation but stays anchored to the prior`() {
        val (mean, variance) = predictor.computePosterior(listOf(cycle(30)))
        assertTrue("posterior mean should move toward 30", mean > BayesianPredictor.PRIOR_MEAN)
        assertTrue("posterior mean should not overshoot the observation", mean < 30.0)
        assertTrue(
            "posterior variance should shrink relative to the prior",
            variance < BayesianPredictor.PRIOR_VARIANCE
        )
    }

    @Test
    fun `computePosterior converges tightly with many consistent cycles`() {
        val cycles = List(20) { cycle(28) }
        val (mean, variance) = predictor.computePosterior(cycles)
        assertEquals(28.0, mean, 0.5)
        assertTrue("variance should shrink dramatically", variance < 1.0)
    }

    @Test
    fun `computePosterior monotonically tightens variance as observations accumulate`() {
        val v1 = predictor.computePosterior(listOf(cycle(28))).second
        val v5 = predictor.computePosterior(List(5) { cycle(28) }).second
        val v15 = predictor.computePosterior(List(15) { cycle(28) }).second
        assertTrue("5 cycles narrower than 1", v5 < v1)
        assertTrue("15 cycles narrower than 5", v15 < v5)
    }

    /* ─── decayWeights ─────────────────────────────────────────────── */

    @Test
    fun `decayWeights starts at 1_0 and decays geometrically`() {
        val w = predictor.decayWeights(5)
        assertEquals(1.0, w[0], 1e-9)
        assertEquals(BayesianPredictor.DECAY_LAMBDA, w[1], 1e-9)
        assertEquals(BayesianPredictor.DECAY_LAMBDA.pow(2), w[2], 1e-9)
        assertEquals(BayesianPredictor.DECAY_LAMBDA.pow(4), w[4], 1e-9)
    }

    @Test
    fun `decayWeights with n=0 returns an empty list`() {
        assertTrue(predictor.decayWeights(0).isEmpty())
    }

    /* ─── computeConfidence — cross-platform contract ──────────────── */

    @Test
    fun `computeConfidence floors at 0_1 when there is no data and full prior variance`() {
        val c = predictor.computeConfidence(0, BayesianPredictor.PRIOR_VARIANCE)
        assertEquals(0.1, c, 1e-9)
    }

    @Test
    fun `computeConfidence equals 0_5 when data factor is saturated but variance equals prior`() {
        // 6 cycles → dataFactor = 1; varianceFactor = 0 → raw = 0.5
        val c = predictor.computeConfidence(6, BayesianPredictor.PRIOR_VARIANCE)
        assertEquals(0.5, c, 1e-9)
    }

    @Test
    fun `computeConfidence ceilings at 0_99 when both factors are at their max`() {
        val c = predictor.computeConfidence(20, 0.0)
        assertEquals(0.99, c, 1e-9)
    }

    @Test
    fun `computeConfidence agrees with the shared cross-platform contract`() {
        // If this fails, web, API and Android have drifted. See
        // new-project/src/utils/__tests__/bayesian.test.ts and
        // PetalAPI/src/services/__tests__/predictionService.test.ts.
        val cases = listOf(
            Triple(0, BayesianPredictor.PRIOR_VARIANCE, 0.1),
            Triple(6, BayesianPredictor.PRIOR_VARIANCE, 0.5),
            Triple(20, 0.0, 0.99),
            Triple(3, BayesianPredictor.PRIOR_VARIANCE / 2.0, 0.5),
        )
        for ((n, v, expected) in cases) {
            assertEquals(
                "computeConfidence($n, $v) should equal $expected",
                expected,
                predictor.computeConfidence(n, v),
                1e-9
            )
        }
    }

    /* ─── predict ──────────────────────────────────────────────────── */

    @Test
    fun `predict returns a sensible next-period date for stable 28-day cycles`() {
        val cycles = List(6) { cycle(28) }
        val r = predictor.predict(cycles)
        // Mean prediction should be very close to 28
        assertEquals(28.0, r.predictedLength, 0.5)
        // Confidence should saturate the data factor (6 cycles) and produce
        // a high score with low variance
        assertTrue("confidence at 6 stable cycles should be high", r.confidence >= 0.7)
        // 95% CI should bracket 28
        val (low, high) = r.confidenceInterval
        assertTrue("CI lower bound below 28", low < 28.5)
        assertTrue("CI upper bound above 28", high > 27.5)
    }
}
