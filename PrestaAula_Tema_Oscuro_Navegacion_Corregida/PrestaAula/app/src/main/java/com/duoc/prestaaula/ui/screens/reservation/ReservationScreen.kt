package com.duoc.prestaaula.ui.screens.reservation

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

import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.domain.model.TimeBlock
import com.duoc.prestaaula.domain.model.TimeBlocks
import com.duoc.prestaaula.nativeaccess.NotificationHelper
import com.duoc.prestaaula.ui.components.*
import com.duoc.prestaaula.ui.theme.StatusColors
import com.duoc.prestaaula.util.DateUtils
import com.duoc.prestaaula.viewmodel.ReservationViewModel

@Composable
fun ReservationScreen(
    user: UserEntity,
    equipmentId: Long,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val vm = appViewModel { ReservationViewModel(it.equipmentRepository, it.loanRepository, equipmentId, user.id) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(ui.completedMessage) {
        ui.completedMessage?.let { NotificationHelper.show(context, "Reserva confirmada", it) }
    }

    Scaffold(topBar = { AppTopBar("Reservar equipo", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val e = ui.equipment
            if (e == null) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            } else {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(equipmentIcon(e.type), contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(e.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Código ${e.internalCode}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                DatePickerField(
                    label = "Fecha de uso",
                    epochDay = ui.date,
                    onDateSelected = vm::onDateChange,
                    error = ui.dateError,
                    minEpochDay = DateUtils.today()
                )
                AppDropdown(
                    label = "Bloque horario",
                    selectedText = TimeBlocks.label(ui.block),
                    options = TimeBlocks.all,
                    optionLabel = { b: TimeBlock -> b.label },
                    onSelected = { vm.onBlockChange(it.number) },
                    leadingIcon = Icons.Default.Schedule,
                    error = ui.blockError,
                    helper = if (ui.blockOk) "Bloque disponible para reservar" else null
                )
                AnimatedVisibility(visible = ui.blockOk) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusColors.Success, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sin choques de horario", color = StatusColors.Success, style = MaterialTheme.typography.bodySmall)
                    }
                }
                ValidatedTextField(
                    value = ui.course,
                    onValueChange = vm::onCourseChange,
                    label = "Curso",
                    leadingIcon = Icons.Default.Groups,
                    error = ui.courseError,
                    imeAction = ImeAction.Done,
                    onImeDone = vm::submit
                )
                FeedbackBanner(ui.generalError)
                LoadingButton(
                    text = "Confirmar reserva",
                    loading = ui.loading,
                    onClick = vm::submit,
                    icon = Icons.Default.EventAvailable,
                    enabled = ui.blockOk && !ui.checkingAvailability
                )
            }
        }
    }

    ui.completedMessage?.let { message ->
        var shown by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { shown = true }
        val scale by animateFloatAsState(
            targetValue = if (shown) 1f else 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "successScale"
        )
        AlertDialog(
            onDismissRequest = onDone,
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = StatusColors.Success,
                    modifier = Modifier.size(48.dp).scale(scale)
                )
            },
            title = { Text("Reserva confirmada") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = onDone) { Text("Ver mis reservas") } }
        )
    }
}
