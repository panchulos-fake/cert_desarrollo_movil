package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.local.entity.LoanDetail
import com.duoc.prestaaula.data.local.entity.LoanStatus
import com.duoc.prestaaula.data.local.entity.Role
import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.data.repository.LoanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class LoanFilter(val label: String, val status: LoanStatus?) {
    TODAS("Todas", null),
    RESERVADAS("Reservadas", LoanStatus.RESERVADA),
    RETIRADAS("Retiradas", LoanStatus.RETIRADA),
    DEVUELTAS("Devueltas", LoanStatus.DEVUELTA),
    CANCELADAS("Canceladas", LoanStatus.CANCELADA)
}

data class LoansUiState(
    val filter: LoanFilter = LoanFilter.TODAS,
    val loans: List<LoanDetail> = emptyList()
)

class LoansViewModel(repo: LoanRepository, user: UserEntity) : ViewModel() {
    private val filter = MutableStateFlow(LoanFilter.TODAS)

    private val source = if (user.role == Role.DOCENTE) repo.observeByTeacher(user.id) else repo.observeAllDetails()

    val uiState: StateFlow<LoansUiState> = combine(source, filter) { loans, f ->
        LoansUiState(f, loans.filter { f.status == null || it.loan.status == f.status })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoansUiState())

    fun onFilterChange(f: LoanFilter) { filter.value = f }
}
