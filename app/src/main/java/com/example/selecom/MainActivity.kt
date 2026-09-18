package com.example.selecom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.example.selecom.data.model.Role
import com.example.selecom.data.repository.SelecomRepository
import com.example.selecom.ui.components.RoleBadge
import com.example.selecom.ui.components.UserAvatar
import com.example.selecom.ui.screens.*
import com.example.selecom.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SelecomTheme {
                SelecomApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecomApp() {
    val currentUser by SelecomRepository.currentUser.collectAsState()
    val notifications by SelecomRepository.notifications.collectAsState()

    var selectedNavIndex by remember { mutableStateOf(0) }
    var selectedOrderId by remember { mutableStateOf<String?>(null) }
    var showRoleMenu by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var moreSubScreen by remember { mutableStateOf<String?>(null) } // "team", "docs", "audit", "profile"

    val unreadCount = remember(notifications) { notifications.count { it.unread } }

    Scaffold(
        topBar = {
            if (selectedOrderId == null) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("SELECOM", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Ink900)
                                Text("Security Operations", fontSize = 10.sp, color = Ink500)
                            }
                        }
                    },
                    actions = {
                        // Current role switcher chip
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PrimaryBlue.copy(alpha = 0.1f))
                                    .clickable { showRoleMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RoleBadge(role = currentUser.role)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showRoleMenu,
                                onDismissRequest = { showRoleMenu = false }
                            ) {
                                Text(
                                    text = "Cambiar Rol de Demostración",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink500
                                )
                                Role.values().forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role.displayName, fontWeight = if (currentUser.role == role) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            SelecomRepository.switchRole(role)
                                            showRoleMenu = false
                                        },
                                        leadingIcon = {
                                            if (currentUser.role == role) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Notifications Icon with badge
                        IconButton(onClick = { showNotificationsDialog = true }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge(containerColor = RedDanger) {
                                            Text(unreadCount.toString(), fontSize = 10.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Ink900)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
                )
            }
        },
        bottomBar = {
            if (selectedOrderId == null) {
                NavigationBar(
                    containerColor = SurfaceLight,
                    tonalElevation = 4.dp
                ) {
                    val navItems = listOf(
                        Triple("Inicio", Icons.Default.Dashboard, 0),
                        Triple("Órdenes", Icons.Default.Assignment, 1),
                        Triple("Asistencia", Icons.Default.AccessTime, 2),
                        Triple("Trámites", Icons.Default.Description, 3),
                        Triple("Más", Icons.Default.MoreHoriz, 4)
                    )

                    navItems.forEach { (label, icon, index) ->
                        NavigationBarItem(
                            selected = selectedNavIndex == index,
                            onClick = {
                                selectedNavIndex = index
                                moreSubScreen = null
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedOrderId != null) {
                WorkOrderDetailScreen(
                    orderId = selectedOrderId!!,
                    onBack = { selectedOrderId = null }
                )
            } else {
                when (selectedNavIndex) {
                    0 -> DashboardScreen(
                        onNavigateToWorkOrders = { selectedNavIndex = 1 },
                        onNavigateToAttendance = { selectedNavIndex = 2 },
                        onNavigateToForms = { selectedNavIndex = 3 },
                        onSelectWorkOrder = { selectedOrderId = it }
                    )
                    1 -> WorkOrdersScreen(
                        onSelectWorkOrder = { selectedOrderId = it }
                    )
                    2 -> AttendanceScreen()
                    3 -> FormsScreen()
                    4 -> {
                        when (moreSubScreen) {
                            "team" -> TeamScreen()
                            "docs" -> DocumentsScreen()
                            "audit" -> AuditScreen()
                            "profile" -> ProfileScreen()
                            else -> MoreHubScreen(
                                onSelectSub = { moreSubScreen = it },
                                currentUserRole = currentUser.role
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = { Text("Notificaciones y Alertas", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notif ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (notif.unread) PrimaryBlue.copy(alpha = 0.08f) else SurfaceLight
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { SelecomRepository.markNotificationRead(notif.id) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        notif.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (notif.isUrgent) RedDanger else Ink900
                                    )
                                    Text(notif.time, fontSize = 10.sp, color = Ink500)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(notif.body, fontSize = 11.sp, color = Ink700)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
fun MoreHubScreen(
    onSelectSub: (String) -> Unit,
    currentUserRole: Role,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Centro de Gestión", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Ink900)
            Text("Herramientas adicionales, equipo técnico y documentación", fontSize = 12.sp, color = Ink500)
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSub("team") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Personal y Técnicos", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                        Text("Directorio telefónico y asignaciones", fontSize = 11.sp, color = Ink500)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Ink500)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSub("docs") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Documentos y Manuales", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                        Text("Planos, fichas técnicas y normativas", fontSize = 11.sp, color = Ink500)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Ink500)
                }
            }
        }

        if (currentUserRole == Role.ADMIN) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSub("audit") }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryBlue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryBlue)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auditoría de Seguridad", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                            Text("Logs de actividad y control de accesos", fontSize = 11.sp, color = Ink500)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Ink500)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSub("profile") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mi Perfil & Cambiar Rol", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Ink900)
                        Text("Configuración y selector rápido de roles", fontSize = 11.sp, color = Ink500)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Ink500)
                }
            }
        }
    }
}
