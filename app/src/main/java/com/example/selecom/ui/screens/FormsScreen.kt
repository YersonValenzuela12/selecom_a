package com.example.selecom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.selecom.data.model.*
import com.example.selecom.data.repository.SelecomRepository
import com.example.selecom.ui.components.*
import com.example.selecom.ui.theme.*

@Composable
fun FormsScreen(
    modifier: Modifier = Modifier
) {
    val currentUser by SelecomRepository.currentUser.collectAsState()
    val forms by SelecomRepository.forms.collectAsState()

    var activeFormType by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Trámites y Solicitudes de Campo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Ink900
            )
            Text(
                text = "Selecciona un formato oficial para tramitar requerimientos",
                style = MaterialTheme.typography.bodySmall,
                color = Ink500
            )
        }

        // Available Form Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormShortcutCard(
                    title = "Movilidad",
                    desc = "Kilometraje y vehículo",
                    icon = Icons.Default.DirectionsCar,
                    tint = PrimaryBlue,
                    onClick = { activeFormType = "mobility" },
                    modifier = Modifier.weight(1f)
                )
                FormShortcutCard(
                    title = "Gastos",
                    desc = "Peajes, viáticos, compras",
                    icon = Icons.Default.ReceiptLong,
                    tint = EmeraldSuccess,
                    onClick = { activeFormType = "expense_claim" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormShortcutCard(
                    title = "Licencia Médica",
                    desc = "Descansos y certificados",
                    icon = Icons.Default.MedicalServices,
                    tint = RedDanger,
                    onClick = { activeFormType = "medical_leave" },
                    modifier = Modifier.weight(1f)
                )
                FormShortcutCard(
                    title = "Reclamación",
                    desc = "Incidencias u observaciones",
                    icon = Icons.Default.ReportProblem,
                    tint = AmberWarning,
                    onClick = { activeFormType = "complaint" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Submissions history
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial de Solicitudes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink900
                )
                Text(
                    text = "${forms.size} registros",
                    fontSize = 12.sp,
                    color = Ink500
                )
            }
        }

        if (forms.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se han enviado formularios aún.", color = Ink500, fontSize = 13.sp)
                }
            }
        } else {
            items(forms, key = { it.id }) { sub ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.formTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Ink900
                            )
                            val statusBg = when (sub.status) {
                                "Aprobado" -> EmeraldSuccess.copy(alpha = 0.12f)
                                "Rechazado" -> RedDanger.copy(alpha = 0.12f)
                                else -> AmberWarning.copy(alpha = 0.12f)
                            }
                            val statusColor = when (sub.status) {
                                "Aprobado" -> EmeraldSuccess
                                "Rechazado" -> RedDanger
                                else -> AmberWarning
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = sub.status,
                                    color = statusColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Solicitado por: ${sub.submittedByName} • ${sub.createdAt}",
                            fontSize = 12.sp,
                            color = Ink500
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            sub.details.forEach { (key, value) ->
                                Row {
                                    Text("$key: ", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink700)
                                    Text(value, fontSize = 12.sp, color = Ink900)
                                }
                            }
                        }

                        // Admin Review Buttons
                        if (currentUser.role == Role.ADMIN && sub.status == "Pendiente") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { SelecomRepository.reviewForm(sub.id, approved = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Aprobar", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { SelecomRepository.reviewForm(sub.id, approved = false) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Rechazar", fontSize = 12.sp, color = RedDanger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeFormType != null) {
        FormSubmitModal(
            formType = activeFormType!!,
            onDismiss = { activeFormType = null },
            onSubmit = { formType, title, details ->
                SelecomRepository.submitForm(formType, title, details)
                activeFormType = null
            }
        )
    }
}

@Composable
fun FormShortcutCard(
    title: String,
    desc: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
            Text(desc, fontSize = 11.sp, color = Ink500)
        }
    }
}

@Composable
fun FormSubmitModal(
    formType: String,
    onDismiss: () -> Unit,
    onSubmit: (formType: String, title: String, details: Map<String, String>) -> Unit
) {
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }
    var field4 by remember { mutableStateOf("") }

    val (title, label1, label2, label3, label4) = when (formType) {
        "mobility" -> listOf("Formulario de Movilidad", "Vehículo / Placa", "Kilometraje Inicial", "Kilometraje Final", "Destino / Motivo")
        "expense_claim" -> listOf("Rendición de Gastos", "Concepto de Gasto", "Monto (S/.)", "Nro. Comprobante / Factura", "Justificación")
        "medical_leave" -> listOf("Licencia Médica", "Centro Médico / Clínica", "Días de Reposo", "Nro. Certificado Médico", "Diagnóstico Referencial")
        else -> listOf("Libro de Reclamaciones", "Sede / Proyecto Afectado", "Tipo de Incidencia", "Detalle de los Hechos", "Acción Solicitada")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = field1,
                    onValueChange = { field1 = it },
                    label = { Text(label1) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = field2,
                    onValueChange = { field2 = it },
                    label = { Text(label2) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = field3,
                    onValueChange = { field3 = it },
                    label = { Text(label3) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = field4,
                    onValueChange = { field4 = it },
                    label = { Text(label4) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (field1.isNotBlank()) {
                        val details = mapOf(
                            label1 to field1,
                            label2 to field2,
                            label3 to field3,
                            label4 to field4
                        )
                        onSubmit(formType, title, details)
                    }
                },
                enabled = field1.isNotBlank()
            ) {
                Text("Enviar Solicitud")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
