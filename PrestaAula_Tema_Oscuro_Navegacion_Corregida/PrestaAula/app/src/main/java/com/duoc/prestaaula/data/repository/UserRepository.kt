package com.duoc.prestaaula.data.repository

import com.duoc.prestaaula.data.local.dao.UserDao
import com.duoc.prestaaula.data.local.entity.Role
import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.domain.model.Outcome
import com.duoc.prestaaula.util.PasswordHasher
import kotlinx.coroutines.flow.Flow

class UserRepository(private val dao: UserDao) {
    fun observeById(id: Long): Flow<UserEntity?> = dao.observeById(id)

    suspend fun login(email: String, password: String): Outcome<UserEntity> {
        val user = dao.findByEmail(email.trim().lowercase())
        return if (user == null || user.passwordHash != PasswordHasher.hash(password)) {
            Outcome.Failure("Correo o contraseña incorrectos")
        } else {
            Outcome.Success(user)
        }
    }

    suspend fun registerTeacher(name: String, email: String, password: String): Outcome<Long> {
        val normalized = email.trim().lowercase()
        if (dao.findByEmail(normalized) != null) {
            return Outcome.Failure("Este correo ya está registrado")
        }
        val id = dao.insert(
            UserEntity(
                name = name.trim(),
                email = normalized,
                passwordHash = PasswordHasher.hash(password),
                role = Role.DOCENTE
            )
        )
        return Outcome.Success(id)
    }
}
