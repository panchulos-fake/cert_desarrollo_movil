package com.duoc.prestaaula.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class Role(val label: String) {
    DOCENTE("Docente"),
    ENCARGADO_CRA("Encargado CRA"),
    JEFE_UTP("Jefe de UTP"),
    JEFE_ADMIN("Jefe de Administración y Finanzas")
}

enum class EquipmentStatus(val label: String) {
    DISPONIBLE("Disponible"),
    PRESTADO("Prestado"),
    MANTENCION("En mantención")
}

enum class LoanStatus(val label: String) {
    RESERVADA("Reservada"),
    RETIRADA("Retirada"),
    DEVUELTA("Devuelta"),
    CANCELADA("Cancelada")
}

enum class ReturnCondition(val label: String) {
    BUEN_ESTADO("Buen estado"),
    CON_DANOS("Con daños"),
    INCOMPLETO("Incompleto (falta accesorio)")
}

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: Role
)

@Entity(tableName = "equipment", indices = [Index(value = ["internalCode"], unique = true)])
data class EquipmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val brand: String,
    val internalCode: String,
    val status: EquipmentStatus = EquipmentStatus.DISPONIBLE,
    val notes: String = ""
) {
    val displayName: String get() = "$type $brand"
}

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(entity = EquipmentEntity::class, parentColumns = ["id"], childColumns = ["equipmentId"]),
        ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["teacherId"])
    ],
    indices = [Index("equipmentId"), Index("teacherId")]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipmentId: Long,
    val teacherId: Long,
    val course: String,
    val dateEpochDay: Long,
    val block: Int,
    val status: LoanStatus = LoanStatus.RESERVADA,
    val createdAt: Long = System.currentTimeMillis(),
    val pickedUpAt: Long? = null,
    val returnedAt: Long? = null,
    val returnCondition: ReturnCondition? = null,
    val observations: String = "",
    val photoPath: String? = null
)

@Entity(
    tableName = "availability",
    foreignKeys = [ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["teacherId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("teacherId")]
)
data class AvailabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherId: Long,
    val dayOfWeek: Int,
    val startBlock: Int,
    val endBlock: Int
)
