package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.local.entity.AvailabilityEntity
import com.duoc.prestaaula.data.repository.AvailabilityRepository
import com.duoc.prestaaula.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val availability: List<AvailabilityEntity> = emptyList(),
    val day: Int = 1,
    val startBlock: Int = 1,
    val endBlock: Int = 1,
    val rangeError: String? = null,
    val message: String? = null
)

class ProfileViewModel(
    private val repo: AvailabilityRepository,
    private val teacherId: Long
) : ViewModel() {
    private val _ui = MutableStateFlow(ProfileUiState())
    val ui: StateFlow<ProfileUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeByTeacher(teacherId).collect { list -> _ui.update { it.copy(availability = list) } }
        }
    }

    fun onDayChange(d: Int) = _ui.update { it.copy(day = d, rangeError = null, message = null) }
    fun onStartChange(b: Int) = _ui.update {
        it.copy(startBlock = b, rangeError = Validators.availabilityRange(b, it.endBlock), message = null)
    }
    fun onEndChange(b: Int) = _ui.update {
        it.copy(endBlock = b, rangeError = Validators.availabilityRange(it.startBlock, b), message = null)
    }

    fun addAvailability() {
        val s = _ui.value
        val rangeError = Validators.availabilityRange(s.startBlock, s.endBlock)
            ?: if (s.availability.any { it.dayOfWeek == s.day && s.startBlock <= it.endBlock && s.endBlock >= it.startBlock }) {
                "Se superpone con un tramo ya registrado ese día"
            } else null
        if (rangeError != null) {
            _ui.update { it.copy(rangeError = rangeError) }
            return
        }
        viewModelScope.launch {
            repo.add(AvailabilityEntity(teacherId = teacherId, dayOfWeek = s.day, startBlock = s.startBlock, endBlock = s.endBlock))
            _ui.update { it.copy(message = "Disponibilidad agregada", rangeError = null) }
        }
    }

    fun removeAvailability(id: Long) {
        viewModelScope.launch { repo.remove(id) }
    }
}
