package com.duoc.prestaaula.data.repository

import com.duoc.prestaaula.data.local.dao.EquipmentDao
import com.duoc.prestaaula.data.local.entity.EquipmentEntity
import com.duoc.prestaaula.data.local.entity.EquipmentStatus
import com.duoc.prestaaula.domain.model.Outcome
import kotlinx.coroutines.flow.Flow

class EquipmentRepository(private val dao: EquipmentDao) {
    fun observeAll(): Flow<List<EquipmentEntity>> = dao.observeAll()
    fun observeById(id: Long): Flow<EquipmentEntity?> = dao.observeById(id)
    suspend fun getById(id: Long): EquipmentEntity? = dao.getById(id)

    /** Crea (id = 0) o actualiza un equipo del inventario. */
    suspend fun save(
        id: Long,
        type: String,
        brand: String,
        code: String,
        status: EquipmentStatus,
        notes: String
    ): Outcome<Long> {
        val normalizedCode = code.trim().uppercase()
        val existing = dao.findByCode(normalizedCode)
        if (existing != null && existing.id != id) {
            return Outcome.Failure("El código $normalizedCode ya existe en el inventario")
        }
        if (id == 0L) {
            val newId = dao.insert(
                EquipmentEntity(
                    type = type.trim(),
                    brand = brand.trim(),
                    internalCode = normalizedCode,
                    status = EquipmentStatus.DISPONIBLE,
                    notes = notes.trim()
                )
            )
            return Outcome.Success(newId)
        }
        val current = dao.getById(id) ?: return Outcome.Failure("Equipo no encontrado")
        val newStatus = if (current.status == EquipmentStatus.PRESTADO) current.status else status
        dao.update(
            current.copy(
                type = type.trim(),
                brand = brand.trim(),
                internalCode = normalizedCode,
                status = newStatus,
                notes = notes.trim()
            )
        )
        return Outcome.Success(id)
    }
}
