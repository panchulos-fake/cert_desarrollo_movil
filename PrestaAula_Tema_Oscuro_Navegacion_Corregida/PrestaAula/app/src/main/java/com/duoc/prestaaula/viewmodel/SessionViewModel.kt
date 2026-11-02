package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.data.repository.UserRepository
import com.duoc.prestaaula.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: UserEntity) : SessionState
}

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(
    userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val state: StateFlow<SessionState> = sessionManager.userId
        .flatMapLatest { id ->
            val flow: Flow<SessionState> = if (id == null) {
                flowOf(SessionState.LoggedOut)
            } else {
                userRepository.observeById(id).map { user ->
                    if (user == null) SessionState.LoggedOut else SessionState.LoggedIn(user)
                }
            }
            flow
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    fun logout() = sessionManager.logout()
}
