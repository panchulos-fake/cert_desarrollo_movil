package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.repository.UserRepository
import com.duoc.prestaaula.data.session.SessionManager
import com.duoc.prestaaula.domain.model.Outcome
import com.duoc.prestaaula.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val generalError: String? = null
)

class LoginViewModel(
    private val users: UserRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun onEmailChange(value: String) = _ui.update {
        it.copy(email = value, emailError = Validators.email(value), generalError = null)
    }

    fun onPasswordChange(value: String) = _ui.update {
        it.copy(password = value, passwordError = Validators.loginPassword(value), generalError = null)
    }

    fun togglePasswordVisibility() = _ui.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun submit() {
        val s = _ui.value
        if (s.loading) return
        val emailError = Validators.email(s.email)
        val passwordError = Validators.loginPassword(s.password)
        if (emailError != null || passwordError != null) {
            _ui.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, generalError = null) }
            when (val result = users.login(s.email, s.password)) {
                is Outcome.Success -> session.login(result.value.id)
                is Outcome.Failure -> _ui.update { it.copy(loading = false, generalError = result.message) }
            }
        }
    }
}
