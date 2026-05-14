package com.petal.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PHASE_6_7_PLAN.md §7.1 — local-first story bookmark store.
 * Mirrors EducationBookmarksRepository from 6A.3.
 */
@Singleton
class StoryBookmarksRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val bookmarks: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[KEY] ?: emptySet()
    }

    suspend fun toggle(storyId: String, next: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[KEY] ?: emptySet()
            prefs[KEY] = if (next) current + storyId else current - storyId
        }
    }

    companion object {
        private val KEY = stringSetPreferencesKey("story_bookmarks_v1")
    }
}
