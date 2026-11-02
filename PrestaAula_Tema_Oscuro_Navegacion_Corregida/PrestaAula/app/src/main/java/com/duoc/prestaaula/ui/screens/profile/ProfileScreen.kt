package com.duoc.prestaaula.ui.screens.profile

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

import com.duoc.prestaaula.data.local.entity.Role
import com.duoc.prestaaula.data.local.entity.UserEntity
import com.duoc.prestaaula.domain.model.TimeBlock
import com.duoc.prestaaula.domain.model.TimeBlocks
import com.duoc.prestaaula.domain.model.WeekDays
import com.duoc.prestaaula.ui.components.*
import com.duoc.prestaaula.ui.theme.StatusColors
import com.duoc.prestaaula.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(user: UserEntity, onLogout: () -> Unit) {
    val vm = appViewModel { ProfileViewModel(it.availabilityRepository, user.id) }
    val ui by vm.ui.collectAsStateWithLifecycle()
    var confirmLogout by remember { mutableStateOf(false) }

    Scaffold(topBar = { AppTopBar("Mi perfil") }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppearAnimated {
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    user.name.take(1).uppercase(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            StatusChip(user.role.label, MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            if (user.role == Role.DOCENTE) {
                SectionTitle("Mi disponibilidad horaria")
                Text(
                    "Indica los bloques en que puedes usar equipos durante la semana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AppDropdown(
                    label = "Día",
                    selectedText = WeekDays.name(ui.day),
                    options = WeekDays.numbers,
                    optionLabel = { d: Int -> WeekDays.name(d) },
                    onSelected = vm::onDayChange,
                    leadingIcon = Icons.Default.CalendarMonth
                )
                AppDropdown(
                    label = "Desde el bloque",
                    selectedText = TimeBlocks.label(ui.startBlock),
                    options = TimeBlocks.all,
                    optionLabel = { b: TimeBlock -> b.label },
                    onSelected = { vm.onStartChange(it.number) },
                    leadingIcon = Icons.Default.Schedule
                )
                AppDropdown(
                    label = "Hasta el bloque",
                    selectedText = TimeBlocks.label(ui.endBlock),
                    options = TimeBlocks.all,
                    optionLabel = { b: TimeBlock -> b.label },
                    onSelected = { vm.onEndChange(it.number) },
                    leadingIcon = Icons.Default.Schedule,
                    error = ui.rangeError
                )
                FeedbackBanner(ui.message, isError = false)
                LoadingButton("Agregar disponibilidad", false, vm::addAvailability, icon = Icons.Default.Add)
                ui.availability.forEach { a ->
                    key(a.id) {
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))) {
                            Row(Modifier.padding(start = 14.dp, top = 4.dp, bottom = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${WeekDays.name(a.dayOfWeek)}: bloque ${a.startBlock} al ${a.endBlock}",
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { vm.removeAvailability(a.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar disponibilidad", tint = StatusColors.Danger)
                                }
                            }
                        }
                    }
                }
                if (ui.availability.isEmpty()) {
                    Text("Aún no registras disponibilidad.", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = { confirmLogout = true }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Deseas salir de PrestaAula?") },
            confirmButton = { TextButton(onClick = { confirmLogout = false; onLogout() }) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } }
        )
    }
}
