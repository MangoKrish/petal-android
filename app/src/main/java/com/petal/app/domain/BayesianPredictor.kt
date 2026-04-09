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

    // ---- Prior distribution (population-level) ----
    // Based on published research: normal menstrual cycles average 28 days with std ~4
    private val priorMean: Double = 28.0
    private val priorVariance: Double = 16.0 // std=4, variance=16

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
        val lowerBound = max(18.0, adjustedMean - z95 * posteriorStd)
        val upperBound = min(45.0, adjustedMean + z95 * posteriorStd)

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
        val ovulationDay = predictedLengthRounded - 14
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

        val finalNextOvulation = finalNextPeriod.minusDays(14)

        return PredictionResult(
            predictedLength = adjustedMean,
            confidenceInterval = Pair(lowerBound, upperBound),
            confidence = confidence,
            nextPeriodDate = finalNextPeriod,
            nextOvulationDate = finalNextOvulation,
            fertileWindowStart = finalNextOvulation.minusDays(3),
            fertileWindowEnd = finalNextOvulation.plusDays(3),
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
            adjustment -= 0.3
        }

        // Mood disruption (irritability, mood swings) can indicate
        // hormonal fluctuations that sometimes extend the luteal phase
        if (symptoms.mood == com.petal.app.data.model.MoodLevel.MoodSwings ||
            symptoms.mood == com.petal.app.data.model.MoodLevel.Irritable) {
            adjustment += 0.2
        }

        // Severe headaches near menstruation can indicate estrogen withdrawal,
        // which in some cases correlates with slightly shorter follicular phases
        if (symptoms.headaches == SymptomLevel.Severe) {
            adjustment -= 0.15
        }

        // Detect trend in recent cycle lengths (are cycles getting longer or shorter?)
        if (cycles.size >= 3) {
            val recentTrend = detectTrend(cycles.take(6).map { it.cycleLength.toDouble() })
            // Apply a mild trend-following adjustment (damped to avoid overcorrection)
            adjustment += recentTrend * 0.3
        }

        // Clamp total adjustment to avoid wild swings
        val clampedAdjustment = adjustment.coerceIn(-1.5, 1.5)

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

        // Split into baseline (first ~60%) and recent (last ~40%)
        val splitPoint = (temperatures.size * 0.6).toInt()
        val baseline = temperatures.take(splitPoint)
        val recent = temperatures.drop(splitPoint)

        if (baseline.isEmpty() || recent.isEmpty()) return "insufficient_data"

        val baselineMean = baseline.average()
        val recentMean = recent.average()

        // A thermal shift of 0.2+ degrees sustained over 3+ days suggests post-ovulation
        val shift = recentMean - baselineMean
        val sustainedRise = recent.count { it > baselineMean + 0.1 }

        return if (shift >= 0.2 && sustainedRise >= 3) {
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
        // Observation factor: saturates around 8-10 cycles
        val observationFactor = 1.0 - exp(-numObservations / 4.0)

        // Precision factor: lower std = higher confidence
        // A std of 1 day = very confident, std of 4+ days = low confidence
        val precisionFactor = max(0.0, 1.0 - posteriorStd / 5.0)

        // Combined score, weighted toward precision
        val combined = 0.4 * observationFactor + 0.6 * precisionFactor

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
