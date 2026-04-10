package com.petal.app.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.NotificationPreferences
import com.petal.app.data.model.ReminderFrequency
import com.petal.app.data.model.SharedLink
import com.petal.app.data.model.User
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.PartnerRepository
import com.petal.app.domain.NotificationScheduler
import com.petal.app.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val notificationPrefs: NotificationPreferences = NotificationPreferences(),
    val shareLinks: List<SharedLink> = emptyList(),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository,
    private val notificationScheduler: NotificationScheduler,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    val themeMode: Flow<ThemeMode> = dataStore.data.map { prefs ->
        ThemeMode.fromStorage(prefs[PREF_THEME_MODE])
    }.distinctUntilChanged()

    init {
        loadSettings()
    }

    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val user = authRepository.getCurrentUser()
            val links = partnerRepository.getShareLinks().getOrNull() ?: emptyList()

            dataStore.data.first().let { prefs ->
                val notifPrefs = NotificationPreferences(
                    upcomingCycleEnabled = prefs[PREF_UPCOMING_CYCLE] ?: true,
                    upcomingCycleLeadDays = prefs[PREF_LEAD_DAYS] ?: 2,
                    dailySymptomEnabled = prefs[PREF_DAILY_SYMPTOM] ?: false,
                    dailySymptomTime = prefs[PREF_SYMPTOM_TIME] ?: "09:00",
                    inAppEnabled = prefs[PREF_IN_APP] ?: true,
                    quietMode = prefs[PREF_QUIET_MODE] ?: false
                )
                val themeMode = ThemeMode.fromStorage(prefs[PREF_THEME_MODE])
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        notificationPrefs = notifPrefs,
                        shareLinks = links,
                        themeMode = themeMode
                    )
                }
            }
        }
    }

    fun updateUpcomingCycleNotification(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PREF_UPCOMING_CYCLE] = enabled }
            _uiState.update { it.copy(notificationPrefs = it.notificationPrefs.copy(upcomingCycleEnabled = enabled)) }
        }
    }

    fun updateDailySymptomNotification(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PREF_DAILY_SYMPTOM] = enabled }
            _uiState.update { it.copy(notificationPrefs = it.notificationPrefs.copy(dailySymptomEnabled = enabled)) }
            if (enabled) {
                notificationScheduler.scheduleDailyReminder()
            } else {
                notificationScheduler.cancelDailyReminder()
            }
        }
    }

    fun updateQuietMode(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[PREF_QUIET_MODE] = enabled }
            _uiState.update { it.copy(notificationPrefs = it.notificationPrefs.copy(quietMode = enabled)) }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            dataStore.edit { it[PREF_THEME_MODE] = themeMode.name }
            _uiState.update { it.copy(themeMode = themeMode) }
        }
    }

    fun createShareLink(
        label: String,
        showCycleLength: Boolean,
        showNextPeriod: Boolean,
        showSymptoms: Boolean,
        showPhase: Boolean
    ) {
        viewModelScope.launch {
            val result = partnerRepository.createShareLink(
                label, showCycleLength, showNextPeriod, showSymptoms, showPhase
            )
            result.onSuccess { loadSettings() }
            result.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun revokeShareLink(linkId: String) {
        viewModelScope.launch {
            partnerRepository.revokeShareLink(linkId)
            loadSettings()
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            notificationScheduler.cancelAll()
            authRepository.logout()
            onComplete()
        }
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            notificationScheduler.cancelAll()
            authRepository.deleteAccount()
            onComplete()
        }
    }

    companion object {
        val PREF_UPCOMING_CYCLE = booleanPreferencesKey("upcoming_cycle_enabled")
        val PREF_LEAD_DAYS = intPreferencesKey("lead_days")
        val PREF_DAILY_SYMPTOM = booleanPreferencesKey("daily_symptom_enabled")
        val PREF_SYMPTOM_TIME = stringPreferencesKey("symptom_time")
        val PREF_IN_APP = booleanPreferencesKey("in_app_enabled")
        val PREF_QUIET_MODE = booleanPreferencesKey("quiet_mode")
        val PREF_THEME_MODE = stringPreferencesKey("theme_mode")
    }
}
