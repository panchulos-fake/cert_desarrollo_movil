package com.duoc.prestaaula.data.session

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Mantiene la sesión activa (persistida en SharedPreferences). */
class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val _userId = MutableStateFlow(prefs.getLong(KEY_USER, -1L).takeIf { it > 0 })
    val userId: StateFlow<Long?> = _userId.asStateFlow()

    fun login(id: Long) {
        prefs.edit().putLong(KEY_USER, id).apply()
        _userId.value = id
    }

    fun logout() {
        prefs.edit().remove(KEY_USER).apply()
        _userId.value = null
    }

    private companion object {
        const val KEY_USER = "user_id"
    }
}
