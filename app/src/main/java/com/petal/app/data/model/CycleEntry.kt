package com.petal.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class FlowIntensity(val display: String) {
    Light("Light"),
    Medium("Medium"),
    Heavy("Heavy");

    companion object {
        fun fromString(value: String): FlowIntensity =
            entries.firstOrNull { it.display == value } ?: Medium
    }
}

@Serializable
enum class SymptomLevel(val display: String) {
    None("None"),
    Mild("Mild"),
    Moderate("Moderate"),
    Severe("Severe");

    companion object {
        fun fromString(value: String): SymptomLevel =
            entries.firstOrNull { it.display == value } ?: None
    }
}

@Serializable
enum class MoodLevel(val display: String) {
    Calm("Calm"),
    Sensitive("Sensitive"),
    Low("Low"),
    Irritable("Irritable"),
    MoodSwings("Mood swings");

    companion object {
        fun fromString(value: String): MoodLevel =
            entries.firstOrNull { it.display == value } ?: Calm
    }
}

@Serializable
enum class CyclePhase(val display: String) {
    Menstrual("Menstrual"),
    Follicular("Follicular"),
    Ovulation("Ovulation"),
    Luteal("Luteal");
}

@Serializable
enum class PredictionConfidence(val display: String) {
    Low("Low"),
    Moderate("Moderate"),
    High("High");
}

@Serializable
data class Symptoms(
    val pain: SymptomLevel = SymptomLevel.None,
    val cramps: SymptomLevel = SymptomLevel.None,
    val cravings: SymptomLevel = SymptomLevel.None,
    val mood: MoodLevel = MoodLevel.Calm,
    val headaches: SymptomLevel = SymptomLevel.None
)

@Serializable
data class CycleLog(
    val start: String,
    val end: String,
    val cycleLength: Int
)

@Entity(tableName = "cycle_entries")
data class CycleEntry(
    @PrimaryKey val id: String,
    val userId: String,
    val start: String,
    val end: String,
    val cycleLength: Int,
    val flowIntensity: FlowIntensity,
    val painLevel: SymptomLevel = SymptomLevel.None,
    val crampsLevel: SymptomLevel = SymptomLevel.None,
    val cravingsLevel: SymptomLevel = SymptomLevel.None,
    val moodLevel: MoodLevel = MoodLevel.Calm,
    val headachesLevel: SymptomLevel = SymptomLevel.None,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = false
) {
    val symptoms: Symptoms
        get() = Symptoms(
            pain = painLevel,
            cramps = crampsLevel,
            cravings = cravingsLevel,
            mood = moodLevel,
            headaches = headachesLevel
        )

    fun toCycleLog(): CycleLog = CycleLog(
        start = start,
        end = end,
        cycleLength = cycleLength
    )
}
