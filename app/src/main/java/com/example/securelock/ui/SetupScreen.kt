package com.example.securelock.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.securelock.R
import com.example.securelock.network.ApiClient
import com.example.securelock.network.SetupInstallRequest
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

    LaunchedEffect(Unit) {
        fetchLocation()
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.setup_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x88EAF4FF),
                            Color(0x66EDE7FF),
                            Color(0x99F7F9FC)
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
                        containerColor = Color(0xFFD32F2F),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Text(
                    text = "Setup iniziale",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16324F),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Configura il tuo sistema",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5E6B7A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        OutlinedTextField(
                            value = buildingName,
                            onValueChange = { buildingName = it },
                            label = { Text("Nome edificio") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7EA8FF),
                                unfocusedBorderColor = Color(0xFFCED8E5),
                                focusedLabelColor = Color(0xFF7EA8FF),
                                unfocusedLabelColor = Color(0xFF7A8696),
                                cursorColor = Color(0xFF7EA8FF),
                                focusedTextColor = Color(0xFF1E2A3A),
                                unfocusedTextColor = Color(0xFF1E2A3A),
                                focusedContainerColor = Color(0xFFF9FBFF),
                                unfocusedContainerColor = Color(0xFFF9FBFF)
                            )
                        )

                        OutlinedTextField(
                            value = buildingAddress,
                            onValueChange = { buildingAddress = it },
                            label = { Text("Indirizzo edificio") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7EA8FF),
                                unfocusedBorderColor = Color(0xFFCED8E5),
                                focusedLabelColor = Color(0xFF7EA8FF),
                                unfocusedLabelColor = Color(0xFF7A8696),
                                cursorColor = Color(0xFF7EA8FF),
                                focusedTextColor = Color(0xFF1E2A3A),
                                unfocusedTextColor = Color(0xFF1E2A3A),
                                focusedContainerColor = Color(0xFFF9FBFF),
                                unfocusedContainerColor = Color(0xFFF9FBFF)
                            )
                        )

                        OutlinedTextField(
                            value = deviceUid,
                            onValueChange = { deviceUid = it },
                            label = { Text("Device UID (ESP)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7EA8FF),
                                unfocusedBorderColor = Color(0xFFCED8E5),
                                focusedLabelColor = Color(0xFF7EA8FF),
                                unfocusedLabelColor = Color(0xFF7A8696),
                                cursorColor = Color(0xFF7EA8FF),
                                focusedTextColor = Color(0xFF1E2A3A),
                                unfocusedTextColor = Color(0xFF1E2A3A),
                                focusedContainerColor = Color(0xFFF9FBFF),
                                unfocusedContainerColor = Color(0xFFF9FBFF)
                            )
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Lat: ${lat ?: "caricamento..."}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF16324F),
                                textAlign = TextAlign.Left
                            )

                            Text(
                                text = "Lng: ${lng ?: "caricamento..."}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF16324F),
                                textAlign = TextAlign.Left
                            )

                            Text(
                                text = "Numero slot: 3",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF16324F),
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Left
                            )
                        }

                        OutlinedTextField(
                            value = adminFullName,
                            onValueChange = { adminFullName = it },
                            label = { Text("Nome admin") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7EA8FF),
                                unfocusedBorderColor = Color(0xFFCED8E5),
                                focusedLabelColor = Color(0xFF7EA8FF),
                                unfocusedLabelColor = Color(0xFF7A8696),
                                cursorColor = Color(0xFF7EA8FF),
                                focusedTextColor = Color(0xFF1E2A3A),
                                unfocusedTextColor = Color(0xFF1E2A3A),
                                focusedContainerColor = Color(0xFFF9FBFF),
                                unfocusedContainerColor = Color(0xFFF9FBFF)
                            )
                        )

                        OutlinedTextField(
                            value = adminUsername,
                            onValueChange = { adminUsername = it },
                            label = { Text("Username admin") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7EA8FF),
                                unfocusedBorderColor = Color(0xFFCED8E5),
                                focusedLabelColor = Color(0xFF7EA8FF),
                                unfocusedLabelColor = Color(0xFF7A8696),
                                cursorColor = Color(0xFF7EA8FF),
                                focusedTextColor = Color(0xFF1E2A3A),
                                unfocusedTextColor = Color(0xFF1E2A3A),
                                focusedContainerColor = Color(0xFFF9FBFF),
                                unfocusedContainerColor = Color(0xFFF9FBFF)
                            )
                        )

                        OutlinedTextField(
                            value = adminPassword,
                            onValueChange = { adminPassword = it },
                            label = { Text("Password admin") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7EA8FF),
                                unfocusedBorderColor = Color(0xFFCED8E5),
                                focusedLabelColor = Color(0xFF7EA8FF),
                                unfocusedLabelColor = Color(0xFF7A8696),
                                cursorColor = Color(0xFF7EA8FF),
                                focusedTextColor = Color(0xFF1E2A3A),
                                unfocusedTextColor = Color(0xFF1E2A3A),
                                focusedContainerColor = Color(0xFFF9FBFF),
                                unfocusedContainerColor = Color(0xFFF9FBFF)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                                lat = lat ?: 45.4642,
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7EA8FF),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Completa setup")
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}