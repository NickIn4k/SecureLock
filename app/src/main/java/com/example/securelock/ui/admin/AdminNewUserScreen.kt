package com.example.securelock.ui.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.DeleteUserRequest
import com.example.securelock.ui.admin.components.AdminActionSelector
import com.example.securelock.ui.admin.components.CreateUserSection
import com.example.securelock.ui.admin.components.DeleteUserSection
import com.example.securelock.ui.components.SecureLockMenu
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminNewUserScreen(
    navController: NavController,
    userId: Int
) {
    var selectedAction by remember { mutableStateOf(AdminAction.CREATE) }
    val cardShape = RoundedCornerShape(28.dp)

    val snackbarHostState = remember { SnackbarHostState() }
    var refreshUsers by remember { mutableStateOf(0) }
    var lastDeletedUserId by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFEAF4FF),
                            Color(0xFFEDE7FF),
                            Color(0xFFF7F9FC)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Color.Red,
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
                                showDiagnostics = false,
                                showNewUser = false,
                                showLogout = true
                            )
                        }
                    }
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
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
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
                        Text(
                            text = "Gestione utenti",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color(0xFF16324F)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Scegli un'azione dal menu qui sotto.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5E6B7A)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        AdminActionSelector(
                            selectedAction = selectedAction,
                            onActionSelected = { selectedAction = it }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (selectedAction) {

                            AdminAction.CREATE -> {
                                CreateUserSection(
                                    currentUserId = userId,
                                    onUserCreated = {
                                        navController.popBackStack()
                                    },
                                    onMessage = { msg ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar(msg)
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
                                                    snackbarHostState.showSnackbar("Utente eliminato")
                                                    refreshUsers++
                                                } else {
                                                    snackbarHostState.showSnackbar(
                                                        body?.message ?: "Errore eliminazione"
                                                    )

                                                    lastDeletedUserId = null
                                                }

                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Errore: ${e.message}")
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