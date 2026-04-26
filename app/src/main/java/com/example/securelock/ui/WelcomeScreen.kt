package com.example.securelock.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.DashboardResponse
import com.example.securelock.network.DashboardSlot
import com.example.securelock.ui.components.SecureLockMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    userId: Int,
    isAdmin: Boolean,
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    var dashboard by remember { mutableStateOf<DashboardResponse?>(null) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        isLoading = true
        try {
            val response = ApiClient.api.getDashboard(userId)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                dashboard = body
            } else {
                message = body?.message ?: "Errore caricamento dashboard"
            }

        } catch (e: Exception) {
            message = "Errore connessione: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val cardShape = RoundedCornerShape(28.dp)

    Box(modifier = Modifier.fillMaxSize()) {

        // BACKGROUND
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF0F6FF),
                            Color(0xFFEDE7FF),
                            Color(0xFFF7F9FC)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,

            // TOP BAR CON MENU
            topBar = {
                TopAppBar(
                    title = { Text("SecureLock") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x44FFFFFF))
                        ) {
                            SecureLockMenu(
                                navController = navController,
                                userId = userId,
                                showCredits = true,
                                showDiagnostics = isAdmin,
                                showNewUser = isAdmin,
                                showLogout = true,
                                onDiagnosticsClick = {
                                    // TODO
                                }
                            )
                        }
                    }
                )
            }

        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // CARD UTENTE
                Card(
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0x22000000)),
                    elevation = CardDefaults.cardElevation(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(24.dp)) {

                        Text(
                            text = "Benvenuto",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text("ID: $userId")

                        Text(
                            text = if (isAdmin) "Ruolo: admin" else "Ruolo: user"
                        )

                        if (message.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(message, color = Color(0xFFC62828))
                        }
                    }
                }

                // 🔐 SEZIONE SLOT
                Text(
                    text = "I tuoi cassetti",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                if (isLoading) {
                    CircularProgressIndicator()
                } else {

                    val slots = dashboard?.slots.orEmpty().take(3)

                    if (slots.isEmpty()) {
                        Text("Nessun cassetto assegnato")
                    } else {
                        slots.forEach { slot ->
                            SlotCardItem(
                                slot = slot,
                                onClick = {
                                    navController.navigate("slot_detail/$userId/${slot.slotId}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlotCardItem(
    slot: DashboardSlot,
    onClick: () -> Unit
) {
    val isOpen = slot.status.equals("open", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0x22000000)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isOpen) Icons.Default.LockOpen else Icons.Default.Lock,
                contentDescription = null
            )

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("Cassetto ${slot.slotId}", fontWeight = FontWeight.SemiBold)
                Text("Stato: ${slot.status}")
                Text("Chiave: ${if (slot.hasKey) "presente" else "assente"}")
                Text(slot.vehicleName ?: "Nessun veicolo")
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}