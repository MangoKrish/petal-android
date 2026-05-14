package com.petal.app.domain

import com.petal.app.data.model.CycleLog
import com.petal.app.data.model.Symptoms
import com.petal.app.data.model.SymptomLevel
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Bayesian cycle length predictor.
 *
 * Uses a conjugate normal-normal model:
 * - Prior: population distribution of cycle lengths (mean=28, std=4)
 * - Likelihood: user's observed cycle lengths
 * - Posterior: updated belief about user's true cycle length
 *
 * Also incorporates symptom-aware adjustments and temperature trend detection
 * for more accurate predictions with additional data signals.
 */
@Singleton
class BayesianPredictor @Inject constructor() {

    companion object {
        // ---- Prior distribution (population-level) ----
        // Based on published research: normal menstrual cycles average 28 days with std ~4
        const val PRIOR_MEAN = 28.0
        const val PRIOR_STD = 4.0
        const val PRIOR_VARIANCE = PRIOR_STD * PRIOR_STD // 16.0

        // ---- Cycle length bounds ----
        const val MIN_CYCLE_LENGTH = 18.0
        const val MAX_CYCLE_LENGTH = 45.0

        // ---- Ovulation & fertile window ----
        const val LUTEAL_PHASE_DAYS = 14L
        const val FERTILE_WINDOW_RADIUS = 3L

        // ---- Symptom adjustment magnitudes ----
        /** Severe cramps: high prostaglandins, sometimes shorter cycles. */
        const val CRAMPS_ADJUSTMENT = -0.3
        /** Mood disruption: hormonal fluctuations, sometimes extends luteal phase. */
        const val MOOD_ADJUSTMENT = 0.2
        /** Severe headaches near menstruation: estrogen withdrawal effect. */
        const val HEADACHE_ADJUSTMENT = -0.15
        /** Damping factor for cycle-length trend following. */
        const val TREND_DAMPING_FACTOR = 0.3
        /** Maximum total symptom adjustment (days). Matches web bayesian.ts MAX_SYMPTOM_ADJUSTMENT_DAYS. */
        const val MAX_SYMPTOM_ADJUSTMENT = 2.0

        /** Exponential decay base — newer cycles weigh more. Matches web/API DECAY_LAMBDA. */
        const val DECAY_LAMBDA = 0.85

        // ---- Temperature trend detection ----
        /** Fraction of readings used as baseline. */
        const val TEMP_BASELINE_FRACTION = 0.6
        /** Minimum temperature shift (degrees C) to indicate post-ovulation. */
        const val TEMP_SHIFT_THRESHOLD = 0.2
        /** Minimum readings above baseline to confirm sustained rise. */
        const val TEMP_SUSTAINED_RISE_MIN = 3
        const val TEMP_RISE_SENSITIVITY = 0.1

        // ---- Confidence scoring ----
        // Kept in lockstep with new-project/src/utils/bayesian.ts and
        // PetalAPI/src/services/predictionService.ts. Change all three together.
        /** Number of cycles at which the data factor saturates to 1.0. */
        const val CONFIDENCE_OBS_SATURATION = 6.0
        /** Equal weighting of data quantity and posterior precision. */
        const val CONFIDENCE_OBS_WEIGHT = 0.5
        const val CONFIDENCE_PRECISION_WEIGHT = 0.5
        /** Floor / ceiling on reported confidence. */
        const val CONFIDENCE_FLOOR = 0.1
        const val CONFIDENCE_CEILING = 0.99
    }

    private val priorMean: Double = PRIOR_MEAN
    private val priorVariance: Double = PRIOR_VARIANCE

    data class PredictionResult(
        val predictedLength: Double,
        val confidenceInterval: Pair<Double, Double>,
        val confidence: Double, // 0.0 to 1.0
        val nextPeriodDate: LocalDate,
        val nextOvulationDate: LocalDate,
        val fertileWindowStart: LocalDate,
        val fertileWindowEnd: LocalDate,
        val posteriorMean: Double,
        val posteriorStd: Double
    )

    /**
     * Computes the posterior distribution by updating the prior with observed cycle data.
     *
     * For a normal-normal conjugate model:
     *   posterior_precision = prior_precision + n * data_precision
     *   posterior_mean = (prior_precision * prior_mean + n * data_precision * data_mean) / posterior_precision
     *
     * Where precision = 1/variance.
     */
    fun predict(
        cycles: List<CycleLog>,
        symptoms: Symptoms? = null,
        lastPeriodStart: LocalDate? = null
    ): PredictionResult {
        // Cycles arrive newest-first from the repository; apply exponential
        // decay weights so the most recent cycles dominate.
        val weights = decayWeights(cycles.size)
        val (posteriorMean, posteriorVariance) = computePosteriorWeighted(cycles, weights)

        // Apply symptom-aware adjustment
        val adjustedMean = if (symptoms != null) {
            applySymptomAdjustment(posteriorMean, symptoms, cycles)
        } else {
            posteriorMean
        }

        val posteriorStd = sqrt(posteriorVariance)

        // 95% confidence interval
        val z95 = 1.96
        val lowerBound = max(MIN_CYCLE_LENGTH, adjustedMean - z95 * posteriorStd)
        val upperBound = min(MAX_CYCLE_LENGTH, adjustedMean + z95 * posteriorStd)

        // Compute confidence score (0-1) — same formula as web + API.
        val confidence = computeConfidence(cycles.size, posteriorVariance)

        // Prediction dates
        val anchor = lastPeriodStart ?: if (cycles.isNotEmpty()) {
            LocalDate.parse(cycles[0].start)
        } else {
            LocalDate.now()
        }

        val predictedLengthRounded = adjustedMean.roundToInt()
        val nextPeriod = anchor.plusDays(predictedLengthRounded.toLong())
        val ovulationDay = predictedLengthRounded - LUTEAL_PHASE_DAYS.toInt()
        val nextOvulation = anchor.plusDays(ovulationDay.toLong())

        // If predicted dates are in the past, roll forward
        val today = LocalDate.now()
        val finalNextPeriod = if (nextPeriod.isBefore(today)) {
            var rolled = nextPeriod
            while (rolled.isBefore(today)) {
                rolled = rolled.plusDays(predictedLengthRounded.toLong())
            }
            rolled
        } else nextPeriod

        val finalNextOvulation = finalNextPeriod.minusDays(LUTEAL_PHASE_DAYS)

        return PredictionResult(
            predictedLength = adjustedMean,
            confidenceInterval = Pair(lowerBound, upperBound),
            confidence = confidence,
            nextPeriodDate = finalNextPeriod,
            nextOvulationDate = finalNextOvulation,
            fertileWindowStart = finalNextOvulation.minusDays(FERTILE_WINDOW_RADIUS),
            fertileWindowEnd = finalNextOvulation.plusDays(FERTILE_WINDOW_RADIUS),
            posteriorMean = adjustedMean,
            posteriorStd = posteriorStd
        )
    }

    /**
     * Normal-normal conjugate posterior update with uniform weights.
     * Returns (posteriorMean, posteriorVariance).
     */
    fun computePosterior(cycles: List<CycleLog>): Pair<Double, Double> =
        computePosteriorWeighted(cycles, List(cycles.size) { 1.0 })

    /**
     * Weighted normal-normal conjugate posterior. observations[i] aligns with
     * weights[i]. Total weight replaces n in the precision update.
     * Returns (posteriorMean, posteriorVariance).
     */
    fun computePosteriorWeighted(
        cycles: List<CycleLog>,
        weights: List<Double>
    ): Pair<Double, Double> {
        if (cycles.isEmpty()) return Pair(priorMean, priorVariance)
        require(cycles.size == weights.size) {
            "computePosteriorWeighted: cycles and weights must align"
        }
        val totalWeight = weights.sum()
        if (totalWeight <= 0.0) return Pair(priorMean, priorVariance)

        val lengths = cycles.map { it.cycleLength.toDouble() }
        val weightedMean = lengths.zip(weights).sumOf { (x, w) -> x * w } / totalWeight

        // Weighted sample variance with Bessel-style correction.
        val dataVariance = if (cycles.size >= 2) {
            val sumSqDiff = lengths.zip(weights).sumOf { (x, w) -> w * (x - weightedMean).pow(2) }
            max(1.0, sumSqDiff / max(1.0, totalWeight - 1.0))
        } else {
            priorVariance
        }

        val priorPrecision = 1.0 / priorVariance
        val dataPrecision = 1.0 / dataVariance
        val posteriorPrecision = priorPrecision + totalWeight * dataPrecision
        val posteriorMean = (priorPrecision * priorMean + totalWeight * dataPrecision * weightedMean) / posteriorPrecision
        return Pair(posteriorMean, 1.0 / posteriorPrecision)
    }

    /**
     * Builds exponential-decay weights for a cycle list assumed to be ordered
     * newest-first (index 0 = most recent). Weight at index i is λ^i.
     * Mirrors web/API decayWeights.
     */
    fun decayWeights(n: Int, lambda: Double = DECAY_LAMBDA): List<Double> =
        (0 until n).map { lambda.pow(it) }

    /**
     * Adjusts the predicted cycle length based on symptom signals.
     *
     * Research shows:
     * - Severe cramps/pain can be associated with slightly shorter cycles
     * - Stress/mood disruption can be associated with slightly longer cycles
     * - Heavy flow in recent cycles may indicate hormonal shifts
     */
    private fun applySymptomAdjustment(
        basePrediction: Double,
        symptoms: Symptoms,
        cycles: List<CycleLog>
    ): Double {
        var adjustment = 0.0

        // Severe cramps tend to correlate with higher prostaglandin levels,
        // sometimes associated with slightly shorter cycles
        if (symptoms.cramps == SymptomLevel.Severe) {
            adjustment += CRAMPS_ADJUSTMENT
        }

        // Mood disruption (irritability, mood swings) can indicate
        // hormonal fluctuations that sometimes extend the luteal phase
        if (symptoms.mood == com.petal.app.data.model.MoodLevel.MoodSwings ||
            symptoms.mood == com.petal.app.data.model.MoodLevel.Irritable) {
            adjustment += MOOD_ADJUSTMENT
        }

        // Severe headaches near menstruation can indicate estrogen withdrawal,
        // which in some cases correlates with slightly shorter follicular phases
        if (symptoms.headaches == SymptomLevel.Severe) {
            adjustment += HEADACHE_ADJUSTMENT
        }

        // Detect trend in recent cycle lengths (are cycles getting longer or shorter?)
        if (cycles.size >= 3) {
            val recentTrend = detectTrend(cycles.take(6).map { it.cycleLength.toDouble() })
            // Apply a mild trend-following adjustment (damped to avoid overcorrection)
            adjustment += recentTrend * TREND_DAMPING_FACTOR
        }

        // Clamp total adjustment to avoid wild swings
        val clampedAdjustment = adjustment.coerceIn(-MAX_SYMPTOM_ADJUSTMENT, MAX_SYMPTOM_ADJUSTMENT)

        return basePrediction + clampedAdjustment
    }

    /**
     * Detects a linear trend in a series of values.
     * Returns the slope of a simple linear regression.
     * Positive = values increasing, negative = decreasing.
     */
    private fun detectTrend(values: List<Double>): Double {
        if (values.size < 2) return 0.0

        val n = values.size
        val xMean = (n - 1) / 2.0
        val yMean = values.average()

        var numerator = 0.0
        var denominator = 0.0

        for (i in values.indices) {
            numerator += (i - xMean) * (values[i] - yMean)
            denominator += (i - xMean).pow(2)
        }

        return if (denominator > 0) numerator / denominator else 0.0
    }

    /**
     * Detects temperature trends from a series of basal body temperature readings.
     * A sustained rise of ~0.2-0.5 degrees C indicates ovulation has occurred.
     *
     * Returns:
     * - "pre_ovulation" if no sustained rise detected
     * - "post_ovulation" if a thermal shift is detected
     * - "insufficient_data" if not enough readings
     */
    fun detectTemperatureTrend(temperatures: List<Double>): String {
        if (temperatures.size < 6) return "insufficient_data"

        // Split into baseline and recent portions
        val splitPoint = (temperatures.size * TEMP_BASELINE_FRACTION).toInt()
        val baseline = temperatures.take(splitPoint)
        val recent = temperatures.drop(splitPoint)

        if (baseline.isEmpty() || recent.isEmpty()) return "insufficient_data"

        val baselineMean = baseline.average()
        val recentMean = recent.average()

        val shift = recentMean - baselineMean
        val sustainedRise = recent.count { it > baselineMean + TEMP_RISE_SENSITIVITY }

        return if (shift >= TEMP_SHIFT_THRESHOLD && sustainedRise >= TEMP_SUSTAINED_RISE_MIN) {
            "post_ovulation"
        } else {
            "pre_ovulation"
        }
    }

    /**
     * Computes a confidence score from CONFIDENCE_FLOOR to CONFIDENCE_CEILING.
     * Mirrors the formula in new-project/src/utils/bayesian.ts and
     * PetalAPI/src/services/predictionService.ts so all three clients agree.
     */
    fun computeConfidence(numObservations: Int, posteriorVariance: Double): Double {
        val dataFactor = min(numObservations / CONFIDENCE_OBS_SATURATION, 1.0)
        val varianceFactor = max(0.0, 1.0 - posteriorVariance / priorVariance)
        val raw = CONFIDENCE_OBS_WEIGHT * dataFactor + CONFIDENCE_PRECISION_WEIGHT * varianceFactor
        val clamped = raw.coerceIn(CONFIDENCE_FLOOR, CONFIDENCE_CEILING)
        return (clamped * 100).roundToInt() / 100.0
    }

    /**
     * Returns a human-readable confidence label.
     */
    fun getConfidenceLabel(confidence: Double): String = when {
        confidence >= 0.7 -> "High"
        confidence >= 0.4 -> "Moderate"
        else -> "Low"
    }
}
