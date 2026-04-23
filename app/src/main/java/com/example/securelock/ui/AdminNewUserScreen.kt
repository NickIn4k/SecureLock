package com.example.securelock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.CreateUserRequest
import com.example.securelock.ui.components.FaceCapturePreview
import kotlinx.coroutines.launch

@Composable
fun AdminNewUserScreen(navController: NavController) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var faceEmbedding by remember { mutableStateOf<List<Float>?>(null) }

    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Nuovo utente",
            style = MaterialTheme.typography.headlineSmall
        )

        // ---------- CARD DATI ----------
        Card {
            Column(Modifier.padding(16.dp)) {

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nome e cognome") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ---------- CARD FACE ----------
        Card {
            Column(Modifier.padding(16.dp)) {

                Text("Riconoscimento facciale")

                Spacer(Modifier.height(8.dp))

                Text(
                    if (faceEmbedding == null)
                        "Nessun volto acquisito"
                    else
                        "Volto acquisito!"
                )

                Spacer(Modifier.height(8.dp))

                // Bottone attiva scanner
                Button(
                    onClick = { isScanning = true },
                    enabled = !isScanning && faceEmbedding == null
                ) {
                    Text("Acquisisci volto")
                }

                Spacer(Modifier.height(8.dp))

                // Camera SOLO se attiva
                if (isScanning) {
                    FaceCapturePreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),

                        onStatusChange = {
                            message = it
                        },

                        onEmbeddingCaptured = { embedding ->
                            faceEmbedding = embedding
                            isScanning = false
                            message = "Volto acquisito correttamente"
                        }
                    )
                }
            }
        }

        // ---------- SALVATAGGIO ----------
        Button(
            onClick = {
                if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                    message = "Compila tutti i campi"
                    return@Button
                }

                val embedding = faceEmbedding
                if (embedding == null) {
                    message = "Acquisisci prima il volto"
                    return@Button
                }

                isLoading = true
                message = ""

                scope.launch {
                    try {
                        val response = ApiClient.api.createUser(
                            CreateUserRequest(
                                fullName = fullName,
                                username = username,
                                password = password,
                                faceEmbedding = embedding
                            )
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            message = "Utente creato correttamente"

                            // Aspetta 1 secondo e torna indietro
                            kotlinx.coroutines.delay(1000)

                            navController.popBackStack()
                        } else {
                            message = "Errore creazione utente"
                        }

                    } catch (e: Exception) {
                        message = "Errore connessione: ${e.message}"
                    }

                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salva utente")
        }

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (message.isNotBlank()) {
            Text(message)
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Indietro")
        }
    }
}