package com.example.securelock.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.example.securelock.R
import com.example.securelock.network.ApiClient
import com.example.securelock.network.BackLoginRequest
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
    val context = LocalContext.current

    var dashboard by remember { mutableStateOf<DashboardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // ── Snackbar ──────────────────────────────────────────────────────────────
    val snackbarHostState = remember { SnackbarHostState() }

    // Funzione helper per mostrare errori in rosso
    suspend fun showError(message: String) {
        snackbarHostState.showSnackbar(
            message = message,
            duration = SnackbarDuration.Short
        )
    }

    LaunchedEffect(userId) {
        isLoading = true
        try {
            val response = ApiClient.api.getDashboard(userId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                dashboard = body
            } else {
                showError(body?.message ?: "Errore caricamento dashboard")
            }
        } catch (e: Exception) {
            showError("Errore connessione: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    val userName = dashboard?.userName ?: ""

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Sfondo immagine ───────────────────────────────────────────────────
        // Metti bg_welcome.jpg in res/drawable/
        Image(
            painter = painterResource(id = R.drawable.bg_welcome),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay semitrasparente chiaro sopra lo sfondo
        // per leggibilità del testo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x55FFFFFF))
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    // Snackbar rossa per gli errori
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x33FFFFFF))
                        ) {
                            SecureLockMenu(
                                navController = navController,
                                userId = userId,
                                showCredits = true,
                                showDiagnostics = isAdmin,
                                showNewUser = isAdmin,
                                showLogout = true,
                                onDiagnosticsClick = {
                                    scope.launch {
                                        try {
                                            val response = ApiClient.api.adminLogin(
                                                BackLoginRequest(userId)
                                            )
                                            val body = response.body()
                                            if (response.isSuccessful && body?.success == true) {
                                                val url = body.url
                                                if (!url.isNullOrBlank()) {
                                                    val intent = Intent(
                                                        Intent.ACTION_VIEW,
                                                        url.toUri()
                                                    )
                                                    context.startActivity(intent)
                                                }
                                            } else {
                                                showError(body?.message ?: "Errore apertura dashboard")
                                            }
                                        } catch (e: Exception) {
                                            showError("Errore: ${e.message}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->

            // ── Contenuto centrato ────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Benvenuto direttamente sullo sfondo ───────────────────────
                Text(
                    text = "Benvenuto",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16324F),
                    letterSpacing = (-1).sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4A5568)
                )

                Spacer(Modifier.height(48.dp))

                // ── Cassetti ──────────────────────────────────────────────────
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF16324F))
                } else {
                    val slots = dashboard?.slots.orEmpty().take(3)

                    if (slots.isEmpty()) {
                        Text(
                            text = "Nessun cassetto assegnato",
                            color = Color(0xFF4A5568),
                            fontSize = 15.sp
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            slots.forEach { slot ->
                                GlassSlotCard(
                                    slot = slot,
                                    onClick = {
                                        navController.navigate(
                                            "slot_detail/$userId/${slot.slotId}"
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Card cassetto con glassmorphism ───────────────────────────────────────────
@Composable
fun GlassSlotCard(
    slot: DashboardSlot,
    onClick: () -> Unit
) {
    val hasKey = slot.hasKey

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        // Layer 1 — sfondo sfocato (effetto glass)
        // Il blur simula il vetro smerigliato
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color(0x55FFFFFF))
        )

        // Layer 2 — gradiente sottile sopra il vetro
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x22FFFFFF)
                        )
                    )
                )
        )

        // Layer 3 — bordo luminoso tipico del glassmorphism
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent)
        )

        // Contenuto della card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Icona cassetto con cerchio colorato
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (hasKey) Color(0x33027A48)
                            else Color(0x33B00020)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (hasKey) Icons.Default.Key else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (hasKey) Color(0xFF027A48) else Color(0xFFB00020),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    // Nome cassetto
                    Text(
                        text = "Cassetto ${slot.slotId}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF16324F)
                    )

                    Spacer(Modifier.height(3.dp))

                    // Stato chiave
                    Text(
                        text = if (hasKey) "Chiave presente" else "Chiave assente",
                        fontSize = 13.sp,
                        color = if (hasKey) Color(0xFF027A48) else Color(0xFFB00020),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Freccia destra
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF4A5568),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}