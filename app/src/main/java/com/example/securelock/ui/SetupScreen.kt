package com.example.securelock.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.securelock.network.ApiClient
import com.example.securelock.network.SetupInstallRequest
import com.example.securelock.storage.SetupPrefs
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    superAdminId: Int,
    onSetupCompleted: () -> Unit
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var buildingName by remember { mutableStateOf("") }
    var buildingAddress by remember { mutableStateOf("") }

    var lat by remember { mutableStateOf<Double?>(null) }
    var lng by remember { mutableStateOf<Double?>(null) }

    var adminFullName by remember { mutableStateOf("") }
    var adminUsername by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var deviceUid by remember { mutableStateOf("") }

    val slotsCount = 3
    var isLoading by remember { mutableStateOf(false) }

    fun showMessage(msg: String) {
        scope.launch {
            snackbarHostState.showSnackbar(msg)
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchLocation() {
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showMessage("Permesso posizione non concesso")
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    lng = location.longitude
                } else {
                    showMessage("Impossibile ottenere posizione")
                }
            }
    }

    // auto GPS al primo render
    LaunchedEffect(Unit) {
        fetchLocation()
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "Setup iniziale",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = buildingName,
                onValueChange = { buildingName = it },
                label = { Text("Nome edificio") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = buildingAddress,
                onValueChange = { buildingAddress = it },
                label = { Text("Indirizzo edificio") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = deviceUid,
                onValueChange = { deviceUid = it },
                label = { Text("Device UID (ESP)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Lat: ${lat ?: "caricamento..."}"
            )

            Text(
                text = "Lng: ${lng ?: "caricamento..."}"
            )

            Text("Numero slot: 3")

            OutlinedTextField(
                value = adminFullName,
                onValueChange = { adminFullName = it },
                label = { Text("Nome admin") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = adminUsername,
                onValueChange = { adminUsername = it },
                label = { Text("Username admin") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = adminPassword,
                onValueChange = { adminPassword = it },
                label = { Text("Password admin") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (
                        buildingName.isBlank() ||
                        buildingAddress.isBlank() ||
                        adminFullName.isBlank() ||
                        adminUsername.isBlank() ||
                        adminPassword.isBlank() ||
                        deviceUid.isBlank()
                    ) {
                        showMessage("Compila tutti i campi")
                        return@Button
                    }

                    scope.launch {
                        isLoading = true
                        try {

                            val response = ApiClient.api.setupInstall(
                                SetupInstallRequest(
                                    superAdminId = superAdminId,
                                    buildingName = buildingName,
                                    buildingAddress = buildingAddress,
                                    lat = lat ?: 45.4642, // fallback Milano
                                    lng = lng ?: 9.1900,
                                    adminFullName = adminFullName,
                                    adminUsername = adminUsername,
                                    adminPassword = adminPassword,
                                    numberOfSlots = slotsCount,
                                    deviceUid = deviceUid
                                )
                            )

                            val body = response.body()

                            if (response.isSuccessful && body?.success == true) {

                                body.buildingId?.let {
                                    SetupPrefs.saveBuildingId(context, it)
                                }

                                SetupPrefs.setSetupCompleted(context, true)

                                showMessage("Setup completato")

                                onSetupCompleted()

                            } else {
                                showMessage(body?.message ?: "Errore setup")
                            }

                        } catch (e: Exception) {
                            showMessage("Errore setup: ${e.message}")
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Completa setup")
            }

            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }
}