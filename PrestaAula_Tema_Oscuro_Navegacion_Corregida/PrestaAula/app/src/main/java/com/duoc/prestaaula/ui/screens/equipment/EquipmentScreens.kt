package com.duoc.prestaaula.ui.screens.equipment

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

import com.duoc.prestaaula.data.local.entity.EquipmentStatus
import com.duoc.prestaaula.data.local.entity.Role
import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.domain.model.EquipmentTypes
import com.duoc.prestaaula.domain.model.TimeBlock
import com.duoc.prestaaula.domain.model.TimeBlocks
import com.duoc.prestaaula.ui.components.*
import com.duoc.prestaaula.ui.theme.StatusColors
import com.duoc.prestaaula.util.DateUtils
import com.duoc.prestaaula.viewmodel.EquipmentDetailViewModel
import com.duoc.prestaaula.viewmodel.EquipmentFormViewModel
import com.duoc.prestaaula.viewmodel.EquipmentItem
import com.duoc.prestaaula.viewmodel.EquipmentListViewModel
import com.duoc.prestaaula.viewmodel.SlotAvailability

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentListScreen(
    user: UserEntity,
    onOpen: (Long) -> Unit,
    onAdd: () -> Unit
) {
    val vm = appViewModel { EquipmentListViewModel(it.equipmentRepository, it.loanRepository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val isCra = user.role == Role.ENCARGADO_CRA

    Scaffold(
        topBar = { AppTopBar("Equipos disponibles") },
        floatingActionButton = {
            if (isCra) {
                ExtendedFloatingActionButton(
                    onClick = onAdd,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Nuevo equipo") }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Hola, ${user.name.substringBefore(' ')} · ${user.role.label}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DatePickerField(
                            label = "Fecha",
                            epochDay = ui.date,
                            onDateSelected = vm::onDateChange,
                            minEpochDay = DateUtils.today()
                        )
                        AppDropdown(
                            label = "Bloque horario",
                            selectedText = TimeBlocks.label(ui.block),
                            options = TimeBlocks.all,
                            optionLabel = { b: TimeBlock -> b.label },
                            onSelected = { vm.onBlockChange(it.number) },
                            leadingIcon = Icons.Default.Schedule
                        )
                        OutlinedTextField(
                            value = ui.query,
                            onValueChange = vm::onQueryChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Buscar por tipo, marca o código") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (ui.query.isNotEmpty()) {
                                    IconButton(onClick = { vm.onQueryChange("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar búsqueda")
                                    }
                                }
                            },
                            singleLine = true
                        )
                    }
                }
            }
            item {
                Text(
                    "${ui.freeCount} de ${ui.items.size} equipos libres en este bloque",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (ui.items.isEmpty()) {
                item {
                    EmptyState(
                        Icons.Default.SearchOff,
                        "Sin resultados",
                        "No hay equipos que coincidan con la búsqueda."
                    )
                }
            }
            items(ui.items, key = { it.equipment.id }) { item ->
                EquipmentCard(item, onClick = { onOpen(item.equipment.id) }, modifier = Modifier.animateItem())
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentCard(item: EquipmentItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val e = item.equipment
    val (label, color) = when (item.slot) {
        SlotAvailability.LIBRE -> "Libre" to StatusColors.Success
        SlotAvailability.OCUPADO -> "Reservado" to StatusColors.Warning
        SlotAvailability.MANTENCION -> "En mantención" to StatusColors.Danger
    }
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(equipmentIcon(e.type), contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(e.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "Código ${e.internalCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.holder != null) {
                    Text(
                        "Reservado por ${item.holder}",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusColors.Warning
                    )
                }
            }
            StatusChip(label, color)
        }
    }
}

@Composable
fun EquipmentDetailScreen(
    user: UserEntity,
    equipmentId: Long,
    onBack: () -> Unit,
    onReserve: (Long) -> Unit,
    onEdit: (Long) -> Unit
) {
    val vm = appViewModel { EquipmentDetailViewModel(it.equipmentRepository, it.loanRepository, equipmentId) }
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val equipment = ui.equipment

    Scaffold(
        topBar = {
            AppTopBar(
                title = equipment?.displayName ?: "Detalle del equipo",
                onBack = onBack,
                actions = {
                    if (user.role == Role.ENCARGADO_CRA && equipment != null) {
                        IconButton(onClick = { onEdit(equipment.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar equipo")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (equipment == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (ui.loaded) Text("Equipo no encontrado") else CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                AppearAnimated {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(equipmentIcon(equipment.type), contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(equipment.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    Text("Código ${equipment.internalCode}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusChip(equipment.status.label, equipment.status.color())
                            }
                            Spacer(Modifier.height(8.dp))
                            InfoRow(Icons.Default.Category, "Tipo", equipment.type)
                            InfoRow(Icons.Default.Sell, "Marca", equipment.brand)
                            if (equipment.notes.isNotBlank()) InfoRow(Icons.Default.Notes, "Notas", equipment.notes)
                            val last = ui.lastResponsible
                            InfoRow(
                                Icons.Default.Person,
                                "Último responsable",
                                if (last == null) "Sin préstamos registrados"
                                else "${last.teacherName} · ${DateUtils.format(last.loan.dateEpochDay)}"
                            )
                        }
                    }
                }
            }
            // Solo el perfil docente puede crear reservas a su propio nombre.
            if (user.role == Role.DOCENTE) {
                item {
                    LoadingButton(
                        text = if (equipment.status == EquipmentStatus.MANTENCION) "En mantención: no se puede reservar" else "Reservar este equipo",
                        loading = false,
                        onClick = { onReserve(equipment.id) },
                        icon = Icons.Default.EventAvailable,
                        enabled = equipment.status != EquipmentStatus.MANTENCION
                    )
                }
            }
            item { SectionTitle("Historial de préstamos") }
            if (ui.history.isEmpty()) {
                item { EmptyState(Icons.Default.History, "Sin historial", "Este equipo aún no registra préstamos.") }
            }
            items(ui.history, key = { it.loan.id }) { h ->
                Card(Modifier.fillMaxWidth().animateItem(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(h.teacherName, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${DateUtils.format(h.loan.dateEpochDay)} · ${TimeBlocks.label(h.loan.block)} · ${h.loan.course}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            h.loan.returnCondition?.let {
                                Text("Devuelto: ${it.label}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        StatusChip(h.loan.status.label, h.loan.status.color())
                    }
                }
            }
        }
    }
}

@Composable
fun EquipmentFormScreen(equipmentId: Long, onBack: () -> Unit) {
    val vm = appViewModel { EquipmentFormViewModel(it.equipmentRepository, equipmentId) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(ui.saved) {
        if (ui.saved) {
            snackbar.showSnackbar("Equipo guardado correctamente")
            onBack()
        }
    }

    Scaffold(
        topBar = { AppTopBar(if (ui.isEdit) "Editar equipo" else "Nuevo equipo", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppDropdown(
                label = "Tipo de equipo",
                selectedText = ui.type,
                options = EquipmentTypes.all,
                optionLabel = { t: String -> t },
                onSelected = vm::onTypeChange,
                leadingIcon = Icons.Default.Category,
                error = ui.typeError
            )
            ValidatedTextField(
                value = ui.brand,
                onValueChange = vm::onBrandChange,
                label = "Marca",
                leadingIcon = Icons.Default.Sell,
                error = ui.brandError
            )
            ValidatedTextField(
                value = ui.code,
                onValueChange = vm::onCodeChange,
                label = "Código interno",
                leadingIcon = Icons.Default.QrCode,
                error = ui.codeError,
                helper = "Formato: 2 letras, guion y 3 dígitos (ej. NB-001)"
            )
            if (ui.isEdit) {
                val locked = ui.currentStatus == EquipmentStatus.PRESTADO
                AppDropdown(
                    label = "Estado",
                    selectedText = ui.status.label,
                    options = EquipmentStatus.entries.filter { it != EquipmentStatus.PRESTADO },
                    optionLabel = { s: EquipmentStatus -> s.label },
                    onSelected = vm::onStatusChange,
                    leadingIcon = Icons.Default.Build,
                    helper = if (locked) "Prestado: el estado cambia al registrar la devolución" else null,
                    enabled = !locked
                )
            }
            ValidatedTextField(
                value = ui.notes,
                onValueChange = vm::onNotesChange,
                label = "Notas (opcional)",
                leadingIcon = Icons.Default.Notes,
                error = ui.notesError,
                singleLine = false,
                minLines = 2,
                imeAction = ImeAction.Done
            )
            FeedbackBanner(ui.generalError)
            LoadingButton(
                text = if (ui.isEdit) "Guardar cambios" else "Agregar al inventario",
                loading = ui.loading,
                onClick = vm::submit,
                icon = Icons.Default.Save
            )
        }
    }
}
