package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.remote.dto.PartnerMessageDto
import com.petal.app.data.remote.dto.PartnerThreadDto
import com.petal.app.data.repository.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MessagesUiState(
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null,
    val thread: PartnerThreadDto? = null,
    val messages: List<PartnerMessageDto> = emptyList(),
    val draft: String = "",
    val newIds: Set<String> = emptySet(),
    val playPing: Long = 0L,
    val burstAt: Long = 0L,
    val soundOn: Boolean = false,
    val currentUserId: String = "",
)

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val repo: MessagesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MessagesUiState())
    val state: StateFlow<MessagesUiState> = _state.asStateFlow()
    private var pollJob: Job? = null
    private val seenIds = mutableSetOf<String>()
    private var firstLoad = true

    fun init(currentUserId: String) {
        _state.update { it.copy(currentUserId = currentUserId) }
        loadThread()
        startPolling()
    }

    fun setSoundOn(on: Boolean) = _state.update { it.copy(soundOn = on) }

    fun setDraft(value: String) = _state.update { it.copy(draft = value) }

    private fun loadThread() = viewModelScope.launch {
        val r = repo.getThread()
        r.onSuccess { thread ->
            _state.update { it.copy(thread = thread, error = null) }
            refreshMessages()
        }.onFailure { e ->
            _state.update { it.copy(error = e.message) }
        }
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (true) {
                refreshMessages()
                delay(4500)
            }
        }
    }

    private suspend fun refreshMessages() {
        val r = repo.listMessages()
        r.onSuccess { fresh ->
            val incoming = fresh.filter { it.senderId != _state.value.currentUserId && it.id !in seenIds }
            seenIds.addAll(fresh.map { it.id })
            val next = _state.value.copy(
                messages = fresh,
                isLoading = false,
                error = null,
                newIds = if (!firstLoad) incoming.map { it.id }.toSet() else emptySet(),
                playPing = if (!firstLoad && incoming.isNotEmpty() && _state.value.soundOn)
                    System.currentTimeMillis() else _state.value.playPing,
            )
            _state.value = next
            firstLoad = false
        }.onFailure { e ->
            _state.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun send() {
        val text = _state.value.draft.trim()
        if (text.isEmpty()) return
        _state.update { it.copy(draft = "", burstAt = System.currentTimeMillis(), isSending = true) }
        // Optimistic
        val tempId = "temp-${System.currentTimeMillis()}"
        val tempMsg = PartnerMessageDto(
            id = tempId,
            threadId = _state.value.thread?.threadId ?: "pending",
            senderId = _state.value.currentUserId,
            content = text,
            sentAt = java.time.Instant.now().toString(),
            readAt = null,
        )
        _state.update { it.copy(messages = it.messages + tempMsg) }
        viewModelScope.launch {
            val r = repo.send(text)
            r.onSuccess { saved ->
                seenIds.add(saved.id)
                _state.update { s ->
                    s.copy(
                        isSending = false,
                        messages = s.messages.map { if (it.id == tempId) saved else it },
                    )
                }
            }.onFailure {
                _state.update { s ->
                    s.copy(
                        isSending = false,
                        messages = s.messages.filter { it.id != tempId },
                        error = "couldn't send right now ⌒",
                    )
                }
            }
        }
    }

    fun markRead() = viewModelScope.launch { repo.markRead() }

    fun delete(messageId: String) = viewModelScope.launch {
        // optimistic
        _state.update { s -> s.copy(messages = s.messages.filter { it.id != messageId }) }
        repo.delete(messageId)
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
