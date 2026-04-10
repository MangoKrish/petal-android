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
        /** Maximum total symptom adjustment (days). */
        const val MAX_SYMPTOM_ADJUSTMENT = 1.5

        // ---- Temperature trend detection ----
        /** Fraction of readings used as baseline. */
        const val TEMP_BASELINE_FRACTION = 0.6
        /** Minimum temperature shift (degrees C) to indicate post-ovulation. */
        const val TEMP_SHIFT_THRESHOLD = 0.2
        /** Minimum readings above baseline to confirm sustained rise. */
        const val TEMP_SUSTAINED_RISE_MIN = 3
        const val TEMP_RISE_SENSITIVITY = 0.1

        // ---- Confidence scoring ----
        /** Observation factor saturation constant (saturates around 8-10 cycles). */
        const val CONFIDENCE_OBS_SCALE = 4.0
        /** Maximum posterior std before confidence drops to zero. */
        const val CONFIDENCE_STD_CEILING = 5.0
        /** Weight given to observation count vs precision in combined score. */
        const val CONFIDENCE_OBS_WEIGHT = 0.4
        const val CONFIDENCE_PRECISION_WEIGHT = 0.6
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
        val (posteriorMean, posteriorVariance) = computePosterior(cycles)

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

        // Compute confidence score (0-1) based on posterior std and number of observations
        val confidence = computeConfidence(cycles.size, posteriorStd)

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
     * Normal-normal conjugate posterior update.
     * Returns (posteriorMean, posteriorVariance).
     */
    private fun computePosterior(cycles: List<CycleLog>): Pair<Double, Double> {
        if (cycles.isEmpty()) {
            return Pair(priorMean, priorVariance)
        }

        val n = cycles.size
        val lengths = cycles.map { it.cycleLength.toDouble() }
        val dataMean = lengths.average()

        // Estimate data variance (sample variance with Bessel's correction, min 1.0)
        val dataVariance = if (n >= 2) {
            val sumSqDiff = lengths.sumOf { (it - dataMean).pow(2) }
            max(1.0, sumSqDiff / (n - 1))
        } else {
            priorVariance // Use prior variance for single observation
        }

        // Precision = 1 / variance
        val priorPrecision = 1.0 / priorVariance
        val dataPrecision = 1.0 / dataVariance

        // Posterior precision = prior precision + n * data precision
        val posteriorPrecision = priorPrecision + n * dataPrecision

        // Posterior mean = weighted combination
        val posteriorMean = (priorPrecision * priorMean + n * dataPrecision * dataMean) / posteriorPrecision

        // Posterior variance = 1 / posterior precision
        val posteriorVariance = 1.0 / posteriorPrecision

        return Pair(posteriorMean, posteriorVariance)
    }

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
     * Computes a confidence score from 0.0 to 1.0.
     * More observations and lower posterior std = higher confidence.
     */
    private fun computeConfidence(numObservations: Int, posteriorStd: Double): Double {
        val observationFactor = 1.0 - exp(-numObservations / CONFIDENCE_OBS_SCALE)

        val precisionFactor = max(0.0, 1.0 - posteriorStd / CONFIDENCE_STD_CEILING)

        val combined = CONFIDENCE_OBS_WEIGHT * observationFactor + CONFIDENCE_PRECISION_WEIGHT * precisionFactor

        return combined.coerceIn(0.0, 1.0)
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
