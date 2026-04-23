package com.example.securelock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.LoginRequest
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavHostController) {
    // Variabili di stato
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // Unico scope con tutte le info su coroutine
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Login",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(24.dp))

            // USERNAME
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // PASSWORD
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // LOGIN
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        message = "Inserisci username e password"
                        return@Button
                    }

                    isLoading = true
                    message = ""

                    scope.launch {
                        message = try {
                            val response = ApiClient.api.login(
                                LoginRequest(username, password)
                            )

                            if (response.isSuccessful && response.body()?.success == true) {
                                "Login riuscito"
                                // TODO GESTIONE HTTP
                            } else {
                                "Credenziali non valide"
                            }
                        } catch (e: Exception) {
                            "Errore: " + e.message
                        }

                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accedi")
            }

            Spacer(Modifier.height(16.dp))

            // LOADING
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
            }

            // MESSAGGIO
            if (message.isNotEmpty()) {
                Text(
                    text = message,
                    fontSize = 14.sp
                )
            }
        }
    }
}