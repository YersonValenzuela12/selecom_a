package com.example.selecom.ui.screens

import androidx.compose.foundation.background
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AttendanceScreen(
    modifier: Modifier = Modifier
) {
    val currentUser by SelecomRepository.currentUser.collectAsState()
    val users by SelecomRepository.users.collectAsState()
    val attendanceRecords by SelecomRepository.attendance.collectAsState()

    var currentTimeStr by remember { mutableStateOf("") }
    var currentDateStr by remember { mutableStateOf("") }

    // Live clock ticker
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            currentDateStr = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "ES")).format(now)
                .replaceFirstChar { it.uppercase() }
            delay(1000)
        }
    }

    val todayIso = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val myRecord = remember(attendanceRecords, currentUser, todayIso) {
        attendanceRecords.firstOrNull { it.userId == currentUser.id && it.attendanceDate == todayIso }
    }

    var selectedTab by remember { mutableStateOf(if (currentUser.role == Role.ADMIN) 1 else 0) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab row if admin
        if (currentUser.role == Role.ADMIN) {
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = SurfaceLight,
                    contentColor = PrimaryBlue,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Mi Marcación", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Asistencia del Equipo", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // Clock Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentDateStr,
                            fontSize = 13.sp,
                            color = Ink300,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (currentTimeStr.isNotEmpty()) currentTimeStr else "--:--:--",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Jornada activa (Reinicio diario: 07:00 a.m.)",
                                fontSize = 11.sp,
                                color = Ink300
                            )
                        }
                    }
                }
            }

            // GPS & Status Info Card
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Geolocalización GPS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                            }
                            Text("Activo", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Coordenadas fijadas: -12.1124° S, -77.0289° O (Sede Lima)",
                            fontSize = 12.sp,
                            color = Ink500
                        )
                    }
                }
            }

            // Mark Action Buttons
            item {
                val hasIn = myRecord?.checkInAt != null
                val hasOut = myRecord?.checkOutAt != null

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Registro de Asistencia de Hoy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Ink900
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Status rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hasIn) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (hasIn) EmeraldSuccess else Ink300,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hora de Entrada:", fontSize = 13.sp, color = Ink700)
                            }
                            Text(
                                text = myRecord?.checkInAt ?: "Pendiente",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasIn) EmeraldSuccess else Ink500
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Ink100)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (hasOut) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (hasOut) PrimaryBlue else Ink300,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hora de Salida:", fontSize = 13.sp, color = Ink700)
                            }
                            Text(
                                text = myRecord?.checkOutAt ?: "Pendiente",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasOut) PrimaryBlue else Ink500
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!hasIn) {
                            Button(
                                onClick = { SelecomRepository.markAttendance(isCheckIn = true, lat = -12.1124, lng = -77.0289) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Marcar Entrada", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        } else if (!hasOut) {
                            Button(
                                onClick = { SelecomRepository.markAttendance(isCheckIn = false, lat = -12.1124, lng = -77.0289) },
                                colors = ButtonDefaults.buttonColors(containerColor = RedDanger),
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Marcar Salida", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EmeraldSuccess.copy(alpha = 0.15f))
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "¡Jornada de hoy completada!",
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Team Attendance Overview (Admin)
            item {
                val nonAdmins = users.filter { it.role != Role.ADMIN }
                val markedInCount = nonAdmins.count { user ->
                    attendanceRecords.any { it.userId == user.id && it.attendanceDate == todayIso && it.checkInAt != null }
                }
                val markedOutCount = nonAdmins.count { user ->
                    attendanceRecords.any { it.userId == user.id && it.attendanceDate == todayIso && it.checkOutAt != null }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Personal Total",
                        value = nonAdmins.size.toString(),
                        icon = Icons.Default.People,
                        accentColor = PrimaryBlue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Entrada Marcada",
                        value = markedInCount.toString(),
                        icon = Icons.Default.CheckCircle,
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Salida Marcada",
                        value = markedOutCount.toString(),
                        icon = Icons.Default.Logout,
                        accentColor = RedDanger,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Personal de Campo y Operaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink900
                )
            }

            items(users.filter { it.role != Role.ADMIN }) { user ->
                val record = attendanceRecords.firstOrNull { it.userId == user.id && it.attendanceDate == todayIso }

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
                            UserAvatar(initials = user.initials, colorHex = user.avatarColorHex, size = 36)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink900)
                                Text("${user.role.displayName} • ${user.region}", fontSize = 11.sp, color = Ink500)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Entrada: ${record?.checkInAt ?: "--"} | Salida: ${record?.checkOutAt ?: "--"}",
                                    fontSize = 11.sp,
                                    color = if (record?.checkInAt != null) EmeraldSuccess else Ink500,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (record?.checkInAt != null) {
                            IconButton(onClick = { SelecomRepository.resetAttendance(user.id) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reiniciar", tint = Ink500)
                            }
                        }
                    }
                }
            }
        }
    }
}
