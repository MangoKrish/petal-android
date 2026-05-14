package com.petal.app.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.CycleMode
import com.petal.app.data.model.NotificationPreferences
import com.petal.app.data.model.ReminderFrequency
import com.petal.app.data.model.SharedLink
import com.petal.app.data.model.User
import com.petal.app.data.model.UserRole
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.CycleRepository
import com.petal.app.data.repository.FertilityLogRepository
import com.petal.app.data.repository.PartnerRepository
import com.petal.app.domain.BayesianPredictor
import com.petal.app.domain.FertilityFusion
import com.petal.app.domain.NotificationScheduler
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    val cycleMode: CycleMode = CycleMode.Tracking,
    val cyclesUsed: Int = 0,
    val averageCycleLength: Int = 0,
    val minCycle: Int? = null,
    val maxCycle: Int? = null,
    val nextPeriodSummary: String = "Log your first cycle to start predictions.",
    val fertilityWindowSummary: String? = null,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val partnerRepository: PartnerRepository,
    private val notificationScheduler: NotificationScheduler,
    private val dataStore: DataStore<Preferences>,
    private val cycleRepository: CycleRepository,
    private val bayesianPredictor: BayesianPredictor,
    private val fertilityFusion: FertilityFusion,
    private val fertilityLogRepository: FertilityLogRepository
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
            val cycleMode = authRepository.getCycleMode()
            val transparency = computeTransparency()

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
                        themeMode = themeMode,
                        cycleMode = cycleMode,
                        cyclesUsed = transparency.cyclesUsed,
                        averageCycleLength = transparency.averageCycleLength,
                        minCycle = transparency.minCycle,
                        maxCycle = transparency.maxCycle,
                        nextPeriodSummary = transparency.nextPeriodSummary,
                        fertilityWindowSummary = transparency.fertilityWindowSummary
                    )
                }
            }
        }
    }

    private data class Transparency(
        val cyclesUsed: Int,
        val averageCycleLength: Int,
        val minCycle: Int?,
        val maxCycle: Int?,
        val nextPeriodSummary: String,
        val fertilityWindowSummary: String?
    )

    private suspend fun computeTransparency(): Transparency {
        val userId = authRepository.getCurrentUserId() ?: return Transparency(
            cyclesUsed = 0,
            averageCycleLength = 0,
            minCycle = null,
            maxCycle = null,
            nextPeriodSummary = "Log your first cycle to start predictions.",
            fertilityWindowSummary = null
        )
        val entries = cycleRepository.getEntries(userId)
        val cycles = entries.map { it.toCycleLog() }
        if (cycles.isEmpty()) {
            return Transparency(
                cyclesUsed = 0,
                averageCycleLength = 0,
                minCycle = null,
                maxCycle = null,
                nextPeriodSummary = "Log your first cycle to start predictions.",
                fertilityWindowSummary = null
            )
        }
        val bayes = bayesianPredictor.predict(
            cycles = cycles,
            symptoms = entries.firstOrNull()?.symptoms,
            lastPeriodStart = LocalDate.parse(cycles[0].start)
        )
        val fmt = DateTimeFormatter.ofPattern("MMMM d")
        val nextPeriodSummary = "There's a 95% chance your next period starts between " +
            "${bayes.confidenceInterval.first.toInt()} and " +
            "${bayes.confidenceInterval.second.toInt()} days from your last cycle, " +
            "with ${bayes.nextPeriodDate.format(fmt)} most likely."

        // Fertility fusion summary
        val fertilityRows = fertilityLogRepository.getRecent(userId, days = 45)
        val fusion = fertilityFusion.predictFertileWindow(
            FertilityFusion.FusionInput(
                today = LocalDate.now(),
                predictedNextPeriod = bayes.nextPeriodDate,
                cycleLengthMean = bayes.predictedLength.toInt(),
                cycleLengthVariance = bayes.posteriorStd * bayes.posteriorStd,
                baseConfidence = bayes.confidence,
                logs = fertilityLogRepository.toFusionLogs(fertilityRows),
                observedCycleCount = cycles.size
            )
        )
        val fertilityWindowSummary = if (fusion.insufficientData) null else buildString {
            append("Petal's current fertile window estimate is ")
            append(fusion.fertileWindow.start.format(fmt))
            append(" – ")
            append(fusion.fertileWindow.end.format(fmt))
            append(", with peak on ")
            append(fusion.peakDay.format(fmt))
            append(". This is based on ")
            append(when (fusion.ovulationEstimate.source) {
                FertilityFusion.OvulationSource.LH -> "your most recent LH test."
                FertilityFusion.OvulationSource.BBT -> "your basal body temperature shift."
                FertilityFusion.OvulationSource.PAIN -> "your cervical mucus and ovulation pain."
                FertilityFusion.OvulationSource.MUCUS -> "your cervical mucus pattern."
                FertilityFusion.OvulationSource.CALENDAR -> "your cycle length history."
            })
            if (fusion.irregular) {
                append(" Your cycles vary more than average, so the window is intentionally wider.")
            }
        }

        val cycleLengths = cycles.map { it.cycleLength }
        return Transparency(
            cyclesUsed = cycles.size,
            averageCycleLength = bayes.predictedLength.toInt(),
            minCycle = cycleLengths.minOrNull(),
            maxCycle = cycleLengths.maxOrNull(),
            nextPeriodSummary = nextPeriodSummary,
            fertilityWindowSummary = fertilityWindowSummary
        )
    }

    fun updateCycleMode(mode: CycleMode) {
        viewModelScope.launch {
            authRepository.setCycleMode(mode)
            _uiState.update { it.copy(cycleMode = mode) }
        }
    }

    /** PHASE_6_7_PLAN.md §6A.1 — role / handle / display-name updates. */
    fun switchRole(target: UserRole, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.setRole(target)
            // Refresh local user record so navigation re-evaluates.
            loadSettings()
            onComplete()
        }
    }

    fun updateUsername(username: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.setUsername(username)
                loadSettings()
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            authRepository.setDisplayName(displayName)
            loadSettings()
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
