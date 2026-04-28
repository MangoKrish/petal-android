package com.petal.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.petal.app.ui.components.kawaii.PetalStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single-source-of-truth for kawaii preferences (petal-style, mascot, sound toggle, etc.).
 * Backed by the existing `petal_preferences` DataStore in [com.petal.app.di.AppModule].
 */
@Singleton
class PetalPreferences @Inject constructor(
    private val store: DataStore<Preferences>,
) {
    private object Keys {
        val PETAL_STYLE = stringPreferencesKey("kawaii_petal_style")
        val PETAL_BG_ENABLED = stringPreferencesKey("kawaii_petal_bg_enabled")
        val MASCOT_VARIANT = stringPreferencesKey("kawaii_mascot_variant")
        val SOFT_TALKS_SOUND = stringPreferencesKey("kawaii_soft_talks_sound")
        val FCM_TOKEN = stringPreferencesKey("kawaii_fcm_token")
        val LOG_STREAK = intPreferencesKey("kawaii_log_streak_days")
    }

    val petalStyle: Flow<PetalStyle> = store.data.map { p ->
        runCatching { PetalStyle.valueOf(p[Keys.PETAL_STYLE] ?: "SAKURA") }
            .getOrDefault(PetalStyle.SAKURA)
    }

    val petalBackgroundEnabled: Flow<Boolean> = store.data.map { p ->
        (p[Keys.PETAL_BG_ENABLED] ?: "true").toBoolean()
    }

    val softTalksSound: Flow<Boolean> = store.data.map { p ->
        (p[Keys.SOFT_TALKS_SOUND] ?: "false").toBoolean()
    }

    val mascotVariant: Flow<String> = store.data.map { p ->
        p[Keys.MASCOT_VARIANT] ?: "bunny"
    }

    val logStreak: Flow<Int> = store.data.map { p ->
        p[Keys.LOG_STREAK] ?: 0
    }

    suspend fun setPetalStyle(style: PetalStyle) {
        store.edit { it[Keys.PETAL_STYLE] = style.name }
    }

    suspend fun setPetalBackgroundEnabled(enabled: Boolean) {
        store.edit { it[Keys.PETAL_BG_ENABLED] = enabled.toString() }
    }

    suspend fun setSoftTalksSound(on: Boolean) {
        store.edit { it[Keys.SOFT_TALKS_SOUND] = on.toString() }
    }

    suspend fun setMascotVariant(variant: String) {
        store.edit { it[Keys.MASCOT_VARIANT] = variant }
    }

    suspend fun setFcmToken(token: String) {
        store.edit { it[Keys.FCM_TOKEN] = token }
    }

    suspend fun bumpLogStreak() {
        store.edit { p -> p[Keys.LOG_STREAK] = (p[Keys.LOG_STREAK] ?: 0) + 1 }
    }
}
