package com.example.securelock.ui.admin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.securelock.network.ApiClient
import com.example.securelock.network.DeleteVehicleRequest
import com.example.securelock.network.VehicleItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteVehicleSection(
    adminUserId: Int,
    onVehicleDeleted: () -> Unit = {},
    onMessage: (String) -> Unit = {}
) {
    var vehicles by remember { mutableStateOf<List<VehicleItem>>(emptyList()) }
    var selectedVehicle by remember { mutableStateOf<VehicleItem?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDeleting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(18.dp)

    fun reloadVehicles() {
        scope.launch {
            isLoading = true
            try {
                val response = ApiClient.api.getAdminBuildingVehicles(adminUserId)
                val body = response.body()
                if (response.isSuccessful && body?.success == true) {
                    vehicles = body.vehicles
                    selectedVehicle = null
                } else {
                    onMessage(body?.message ?: "Errore caricamento veicoli")
                }
            } catch (e: Exception) {
                onMessage("Errore caricamento veicoli: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(adminUserId) {
        reloadVehicles()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Elimina veicolo",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF16324F)
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            vehicles.forEach { vehicle ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = shape,
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFF)),
                    border = BorderStroke(1.dp, Color(0x22000000))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedVehicle?.id == vehicle.id,
                            onClick = { selectedVehicle = vehicle }
                        )

                        Spacer(Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(vehicle.name, style = MaterialTheme.typography.bodyLarge)
                            Text(vehicle.type, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val vehicle = selectedVehicle ?: run {
                    onMessage("Seleziona un veicolo")
                    return@Button
                }

                scope.launch {
                    isDeleting = true
                    try {
                        val response = ApiClient.api.deleteVehicle(
                            DeleteVehicleRequest(
                                adminUserId = adminUserId,
                                vehicleId = vehicle.id
                            )
                        )

                        val body = response.body()
                        if (response.isSuccessful && body?.success == true) {
                            onVehicleDeleted()
                            reloadVehicles()
                            onMessage("Veicolo eliminato")
                        } else {
                            onMessage(body?.message ?: "Errore eliminazione veicolo")
                        }
                    } catch (e: Exception) {
                        onMessage("Errore: ${e.message}")
                    } finally {
                        isDeleting = false
                    }
                }
            },
            enabled = !isDeleting,
            modifier = Modifier.fillMaxWidth(),
            shape = shape
        ) {
            Text("Elimina veicolo")
        }
    }
}