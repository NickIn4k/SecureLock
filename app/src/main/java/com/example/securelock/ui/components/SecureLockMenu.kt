package com.example.securelock.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.example.securelock.ui.Routes

@Composable
fun SecureLockMenu(
    navController: NavController,
    showDiagnostics: Boolean = false,
    showNewUser: Boolean = false,
    showCredits: Boolean = true,
    onDiagnosticsClick: () -> Unit = {}
) {
    var menuExpanded by remember { mutableStateOf(false) }

    IconButton(onClick = { menuExpanded = true }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Apri menu"
        )
    }

    DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
    ) {

        if (showDiagnostics) {
            DropdownMenuItem(
                text = { Text("Diagnostica") },
                leadingIcon = {
                    Icon(Icons.Default.Security, contentDescription = null)
                },
                onClick = {
                    menuExpanded = false
                    onDiagnosticsClick()
                }
            )
        }

        if (showNewUser) {
            DropdownMenuItem(
                text = { Text("Inserimento nuovo utente") },
                leadingIcon = {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                },
                onClick = {
                    menuExpanded = false
                    navController.navigate(Routes.ADMIN_NEW_USER)
                }
            )
        }

        if (showCredits) {
            DropdownMenuItem(
                text = { Text("Crediti") },
                leadingIcon = {
                    Icon(Icons.Default.Info, contentDescription = null)
                },
                onClick = {
                    menuExpanded = false
                    navController.navigate(Routes.CREDITS)
                }
            )
        }
    }
}