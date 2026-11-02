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

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirm: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val passwordVisible: Boolean = false,
    val loading: Boolean = false,
    val generalError: String? = null
)

class RegisterViewModel(
    private val users: UserRepository,
    private val session: SessionManager
) : ViewModel() {
    private val _ui = MutableStateFlow(RegisterUiState())
    val ui: StateFlow<RegisterUiState> = _ui.asStateFlow()

    fun onNameChange(v: String) = _ui.update { it.copy(name = v, nameError = Validators.personName(v), generalError = null) }
    fun onEmailChange(v: String) = _ui.update { it.copy(email = v, emailError = Validators.email(v), generalError = null) }
    fun onPasswordChange(v: String) = _ui.update {
        it.copy(
            password = v,
            passwordError = Validators.newPassword(v),
            confirmError = if (it.confirm.isEmpty()) null else Validators.confirmPassword(v, it.confirm),
            generalError = null
        )
    }
    fun onConfirmChange(v: String) = _ui.update {
        it.copy(confirm = v, confirmError = Validators.confirmPassword(it.password, v), generalError = null)
    }
    fun togglePasswordVisibility() = _ui.update { it.copy(passwordVisible = !it.passwordVisible) }

    fun submit() {
        val s = _ui.value
        if (s.loading) return
        val nameError = Validators.personName(s.name)
        val emailError = Validators.email(s.email)
        val passwordError = Validators.newPassword(s.password)
        val confirmError = Validators.confirmPassword(s.password, s.confirm)
        if (listOf(nameError, emailError, passwordError, confirmError).any { it != null }) {
            _ui.update {
                it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError, confirmError = confirmError)
            }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, generalError = null) }
            when (val result = users.registerTeacher(s.name, s.email, s.password)) {
                is Outcome.Success -> session.login(result.value)
                is Outcome.Failure -> _ui.update { it.copy(loading = false, generalError = result.message) }
            }
        }
    }
}
