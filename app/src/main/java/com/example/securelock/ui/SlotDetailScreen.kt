package com.example.securelock.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.SlotActionRequest
import com.example.securelock.network.SlotDetailResponse
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            snackbarHostState.showSnackbar(message)
            message = ""
        }
    }

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

    val cardShape = RoundedCornerShape(28.dp)
    val buttonShape = RoundedCornerShape(18.dp)

    val isOpen = detail?.status.equals("open", ignoreCase = true)

    // 🕒 FORMAT DATA
    fun formatDate(raw: String?): String {
        return try {
            if (raw == null) return "-"
            val parsed = LocalDateTime.parse(raw)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            parsed.format(formatter)
        } catch (e: Exception) {
            raw ?: "-"
        }
    }

    // 🎨 COMPONENTE RIGA CARINA
    @Composable
    fun InfoRow(label: String, value: String) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A8A9A)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF16324F)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 🌈 GRADIENT CHIARO
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFEAF4FF),
                            Color(0xFFF0ECFF),
                            Color(0xFFF7F9FC)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,

            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) {
                    Snackbar(
                        snackbarData = it,
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White
                    )
                }
            },

            topBar = {
                TopAppBar(
                    title = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }

        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0x55B7A6FF),
                                Color(0x558FD3FF)
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                            return@Column
                        }

                        // ✨ TITOLO CENTRATO + CORSIVO
                        Text(
                            text = "Cassetto #$slotId",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF16324F)
                        )

                        Spacer(Modifier.height(20.dp))

                        // 📊 DATI FORMATTATI
                        InfoRow("Stato", detail?.status ?: "-")
                        InfoRow(
                            "Chiave",
                            if (detail?.hasKey == true) "Presente" else "Assente"
                        )
                        InfoRow("Veicolo", detail?.vehicleName ?: "Nessuno")
                        InfoRow(
                            "Ultimo aggiornamento",
                            detail?.lastUpdated
                                ?.replace("T", " ")
                                ?.substring(0, 16)
                                ?: "-"
                        )

                        Spacer(Modifier.height(20.dp))

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

                                        message =
                                            if (response.isSuccessful && body?.success == true) {
                                                body.message
                                            } else {
                                                body?.message ?: "Errore comando slot"
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
                            shape = buttonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7EA8FF),
                                contentColor = Color.White
                            )
                        ) {
                            if (isActionLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    if (isOpen) "Chiudi cassetto" else "Apri cassetto",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        TextButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Indietro")
                        }
                    }
                }
            }
        }
    }
}