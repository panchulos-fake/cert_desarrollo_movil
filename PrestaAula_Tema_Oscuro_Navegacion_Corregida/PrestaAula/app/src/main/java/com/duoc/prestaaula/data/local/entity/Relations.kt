package com.duoc.prestaaula.data.local.entity

import androidx.room.Embedded

/** Préstamo con los datos del equipo y del docente (resultado de un JOIN). */
data class LoanDetail(
    @Embedded val loan: LoanEntity,
    val equipmentType: String,
    val equipmentBrand: String,
    val equipmentCode: String,
    val teacherName: String
) {
    val equipmentName: String get() = "$equipmentType $equipmentBrand"
}

/** Estadísticas de uso e incidencias por equipo. */
data class EquipmentStats(
    val equipmentId: Long,
    val type: String,
    val brand: String,
    val internalCode: String,
    val total: Int,
    val incidents: Int
)
