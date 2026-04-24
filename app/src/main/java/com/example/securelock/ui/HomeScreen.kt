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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.R
import com.example.securelock.ui.components.SecureLockMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val cardShape = RoundedCornerShape(28.dp)
    val buttonShape = RoundedCornerShape(18.dp)

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(id = R.drawable.bg_clouds),
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
            modifier = Modifier.fillMaxSize(),
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
                                showCredits = true,
                                showDiagnostics = false,
                                showNewUser = false,
                                showLogout = false
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.securelock_logo),
                            contentDescription = "SecureLock logo",
                            modifier = Modifier
                                .size(88.dp)
                                .alpha(0.95f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "SecureLock",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16324F)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Scegli il metodo di accesso",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF5E6B7A)
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = { navController.navigate(Routes.FACE_AUTH) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = buttonShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7EA8FF),
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Text(
                                text = "Riconoscimento facciale",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = { navController.navigate(Routes.LOGIN) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = buttonShape,
                            border = BorderStroke(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFB7A6FF),
                                        Color(0xFF8FD3FF)
                                    )
                                )
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF4D5E77)
                            )
                        ) {
                            Text(
                                text = "Login classico",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}