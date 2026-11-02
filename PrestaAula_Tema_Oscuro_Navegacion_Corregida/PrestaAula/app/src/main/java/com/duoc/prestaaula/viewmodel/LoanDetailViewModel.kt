package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.local.entity.LoanDetail
import com.duoc.prestaaula.data.repository.LoanRepository
import com.duoc.prestaaula.domain.model.Outcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoanDetailUiState(
    val detail: LoanDetail? = null,
    val loaded: Boolean = false,
    val working: Boolean = false,
    val message: String? = null,
    val messageIsError: Boolean = false,
    val pickupDone: Boolean = false
)

class LoanDetailViewModel(
    private val repo: LoanRepository,
    private val loanId: Long
) : ViewModel() {
    private val _ui = MutableStateFlow(LoanDetailUiState())
    val ui: StateFlow<LoanDetailUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeDetail(loanId).collect { d -> _ui.update { it.copy(detail = d, loaded = true) } }
        }
    }

    fun pickup() = execute("Retiro registrado correctamente", markPickup = true) { repo.pickup(loanId) }
    fun cancel() = execute("Reserva cancelada") { repo.cancel(loanId) }

    private fun execute(success: String, markPickup: Boolean = false, action: suspend () -> Outcome<Unit>) {
        viewModelScope.launch {
            _ui.update { it.copy(working = true) }
            when (val r = action()) {
                is Outcome.Success -> _ui.update {
                    it.copy(working = false, message = success, messageIsError = false, pickupDone = markPickup)
                }
                is Outcome.Failure -> _ui.update {
                    it.copy(working = false, message = r.message, messageIsError = true)
                }
            }
        }
    }

    fun clearMessage() = _ui.update { it.copy(message = null, pickupDone = false) }
}
