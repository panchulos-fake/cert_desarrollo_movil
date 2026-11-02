package com.duoc.prestaaula.ui.screens.reports

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

import com.duoc.prestaaula.data.local.entity.EquipmentStats
import com.duoc.prestaaula.ui.components.*
import com.duoc.prestaaula.ui.theme.StatusColors
import com.duoc.prestaaula.util.DateUtils
import com.duoc.prestaaula.viewmodel.ReportsViewModel

@Composable
fun ReportsScreen() {
    val vm = appViewModel { ReportsViewModel(it.equipmentRepository, it.loanRepository) }
    val ui by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = { AppTopBar("Reportes de uso") }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard("Disponibles", ui.available, StatusColors.Success, Icons.Default.CheckCircle, Modifier.weight(1f))
                    KpiCard("Prestados", ui.lent, StatusColors.Warning, Icons.Default.Output, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiCard("En mantención", ui.maintenance, StatusColors.Danger, Icons.Default.Build, Modifier.weight(1f))
                    KpiCard("Reservas activas", ui.activeLoans, StatusColors.Info, Icons.Default.EventAvailable, Modifier.weight(1f))
                }
            }
            item { SectionTitle("Equipos más utilizados") }
            if (ui.mostUsed.isEmpty()) {
                item { EmptyState(Icons.Default.BarChart, "Sin datos", "Aún no hay préstamos registrados.") }
            }
            items(ui.mostUsed, key = { "u${it.equipmentId}" }) { s ->
                UsageBar(s, maxValue = ui.mostUsed.maxOf { it.total }, value = s.total, label = "préstamos", color = MaterialTheme.colorScheme.primary)
            }
            item { SectionTitle("Equipos con incidencias frecuentes") }
            if (ui.frequentIncidents.isEmpty()) {
                item { EmptyState(Icons.Default.VerifiedUser, "Sin incidencias", "Ningún equipo ha sido devuelto con problemas.") }
            }
            items(ui.frequentIncidents, key = { "i${it.equipmentId}" }) { s ->
                UsageBar(s, maxValue = ui.frequentIncidents.maxOf { it.incidents }, value = s.incidents, label = "incidencias", color = StatusColors.Danger)
            }
            item { SectionTitle("Últimas incidencias (con responsable)") }
            items(ui.incidents, key = { "d${it.loan.id}" }) { d ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = StatusColors.Warning)
                            Spacer(Modifier.width(8.dp))
                            Text("${d.equipmentName} (${d.equipmentCode})", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            StatusChip(d.loan.returnCondition?.label ?: "", StatusColors.Danger)
                        }
                        Text(
                            "Responsable: ${d.teacherName} · ${d.loan.returnedAt?.let { DateUtils.formatMillis(it) } ?: ""}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (d.loan.observations.isNotBlank()) {
                            Text(d.loan.observations, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: Int, color: Color, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(Modifier.height(6.dp))
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun UsageBar(s: EquipmentStats, maxValue: Int, value: Int, label: String, color: Color) {
    val target = if (maxValue == 0) 0f else value.toFloat() / maxValue
    val progress by animateFloatAsState(targetValue = target, animationSpec = tween(700), label = "bar")
    Column(Modifier.fillMaxWidth()) {
        Row {
            Text("${s.type} ${s.brand} (${s.internalCode})", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("$value $label", style = MaterialTheme.typography.labelMedium, color = color)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color
        )
    }
}
