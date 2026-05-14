package com.petal.app.data.model

import kotlinx.serialization.Serializable

/**
 * PHASE_6_7_PLAN.md §7.1 — Stories tab.
 * Mirrors new-project/src/lib/stories.ts. Source of truth will move to the
 * /stories API endpoint once the seed migration runs; until then both
 * clients read from their static libraries (same pattern as 6A.3).
 */
@Serializable
data class Story(
    val id: String,
    val stream: StoryStream,
    val subjectName: String,
    val subjectRole: String? = null,
    val bodyShort: String,
    val bodyLong: String? = null,
    val pullQuote: String,
    val tags: List<String> = emptyList(),
    val source: String? = null,
    val consentStatus: StoryConsentStatus = StoryConsentStatus.PublicDomain,
)

@Serializable
enum class StoryStream(val display: String) {
    DidItAnyway("did_it_anyway"),
    OkayToRest("okay_to_rest");

    companion object {
        fun fromString(value: String?): StoryStream? =
            entries.firstOrNull { it.display == value }
    }
}

@Serializable
enum class StoryConsentStatus(val display: String) {
    PublicDomain("public_domain"),
    Licensed("licensed"),
    ConsentedUser("consented_user"),
    PermissionPending("permission_pending");

    companion object {
        fun fromString(value: String?): StoryConsentStatus =
            entries.firstOrNull { it.display == value } ?: PublicDomain
    }
}
