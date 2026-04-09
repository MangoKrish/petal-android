package com.petal.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyInsight(
    val headline: String,
    val body: String,
    val tip: String,
    val emoji: String,
    val category: InsightCategory
)

@Serializable
enum class InsightCategory(val display: String) {
    Nutrition("nutrition"),
    Exercise("exercise"),
    SelfCare("selfcare"),
    Health("health");
}

@Serializable
data class DayInsights(
    val greeting: String,
    val phase: String,
    val hormoneNote: String,
    val cards: List<DailyInsight>,
    val partnerNote: String
)
