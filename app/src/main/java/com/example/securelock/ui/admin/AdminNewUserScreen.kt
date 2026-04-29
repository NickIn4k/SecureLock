package com.example.securelock.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.R
import com.example.securelock.network.ApiClient
import com.example.securelock.network.DeleteUserRequest
import com.example.securelock.ui.admin.components.AdminActionSelector
import com.example.securelock.ui.admin.components.CreateUserSection
import com.example.securelock.ui.admin.components.DeleteUserSection
import com.example.securelock.ui.components.SecureLockMenu
import kotlinx.coroutines.launch

// Enum per distinguere se il messaggio è un errore o un successo
private enum class SnackbarType { SUCCESS, ERROR }
private data class SnackbarData(val message: String, val type: SnackbarType)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNewUserScreen(
    navController: NavController,
    userId: Int
) {
    var selectedAction by remember { mutableStateOf(AdminAction.CREATE) }
    val cardShape = RoundedCornerShape(28.dp)

    val snackbarHostState = remember { SnackbarHostState() }
    var currentSnackbar by remember { mutableStateOf<SnackbarData?>(null) }
    var refreshUsers by remember { mutableStateOf(0) }
    var lastDeletedUserId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    // Helper per mostrare snackbar con tipo
    suspend fun showSnackbar(message: String, type: SnackbarType) {
        currentSnackbar = SnackbarData(message, type)
        snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Sfondo immagine ───────────────────────────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.shadows_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay semitrasparente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x55FFFFFF))
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    // Colore diverso in base al tipo di messaggio
                    val isError = currentSnackbar?.type == SnackbarType.ERROR
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (isError) Color(0xFFB00020) else Color(0xFF1B5E20),
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
                                showDiagnostics = false,
                                showNewUser = false,
                                showLogout = true
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
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ── Titolo sullo sfondo ───────────────────────────────────────
                Text(
                    text = "Gestione utenti",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF16324F)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Scegli un'azione dal menu qui sotto.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4A5568)
                )

                Spacer(Modifier.height(20.dp))

                // ── Card glassmorphism ────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(cardShape)
                ) {
                    // Layer vetro
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0x66FFFFFF))
                    )
                    // Layer gradiente
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp) // ← spazio ridotto
                    ) {

                        AdminActionSelector(
                            selectedAction = selectedAction,
                            onActionSelected = { selectedAction = it }
                        )

                        when (selectedAction) {

                            AdminAction.CREATE -> {
                                CreateUserSection(
                                    currentUserId = userId,
                                    onUserCreated = {
                                        navController.popBackStack()
                                    },
                                    onMessage = { msg ->
                                        scope.launch {
                                            // I messaggi di creazione sono successi
                                            showSnackbar(msg, SnackbarType.SUCCESS)
                                        }
                                    }
                                )
                            }

                            AdminAction.DELETE -> {
                                DeleteUserSection(
                                    currentUserId = userId,
                                    refreshTrigger = refreshUsers,
                                    deletedUserId = lastDeletedUserId,
                                    onDeleteClick = { user ->
                                        scope.launch {
                                            try {
                                                lastDeletedUserId = user.id
                                                val response = ApiClient.api.deleteUser(
                                                    DeleteUserRequest(userId, user.id)
                                                )
                                                val body = response.body()
                                                if (response.isSuccessful && body?.success == true) {
                                                    showSnackbar("Utente eliminato", SnackbarType.SUCCESS)
                                                    refreshUsers++
                                                } else {
                                                    showSnackbar(
                                                        body?.message ?: "Errore eliminazione",
                                                        SnackbarType.ERROR
                                                    )
                                                    lastDeletedUserId = null
                                                }
                                            } catch (e: Exception) {
                                                showSnackbar("Errore: ${e.message}", SnackbarType.ERROR)
                                            }
                                        }
                                    }
                                )
                            }

                            AdminAction.EDIT -> {
                                Text(
                                    text = "TODO: modifica utente",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF5E6B7A)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}