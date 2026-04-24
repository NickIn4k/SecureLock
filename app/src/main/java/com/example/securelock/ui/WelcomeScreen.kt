package com.example.securelock.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.ui.components.SecureLockMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    userId: Int,
    isAdmin: Boolean,
    navController: NavController
) {

    val cardShape = RoundedCornerShape(28.dp)

    Box(modifier = Modifier.fillMaxSize()) {

        // SFONDO GRADIENTE
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF0F6FF),
                            Color(0xFFEDE7FF),
                            Color(0xFFF7F9FC)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0x44FFFFFF))
                        ) {
                            SecureLockMenu(
                                navController = navController,
                                userId = userId,
                                showCredits = true,
                                showDiagnostics = isAdmin,
                                showNewUser = isAdmin,
                                showLogout = true,
                                onDiagnosticsClick = {
                                    // TODO
                                }
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = cardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(
                        1.dp,
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0x55B7A6FF),
                                Color(0x558FD3FF)
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Benvenuto",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16324F)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Utente #$userId",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF5E6B7A)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = if (isAdmin)
                                "Accesso amministratore attivo"
                            else
                                "Accesso standard",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF2E3A4B)
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}