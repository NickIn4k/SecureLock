package com.example.securelock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.securelock.ui.components.SecureLockMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    userId: Int,
    isAdmin: Boolean,
    navController: NavController
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SecureLock") },
                actions = {
                    SecureLockMenu(
                        navController = navController,
                        showCredits = true,
                        showDiagnostics = isAdmin,
                        showNewUser = isAdmin,
                        onDiagnosticsClick = {
                            // TODO: HTTP page
                        }
                    )
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Benvenuto utente $userId")
        }
    }
}