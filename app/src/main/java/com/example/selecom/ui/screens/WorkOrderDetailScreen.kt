package com.example.selecom.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.selecom.data.model.*
import com.example.selecom.data.repository.SelecomRepository
import com.example.selecom.ui.components.*
import com.example.selecom.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkOrderDetailScreen(
    orderId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workOrders by SelecomRepository.workOrders.collectAsState()
    val users by SelecomRepository.users.collectAsState()
    val currentUser by SelecomRepository.currentUser.collectAsState()
    val context = LocalContext.current

    val order = workOrders.firstOrNull { it.id == orderId }

    if (order == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Orden no encontrada", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) { Text("Volver") }
            }
        }
        return
    }

    val tech = users.firstOrNull { it.id == order.technicianId }
    val sup = users.firstOrNull { it.id == order.supervisorId }

    var selectedTab by remember { mutableStateOf("Resumen") }
    var newCommentText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(order.code, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Text(order.client, fontSize = 12.sp, color = Ink500)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Badges & Action Buttons
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusBadge(status = order.status)
                                PriorityBadge(priority = order.priority)
                                ServiceTypeBadge(serviceType = order.serviceType)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = order.client,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                        Text(
                            text = "${order.site} • Programado: ${order.scheduledDate} ${order.scheduledTime}",
                            fontSize = 13.sp,
                            color = Ink500
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        // Progress Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Progreso de ejecución", fontSize = 12.sp, color = Ink700, fontWeight = FontWeight.Medium)
                            Text("${order.progress}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { order.progress / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = PrimaryBlue,
                            trackColor = Ink100
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        // Action buttons according to state
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (order.status) {
                                WOStatus.OPEN, WOStatus.SCHEDULED -> {
                                    Button(
                                        onClick = { SelecomRepository.updateWorkOrderStatus(order.id, WOStatus.IN_PROGRESS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Iniciar Trabajo", fontSize = 13.sp)
                                    }
                                }
                                WOStatus.IN_PROGRESS -> {
                                    Button(
                                        onClick = { SelecomRepository.updateWorkOrderStatus(order.id, WOStatus.PAUSED) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pausar", fontSize = 13.sp)
                                    }
                                    Button(
                                        onClick = { SelecomRepository.updateWorkOrderStatus(order.id, WOStatus.COMPLETED) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Completar", fontSize = 13.sp)
                                    }
                                }
                                WOStatus.PAUSED -> {
                                    Button(
                                        onClick = { SelecomRepository.updateWorkOrderStatus(order.id, WOStatus.IN_PROGRESS) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reanudar", fontSize = 13.sp)
                                    }
                                }
                                WOStatus.COMPLETED -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(EmeraldSuccess.copy(alpha = 0.12f))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Trabajo Completado y Firmado", color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                WOStatus.CANCELLED -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(RedDanger.copy(alpha = 0.12f))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Orden Cancelada", color = RedDanger, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tabs Selector
            item {
                val tabs = listOf("Resumen", "Checklist", "Materiales", "Comentarios", "Historial")
                ScrollableTabRow(
                    selectedTabIndex = tabs.indexOf(selectedTab),
                    containerColor = SurfaceLight,
                    contentColor = PrimaryBlue,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    tabs.forEach { tabTitle ->
                        Tab(
                            selected = selectedTab == tabTitle,
                            onClick = { selectedTab = tabTitle },
                            text = {
                                Text(
                                    text = tabTitle,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == tabTitle) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                "Resumen" -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Descripción del Servicio", fontWeight = FontWeight.Bold, color = Ink900, fontSize = 14.sp)
                                Text(
                                    text = if (order.description.isNotBlank()) order.description else "Sin descripción detallada.",
                                    fontSize = 13.sp,
                                    color = Ink700
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = Ink100)
                                Spacer(modifier = Modifier.height(4.dp))

                                Text("Equipos Involucrados", fontWeight = FontWeight.Bold, color = Ink900, fontSize = 14.sp)
                                Text(
                                    text = if (order.equipment.isNotBlank()) order.equipment else "No especificado.",
                                    fontSize = 13.sp,
                                    color = Ink700
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = Ink100)
                                Spacer(modifier = Modifier.height(4.dp))

                                Text("Ubicación y Navegación", fontWeight = FontWeight.Bold, color = Ink900, fontSize = 14.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(order.site, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Ink900)
                                        Text(order.address, fontSize = 12.sp, color = Ink500)
                                    }
                                    IconButton(
                                        onClick = {
                                            val query = "${order.address} ${order.site}"
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}"))
                                            context.startActivity(mapIntent)
                                        }
                                    ) {
                                        Icon(Icons.Default.Navigation, contentDescription = "Ver en mapa", tint = PrimaryBlue)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = Ink100)
                                Spacer(modifier = Modifier.height(4.dp))

                                Text("Personal Asignado", fontWeight = FontWeight.Bold, color = Ink900, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (tech != null) {
                                        UserAvatar(initials = tech.initials, colorHex = tech.avatarColorHex, size = 32)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(tech.name, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Ink900)
                                            Text("Técnico a cargo • ${tech.phone}", fontSize = 11.sp, color = Ink500)
                                        }
                                    } else {
                                        Text("Sin técnico asignado", fontSize = 13.sp, color = AmberWarning)
                                    }
                                }
                            }
                        }
                    }
                }

                "Checklist" -> {
                    if (order.checklist.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("No hay tareas en el checklist", color = Ink500, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(order.checklist) { item ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { SelecomRepository.toggleChecklistItem(order.id, item.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.done,
                                        onCheckedChange = { SelecomRepository.toggleChecklistItem(order.id, item.id) },
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = item.label,
                                        fontSize = 13.sp,
                                        color = if (item.done) Ink500 else Ink900,
                                        fontWeight = if (item.done) FontWeight.Normal else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                "Materiales" -> {
                    if (order.materials.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("No hay materiales registrados para esta orden", color = Ink500, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(order.materials) { mat ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(mat.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink900)
                                        Text("SKU: ${mat.sku}", fontSize = 11.sp, color = Ink500)
                                    }
                                    Text("${mat.qty} ${mat.unit}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                                }
                            }
                        }
                    }
                }

                "Comentarios" -> {
                    // Add comment field
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    placeholder = { Text("Escribir nota o reporte...", fontSize = 13.sp) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = {
                                        if (newCommentText.isNotBlank()) {
                                            SelecomRepository.addWorkOrderComment(order.id, newCommentText)
                                            newCommentText = ""
                                        }
                                    },
                                    enabled = newCommentText.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Enviar", tint = PrimaryBlue)
                                }
                            }
                        }
                    }

                    if (order.comments.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                Text("Sin comentarios aún.", fontSize = 13.sp, color = Ink500)
                            }
                        }
                    } else {
                        items(order.comments) { comment ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(comment.author, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Ink900)
                                        Text(comment.time, fontSize = 11.sp, color = Ink500)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(comment.text, fontSize = 13.sp, color = Ink700)
                                }
                            }
                        }
                    }
                }

                "Historial" -> {
                    if (order.history.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                Text("Sin registros de cambios.", fontSize = 13.sp, color = Ink500)
                            }
                        }
                    } else {
                        items(order.history) { h ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(h.action, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Ink900)
                                        Text("Por: ${h.actor}", fontSize = 11.sp, color = Ink500)
                                    }
                                    Text(h.time, fontSize = 11.sp, color = Ink500)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
