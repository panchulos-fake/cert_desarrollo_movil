package com.duoc.prestaaula.data.repository

import com.duoc.prestaaula.data.local.dao.AvailabilityDao
import com.duoc.prestaaula.data.local.entity.AvailabilityEntity
import kotlinx.coroutines.flow.Flow

class AvailabilityRepository(private val dao: AvailabilityDao) {
    fun observeByTeacher(teacherId: Long): Flow<List<AvailabilityEntity>> = dao.observeByTeacher(teacherId)
    suspend fun add(item: AvailabilityEntity) { dao.insert(item) }
    suspend fun remove(id: Long) { dao.delete(id) }
}
