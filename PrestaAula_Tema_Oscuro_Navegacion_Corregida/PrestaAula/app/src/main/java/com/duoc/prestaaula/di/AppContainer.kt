package com.duoc.prestaaula.di

import android.content.Context
import com.duoc.prestaaula.data.local.PrestaAulaDatabase
import com.duoc.prestaaula.data.repository.AvailabilityRepository
import com.duoc.prestaaula.data.repository.EquipmentRepository
import com.duoc.prestaaula.data.repository.LoanRepository
import com.duoc.prestaaula.data.repository.UserRepository
import com.duoc.prestaaula.data.seed.SeedData
import com.duoc.prestaaula.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Contenedor de dependencias manual (sin librerías de inyección). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = PrestaAulaDatabase.getInstance(appContext)
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val sessionManager = SessionManager(appContext)
    val userRepository = UserRepository(database.userDao())
    val equipmentRepository = EquipmentRepository(database.equipmentDao())
    val loanRepository = LoanRepository(database, database.loanDao(), database.equipmentDao())
    val availabilityRepository = AvailabilityRepository(database.availabilityDao())

    // El usuario no debe intentar iniciar sesión antes de crear las cuentas de demostración.
    private val _seedReady = MutableStateFlow(false)
    val seedReady = _seedReady.asStateFlow()

    fun seedIfEmpty() {
        appScope.launch {
            SeedData.insertIfEmpty(database)
            _seedReady.value = true
        }
    }
}
