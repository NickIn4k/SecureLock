package com.example.securelock.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.ui.Routes

@Composable
fun SecureLockMenu(
    navController: NavController,
    userId: Int? = null,
    showDiagnostics: Boolean = false,
    showNewUser: Boolean = false,
    showVehicles: Boolean = false,
    showCredits: Boolean = true,
    showLogout: Boolean = false,
    onDiagnosticsClick: () -> Unit = {},
    onVehiclesClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(
        onClick = { menuExpanded = true },
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Transparent)
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Apri menu",
            tint = Color(0xFF4D5E77)
        )
    }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFFF7F8FC),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        @Composable
        fun menuItem(
            text: String,
            icon: ImageVector,
            onClick: () -> Unit
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF2E3A4B)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF7EA8FF)
                    )
                },
                onClick = {
                    menuExpanded = false
                    onClick()
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            )
        }

        if (showDiagnostics) {
            menuItem("Diagnostica", Icons.Default.Security) {
                onDiagnosticsClick()
            }
        }

        if (showNewUser) {
            menuItem("Nuovo utente", Icons.Default.PersonAdd) {
                userId?.let { navController.navigate("admin_new_user/$it") }
            }
        }

        if (showCredits) {
            menuItem("Crediti", Icons.Default.Info) {
                navController.navigate(Routes.CREDITS)
            }
        }

        if (showVehicles) {
            menuItem("Veicoli", Icons.Default.DirectionsCar) {
                onVehiclesClick()
            }
        }

        if (showLogout) {
            Spacer(modifier = Modifier.height(4.dp))

            HorizontalDivider(
                color = Color(0xFFE0E3EB),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            DropdownMenuItem(
                text = {
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFB00020)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xFFB00020)
                    )
                },
                onClick = {
                    menuExpanded = false
                    onLogout()
                    navController.navigate(Routes.HOME) {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            )
        }
    }
}