package com.example.securelock.ui.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.securelock.network.ApiClient
import com.example.securelock.network.CreateVehicleRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVehicleSection(
    adminUserId: Int,
    onVehicleCreated: () -> Unit = {},
    onMessage: (String) -> Unit = {}
) {
    var vehicleName by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Crea veicolo",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF16324F)
        )

        OutlinedTextField(
            value = vehicleName,
            onValueChange = { vehicleName = it },
            label = { Text("Nome veicolo") },
            modifier = Modifier.fillMaxWidth(),
            shape = shape
        )

        OutlinedTextField(
            value = vehicleType,
            onValueChange = { vehicleType = it },
            label = { Text("Tipo veicolo") },
            modifier = Modifier.fillMaxWidth(),
            shape = shape
        )

        Button(
            onClick = {
                if (vehicleName.isBlank() || vehicleType.isBlank()) {
                    onMessage("Compila nome e tipo veicolo")
                    return@Button
                }

                scope.launch {
                    isLoading = true
                    try {
                        val response = ApiClient.api.createVehicle(
                            CreateVehicleRequest(
                                adminUserId = adminUserId,
                                name = vehicleName.trim(),
                                type = vehicleType.trim()
                            )
                        )

                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            vehicleName = ""
                            vehicleType = ""
                            onVehicleCreated()
                        } else {
                            onMessage(body?.message ?: "Errore creazione veicolo")
                        }
                    } catch (e: Exception) {
                        onMessage("Errore: ${e.message}")
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = shape
        ) {
            Text("Salva veicolo")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }
    }
}