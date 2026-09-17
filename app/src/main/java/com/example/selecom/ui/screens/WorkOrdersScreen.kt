package com.example.selecom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrdersScreen(
    onSelectWorkOrder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val workOrders by SelecomRepository.workOrders.collectAsState()
    val users by SelecomRepository.users.collectAsState()
    val currentUser by SelecomRepository.currentUser.collectAsState()

    var selectedDateFilter by remember { mutableStateOf("Hoy") }
    var selectedStatusFilter by remember { mutableStateOf("Todos") }
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val tomorrowStr = remember {
        val d = Date(System.currentTimeMillis() + 86400000L)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d)
    }

    val filteredOrders = remember(workOrders, selectedDateFilter, selectedStatusFilter, searchQuery) {
        workOrders.filter { order ->
            val matchesDate = when (selectedDateFilter) {
                "Hoy" -> order.scheduledDate == todayStr
                "Mañana" -> order.scheduledDate == tomorrowStr
                "Esta Semana" -> true
                else -> true
            }

            val matchesStatus = when (selectedStatusFilter) {
                "Todos" -> true
                "Abierto" -> order.status == WOStatus.OPEN
                "Programado" -> order.status == WOStatus.SCHEDULED
                "En Progreso" -> order.status == WOStatus.IN_PROGRESS
                "Pausado" -> order.status == WOStatus.PAUSED
                "Completado" -> order.status == WOStatus.COMPLETED
                else -> true
            }

            val matchesQuery = if (searchQuery.isBlank()) true else {
                val q = searchQuery.lowercase()
                order.code.lowercase().contains(q) ||
                        order.client.lowercase().contains(q) ||
                        order.site.lowercase().contains(q) ||
                        order.equipment.lowercase().contains(q) ||
                        order.address.lowercase().contains(q)
            }

            matchesDate && matchesStatus && matchesQuery
        }
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.role != Role.TECHNICIAN) {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Orden")
                }
            }
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar cliente, código, dirección...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Ink500) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = SurfaceLight,
                    focusedContainerColor = SurfaceLight,
                    unfocusedBorderColor = Ink300,
                    focusedBorderColor = PrimaryBlue
                ),
                singleLine = true
            )

            // Date Filters Row (Hoy, Mañana, Esta Semana, Todo)
            val dateFilters = listOf("Hoy", "Mañana", "Esta Semana", "Todo")
            TabRow(
                selectedTabIndex = dateFilters.indexOf(selectedDateFilter),
                containerColor = SurfaceLight,
                contentColor = PrimaryBlue,
                divider = {}
            ) {
                dateFilters.forEach { label ->
                    Tab(
                        selected = selectedDateFilter == label,
                        onClick = { selectedDateFilter = label },
                        text = {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (selectedDateFilter == label) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Status Filter Chips
            val statusFilters = listOf("Todos", "En Progreso", "Programado", "Abierto", "Pausado", "Completado")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusFilters) { statusLabel ->
                    val isSelected = selectedStatusFilter == statusLabel
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStatusFilter = statusLabel },
                        label = { Text(statusLabel, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                            selectedLabelColor = PrimaryBlue
                        )
                    )
                }
            }

            // Orders List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredOrders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = Ink300,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No se encontraron órdenes",
                                    fontSize = 15.sp,
                                    color = Ink500
                                )
                            }
                        }
                    }
                }

                items(filteredOrders, key = { it.id }) { order ->
                    val assignedTech = users.firstOrNull { it.id == order.technicianId }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ServiceTypeBadge(serviceType = order.serviceType)
                                    StatusBadge(status = order.status)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = order.client,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Ink900
                            )
                            Text(
                                text = "${order.site} • ${order.address}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Ink500
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = Ink100)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (assignedTech != null) {
                                        UserAvatar(
                                            initials = assignedTech.initials,
                                            colorHex = assignedTech.avatarColorHex,
                                            size = 26
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = assignedTech.name,
                                            fontSize = 12.sp,
                                            color = Ink700,
                                            fontWeight = FontWeight.Medium
                                        )
                                    } else {
                                        Text(
                                            text = "Sin asignar",
                                            fontSize = 12.sp,
                                            color = AmberWarning,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Ink500,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${order.scheduledDate.takeLast(5)} ${order.scheduledTime}",
                                        fontSize = 12.sp,
                                        color = Ink700
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateWorkOrderDialog(
            technicians = users.filter { it.role == Role.TECHNICIAN },
            onDismiss = { showCreateDialog = false },
            onSave = { client, site, address, serviceType, priority, techId, date, time, duration, desc, equipment ->
                SelecomRepository.createWorkOrder(
                    client = client,
                    site = site,
                    address = address,
                    serviceType = serviceType,
                    priority = priority,
                    technicianId = techId,
                    scheduledDate = date,
                    scheduledTime = time,
                    durationHrs = duration,
                    description = desc,
                    equipment = equipment
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateWorkOrderDialog(
    technicians: List<User>,
    onDismiss: () -> Unit,
    onSave: (
        client: String,
        site: String,
        address: String,
        serviceType: ServiceType,
        priority: Priority,
        technicianId: String?,
        date: String,
        time: String,
        duration: Double,
        description: String,
        equipment: String
    ) -> Unit
) {
    var client by remember { mutableStateOf("") }
    var site by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(ServiceType.CCTV) }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedTechId by remember { mutableStateOf<String?>(null) }
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    var scheduledDate by remember { mutableStateOf(today) }
    var scheduledTime by remember { mutableStateOf("09:00") }
    var description by remember { mutableStateOf("") }
    var equipment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Orden de Trabajo", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = client,
                        onValueChange = { client = it },
                        label = { Text("Cliente / Empresa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = site,
                        onValueChange = { site = it },
                        label = { Text("Sede / Área") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección completa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Tipo de Servicio:", fontSize = 12.sp, color = Ink500)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(ServiceType.values()) { st ->
                            FilterChip(
                                selected = serviceType == st,
                                onClick = { serviceType = st },
                                label = { Text(st.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    Text("Prioridad:", fontSize = 12.sp, color = Ink500)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Priority.values()) { pr ->
                            FilterChip(
                                selected = priority == pr,
                                onClick = { priority = pr },
                                label = { Text(pr.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    Text("Técnico Asignado:", fontSize = 12.sp, color = Ink500)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedTechId == null,
                                onClick = { selectedTechId = null },
                                label = { Text("Sin asignar", fontSize = 11.sp) }
                            )
                        }
                        items(technicians) { tech ->
                            FilterChip(
                                selected = selectedTechId == tech.id,
                                onClick = { selectedTechId = tech.id },
                                label = { Text(tech.name.split(" ").first(), fontSize = 11.sp) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("Equipos / Materiales") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción del requerimiento") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (client.isNotBlank()) {
                        onSave(
                            client, site, address, serviceType, priority, selectedTechId,
                            scheduledDate, scheduledTime, 2.0, description, equipment
                        )
                    }
                },
                enabled = client.isNotBlank()
            ) {
                Text("Crear Orden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
