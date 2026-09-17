package com.example.selecom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.selecom.data.model.*
import com.example.selecom.data.repository.SelecomRepository
import com.example.selecom.ui.components.*
import com.example.selecom.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigateToWorkOrders: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToForms: () -> Unit,
    onSelectWorkOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by SelecomRepository.currentUser.collectAsState()
    val workOrders by SelecomRepository.workOrders.collectAsState()
    val attendance by SelecomRepository.attendance.collectAsState()
    val forms by SelecomRepository.forms.collectAsState()

    val myOrders = remember(workOrders, currentUser) {
        if (currentUser.role == Role.TECHNICIAN) {
            workOrders.filter { it.technicianId == currentUser.id }
        } else {
            workOrders
        }
    }

    val activeOrder = remember(myOrders) {
        myOrders.firstOrNull { it.status == WOStatus.IN_PROGRESS }
            ?: myOrders.firstOrNull { it.status == WOStatus.SCHEDULED }
    }

    val myAttendance = remember(attendance, currentUser) {
        attendance.firstOrNull { it.userId == currentUser.id }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(
                                initials = currentUser.initials,
                                colorHex = currentUser.avatarColorHex,
                                size = 44
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "¡Hola, ${currentUser.name.split(" ").first()}!",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = currentUser.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Ink300
                                )
                            }
                        }
                        RoleBadge(role = currentUser.role)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sede: ${currentUser.region}",
                                fontSize = 12.sp,
                                color = Ink100
                            )
                        }

                        // Quick Attendance Status
                        val hasCheckedIn = myAttendance?.checkInAt != null
                        val hasCheckedOut = myAttendance?.checkOutAt != null
                        val attText = when {
                            hasCheckedOut -> "Salida: ${myAttendance?.checkOutAt?.take(5)}"
                            hasCheckedIn -> "Entrada: ${myAttendance?.checkInAt?.take(5)}"
                            else -> "Sin marcar"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (hasCheckedIn && !hasCheckedOut) EmeraldSuccess.copy(alpha = 0.2f)
                                    else AmberWarning.copy(alpha = 0.2f)
                                )
                                .clickable { onNavigateToAttendance() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = attText,
                                fontSize = 11.sp,
                                color = if (hasCheckedIn && !hasCheckedOut) EmeraldSuccess else AmberWarning,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Order Banner for Technician
        if (currentUser.role == Role.TECHNICIAN && activeOrder != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onSelectWorkOrder(activeOrder.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (activeOrder.status == WOStatus.IN_PROGRESS) EmeraldSuccess
                                            else PrimaryBlue
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (activeOrder.status == WOStatus.IN_PROGRESS) "TRABAJO EN CURSO" else "PRÓXIMO SERVICIO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeOrder.status == WOStatus.IN_PROGRESS) EmeraldSuccess else PrimaryBlue
                                )
                            }
                            StatusBadge(status = activeOrder.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeOrder.client,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                        Text(
                            text = "${activeOrder.site} • ${activeOrder.serviceType.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink500
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progreso del trabajo",
                                fontSize = 12.sp,
                                color = Ink700
                            )
                            Text(
                                text = "${activeOrder.progress}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { activeOrder.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = PrimaryBlue,
                            trackColor = Ink100
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (activeOrder.status == WOStatus.IN_PROGRESS) {
                                Button(
                                    onClick = { SelecomRepository.updateWorkOrderStatus(activeOrder.id, WOStatus.PAUSED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pausar", fontSize = 13.sp)
                                }
                                Button(
                                    onClick = { SelecomRepository.updateWorkOrderStatus(activeOrder.id, WOStatus.COMPLETED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Finalizar", fontSize = 13.sp)
                                }
                            } else {
                                Button(
                                    onClick = { SelecomRepository.updateWorkOrderStatus(activeOrder.id, WOStatus.IN_PROGRESS) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Iniciar Trabajo", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Role-Specific KPI Grid
        item {
            Text(
                text = "Métricas Clave",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Ink900
            )
        }

        item {
            val totalOrders = workOrders.size
            val inProgressCount = workOrders.count { it.status == WOStatus.IN_PROGRESS }
            val completedCount = workOrders.count { it.status == WOStatus.COMPLETED }
            val scheduledCount = workOrders.count { it.status == WOStatus.SCHEDULED }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Órdenes Totales",
                    value = totalOrders.toString(),
                    subtitle = "$scheduledCount programadas",
                    icon = Icons.Default.Assignment,
                    accentColor = PrimaryBlue,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "En Ejecución",
                    value = inProgressCount.toString(),
                    subtitle = "Tiempo real",
                    icon = Icons.Default.Engineering,
                    accentColor = CyanAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            val completedRate = if (workOrders.isNotEmpty()) {
                ((workOrders.count { it.status == WOStatus.COMPLETED }.toDouble() / workOrders.size) * 100).toInt()
            } else 0

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Eficacia SLA",
                    value = "$completedRate%",
                    subtitle = "Objetivo 90%",
                    icon = Icons.Default.TrendingUp,
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Trámites Pend.",
                    value = forms.count { it.status == "Pendiente" }.toString(),
                    subtitle = "Aprobaciones",
                    icon = Icons.Default.Description,
                    accentColor = AmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onNavigateToWorkOrders,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver Órdenes", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onNavigateToAttendance,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Asistencia", fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = onNavigateToForms,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Trámites", fontSize = 12.sp)
                }
            }
        }

        // Today's Work Orders Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (currentUser.role == Role.TECHNICIAN) "Mis Órdenes Asignadas" else "Órdenes Recientes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink900
                )
                TextButton(onClick = onNavigateToWorkOrders) {
                    Text("Ver todas", fontSize = 13.sp)
                }
            }
        }

        items(myOrders.take(4)) { order ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectWorkOrder(order.id) }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = order.code,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            PriorityBadge(priority = order.priority)
                            StatusBadge(status = order.status)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = order.client,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink900
                    )
                    Text(
                        text = "${order.site} • ${order.serviceType.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink500
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Ink500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${order.scheduledDate} ${order.scheduledTime}",
                                fontSize = 11.sp,
                                color = Ink500
                            )
                        }
                        Text(
                            text = "${order.progress}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    }
                }
            }
        }
    }
}
