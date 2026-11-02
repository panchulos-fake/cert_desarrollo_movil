package com.duoc.prestaaula.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.duoc.prestaaula.data.local.entity.Role
import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.nativeaccess.NotificationHelper
import com.duoc.prestaaula.ui.components.appViewModel
import com.duoc.prestaaula.ui.screens.auth.LoginScreen
import com.duoc.prestaaula.ui.screens.auth.RegisterScreen
import com.duoc.prestaaula.ui.screens.equipment.EquipmentDetailScreen
import com.duoc.prestaaula.ui.screens.equipment.EquipmentFormScreen
import com.duoc.prestaaula.ui.screens.equipment.EquipmentListScreen
import com.duoc.prestaaula.ui.screens.loans.LoanDetailScreen
import com.duoc.prestaaula.ui.screens.loans.LoansScreen
import com.duoc.prestaaula.ui.screens.loans.ReturnScreen
import com.duoc.prestaaula.ui.screens.profile.ProfileScreen
import com.duoc.prestaaula.ui.screens.reports.ReportsScreen
import com.duoc.prestaaula.ui.screens.reservation.ReservationScreen
import com.duoc.prestaaula.PrestaAulaApp
import com.duoc.prestaaula.viewmodel.SessionState
import com.duoc.prestaaula.viewmodel.SessionViewModel

@Composable
fun AppRoot() {
    val sessionVm = appViewModel { SessionViewModel(it.userRepository, it.sessionManager) }
    val state by sessionVm.state.collectAsStateWithLifecycle()
    val container = (LocalContext.current.applicationContext as PrestaAulaApp).container
    val seedReady by container.seedReady.collectAsStateWithLifecycle()

    if (!seedReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    AnimatedContent(
        targetState = state,
        contentKey = { it::class },
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "root"
    ) { s ->
        when (s) {
            SessionState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            SessionState.LoggedOut -> AuthNavHost()
            is SessionState.LoggedIn -> MainScaffold(user = s.user, onLogout = sessionVm::logout)
        }
    }
}

@Composable
private fun AuthNavHost() {
    val nav = rememberNavController()
    NavHost(
        navController = nav,
        startDestination = "login",
        enterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(300)) },
        popEnterTransition = { slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
    ) {
        composable("login") { LoginScreen(onRegister = { nav.navigate("register") }) }
        composable("register") { RegisterScreen(onBack = { nav.popBackStack() }) }
    }
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private fun bottomItemsFor(role: Role): List<BottomItem> {
    val equipment = BottomItem(Routes.EQUIPMENT, "Equipos", Icons.Default.Inventory2)
    val profile = BottomItem(Routes.PROFILE, "Perfil", Icons.Default.AccountCircle)
    return when (role) {
        Role.DOCENTE -> listOf(
            equipment,
            BottomItem(Routes.LOANS, "Mis reservas", Icons.Default.EventAvailable),
            profile
        )
        else -> listOf(
            equipment,
            BottomItem(Routes.LOANS, "Préstamos", Icons.AutoMirrored.Filled.Assignment),
            BottomItem(Routes.REPORTS, "Reportes", Icons.Default.BarChart),
            profile
        )
    }
}

@Composable
private fun MainScaffold(user: UserEntity, onLogout: () -> Unit) {
    val context = LocalContext.current
    val nav = rememberNavController()
    val items = remember(user.role) { bottomItemsFor(user.role) }
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val showBottomBar = items.any { it.route == currentRoute }

    // Recurso nativo: permiso de notificaciones (Android 13+), se solicita al ingresar.
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 && !NotificationHelper.canNotify(context)) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                NavigationBar {
                    items.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                // Las pestañas principales siempre regresan a una ruta limpia.
                                // No usar saveState/restoreState: podían reactivar pantallas
                                // descartadas y bloquear el regreso a "Equipos".
                                nav.navigate(item.route) {
                                    popUpTo(Routes.EQUIPMENT) { inclusive = false }
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { inner ->
        NavHost(
            navController = nav,
            startDestination = Routes.EQUIPMENT,
            modifier = Modifier
                .padding(inner)
                .consumeWindowInsets(inner),
            enterTransition = { fadeIn(tween(250)) + slideInHorizontally(tween(250)) { it / 6 } },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(tween(250)) { it / 6 } }
        ) {
            composable(Routes.EQUIPMENT) {
                EquipmentListScreen(
                    user = user,
                    onOpen = { nav.navigate(Routes.equipmentDetail(it)) },
                    onAdd = { nav.navigate(Routes.equipmentForm(0L)) }
                )
            }
            composable(Routes.LOANS) {
                LoansScreen(user = user, onOpen = { nav.navigate(Routes.loanDetail(it)) })
            }
            composable(Routes.REPORTS) { ReportsScreen() }
            composable(Routes.PROFILE) { ProfileScreen(user = user, onLogout = onLogout) }

            composable(
                Routes.EQUIPMENT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                EquipmentDetailScreen(
                    user = user,
                    equipmentId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onReserve = { nav.navigate(Routes.reserve(it)) },
                    onEdit = { nav.navigate(Routes.equipmentForm(it)) }
                )
            }
            composable(
                Routes.EQUIPMENT_FORM,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                EquipmentFormScreen(
                    equipmentId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() }
                )
            }
            composable(
                Routes.RESERVE,
                arguments = listOf(navArgument("equipmentId") { type = NavType.LongType })
            ) { entry ->
                if (user.role == Role.DOCENTE) ReservationScreen(
                    user = user,
                    equipmentId = entry.arguments?.getLong("equipmentId") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onDone = {
                        // Al confirmar una reserva, retiramos la pantalla del historial.
                        // De lo contrario, su ViewModel conserva completedMessage y
                        // vuelve a mostrar el dialogo al regresar a ella.
                        nav.navigate(Routes.LOANS) {
                            popUpTo(Routes.EQUIPMENT) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                Routes.LOAN_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                LoanDetailScreen(
                    user = user,
                    loanId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onReturn = { nav.navigate(Routes.returnLoan(it)) }
                )
            }
            composable(
                Routes.RETURN,
                arguments = listOf(navArgument("loanId") { type = NavType.LongType })
            ) { entry ->
                ReturnScreen(
                    loanId = entry.arguments?.getLong("loanId") ?: 0L,
                    onBack = { nav.popBackStack() },
                    onFinished = { nav.popBackStack(Routes.LOANS, inclusive = false) }
                )
            }
        }
    }
}
