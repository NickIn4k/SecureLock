package com.example.securelock.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.securelock.R
import com.example.securelock.network.ApiClient
import com.example.securelock.network.LoginRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavHostController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            snackbarHostState.showSnackbar(message)
        }
    }

    val cardShape = RoundedCornerShape(28.dp)
    val fieldShape = RoundedCornerShape(18.dp)
    val buttonShape = RoundedCornerShape(18.dp)

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
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
                                .size(84.dp)
                                .alpha(0.95f)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16324F)
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Inserisci le tue credenziali",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF5E6B7A)
                        )

                        Spacer(Modifier.height(28.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            singleLine = true,
                            shape = fieldShape,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7EA8FF),
                                unfocusedBorderColor = Color(0xFFCED8E5),
                                focusedLabelColor = Color(0xFF7EA8FF),
                                unfocusedLabelColor = Color(0xFF7A8696),
                                cursorColor = Color(0xFF7EA8FF),
                                focusedTextColor = Color(0xFF1E2A3A),
                                unfocusedTextColor = Color(0xFF1E2A3A),
                                focusedContainerColor = Color(0xFFF9FBFF),
                                unfocusedContainerColor = Color(0xFFF9FBFF)
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            shape = fieldShape,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                TextButton(onClick = { showPassword = !showPassword }) {
                                    Text(
                                        text = if (showPassword) "Nascondi" else "Mostra",
                                        color = Color(0xFF7EA8FF)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7EA8FF),
                                unfocusedBorderColor = Color(0xFFCED8E5),
                                focusedLabelColor = Color(0xFF7EA8FF),
                                unfocusedLabelColor = Color(0xFF7A8696),
                                cursorColor = Color(0xFF7EA8FF),
                                focusedTextColor = Color(0xFF1E2A3A),
                                unfocusedTextColor = Color(0xFF1E2A3A),
                                focusedContainerColor = Color(0xFFF9FBFF),
                                unfocusedContainerColor = Color(0xFFF9FBFF)
                            )
                        )

                        Spacer(Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (username.isBlank() || password.isBlank()) {
                                    message = "Inserisci username e password"
                                    return@Button
                                }

                                isLoading = true
                                message = ""

                                scope.launch {
                                    try {
                                        val response = ApiClient.api.login(
                                            LoginRequest(username, password)
                                        )

                                        val body = response.body()

                                        if (response.isSuccessful && body?.success == true) {
                                            val userId = body.userId ?: 0
                                            val isAdmin = body.isAdmin ?: (body.userRole == "admin")

                                            message = "Login riuscito"

                                            navController.navigate("welcome/$userId/$isAdmin") {
                                                popUpTo(Routes.LOGIN) { inclusive = true }
                                            }
                                        } else {
                                            message = body?.message ?: "Credenziali non valide"
                                        }
                                    } catch (e: Exception) {
                                        message = "Errore: ${e.message ?: "connessione non disponibile"}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
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
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Accedi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}