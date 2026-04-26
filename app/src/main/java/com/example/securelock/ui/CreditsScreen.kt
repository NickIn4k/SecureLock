package com.example.securelock.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.securelock.R
import com.example.securelock.ui.components.SecureLockMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(navController: NavHostController) {
    val cardShape = RoundedCornerShape(28.dp)

    Box(modifier = Modifier.fillMaxSize()) {

        // SFONDO
        Image(
            painter = painterResource(id = R.drawable.rossi_bg),
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
                            Color(0x99EAF4FF),
                            Color(0x88E8E2FF),
                            Color(0x99F4F7FB)
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
                                .background(Color(0x44FFFFFF))
                        ) {
                            SecureLockMenu(
                                navController = navController,
                                userId = null,
                                showCredits = true,
                                showDiagnostics = false,
                                showNewUser = false,
                                showLogout = true
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
                                Color(0x66B7A6FF),
                                Color(0x668FD3FF)
                            )
                        )
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "Crediti",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16324F)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SecureLock",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF5E6B7A)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "CREAZZO NICOLA",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF2E3A4B)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "BASSAN ANDREA",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF2E3A4B)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}