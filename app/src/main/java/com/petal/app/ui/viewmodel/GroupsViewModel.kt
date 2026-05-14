package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.remote.dto.FriendGroupMemberDto
import com.petal.app.data.remote.dto.FriendGroupSummaryDto
import com.petal.app.data.remote.dto.ScoreboardEntryDto
import com.petal.app.data.repository.AuthRepository
import com.petal.app.data.repository.GroupsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PHASE_6_7_PLAN.md §6B.3 — single VM for the Groups list + detail screens
 * so create/join/membership-change events invalidate from one source.
 */
data class GroupsUiState(
    val isLoading: Boolean = false,
    val groups: List<FriendGroupSummaryDto> = emptyList(),
    val currentUserId: String? = null,
    val error: String? = null,
    // Detail-screen state, keyed implicitly to whichever group is open.
    val openGroupId: String? = null,
    val members: List<FriendGroupMemberDto> = emptyList(),
    val scoreboard: List<ScoreboardEntryDto> = emptyList(),
    val range: String = "week",
    val isLoadingDetail: Boolean = false,
    val pendingActionId: String? = null,
    val pingResultMessage: String? = null,
)

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val repository: GroupsRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(GroupsUiState())
    val ui: StateFlow<GroupsUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            _ui.update { it.copy(currentUserId = authRepository.getCurrentUserId()) }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val r = repository.listGroups()
            r.fold(
                onSuccess = { groups -> _ui.update { it.copy(isLoading = false, groups = groups) } },
                onFailure = { e -> _ui.update { it.copy(isLoading = false, error = e.message) } },
            )
        }
    }

    fun createGroup(name: String, emoji: String?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val r = repository.createGroup(name, emoji)
            r.fold(
                onSuccess = { group ->
                    _ui.update { state ->
                        state.copy(groups = listOf(group) + state.groups.filterNot { it.id == group.id })
                    }
                    onResult(true, null)
                },
                onFailure = { e -> onResult(false, e.message) },
            )
        }
    }

    fun joinGroup(code: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val r = repository.joinGroup(code)
            r.fold(
                onSuccess = { group ->
                    _ui.update { state ->
                        state.copy(groups = listOf(group) + state.groups.filterNot { it.id == group.id })
                    }
                    onResult(true, null)
                },
                onFailure = { e -> onResult(false, e.message) },
            )
        }
    }

    fun openGroup(groupId: String, range: String = "week") {
        viewModelScope.launch {
            _ui.update { it.copy(openGroupId = groupId, isLoadingDetail = true, range = range) }
            val membersResult = repository.listMembers(groupId)
            val scoreboardResult = repository.getScoreboard(groupId, range)
            _ui.update {
                it.copy(
                    isLoadingDetail = false,
                    members = membersResult.getOrDefault(emptyList()),
                    scoreboard = scoreboardResult.getOrDefault(emptyList()),
                )
            }
        }
    }

    fun changeRange(range: String) {
        val groupId = _ui.value.openGroupId ?: return
        viewModelScope.launch {
            _ui.update { it.copy(range = range, isLoadingDetail = true) }
            val r = repository.getScoreboard(groupId, range)
            _ui.update {
                it.copy(isLoadingDetail = false, scoreboard = r.getOrDefault(emptyList()))
            }
        }
    }

    fun closeGroup() {
        _ui.update { it.copy(openGroupId = null, members = emptyList(), scoreboard = emptyList()) }
    }

    fun updateShareLevel(groupId: String, shareLevel: String) {
        viewModelScope.launch {
            _ui.update { it.copy(pendingActionId = groupId) }
            repository.updateMembership(groupId, shareLevel = shareLevel)
            // Optimistic refresh of the affected group.
            val refreshed = repository.listGroups().getOrDefault(_ui.value.groups)
            _ui.update { it.copy(pendingActionId = null, groups = refreshed) }
        }
    }

    fun toggleReceivePings(groupId: String, receive: Boolean) {
        viewModelScope.launch {
            _ui.update { it.copy(pendingActionId = groupId) }
            repository.updateMembership(groupId, receiveUnwellPings = receive)
            val refreshed = repository.listGroups().getOrDefault(_ui.value.groups)
            _ui.update { it.copy(pendingActionId = null, groups = refreshed) }
        }
    }

    fun fireUnwell(groupId: String, message: String?) {
        viewModelScope.launch {
            val r = repository.fireUnwellPing(groupId, message)
            r.fold(
                onSuccess = { resp ->
                    val n = resp.recipientCount
                    val msg = if (n == 0) "sent — no one's set to receive these right now"
                              else "sent — $n ${if (n == 1) "friend" else "friends"} will see this"
                    _ui.update { it.copy(pingResultMessage = msg) }
                },
                onFailure = { e -> _ui.update { it.copy(error = e.message) } },
            )
        }
    }

    fun clearPingResult() { _ui.update { it.copy(pingResultMessage = null) } }

    fun leaveGroup(groupId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val r = repository.leaveGroup(groupId)
            r.fold(
                onSuccess = {
                    _ui.update { state ->
                        state.copy(
                            groups = state.groups.filterNot { it.id == groupId },
                            openGroupId = null
                        )
                    }
                    onComplete()
                },
                onFailure = { e -> _ui.update { it.copy(error = e.message) } },
            )
        }
    }

    fun disbandGroup(groupId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val r = repository.disbandGroup(groupId)
            r.fold(
                onSuccess = {
                    _ui.update { state ->
                        state.copy(
                            groups = state.groups.filterNot { it.id == groupId },
                            openGroupId = null
                        )
                    }
                    onComplete()
                },
                onFailure = { e -> _ui.update { it.copy(error = e.message) } },
            )
        }
    }

    fun removeMember(groupId: String, targetUserId: String) {
        viewModelScope.launch {
            val r = repository.removeMember(groupId, targetUserId)
            r.fold(
                onSuccess = {
                    _ui.update { state ->
                        state.copy(members = state.members.filterNot { it.userId == targetUserId })
                    }
                    refresh() // member_count changed
                },
                onFailure = { e -> _ui.update { it.copy(error = e.message) } },
            )
        }
    }
}
