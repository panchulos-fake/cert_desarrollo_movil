package com.duoc.prestaaula.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.duoc.prestaaula.data.local.entity.AvailabilityEntity
import com.duoc.prestaaula.data.local.entity.EquipmentEntity
import com.duoc.prestaaula.data.local.entity.EquipmentStats
import com.duoc.prestaaula.data.local.entity.LoanDetail
import com.duoc.prestaaula.data.local.entity.LoanEntity
import com.duoc.prestaaula.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeById(id: Long): Flow<UserEntity?>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}

@Dao
interface EquipmentDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(equipment: EquipmentEntity): Long

    @Update
    suspend fun update(equipment: EquipmentEntity)

    @Query("SELECT * FROM equipment ORDER BY type, internalCode")
    fun observeAll(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment WHERE id = :id")
    fun observeById(id: Long): Flow<EquipmentEntity?>

    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun getById(id: Long): EquipmentEntity?

    @Query("SELECT * FROM equipment WHERE internalCode = :code LIMIT 1")
    suspend fun findByCode(code: String): EquipmentEntity?

    @Query("SELECT COUNT(*) FROM equipment")
    suspend fun count(): Int
}

@Dao
interface LoanDao {
    @Insert
    suspend fun insert(loan: LoanEntity): Long

    @Update
    suspend fun update(loan: LoanEntity)

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getById(id: Long): LoanEntity?

    @Query(
        "SELECT l.*, e.type AS equipmentType, e.brand AS equipmentBrand, e.internalCode AS equipmentCode, u.name AS teacherName " +
            "FROM loans l INNER JOIN equipment e ON e.id = l.equipmentId INNER JOIN users u ON u.id = l.teacherId " +
            "WHERE l.id = :id"
    )
    fun observeDetail(id: Long): Flow<LoanDetail?>

    @Query(
        "SELECT l.*, e.type AS equipmentType, e.brand AS equipmentBrand, e.internalCode AS equipmentCode, u.name AS teacherName " +
            "FROM loans l INNER JOIN equipment e ON e.id = l.equipmentId INNER JOIN users u ON u.id = l.teacherId " +
            "ORDER BY l.dateEpochDay DESC, l.block DESC"
    )
    fun observeAllDetails(): Flow<List<LoanDetail>>

    @Query(
        "SELECT l.*, e.type AS equipmentType, e.brand AS equipmentBrand, e.internalCode AS equipmentCode, u.name AS teacherName " +
            "FROM loans l INNER JOIN equipment e ON e.id = l.equipmentId INNER JOIN users u ON u.id = l.teacherId " +
            "WHERE l.teacherId = :teacherId ORDER BY l.dateEpochDay DESC, l.block DESC"
    )
    fun observeByTeacher(teacherId: Long): Flow<List<LoanDetail>>

    @Query(
        "SELECT l.*, e.type AS equipmentType, e.brand AS equipmentBrand, e.internalCode AS equipmentCode, u.name AS teacherName " +
            "FROM loans l INNER JOIN equipment e ON e.id = l.equipmentId INNER JOIN users u ON u.id = l.teacherId " +
            "WHERE l.equipmentId = :equipmentId ORDER BY l.dateEpochDay DESC, l.block DESC"
    )
    fun observeByEquipment(equipmentId: Long): Flow<List<LoanDetail>>

    @Query(
        "SELECT l.*, e.type AS equipmentType, e.brand AS equipmentBrand, e.internalCode AS equipmentCode, u.name AS teacherName " +
            "FROM loans l INNER JOIN equipment e ON e.id = l.equipmentId INNER JOIN users u ON u.id = l.teacherId " +
            "WHERE l.dateEpochDay = :date AND l.status IN ('RESERVADA','RETIRADA')"
    )
    fun observeActiveByDate(date: Long): Flow<List<LoanDetail>>

    @Query(
        "SELECT l.*, e.type AS equipmentType, e.brand AS equipmentBrand, e.internalCode AS equipmentCode, u.name AS teacherName " +
            "FROM loans l INNER JOIN equipment e ON e.id = l.equipmentId INNER JOIN users u ON u.id = l.teacherId " +
            "WHERE l.returnCondition IS NOT NULL AND l.returnCondition != 'BUEN_ESTADO' ORDER BY l.returnedAt DESC"
    )
    fun observeIncidents(): Flow<List<LoanDetail>>

    @Query(
        "SELECT COUNT(*) FROM loans WHERE equipmentId = :equipmentId AND dateEpochDay = :date " +
            "AND block = :block AND status IN ('RESERVADA','RETIRADA')"
    )
    suspend fun countEquipmentConflicts(equipmentId: Long, date: Long, block: Int): Int

    @Query(
        "SELECT COUNT(*) FROM loans WHERE teacherId = :teacherId AND dateEpochDay = :date " +
            "AND block = :block AND status IN ('RESERVADA','RETIRADA')"
    )
    suspend fun countTeacherConflicts(teacherId: Long, date: Long, block: Int): Int

    @Query(
        "SELECT e.id AS equipmentId, e.type AS type, e.brand AS brand, e.internalCode AS internalCode, " +
            "COUNT(l.id) AS total, " +
            "COALESCE(SUM(CASE WHEN l.returnCondition IS NOT NULL AND l.returnCondition != 'BUEN_ESTADO' THEN 1 ELSE 0 END), 0) AS incidents " +
            "FROM equipment e LEFT JOIN loans l ON l.equipmentId = e.id AND l.status != 'CANCELADA' " +
            "GROUP BY e.id ORDER BY incidents DESC, total DESC"
    )
    fun observeStats(): Flow<List<EquipmentStats>>

    @Query("SELECT COUNT(*) FROM loans WHERE status IN ('RESERVADA','RETIRADA')")
    fun observeActiveCount(): Flow<Int>
}

@Dao
interface AvailabilityDao {
    @Insert
    suspend fun insert(item: AvailabilityEntity): Long

    @Query("DELETE FROM availability WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM availability WHERE teacherId = :teacherId ORDER BY dayOfWeek, startBlock")
    fun observeByTeacher(teacherId: Long): Flow<List<AvailabilityEntity>>
}
