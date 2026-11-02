package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.local.entity.LoanDetail
import com.duoc.prestaaula.data.local.entity.ReturnCondition
import com.duoc.prestaaula.data.repository.LoanRepository
import com.duoc.prestaaula.domain.model.Outcome
import com.duoc.prestaaula.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReturnUiState(
    val detail: LoanDetail? = null,
    val condition: ReturnCondition = ReturnCondition.BUEN_ESTADO,
    val observations: String = "",
    val photoPath: String? = null,
    val observationsError: String? = null,
    val photoError: String? = null,
    val cameraMessage: String? = null,
    val loading: Boolean = false,
    val generalError: String? = null,
    val completed: Boolean = false,
    val incidentNotice: String? = null
) {
    val observationsRequired: Boolean get() = condition != ReturnCondition.BUEN_ESTADO
    val photoRequired: Boolean get() = condition == ReturnCondition.CON_DANOS
}

class ReturnViewModel(
    private val repo: LoanRepository,
    private val loanId: Long
) : ViewModel() {
    private val _ui = MutableStateFlow(ReturnUiState())
    val ui: StateFlow<ReturnUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeDetail(loanId).collect { d -> _ui.update { if (it.completed) it else it.copy(detail = d) } }
        }
    }

    fun onConditionChange(c: ReturnCondition) = _ui.update {
        it.copy(
            condition = c,
            observationsError = if (it.observations.isEmpty()) null else Validators.observations(it.observations, c != ReturnCondition.BUEN_ESTADO),
            photoError = null,
            generalError = null
        )
    }

    fun onObservationsChange(v: String) = _ui.update {
        it.copy(observations = v, observationsError = Validators.observations(v, it.observationsRequired), generalError = null)
    }

    fun onPhotoCaptured(path: String) = _ui.update { it.copy(photoPath = path, photoError = null, cameraMessage = null) }
    fun onPhotoRemoved() = _ui.update { it.copy(photoPath = null) }
    fun onCameraMessage(message: String?) = _ui.update { it.copy(cameraMessage = message) }

    fun submit() {
        val s = _ui.value
        if (s.loading || s.completed) return
        val obsError = Validators.observations(s.observations, s.observationsRequired)
        val photoError = Validators.photo(s.photoPath, s.photoRequired)
        if (obsError != null || photoError != null) {
            _ui.update { it.copy(observationsError = obsError, photoError = photoError) }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, generalError = null) }
            when (val r = repo.returnLoan(loanId, s.condition, s.observations, s.photoPath)) {
                is Outcome.Success -> _ui.update {
                    it.copy(
                        loading = false,
                        completed = true,
                        incidentNotice = if (r.value) {
                            "${s.detail?.equipmentName ?: "El equipo"} (${s.detail?.equipmentCode ?: ""}) " +
                                "fue devuelto con observaciones y quedó en mantención."
                        } else null
                    )
                }
                is Outcome.Failure -> _ui.update { it.copy(loading = false, generalError = r.message) }
            }
        }
    }
}
