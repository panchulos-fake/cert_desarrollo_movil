package com.duoc.prestaaula.ui.screens.loans

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
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.duoc.prestaaula.data.local.entity.LoanDetail
import com.duoc.prestaaula.data.local.entity.LoanStatus
import com.duoc.prestaaula.data.local.entity.Role
import com.duoc.prestaaula.data.local.entity.ReturnCondition
import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.domain.model.TimeBlocks
import com.duoc.prestaaula.nativeaccess.CameraHelper
import com.duoc.prestaaula.nativeaccess.NotificationHelper
import com.duoc.prestaaula.ui.components.*
import com.duoc.prestaaula.ui.theme.StatusColors
import com.duoc.prestaaula.util.DateUtils
import com.duoc.prestaaula.viewmodel.LoanDetailViewModel
import com.duoc.prestaaula.viewmodel.LoanFilter
import com.duoc.prestaaula.viewmodel.LoansViewModel
import com.duoc.prestaaula.viewmodel.ReturnViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(user: UserEntity, onOpen: (Long) -> Unit) {
    val vm = appViewModel { LoansViewModel(it.loanRepository, user) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val title = if (user.role == Role.DOCENTE) "Mis reservas" else "Préstamos"

    Scaffold(topBar = { AppTopBar(title) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(LoanFilter.entries.toList()) { f ->
                    FilterChip(
                        selected = ui.filter == f,
                        onClick = { vm.onFilterChange(f) },
                        label = { Text(f.label) },
                        leadingIcon = if (ui.filter == f) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
            if (ui.loans.isEmpty()) {
                EmptyState(
                    Icons.Default.Inbox,
                    "Sin registros",
                    if (user.role == Role.DOCENTE) "Reserva un equipo desde la pestaña Equipos." else "Aún no hay préstamos con este filtro."
                )
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ui.loans, key = { it.loan.id }) { d ->
                    LoanCard(d, showTeacher = user.role != Role.DOCENTE, onClick = { onOpen(d.loan.id) }, modifier = Modifier.animateItem())
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanCard(d: LoanDetail, showTeacher: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(equipmentIcon(d.equipmentType), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${d.equipmentName} (${d.equipmentCode})", fontWeight = FontWeight.SemiBold)
                Text(
                    "${DateUtils.format(d.loan.dateEpochDay)} · Bloque ${d.loan.block} · ${d.loan.course}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (showTeacher) {
                    Text(d.teacherName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            StatusChip(d.loan.status.label, d.loan.status.color())
        }
    }
}

@Composable
fun LoanDetailScreen(
    user: UserEntity,
    loanId: Long,
    onBack: () -> Unit,
    onReturn: (Long) -> Unit
) {
    val vm = appViewModel { LoanDetailViewModel(it.loanRepository, loanId) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var confirmCancel by remember { mutableStateOf(false) }

    LaunchedEffect(ui.message) {
        ui.message?.let {
            snackbar.showSnackbar(it)
            vm.clearMessage()
        }
    }
    LaunchedEffect(ui.pickupDone) {
        val d = ui.detail
        if (ui.pickupDone && d != null) {
            NotificationHelper.show(context, "Retiro registrado", "${d.equipmentName} (${d.equipmentCode}) retirado por ${d.teacherName}.")
        }
    }

    Scaffold(
        topBar = { AppTopBar("Detalle del préstamo", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val d = ui.detail
        if (d == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (ui.loaded) Text("Préstamo no encontrado") else CircularProgressIndicator()
            }
            return@Scaffold
        }
        val canManage = user.role == Role.ENCARGADO_CRA || (user.role == Role.DOCENTE && d.loan.teacherId == user.id)
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppearAnimated {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${d.equipmentName} (${d.equipmentCode})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusChip(d.loan.status.label, d.loan.status.color())
                        }
                        Spacer(Modifier.height(8.dp))
                        InfoRow(Icons.Default.Person, "Docente responsable", d.teacherName)
                        InfoRow(Icons.Default.Groups, "Curso", d.loan.course)
                        InfoRow(Icons.Default.Event, "Fecha", DateUtils.format(d.loan.dateEpochDay))
                        InfoRow(Icons.Default.Schedule, "Bloque", TimeBlocks.label(d.loan.block))
                        d.loan.pickedUpAt?.let { InfoRow(Icons.Default.Login, "Retirado el", DateUtils.formatMillis(it)) }
                        d.loan.returnedAt?.let { InfoRow(Icons.Default.AssignmentReturn, "Devuelto el", DateUtils.formatMillis(it)) }
                        d.loan.returnCondition?.let { InfoRow(Icons.Default.FactCheck, "Estado al devolver", it.label) }
                        if (d.loan.observations.isNotBlank()) InfoRow(Icons.Default.Notes, "Observaciones", d.loan.observations)
                    }
                }
            }
            d.loan.photoPath?.let { path ->
                SectionTitle("Fotografía de evidencia")
                AsyncImage(
                    model = File(path),
                    contentDescription = "Fotografía del estado del equipo al devolverlo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp))
                )
            }
            if (canManage && d.loan.status == LoanStatus.RESERVADA) {
                LoadingButton("Registrar retiro", ui.working, vm::pickup, icon = Icons.Default.Login)
                OutlinedButton(
                    onClick = { confirmCancel = true },
                    enabled = !ui.working,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cancelar reserva")
                }
            }
            if (canManage && d.loan.status == LoanStatus.RETIRADA) {
                LoadingButton("Registrar devolución", false, { onReturn(d.loan.id) }, icon = Icons.Default.AssignmentReturn)
            }
            if (!canManage && (d.loan.status == LoanStatus.RESERVADA || d.loan.status == LoanStatus.RETIRADA)) {
                Text(
                    "Solo el docente responsable o el encargado del CRA pueden gestionar este préstamo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (confirmCancel) {
        AlertDialog(
            onDismissRequest = { confirmCancel = false },
            title = { Text("¿Cancelar la reserva?") },
            text = { Text("El bloque quedará libre para otros docentes.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmCancel = false
                    vm.cancel()
                }) { Text("Sí, cancelar") }
            },
            dismissButton = { TextButton(onClick = { confirmCancel = false }) { Text("Volver") } }
        )
    }
}

@Composable
fun ReturnScreen(loanId: Long, onBack: () -> Unit, onFinished: () -> Unit) {
    val vm = appViewModel { ReturnViewModel(it.loanRepository, loanId) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Recurso nativo: cámara (captura con TakePicture + permiso en tiempo de ejecución).
    var pendingPath by rememberSaveable { mutableStateOf<String?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = pendingPath
        if (ok && path != null) {
            vm.onPhotoCaptured(path)
        } else {
            path?.let { File(it).delete() }
            vm.onCameraMessage("No se capturó la fotografía")
        }
    }
    val launchCamera = {
        val file = CameraHelper.createPhotoFile(context)
        pendingPath = file.absolutePath
        takePicture.launch(CameraHelper.uriFor(context, file))
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) launchCamera() else vm.onCameraMessage("Permiso de cámara denegado. Actívalo en Ajustes para adjuntar evidencia.")
    }

    LaunchedEffect(ui.completed) {
        val notice = ui.incidentNotice
        if (ui.completed && notice != null) {
            NotificationHelper.show(context, "Incidencia registrada", notice)
        }
    }

    Scaffold(topBar = { AppTopBar("Registrar devolución", onBack = onBack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ui.detail?.let { d ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${d.equipmentName} (${d.equipmentCode})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Responsable: ${d.teacherName} · ${d.loan.course}")
                    }
                }
            }
            SectionTitle("Estado del equipo")
            ReturnCondition.entries.forEach { c ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = ui.condition == c, onClick = { vm.onConditionChange(c) })
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = ui.condition == c, onClick = { vm.onConditionChange(c) })
                    Text(c.label)
                }
            }
            ValidatedTextField(
                value = ui.observations,
                onValueChange = vm::onObservationsChange,
                label = if (ui.observationsRequired) "Observaciones (obligatorio)" else "Observaciones (opcional)",
                leadingIcon = Icons.Default.Notes,
                error = ui.observationsError,
                singleLine = false,
                minLines = 3,
                imeAction = ImeAction.Done
            )

            AnimatedVisibility(visible = ui.condition != ReturnCondition.BUEN_ESTADO) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle(if (ui.photoRequired) "Fotografía del daño (obligatoria)" else "Fotografía (opcional)")
                    AnimatedContent(targetState = ui.photoPath, label = "photo") { path ->
                        if (path == null) {
                            OutlinedButton(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        launchCamera()
                                    } else {
                                        cameraPermission.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Tomar fotografía")
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = "Fotografía capturada",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(16.dp))
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { launchCamera() }, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Repetir")
                                    }
                                    OutlinedButton(onClick = vm::onPhotoRemoved, modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Default.Delete, contentDescription = null)
                                        Spacer(Modifier.width(6.dp))
                                        Text("Quitar")
                                    }
                                }
                            }
                        }
                    }
                    ui.photoError?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            FeedbackBanner(ui.cameraMessage)
            FeedbackBanner(ui.generalError)
            LoadingButton(
                text = "Confirmar devolución",
                loading = ui.loading,
                onClick = vm::submit,
                icon = Icons.Default.AssignmentReturn
            )
        }
    }

    if (ui.completed) {
        AlertDialog(
            onDismissRequest = onFinished,
            icon = {
                Icon(
                    if (ui.incidentNotice == null) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (ui.incidentNotice == null) StatusColors.Success else StatusColors.Warning,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Devolución registrada") },
            text = {
                Text(ui.incidentNotice ?: "El equipo quedó disponible nuevamente en el inventario.")
            },
            confirmButton = { TextButton(onClick = onFinished) { Text("Aceptar") } }
        )
    }
}
