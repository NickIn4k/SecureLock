package com.example.securelock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.securelock.network.ApiClient
import com.example.securelock.network.CreateAdminRequest
import com.example.securelock.network.CreateBuildingRequest
import com.example.securelock.network.CreateSlotsRequest
import com.example.securelock.storage.SetupPrefs
import kotlinx.coroutines.launch

enum class SetupStep {
    ADMIN,
    BUILDING,
    SLOTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    onSetupCompleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var step by remember { mutableStateOf(SetupStep.ADMIN) }

    var adminFullName by remember { mutableStateOf("") }
    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }

    var buildingName by remember { mutableStateOf("") }
    var buildingAddress by remember { mutableStateOf("") }

    var slotsCount by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var adminId by remember { mutableStateOf<Int?>(null) }
    var buildingId by remember { mutableStateOf<Int?>(null) }

    fun showMessage(msg: String) {
        scope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configurazione iniziale",
                style = MaterialTheme.typography.headlineMedium
            )

            when (step) {
                SetupStep.ADMIN -> {
                    Text("Crea l'account admin")

                    OutlinedTextField(
                        value = adminFullName,
                        onValueChange = { adminFullName = it },
                        label = { Text("Nome e cognome") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = adminUsername,
                        onValueChange = { adminUsername = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (adminFullName.isBlank() || adminUsername.isBlank() || adminPassword.isBlank()) {
                                showMessage("Compila tutti i campi admin")
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                try {
                                    val response = ApiClient.api.createAdminUser(
                                        CreateAdminRequest(
                                            fullName = adminFullName,
                                            username = adminUsername,
                                            password = adminPassword
                                        )
                                    )

                                    val body = response.body()
                                    if (response.isSuccessful && body?.success == true) {
                                        val createdAdminId = body.userId
                                        if (createdAdminId == null) {
                                            showMessage("Admin creato ma ID mancante")
                                            return@launch
                                        }

                                        adminId = createdAdminId
                                        SetupPrefs.saveAdminId(context, createdAdminId)
                                        step = SetupStep.BUILDING
                                        showMessage("Admin creato correttamente")
                                    } else {
                                        showMessage(body?.message ?: "Errore creazione admin")
                                    }
                                } catch (e: Exception) {
                                    showMessage("Errore creazione admin: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Crea admin")
                    }
                }

                SetupStep.BUILDING -> {
                    Text("Crea la casa")

                    OutlinedTextField(
                        value = buildingName,
                        onValueChange = { buildingName = it },
                        label = { Text("Nome casa") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = buildingAddress,
                        onValueChange = { buildingAddress = it },
                        label = { Text("Indirizzo (opzionale)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (buildingName.isBlank()) {
                                showMessage("Inserisci il nome della casa")
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                try {
                                    val response = ApiClient.api.createBuilding(
                                        CreateBuildingRequest(
                                            name = buildingName,
                                            address = buildingAddress.ifBlank { null }
                                        )
                                    )

                                    val body = response.body()
                                    if (response.isSuccessful && body?.success == true) {
                                        val createdBuildingId = body.buildingId ?: body.userId
                                        if (createdBuildingId == null) {
                                            showMessage("Casa creata ma ID mancante")
                                            return@launch
                                        }

                                        buildingId = createdBuildingId
                                        SetupPrefs.saveBuildingId(context, createdBuildingId)
                                        step = SetupStep.SLOTS
                                        showMessage("Casa creata correttamente")
                                    } else {
                                        showMessage(body?.message ?: "Errore creazione casa")
                                    }
                                } catch (e: Exception) {
                                    showMessage("Errore creazione casa: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Crea casa")
                    }
                }

                SetupStep.SLOTS -> {
                    Text("Inserisci il numero dei cassetti")

                    OutlinedTextField(
                        value = slotsCount,
                        onValueChange = { slotsCount = it },
                        label = { Text("Numero cassetti") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val count = slotsCount.toIntOrNull()
                            if (count == null || count <= 0) {
                                showMessage("Inserisci un numero valido")
                                return@Button
                            }

                            val bId = buildingId ?: SetupPrefs.getBuildingId(context)
                            if (bId == -1) {
                                showMessage("Building ID mancante")
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                try {
                                    val response = ApiClient.api.createAdminSlots(
                                        CreateSlotsRequest(
                                            buildingId = bId,
                                            numberOfSlots = count
                                        )
                                    )

                                    val body = response.body()
                                    if (response.isSuccessful && body?.success == true) {
                                        SetupPrefs.setSetupCompleted(context, true)
                                        showMessage("Setup completato")
                                        onSetupCompleted()
                                    } else {
                                        showMessage(body?.message ?: "Errore creazione cassetti")
                                    }
                                } catch (e: Exception) {
                                    showMessage("Errore creazione cassetti: ${e.message}")
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Completa setup")
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}