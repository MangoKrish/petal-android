package com.petal.app.data.model

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * Per-day fertility-relevant signals (mirrors PetalAPI/daily_logs columns added
 * in migration 004). Stored separately from CycleEntry because CycleEntry is
 * cycle-scoped, not day-scoped — these signals change daily and feed the
 * symptom-fusion fertility estimator (FertilityFusion.kt).
 *
 * Composite PK on (userId, logDate) enforces one row per user per day.
 */
@Entity(
    tableName = "daily_fertility_logs",
    primaryKeys = ["userId", "logDate"],
    indices = [Index(value = ["userId", "logDate"], name = "idx_dfl_user_date")]
)
data class DailyFertilityLog(
    val userId: String,
    val logDate: String, // YYYY-MM-DD
    val temperature: Double? = null,
    val cervicalMucus: String? = null,
    val ovulationPain: Boolean = false,
    /** "positive" | "negative" | "inconclusive" | null */
    val lhTestResult: String? = null,
    val sexualActivity: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = false
)

@Serializable
enum class CycleMode(val display: String) {
    Tracking("tracking"),
    TryingToConceive("ttc"),
    AvoidingPregnancy("avoiding");

    companion object {
        fun fromString(value: String?): CycleMode =
            entries.firstOrNull { it.display == value } ?: Tracking
    }
}
