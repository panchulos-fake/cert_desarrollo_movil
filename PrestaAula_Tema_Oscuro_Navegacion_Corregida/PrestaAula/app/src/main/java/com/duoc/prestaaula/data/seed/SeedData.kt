package com.duoc.prestaaula.data.seed

import com.duoc.prestaaula.data.local.PrestaAulaDatabase
import com.duoc.prestaaula.data.local.entity.EquipmentEntity
import com.duoc.prestaaula.data.local.entity.Role
import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.util.PasswordHasher

/** Datos iniciales de demostración (se insertan solo si la base está vacía). */
object SeedData {
    suspend fun insertIfEmpty(db: PrestaAulaDatabase) {
        val users = db.userDao()
        if (users.count() == 0) {
            listOf(
                UserEntity(name = "Marcela Soto", email = "cra@marcelapaz.cl", passwordHash = PasswordHasher.hash("Cra12345"), role = Role.ENCARGADO_CRA),
                UserEntity(name = "Carlos Rojas", email = "docente@marcelapaz.cl", passwordHash = PasswordHasher.hash("Docente123"), role = Role.DOCENTE),
                UserEntity(name = "Ana Pérez", email = "docente2@marcelapaz.cl", passwordHash = PasswordHasher.hash("Docente123"), role = Role.DOCENTE),
                UserEntity(name = "Luis Fuentes", email = "utp@marcelapaz.cl", passwordHash = PasswordHasher.hash("Utp12345"), role = Role.JEFE_UTP),
                UserEntity(name = "Patricia Mella", email = "admin@marcelapaz.cl", passwordHash = PasswordHasher.hash("Admin12345"), role = Role.JEFE_ADMIN)
            ).forEach { users.insert(it) }
        }
        val equipment = db.equipmentDao()
        if (equipment.count() == 0) {
            listOf(
                EquipmentEntity(type = "Notebook", brand = "Lenovo", internalCode = "NB-001"),
                EquipmentEntity(type = "Notebook", brand = "HP", internalCode = "NB-002"),
                EquipmentEntity(type = "Notebook", brand = "Acer", internalCode = "NB-003"),
                EquipmentEntity(type = "Tablet", brand = "Samsung", internalCode = "TB-001"),
                EquipmentEntity(type = "Tablet", brand = "Samsung", internalCode = "TB-002"),
                EquipmentEntity(type = "Proyector", brand = "Epson", internalCode = "PY-001"),
                EquipmentEntity(type = "Proyector", brand = "BenQ", internalCode = "PY-002"),
                EquipmentEntity(type = "Cable HDMI", brand = "Genérico", internalCode = "CH-001")
            ).forEach { equipment.insert(it) }
        }
    }
}
