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
 * PHASE_6_7_PLAN.md §6A.3 — local-first bookmark store for the education
 * card deck. Mirrors web's localStorage approach. When the API bookmark
 * endpoints are wired into Android, this repo can call out and keep
 * DataStore as a sync mirror.
 */
@Singleton
class EducationBookmarksRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val bookmarks: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[KEY] ?: emptySet()
    }

    suspend fun toggle(cardId: String, next: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[KEY] ?: emptySet()
            prefs[KEY] = if (next) current + cardId else current - cardId
        }
    }

    companion object {
        private val KEY = stringSetPreferencesKey("education_bookmarks_v1")
    }
}
