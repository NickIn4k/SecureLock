package com.example.securelock.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.securelock.R
import com.example.securelock.network.ApiClient
import com.example.securelock.network.SlotActionRequest
import com.example.securelock.network.SlotDetailResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotDetailScreen(
    userId: Int,
    deviceId: Int,
    slotId: Int,
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    var detail by remember { mutableStateOf<SlotDetailResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isActionLoading by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    suspend fun showError(message: String) {
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
    }

    suspend fun fetchDetail() {
        try {
            val response = ApiClient.api.getSlotDetail(userId, deviceId, slotId)
            val body = response.body()

            if (response.isSuccessful && body?.success == true) {
                detail = body
            } else {
                showError(body?.message ?: "Errore caricamento slot")
            }
        } catch (e: Exception) {
            showError("Errore connessione: ${e.message}")
        }
    }

    LaunchedEffect(userId, deviceId, slotId) {
        isLoading = true
        fetchDetail()
        isLoading = false
    }

    val isOpen = detail?.status.equals("open", ignoreCase = true)

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.bg_welcome),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x55FFFFFF))
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color(0xFFB00020),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Indietro",
                                tint = Color(0xFF16324F)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF16324F))
                } else {
                    Text(
                        text = "Cassetto $slotId",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF16324F),
                        letterSpacing = (-1).sp
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = detail?.vehicleName ?: "Nessun veicolo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4A5568)
                    )

                    Spacer(Modifier.height(40.dp))

                    GlassInfoCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            GlassInfoRow(
                                icon = Icons.Default.Lock,
                                label = "Stato",
                                value = if (isOpen) "Aperto" else "Chiuso",
                                valueColor = if (isOpen) Color(0xFF027A48) else Color(0xFF4A5568)
                            )

                            HorizontalDivider(color = Color(0x22000000))

                            GlassInfoRow(
                                icon = Icons.Default.Key,
                                label = "Chiave",
                                value = if (detail?.hasKey == true) "Presente" else "Assente",
                                valueColor = if (detail?.hasKey == true)
                                    Color(0xFF027A48) else Color(0xFFB00020)
                            )

                            HorizontalDivider(color = Color(0x22000000))

                            GlassInfoRow(
                                icon = Icons.Default.DirectionsCar,
                                label = "Veicolo",
                                value = detail?.vehicleName ?: "Nessuno",
                                valueColor = Color(0xFF16324F)
                            )

                            HorizontalDivider(color = Color(0x22000000))

                            GlassInfoRow(
                                icon = Icons.Default.Schedule,
                                label = "Ultimo aggiornamento",
                                value = detail?.lastUpdated
                                    ?.replace("T", " ")
                                    ?.substring(0, 16)
                                    ?: "-",
                                valueColor = Color(0xFF4A5568)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isActionLoading = true
                                try {
                                    val action = "open"

                                    val response = ApiClient.api.slotAction(
                                        SlotActionRequest(
                                            userId = userId,
                                            deviceId = deviceId,
                                            slotId = slotId,
                                            action = action
                                        )
                                    )

                                    val body = response.body()

                                    if (response.isSuccessful && body?.success == true) {
                                        snackbarHostState.showSnackbar(
                                            message = body.message,
                                            duration = SnackbarDuration.Short
                                        )

                                        // Aggiornamento ottimistico subito
                                        detail = detail?.copy(
                                            status = if (action == "open") "open" else "closed"
                                        )

                                        // Polling breve per vedere lo stato reale aggiornato dal DB
                                        scope.launch {
                                            repeat(8) {
                                                delay(1000)
                                                fetchDetail()
                                                if (detail?.status.equals("closed", ignoreCase = true)) return@launch
                                            }
                                        }
                                    } else {
                                        showError(body?.message ?: "Errore comando slot")
                                    }
                                } catch (e: Exception) {
                                    showError("Errore connessione: ${e.message}")
                                } finally {
                                    isActionLoading = false
                                }
                            }
                        },
                        enabled = !isActionLoading && !isOpen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16324F),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0x8816324F)
                        )
                    ) {
                        if (isActionLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Apri cassetto",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlassInfoCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0x66FFFFFF))
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x55FFFFFF),
                            Color(0x11FFFFFF)
                        )
                    )
                )
        )

        content()
    }
}

@Composable
fun GlassInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x1516324F)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF16324F),
                modifier = Modifier.size(20.dp)
            )
        }

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF7A8A9A),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}