package com.petal.app.data.model

import kotlinx.serialization.Serializable

/**
 * Card-shaped education content for the swipeable deck UI.
 * Mirrors new-project/src/lib/education-cards.ts. PHASE_6_7_PLAN.md §6A.3.
 *
 * The full library lives in EducationCards.kt as static Kotlin data until
 * the API content migration lands; both clients then read from the same
 * /education/cards endpoint.
 */
@Serializable
data class EducationCard(
    val id: String,
    val category: EducationCategory,
    val title: String,
    val bodyShort: String,
    val bodyLong: String? = null,
    val source: String? = null,
    val tags: List<String> = emptyList(),
    val illustrationKey: String? = null,
    val audience: EducationAudience = EducationAudience.All,
    val readingTimeSeconds: Int = 20,
)

@Serializable
enum class EducationCategory(val display: String) {
    Period("period"),
    Pcos("pcos"),
    Pcod("pcod"),
    Fertility("fertility"),
    Myth("myth"),
    Body("body"),
    Partner("partner");

    companion object {
        fun fromString(value: String?): EducationCategory? =
            entries.firstOrNull { it.display == value }
    }
}

@Serializable
enum class EducationAudience(val display: String) {
    All("all"),
    Primary("primary"),
    Supporter("supporter");

    companion object {
        fun fromString(value: String?): EducationAudience =
            entries.firstOrNull { it.display == value } ?: All
    }
}
