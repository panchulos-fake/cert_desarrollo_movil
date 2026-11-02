package com.duoc.prestaaula.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duoc.prestaaula.data.local.entity.EquipmentEntity
import com.duoc.prestaaula.data.local.entity.EquipmentStatus
import com.duoc.prestaaula.data.repository.EquipmentRepository
import com.duoc.prestaaula.data.repository.LoanRepository
import com.duoc.prestaaula.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class SlotAvailability { LIBRE, OCUPADO, MANTENCION }

data class EquipmentItem(
    val equipment: EquipmentEntity,
    val slot: SlotAvailability,
    val holder: String?
)

data class EquipmentListUiState(
    val date: Long,
    val block: Int,
    val query: String,
    val items: List<EquipmentItem>
) {
    val freeCount: Int get() = items.count { it.slot == SlotAvailability.LIBRE }
}

@OptIn(ExperimentalCoroutinesApi::class)
class EquipmentListViewModel(
    equipmentRepo: EquipmentRepository,
    loanRepo: LoanRepository
) : ViewModel() {
    private val date = MutableStateFlow(DateUtils.today())
    private val block = MutableStateFlow(1)
    private val query = MutableStateFlow("")

    val uiState: StateFlow<EquipmentListUiState> = combine(
        equipmentRepo.observeAll(),
        date.flatMapLatest { loanRepo.observeActiveByDate(it) },
        date,
        block,
        query
    ) { equipment, loans, d, b, q ->
        val term = q.trim()
        val items = equipment
            .filter { e ->
                term.isEmpty() || listOf(e.type, e.brand, e.internalCode).any { it.contains(term, ignoreCase = true) }
            }
            .map { e ->
                val holder = loans.firstOrNull { it.loan.equipmentId == e.id && it.loan.block == b }
                val slot = when {
                    e.status == EquipmentStatus.MANTENCION -> SlotAvailability.MANTENCION
                    holder != null -> SlotAvailability.OCUPADO
                    // Un equipo físicamente prestado hoy no puede anunciarse como libre
                    // en otros bloques mientras no se haya registrado su devolución.
                    e.status == EquipmentStatus.PRESTADO && d <= DateUtils.today() -> SlotAvailability.OCUPADO
                    else -> SlotAvailability.LIBRE
                }
                EquipmentItem(e, slot, holder?.teacherName)
            }
        EquipmentListUiState(d, b, q, items)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        EquipmentListUiState(DateUtils.today(), 1, "", emptyList())
    )

    fun onDateChange(epochDay: Long) { date.value = epochDay }
    fun onBlockChange(number: Int) { block.value = number }
    fun onQueryChange(value: String) { query.value = value }
}
