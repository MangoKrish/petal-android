package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.*
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.CycleRepository
import com.petal.app.data.repository.PartnerRepository
import com.petal.app.domain.CycleCalculator
import com.petal.app.domain.DailyInsightsEngine
import com.petal.app.domain.RecommendationsEngine
import com.petal.app.domain.PhaseRecommendations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PartnerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val hasPartner: Boolean = false,
    val partnerConnections: List<PartnerConnection> = emptyList(),
    // Data visible to partner
    val partnerName: String = "",
    val trackerName: String = "",
    val currentPhase: CyclePhase = CyclePhase.Follicular,
    val cycleDay: Int = 1,
    val cycleLength: Int = 28,
    val daysUntilPeriod: Int = 0,
    val partnerNote: String = "",
    val phaseAdvice: List<String> = emptyList(),
    val recommendations: PhaseRecommendations? = null,
    val supportTips: List<String> = emptyList(),
    // Invite form
    val inviteName: String = "",
    val inviteEmail: String = "",
    val inviteNote: String = "",
    val isCaregiver: Boolean = false,
    val isSending: Boolean = false
)

@HiltViewModel
class PartnerViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cycleRepository: CycleRepository,
    private val partnerRepository: PartnerRepository,
    private val cycleCalculator: CycleCalculator,
    private val insightsEngine: DailyInsightsEngine,
    private val recommendationsEngine: RecommendationsEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartnerUiState())
    val uiState: StateFlow<PartnerUiState> = _uiState.asStateFlow()

    init {
        loadPartnerData()
    }

    fun loadPartnerData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val userId = authRepository.getCurrentUserId() ?: return@launch
            val user = authRepository.getCurrentUser()
            val entries = cycleRepository.getEntries(userId)
            val cycles = entries.map { it.toCycleLog() }

            val trackerName = user?.name ?: "Your partner"
            val cycleDay = cycleCalculator.getCurrentCycleDay(cycles)
            val phase = cycleCalculator.getCurrentPhase(cycles)
            val avgLength = cycleCalculator.getAverageCycleLength(cycles)
            val nextPeriod = cycleCalculator.getNextPeriodDate(cycles)
            val daysUntil = cycleCalculator.getDaysUntil(nextPeriod)

            val insights = insightsEngine.getDailyInsights(cycleDay, avgLength, trackerName)
            val recs = recommendationsEngine.getRecommendations(phase.display)

            val supportTips = generateSupportTips(phase, cycleDay, avgLength, trackerName)

            // Try loading partner connections
            val connections = partnerRepository.getPartnerConnections().getOrNull() ?: emptyList()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasPartner = connections.isNotEmpty(),
                    partnerConnections = connections,
                    trackerName = trackerName,
                    currentPhase = phase,
                    cycleDay = cycleDay,
                    cycleLength = avgLength,
                    daysUntilPeriod = daysUntil,
                    partnerNote = insights.partnerNote,
                    recommendations = recs,
                    supportTips = supportTips
                )
            }
        }
    }

    private fun generateSupportTips(phase: CyclePhase, cycleDay: Int, cycleLength: Int, name: String): List<String> {
        val daysUntilPeriod = cycleLength - cycleDay
        return when (phase) {
            CyclePhase.Menstrual -> listOf(
                "Bring $name a warm drink without being asked",
                "Offer a heating pad or hot water bottle",
                "Don't take quietness or low energy personally",
                "Suggest a cozy movie night at home",
                "Pick up their favorite comfort food",
                "Be extra patient and supportive today"
            )
            CyclePhase.Follicular -> listOf(
                "$name has rising energy -- great time for date night!",
                "They're feeling social and creative right now",
                "Suggest trying a new activity or restaurant together",
                "This is a good time for deeper conversations",
                "Match their energy -- they'll appreciate enthusiasm",
                "Plan something adventurous for this week"
            )
            CyclePhase.Ovulation -> listOf(
                "$name is at peak confidence and energy",
                "They may want more connection and conversation",
                "Great time for social plans with friends",
                "Match their outgoing energy",
                "Compliments will land especially well right now",
                "Suggest active plans -- hiking, dancing, exploring"
            )
            CyclePhase.Luteal -> if (daysUntilPeriod <= 5) {
                listOf(
                    "PMS may be peaking -- extra patience goes a long way",
                    "Don't take irritability personally, it's hormonal",
                    "Bring comfort food without being asked",
                    "Suggest a cozy night in rather than going out",
                    "Help them prepare: stock up on period supplies",
                    "A warm bath or massage can work wonders"
                )
            } else {
                listOf(
                    "$name may be more tired or withdrawn than usual",
                    "Respect their need for quiet time",
                    "Be flexible with plans -- they may cancel",
                    "Don't pressure them into high-energy activities",
                    "Comfort foods and cozy settings help",
                    "Check in gently but don't hover"
                )
            }
        }
    }

    fun updateInviteName(name: String) {
        _uiState.update { it.copy(inviteName = name) }
    }

    fun updateInviteEmail(email: String) {
        _uiState.update { it.copy(inviteEmail = email) }
    }

    fun updateInviteNote(note: String) {
        _uiState.update { it.copy(inviteNote = note) }
    }

    fun updateIsCaregiver(isCaregiver: Boolean) {
        _uiState.update { it.copy(isCaregiver = isCaregiver) }
    }

    fun sendInvite(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSending = true, error = null) }

            val result = partnerRepository.invitePartner(
                name = state.inviteName,
                email = state.inviteEmail,
                note = state.inviteNote,
                isCaregiver = state.isCaregiver
            )

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSending = false) }
                    loadPartnerData()
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isSending = false, error = e.message) }
                }
            )
        }
    }

    fun removePartner(connectionId: String) {
        viewModelScope.launch {
            partnerRepository.removePartner(connectionId)
            loadPartnerData()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
