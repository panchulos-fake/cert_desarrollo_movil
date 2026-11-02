package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.local.entity.EquipmentStatus
import com.duoc.prestaaula.data.repository.EquipmentRepository
import com.duoc.prestaaula.domain.model.Outcome
import com.duoc.prestaaula.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EquipmentFormUiState(
    val isEdit: Boolean = false,
    val type: String = "",
    val brand: String = "",
    val code: String = "",
    val status: EquipmentStatus = EquipmentStatus.DISPONIBLE,
    val notes: String = "",
    val currentStatus: EquipmentStatus? = null,
    val typeError: String? = null,
    val brandError: String? = null,
    val codeError: String? = null,
    val notesError: String? = null,
    val loading: Boolean = false,
    val generalError: String? = null,
    val saved: Boolean = false
)

class EquipmentFormViewModel(
    private val repo: EquipmentRepository,
    private val equipmentId: Long
) : ViewModel() {
    private val _ui = MutableStateFlow(EquipmentFormUiState(isEdit = equipmentId != 0L))
    val ui: StateFlow<EquipmentFormUiState> = _ui.asStateFlow()

    init {
        if (equipmentId != 0L) {
            viewModelScope.launch {
                repo.getById(equipmentId)?.let { e ->
                    _ui.update {
                        it.copy(
                            type = e.type,
                            brand = e.brand,
                            code = e.internalCode,
                            status = e.status,
                            notes = e.notes,
                            currentStatus = e.status
                        )
                    }
                }
            }
        }
    }

    fun onTypeChange(v: String) = _ui.update { it.copy(type = v, typeError = Validators.equipmentType(v), generalError = null) }
    fun onBrandChange(v: String) = _ui.update { it.copy(brand = v, brandError = Validators.brand(v), generalError = null) }
    fun onCodeChange(v: String) = _ui.update {
        val upper = v.uppercase()
        it.copy(code = upper, codeError = Validators.internalCode(upper), generalError = null)
    }
    fun onStatusChange(v: EquipmentStatus) = _ui.update { it.copy(status = v) }
    fun onNotesChange(v: String) = _ui.update { it.copy(notes = v, notesError = Validators.equipmentNotes(v)) }

    fun submit() {
        val s = _ui.value
        if (s.loading || s.saved) return
        val typeError = Validators.equipmentType(s.type)
        val brandError = Validators.brand(s.brand)
        val codeError = Validators.internalCode(s.code)
        val notesError = Validators.equipmentNotes(s.notes)
        if (listOf(typeError, brandError, codeError, notesError).any { it != null }) {
            _ui.update {
                it.copy(typeError = typeError, brandError = brandError, codeError = codeError, notesError = notesError)
            }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, generalError = null) }
            when (val r = repo.save(equipmentId, s.type, s.brand, s.code, s.status, s.notes)) {
                is Outcome.Success -> _ui.update { it.copy(loading = false, saved = true) }
                is Outcome.Failure -> _ui.update {
                    it.copy(
                        loading = false,
                        codeError = if (r.message.contains("código")) r.message else it.codeError,
                        generalError = r.message
                    )
                }
            }
        }
    }
}
