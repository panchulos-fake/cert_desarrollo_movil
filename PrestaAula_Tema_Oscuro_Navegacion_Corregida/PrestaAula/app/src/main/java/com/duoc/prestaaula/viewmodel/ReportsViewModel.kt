package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.local.entity.EquipmentStats
import com.duoc.prestaaula.data.local.entity.EquipmentStatus
import com.duoc.prestaaula.data.local.entity.LoanDetail
import com.duoc.prestaaula.data.repository.EquipmentRepository
import com.duoc.prestaaula.data.repository.LoanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ReportsUiState(
    val totalEquipment: Int = 0,
    val available: Int = 0,
    val lent: Int = 0,
    val maintenance: Int = 0,
    val activeLoans: Int = 0,
    val totalLoans: Int = 0,
    val mostUsed: List<EquipmentStats> = emptyList(),
    val frequentIncidents: List<EquipmentStats> = emptyList(),
    val incidents: List<LoanDetail> = emptyList()
)

class ReportsViewModel(
    equipmentRepo: EquipmentRepository,
    loanRepo: LoanRepository
) : ViewModel() {
    val uiState: StateFlow<ReportsUiState> = combine(
        equipmentRepo.observeAll(),
        loanRepo.observeStats(),
        loanRepo.observeIncidents(),
        loanRepo.observeActiveCount()
    ) { equipment, stats, incidents, active ->
        ReportsUiState(
            totalEquipment = equipment.size,
            available = equipment.count { it.status == EquipmentStatus.DISPONIBLE },
            lent = equipment.count { it.status == EquipmentStatus.PRESTADO },
            maintenance = equipment.count { it.status == EquipmentStatus.MANTENCION },
            activeLoans = active,
            totalLoans = stats.sumOf { it.total },
            mostUsed = stats.filter { it.total > 0 }.sortedByDescending { it.total }.take(5),
            frequentIncidents = stats.filter { it.incidents > 0 }.sortedByDescending { it.incidents }.take(5),
            incidents = incidents
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState())
}
