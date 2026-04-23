package com.example.securelock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.CreateUserRequest
import com.example.securelock.ui.components.FaceCapturePreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AdminNewUserScreen(navController: NavController) {

    // ----------- STATE -----------
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var faceEmbedding by remember { mutableStateOf<List<Float>?>(null) }

    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    // Selezione cassetti
    var selectedDrawers by remember { mutableStateOf(setOf<Int>()) }

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

        // ----------- CARD DATI UTENTE -----------
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

        // ----------- CARD FACE -----------
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

                Button(
                    onClick = { isScanning = true },
                    enabled = !isScanning && faceEmbedding == null
                ) {
                    Text("Acquisisci volto")
                }

                Spacer(Modifier.height(8.dp))

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

        // ----------- CARD CASSETTI -----------
        Card {
            Column(Modifier.padding(16.dp)) {

                Text("Cassetti assegnati")

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Selezionati: ${selectedDrawers.size}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(8.dp))

                // TODO ESEMPIO STATICO - COLLEGA BACKEND
                // Qui stai usando una lista finta.
                // In futuro sostituiscila con una chiamata API (GET /drawers)
                val drawers = listOf(
                    1 to "Cassetto 1",
                    2 to "Cassetto 2",
                    3 to "Cassetto 3",
                    4 to "Cassetto 4"
                )

                drawers.forEach { (id, name) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name)

                        Checkbox(
                            checked = selectedDrawers.contains(id),
                            onCheckedChange = { checked ->
                                selectedDrawers = if (checked)
                                    selectedDrawers + id
                                else
                                    selectedDrawers - id
                            }
                        )
                    }
                }
            }
        }

        // ----------- SALVATAGGIO -----------
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
                                faceEmbedding = embedding,
                                drawerIds = selectedDrawers.toList()
                            )
                        )

                        if (response.isSuccessful && response.body()?.success == true) {
                            message = "Utente creato correttamente"

                            delay(1000)
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