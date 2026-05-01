package com.example.securelock.ui.admin.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.securelock.network.ApiClient
import com.example.securelock.network.AssignVehicleRequest
import com.example.securelock.network.AdminSlotItem
import com.example.securelock.network.VehicleItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignVehicleToSlotSection(
    adminUserId: Int,
    onMessage: (String) -> Unit = {}
) {
    var vehicles by remember { mutableStateOf<List<VehicleItem>>(emptyList()) }
    var slots by remember { mutableStateOf<List<AdminSlotItem>>(emptyList()) }

    var selectedVehicleId by remember { mutableStateOf<Int?>(null) }
    var selectedSlotId by remember { mutableStateOf<Int?>(null) }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(18.dp)

    fun reload() {
        scope.launch {
            isLoading = true
            try {
                val vResp = ApiClient.api.getAdminBuildingVehicles(adminUserId)
                val sResp = ApiClient.api.getAdminBuildingSlots(adminUserId)

                if (vResp.isSuccessful && vResp.body()?.success == true) {
                    vehicles = vResp.body()?.vehicles.orEmpty()
                }

                if (sResp.isSuccessful && sResp.body()?.success == true) {
                    slots = sResp.body()?.slots.orEmpty()
                }
            } catch (e: Exception) {
                onMessage("Errore caricamento dati: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(adminUserId) {
        reload()
    }

    var vehicleExpanded by remember { mutableStateOf(false) }
    var slotExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Assegna veicolo a cassetto",
            style = MaterialTheme.typography.titleLarge
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            ExposedDropdownMenuBox(
                expanded = vehicleExpanded,
                onExpandedChange = { vehicleExpanded = !vehicleExpanded }
            ) {
                OutlinedTextField(
                    value = vehicles.firstOrNull { it.id == selectedVehicleId }?.let { "${it.name} (${it.type})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Veicolo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = shape
                )

                ExposedDropdownMenu(
                    expanded = vehicleExpanded,
                    onDismissRequest = { vehicleExpanded = false }
                ) {
                    vehicles.forEach { vehicle ->
                        DropdownMenuItem(
                            text = { Text("${vehicle.name} (${vehicle.type})") },
                            onClick = {
                                selectedVehicleId = vehicle.id
                                vehicleExpanded = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = slotExpanded,
                onExpandedChange = { slotExpanded = !slotExpanded }
            ) {
                OutlinedTextField(
                    value = slots.firstOrNull { it.slotId == selectedSlotId }?.let { "Slot ${it.slotId}" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Cassetto") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = shape
                )

                ExposedDropdownMenu(
                    expanded = slotExpanded,
                    onDismissRequest = { slotExpanded = false }
                ) {
                    slots.forEach { slot ->
                        DropdownMenuItem(
                            text = { Text("Slot ${slot.slotId} - ${slot.status}") },
                            onClick = {
                                selectedSlotId = slot.slotId
                                slotExpanded = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val vehicleId = selectedVehicleId
                    val slotId = selectedSlotId

                    if (vehicleId == null || slotId == null) {
                        onMessage("Seleziona veicolo e cassetto")
                        return@Button
                    }

                    scope.launch {
                        isSaving = true
                        try {
                            val response = ApiClient.api.assignVehicleToSlot(
                                AssignVehicleRequest(
                                    adminUserId = adminUserId,
                                    slotId = slotId,
                                    vehicleId = vehicleId
                                )
                            )

                            val body = response.body()
                            if (response.isSuccessful && body?.success == true) {
                                onMessage("Veicolo assegnato")
                                reload()
                            } else {
                                onMessage(body?.message ?: "Errore assegnazione")
                            }
                        } catch (e: Exception) {
                            onMessage("Errore: ${e.message}")
                        } finally {
                            isSaving = false
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = shape
            ) {
                Text("Assegna")
            }
        }
    }
}