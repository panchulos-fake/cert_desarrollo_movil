package com.duoc.prestaaula.data.repository

import androidx.room.withTransaction
import com.duoc.prestaaula.data.local.PrestaAulaDatabase
import com.duoc.prestaaula.data.local.dao.EquipmentDao
import com.duoc.prestaaula.data.local.dao.LoanDao
import com.duoc.prestaaula.data.local.entity.EquipmentStats
import com.duoc.prestaaula.data.local.entity.EquipmentStatus
import com.duoc.prestaaula.data.local.entity.LoanDetail
import com.duoc.prestaaula.data.local.entity.LoanEntity
import com.duoc.prestaaula.data.local.entity.LoanStatus
import com.duoc.prestaaula.data.local.entity.ReturnCondition
import com.duoc.prestaaula.domain.model.Outcome
import com.duoc.prestaaula.domain.model.TimeBlocks
import com.duoc.prestaaula.domain.validation.Validators
import com.duoc.prestaaula.util.DateUtils
import kotlinx.coroutines.flow.Flow

/**
 * Reglas de negocio de reservas y préstamos. Cada movimiento actualiza el inventario
 * en la misma transacción, de modo que la disponibilidad siempre esté al día.
 */
class LoanRepository(
    private val db: PrestaAulaDatabase,
    private val loanDao: LoanDao,
    private val equipmentDao: EquipmentDao
) {
    fun observeDetail(id: Long): Flow<LoanDetail?> = loanDao.observeDetail(id)
    fun observeAllDetails(): Flow<List<LoanDetail>> = loanDao.observeAllDetails()
    fun observeByTeacher(teacherId: Long): Flow<List<LoanDetail>> = loanDao.observeByTeacher(teacherId)
    fun observeByEquipment(equipmentId: Long): Flow<List<LoanDetail>> = loanDao.observeByEquipment(equipmentId)
    fun observeActiveByDate(date: Long): Flow<List<LoanDetail>> = loanDao.observeActiveByDate(date)
    fun observeIncidents(): Flow<List<LoanDetail>> = loanDao.observeIncidents()
    fun observeStats(): Flow<List<EquipmentStats>> = loanDao.observeStats()
    fun observeActiveCount(): Flow<Int> = loanDao.observeActiveCount()

    /** Retorna un mensaje si el bloque no está disponible, o null si está libre. */
    suspend fun checkSlot(equipmentId: Long, teacherId: Long, date: Long, block: Int): String? {
        if (loanDao.countEquipmentConflicts(equipmentId, date, block) > 0) {
            return "Este equipo ya está reservado en ese bloque"
        }
        if (loanDao.countTeacherConflicts(teacherId, date, block) > 0) {
            return "Ya tienes otra reserva en ese mismo bloque"
        }
        return null
    }

    suspend fun reserve(
        equipmentId: Long,
        teacherId: Long,
        course: String,
        date: Long,
        block: Int
    ): Outcome<Long> = db.withTransaction {
        val equipment = equipmentDao.getById(equipmentId)
            ?: return@withTransaction Outcome.Failure("Equipo no encontrado")
        if (equipment.status == EquipmentStatus.MANTENCION) {
            return@withTransaction Outcome.Failure("El equipo está en mantención y no se puede reservar")
        }
        Validators.reservationDate(date, DateUtils.today())?.let {
            return@withTransaction Outcome.Failure(it)
        }
        Validators.course(course)?.let {
            return@withTransaction Outcome.Failure(it)
        }
        if (TimeBlocks.all.none { it.number == block }) {
            return@withTransaction Outcome.Failure("Bloque horario no válido")
        }
        if (equipment.status == EquipmentStatus.PRESTADO && date == DateUtils.today()) {
            return@withTransaction Outcome.Failure("El equipo todavía está prestado y no puede reservarse hoy")
        }
        val conflict = checkSlot(equipmentId, teacherId, date, block)
        if (conflict != null) return@withTransaction Outcome.Failure(conflict)
        val id = loanDao.insert(
            LoanEntity(
                equipmentId = equipmentId,
                teacherId = teacherId,
                course = course.trim(),
                dateEpochDay = date,
                block = block
            )
        )
        Outcome.Success(id)
    }

    suspend fun pickup(loanId: Long): Outcome<Unit> = db.withTransaction {
        val loan = loanDao.getById(loanId)
            ?: return@withTransaction Outcome.Failure("Préstamo no encontrado")
        if (loan.status != LoanStatus.RESERVADA) {
            return@withTransaction Outcome.Failure("Solo se pueden retirar reservas activas")
        }
        if (loan.dateEpochDay > DateUtils.today()) {
            return@withTransaction Outcome.Failure(
                "El retiro se habilita desde el día de la reserva (${DateUtils.format(loan.dateEpochDay)})"
            )
        }
        val equipment = equipmentDao.getById(loan.equipmentId)
            ?: return@withTransaction Outcome.Failure("Equipo no encontrado")
        if (equipment.status == EquipmentStatus.PRESTADO) {
            return@withTransaction Outcome.Failure("El equipo aún no ha sido devuelto por el responsable anterior")
        }
        if (equipment.status == EquipmentStatus.MANTENCION) {
            return@withTransaction Outcome.Failure("El equipo está en mantención")
        }
        loanDao.update(loan.copy(status = LoanStatus.RETIRADA, pickedUpAt = System.currentTimeMillis()))
        equipmentDao.update(equipment.copy(status = EquipmentStatus.PRESTADO))
        Outcome.Success(Unit)
    }

    /** Retorna Success(true) si el equipo quedó con incidencia (pasa a mantención). */
    suspend fun returnLoan(
        loanId: Long,
        condition: ReturnCondition,
        observations: String,
        photoPath: String?
    ): Outcome<Boolean> = db.withTransaction {
        val loan = loanDao.getById(loanId)
            ?: return@withTransaction Outcome.Failure("Préstamo no encontrado")
        if (loan.status != LoanStatus.RETIRADA) {
            return@withTransaction Outcome.Failure("Solo se pueden devolver equipos que fueron retirados")
        }
        val equipment = equipmentDao.getById(loan.equipmentId)
            ?: return@withTransaction Outcome.Failure("Equipo no encontrado")
        val requiresObservations = condition != ReturnCondition.BUEN_ESTADO
        val requiresPhoto = condition == ReturnCondition.CON_DANOS
        Validators.observations(observations, requiresObservations)?.let {
            return@withTransaction Outcome.Failure(it)
        }
        Validators.photo(photoPath, requiresPhoto)?.let {
            return@withTransaction Outcome.Failure(it)
        }
        loanDao.update(
            loan.copy(
                status = LoanStatus.DEVUELTA,
                returnedAt = System.currentTimeMillis(),
                returnCondition = condition,
                observations = observations.trim(),
                photoPath = photoPath
            )
        )
        val damaged = condition != ReturnCondition.BUEN_ESTADO
        equipmentDao.update(
            equipment.copy(status = if (damaged) EquipmentStatus.MANTENCION else EquipmentStatus.DISPONIBLE)
        )
        Outcome.Success(damaged)
    }

    suspend fun cancel(loanId: Long): Outcome<Unit> = db.withTransaction {
        val loan = loanDao.getById(loanId)
            ?: return@withTransaction Outcome.Failure("Reserva no encontrada")
        if (loan.status != LoanStatus.RESERVADA) {
            return@withTransaction Outcome.Failure("Solo se pueden cancelar reservas pendientes de retiro")
        }
        loanDao.update(loan.copy(status = LoanStatus.CANCELADA))
        Outcome.Success(Unit)
    }
}
