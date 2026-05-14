package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.remote.dto.BlockedUserDto
import com.petal.app.data.repository.ModerationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PHASE_6_7_PLAN.md §6B.1 — moderation state for the Settings + Report
 * affordances. Single VM rather than two so the dialog and the list can
 * both invalidate via one source.
 */
data class ModerationUiState(
    val isLoading: Boolean = false,
    val blocks: List<BlockedUserDto> = emptyList(),
    val error: String? = null,
    val savingBlockId: String? = null,
    val isSubmittingReport: Boolean = false,
)

@HiltViewModel
class ModerationViewModel @Inject constructor(
    private val repository: ModerationRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(ModerationUiState())
    val ui: StateFlow<ModerationUiState> = _ui.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            val result = repository.listBlocks()
            result.fold(
                onSuccess = { list -> _ui.update { it.copy(isLoading = false, blocks = list) } },
                onFailure = { e -> _ui.update { it.copy(isLoading = false, error = e.message) } },
            )
        }
    }

    fun blockHandle(handle: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _ui.update { it.copy(savingBlockId = handle, error = null) }
            val res = repository.blockHandle(handle)
            res.fold(
                onSuccess = { row ->
                    _ui.update { state ->
                        val without = state.blocks.filterNot { it.id == row.id }
                        state.copy(savingBlockId = null, blocks = listOf(row) + without)
                    }
                    onResult(true, null)
                },
                onFailure = { e ->
                    _ui.update { it.copy(savingBlockId = null, error = e.message) }
                    onResult(false, e.message)
                },
            )
        }
    }

    fun removeBlock(blockId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(savingBlockId = blockId, error = null) }
            val res = repository.removeBlock(blockId)
            res.fold(
                onSuccess = {
                    _ui.update { state ->
                        state.copy(
                            savingBlockId = null,
                            blocks = state.blocks.filterNot { it.id == blockId }
                        )
                    }
                },
                onFailure = { e ->
                    _ui.update { it.copy(savingBlockId = null, error = e.message) }
                },
            )
        }
    }

    fun submitReport(
        context: String,
        reason: String,
        details: String? = null,
        reportedUsername: String? = null,
        reportedUserId: String? = null,
        onResult: (Boolean, String?) -> Unit,
    ) {
        viewModelScope.launch {
            _ui.update { it.copy(isSubmittingReport = true, error = null) }
            val res = repository.submitReport(
                context = context,
                reason = reason,
                details = details,
                reportedUsername = reportedUsername,
                reportedUserId = reportedUserId,
            )
            _ui.update { it.copy(isSubmittingReport = false) }
            res.fold(
                onSuccess = { onResult(true, null) },
                onFailure = { e -> onResult(false, e.message) },
            )
        }
    }
}
