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
import com.example.securelock.ui.admin.AdminAction

@Composable
fun AdminActionSelector(
    selectedAction: AdminAction,
    onActionSelected: (AdminAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        FilterChip(
            selected = selectedAction == AdminAction.CREATE,
            onClick = { onActionSelected(AdminAction.CREATE) },
            label = { Text("Crea") },
            colors = FilterChipDefaults.filterChipColors()
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 6.dp))

        FilterChip(
            selected = selectedAction == AdminAction.DELETE,
            onClick = { onActionSelected(AdminAction.DELETE) },
            label = { Text("Elimina") },
            colors = FilterChipDefaults.filterChipColors()
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(horizontal = 6.dp))

        FilterChip(
            selected = selectedAction == AdminAction.EDIT,
            onClick = { onActionSelected(AdminAction.EDIT) },
            label = { Text("Modifica") },
            colors = FilterChipDefaults.filterChipColors()
        )
    }
}