package com.example.securelock.ui.admin.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.securelock.ui.admin.VehicleAction

@Composable
fun AdminVehicleActionSelector(
    selectedAction: VehicleAction,
    onActionSelected: (VehicleAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        FilterChip(
            selected = selectedAction == VehicleAction.CREATE,
            onClick = { onActionSelected(VehicleAction.CREATE) },
            label = { Text("Crea") },
            colors = FilterChipDefaults.filterChipColors()
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 6.dp))

        FilterChip(
            selected = selectedAction == VehicleAction.ASSIGN,
            onClick = { onActionSelected(VehicleAction.ASSIGN) },
            label = { Text("Assegna") },
            colors = FilterChipDefaults.filterChipColors()
        )
    }
}