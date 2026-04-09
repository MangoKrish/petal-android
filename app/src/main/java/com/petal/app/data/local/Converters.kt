package com.petal.app.data.local

import androidx.room.TypeConverter
import com.petal.app.data.model.FlowIntensity
import com.petal.app.data.model.MoodLevel
import com.petal.app.data.model.SymptomLevel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromFlowIntensity(value: FlowIntensity): String = value.name

    @TypeConverter
    fun toFlowIntensity(value: String): FlowIntensity =
        FlowIntensity.entries.firstOrNull { it.name == value } ?: FlowIntensity.Medium

    @TypeConverter
    fun fromSymptomLevel(value: SymptomLevel): String = value.name

    @TypeConverter
    fun toSymptomLevel(value: String): SymptomLevel =
        SymptomLevel.entries.firstOrNull { it.name == value } ?: SymptomLevel.None

    @TypeConverter
    fun fromMoodLevel(value: MoodLevel): String = value.name

    @TypeConverter
    fun toMoodLevel(value: String): MoodLevel =
        MoodLevel.entries.firstOrNull { it.name == value } ?: MoodLevel.Calm

    @TypeConverter
    fun fromStringList(value: List<String>): String = json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> = try {
        json.decodeFromString<List<String>>(value)
    } catch (_: Exception) {
        emptyList()
    }
}
