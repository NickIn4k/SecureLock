package com.example.securelock.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.SlotActionRequest
import com.example.securelock.network.SlotDetailResponse
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotDetailScreen(
    userId: Int,
    slotId: Int,
    navController: NavController
) {
    val scope = rememberCoroutineScope()
    var detail by remember { mutableStateOf<SlotDetailResponse?>(null) }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isActionLoading by remember { mutableStateOf(false) }

    LaunchedEffect(userId, slotId) {
        isLoading = true
        try {
            val response = ApiClient.api.getSlotDetail(userId, slotId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                detail = body
            } else {
                message = body?.message ?: "Errore caricamento slot"
            }
        } catch (e: Exception) {
            message = "Errore connessione: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val shape = RoundedCornerShape(28.dp)
    val isOpen = detail?.status.equals("open", ignoreCase = true)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dettaglio cassetto") })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0x22000000)),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Column(Modifier.padding(24.dp)) {
                    if (isLoading) {
                        CircularProgressIndicator()
                        return@Column
                    }

                    Text(
                        text = "Cassetto #$slotId",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Stato: ${detail?.status ?: "-"}")
                    Text("Chiave: ${if (detail?.hasKey == true) "presente" else "assente"}")
                    Text("Veicolo: ${detail?.vehicleName ?: "nessuno"}")
                    Text("Tipo veicolo: ${detail?.vehicleType ?: "nessuno"}")
                    Text("Ultimo aggiornamento: ${detail?.lastUpdated ?: "-"}")

                    if (message.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(message, color = Color(0xFFC62828))
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isActionLoading = true
                                try {
                                    val action = if (isOpen) "close" else "open"
                                    val response = ApiClient.api.slotAction(
                                        SlotActionRequest(
                                            userId = userId,
                                            slotId = slotId,
                                            action = action
                                        )
                                    )
                                    val body = response.body()
                                    if (response.isSuccessful && body?.success == true) {
                                        message = body.message
                                    } else {
                                        message = body?.message ?: "Errore comando slot"
                                    }
                                } catch (e: Exception) {
                                    message = "Errore connessione: ${e.message}"
                                } finally {
                                    isActionLoading = false
                                }
                            }
                        },
                        enabled = !isActionLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(Modifier.size(22.dp))
                        } else {
                            Text(if (isOpen) "Chiudi cassetto" else "Apri cassetto")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Indietro")
                    }
                }
            }
        }
    }
}