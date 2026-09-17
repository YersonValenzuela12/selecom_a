package com.example.selecom.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.selecom.data.model.*
import com.example.selecom.data.repository.SelecomRepository
import com.example.selecom.ui.components.*
import com.example.selecom.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun TeamScreen(
    modifier: Modifier = Modifier
) {
    val users by SelecomRepository.users.collectAsState()
    val workOrders by SelecomRepository.workOrders.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Directorio del Personal Selecom",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Ink900
            )
            Text(
                text = "Supervisores, coordinadores y cuadrillas técnicas en campo",
                style = MaterialTheme.typography.bodySmall,
                color = Ink500
            )
        }

        items(users, key = { it.id }) { user ->
            val activeJobs = workOrders.count {
                it.technicianId == user.id && (it.status == WOStatus.IN_PROGRESS || it.status == WOStatus.SCHEDULED)
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(initials = user.initials, colorHex = user.avatarColorHex, size = 44)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                                Spacer(modifier = Modifier.width(6.dp))
                                RoleBadge(role = user.role)
                            }
                            Text(user.title, fontSize = 12.sp, color = Ink500)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Sede: ${user.region} • $activeJobs asignadas", fontSize = 11.sp, color = PrimaryBlue)
                        }
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${user.phone}"))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Llamar", tint = PrimaryBlue)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentsScreen(
    modifier: Modifier = Modifier
) {
    val docs by SelecomRepository.documents.collectAsState()
    var selectedCategory by remember { mutableStateOf("Todos") }
    var showUploadDialog by remember { mutableStateOf(false) }
    var isUploadingAsync by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("Todos", "CCTV", "Fire Alarm", "BMS", "Site", "Operations")
    val filtered = remember(docs, selectedCategory) {
        if (selectedCategory == "Todos") docs else docs.filter { it.category == selectedCategory }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Repositorio Supabase",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Ink900
                    )
                    Text(
                        text = "Manuales técnicos, diagramas y sincronización asíncrona",
                        style = MaterialTheme.typography.bodySmall,
                        color = Ink500
                    )
                }
                Button(
                    onClick = { showUploadDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isUploadingAsync) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Sincronizando con Supabase Storage...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text("${(uploadProgress * 100).toInt()}%", fontSize = 12.sp, color = PrimaryBlue)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = PrimaryBlue,
                            trackColor = PrimaryBlue.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { doc ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = PrimaryBlue)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(doc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink900)
                            Text("${doc.category} • ${doc.size} • Subido por: ${doc.by}", fontSize = 11.sp, color = Ink500)
                        }
                    }

                    IconButton(onClick = { /* Download */ }) {
                        Icon(Icons.Default.Download, contentDescription = "Descargar", tint = PrimaryBlue)
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        var docName by remember { mutableStateOf("") }
        var docCategory by remember { mutableStateOf("CCTV") }
        var docType by remember { mutableStateOf("Manual") }

        AlertDialog(
            onDismissRequest = { showUploadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subir Documentación a Supabase", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Carga asíncrona de archivos al bucket de almacenamiento de Selecom.",
                        fontSize = 12.sp,
                        color = Ink500
                    )

                    OutlinedTextField(
                        value = docName,
                        onValueChange = { docName = it },
                        label = { Text("Nombre del Documento / Plano") },
                        placeholder = { Text("Ej. Plano As-Built CCTV Piso 2.pdf") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("Categoría:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink700)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val subCats = listOf("CCTV", "Fire Alarm", "BMS", "Site", "Operations")
                        items(subCats) { cat ->
                            FilterChip(
                                selected = docCategory == cat,
                                onClick = { docCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Tipo de Documento:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink700)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val types = listOf("Manual", "Plano", "Formato", "Reporte", "Cronograma")
                        items(types) { t ->
                            FilterChip(
                                selected = docType == t,
                                onClick = { docType = t },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = if (docName.isNotBlank()) docName.trim() else "Documento_${System.currentTimeMillis()}.pdf"
                        showUploadDialog = false
                        isUploadingAsync = true
                        uploadProgress = 0f

                        // Simulate asynchronous background upload coroutine
                        coroutineScope.launch {
                            for (i in 1..10) {
                                kotlinx.coroutines.delay(180)
                                uploadProgress = i / 10f
                            }
                            SelecomRepository.uploadDocument(
                                name = if (finalName.endsWith(".pdf") || finalName.endsWith(".xlsx")) finalName else "$finalName.pdf",
                                category = docCategory,
                                size = "${(1.2 + Math.random() * 8.0).toString().take(3)} MB",
                                type = docType
                            )
                            isUploadingAsync = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Iniciar Carga Asíncrona")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AuditScreen(
    modifier: Modifier = Modifier
) {
    val auditLogs by SelecomRepository.auditLogs.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Auditoría del Sistema",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Ink900
            )
            Text(
                text = "Trazabilidad de seguridad, cambios de estado y accesos",
                style = MaterialTheme.typography.bodySmall,
                color = Ink500
            )
        }

        items(auditLogs, key = { it.id }) { log ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("• ${log.target}", fontSize = 12.sp, color = Ink700)
                        }
                        Text(log.detail, fontSize = 11.sp, color = Ink500)
                        Text("Actor: ${log.actor} (IP: ${log.ip})", fontSize = 10.sp, color = Ink500)
                    }
                    Text(log.time, fontSize = 11.sp, color = Ink500)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    val currentUser by SelecomRepository.currentUser.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserAvatar(initials = currentUser.initials, colorHex = currentUser.avatarColorHex, size = 64)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(currentUser.name, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(currentUser.title, fontSize = 13.sp, color = Ink300)
                    Spacer(modifier = Modifier.height(8.dp))
                    RoleBadge(role = currentUser.role)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Cambiar Rol de Demostración", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                    Text(
                        "Prueba la experiencia de la aplicación cambiando entre los 4 perfiles del sistema:",
                        fontSize = 12.sp,
                        color = Ink500
                    )

                    Role.values().forEach { role ->
                        val isCurrent = currentUser.role == role
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { SelecomRepository.switchRole(role) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    role.displayName,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) PrimaryBlue else Ink900,
                                    fontSize = 13.sp
                                )
                                Text(
                                    when (role) {
                                        Role.ADMIN -> "Gestión global, auditoría y control de personal"
                                        Role.SUPERVISOR -> "Despacho, monitoreo de cuadrilla y supervisión"
                                        Role.COORDINADOR -> "Programación, planificación y operaciones"
                                        Role.TECHNICIAN -> "Ejecución en campo, checklist, asistencia y trámites"
                                    },
                                    fontSize = 11.sp,
                                    color = Ink500
                                )
                            }
                            if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Información de Contacto", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                    Text("Email: ${currentUser.email}", fontSize = 12.sp, color = Ink700)
                    Text("Teléfono: ${currentUser.phone}", fontSize = 12.sp, color = Ink700)
                    Text("Región Operativa: ${currentUser.region}", fontSize = 12.sp, color = Ink700)
                    Text("Último Acceso: ${currentUser.lastLogin}", fontSize = 12.sp, color = Ink700)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Acerca de Selecom Android", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                    Text("Versión 2.0.0 (Jetpack Compose)", fontSize = 12.sp, color = Ink500)
                    Text("Plataforma FSM para Seguridad Electrónica & Telecomunicaciones", fontSize = 12.sp, color = Ink500)
                }
            }
        }
    }
}
