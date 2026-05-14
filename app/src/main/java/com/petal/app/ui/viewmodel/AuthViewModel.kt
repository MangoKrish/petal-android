package com.petal.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petal.app.data.model.User
import com.petal.app.data.model.UserRole
import com.petal.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val securityQuestion: String? = null,
    val passwordResetSuccess: Boolean = false,
    /** PHASE_6_7_PLAN.md §6A.1 — pre-auth role choice; null until the user picks. */
    val pendingRole: UserRole? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasOnboarded: StateFlow<Boolean> = authRepository.hasCompletedOnboarding
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUser: StateFlow<User?> = authRepository.observeCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user) }
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val role = _uiState.value.pendingRole ?: UserRole.Primary
            val result = authRepository.register(name, email, password, securityQuestion, securityAnswer, role)
            result.fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, user = user, pendingRole = null) }
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    /** PHASE_6_7_PLAN.md §6A.1 — pre-auth role chooser sets this; the
     *  signup screen reads it and the register call attaches it. */
    fun setPendingRole(role: UserRole) {
        _uiState.update { it.copy(pendingRole = role) }
    }

    fun clearPendingRole() {
        _uiState.update { it.copy(pendingRole = null) }
    }

    fun getSecurityQuestion(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.getSecurityQuestion(email)
            result.fold(
                onSuccess = { question ->
                    _uiState.update { it.copy(isLoading = false, securityQuestion = question) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun resetPassword(email: String, securityAnswer: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = authRepository.resetPassword(email, securityAnswer, newPassword)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, passwordResetSuccess = true) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
