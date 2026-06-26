package com.harnie.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harnie.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoginMode = MutableStateFlow(true)
    val isLoginMode: StateFlow<Boolean> = _isLoginMode.asStateFlow()

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }
    fun toggleMode() { _isLoginMode.value = !_isLoginMode.value }

    fun submit() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = if (_isLoginMode.value) {
                authRepository.signIn(_email.value, _password.value)
            } else {
                authRepository.signUp(_email.value, _password.value)
            }
            _uiState.value = result.fold(
                onSuccess = { AuthUiState.Success },
                onFailure = { AuthUiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            if (authRepository.isAuthenticated()) {
                _uiState.value = AuthUiState.Success
            }
        }
    }
}