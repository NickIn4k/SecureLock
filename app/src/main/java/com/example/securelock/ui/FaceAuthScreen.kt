package com.example.securelock.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.FaceAuthRequest
import com.example.securelock.ui.components.FaceCapturePreview
import kotlinx.coroutines.launch

@Composable
fun FaceAuthScreen(navController: NavController) {
    val scope = rememberCoroutineScope()

    var statusMessage by remember { mutableStateOf("Posiziona il viso davanti alla fotocamera") }

    Box(Modifier.fillMaxSize()) {

        FaceCapturePreview(
            modifier = Modifier.fillMaxSize(),
            onStatusChange = { statusMessage = it },
            onEmbeddingCaptured = { embedding ->
                statusMessage = "Invio embedding al server..."

                scope.launch {
                    sendToBackend(
                        embedding = embedding,

                        onSuccess = { userId ->
                            statusMessage = "Accesso autorizzato!"
                            navController.navigate("welcome/$userId")
                        },

                        onFailure = {
                            statusMessage = "Volto non riconosciuto"
                        },

                        onError = {
                            statusMessage = "Errore connessione"
                        }
                    )
                }
            }
        )

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(statusMessage, fontSize = 16.sp)

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Annulla")
                }
            }
        }
    }
}

private suspend fun sendToBackend(
    embedding: List<Float>,
    onSuccess: (Int) -> Unit,
    onFailure: () -> Unit,
    onError: () -> Unit
) {
    try {
        val response = ApiClient.api.authWithFace(
            FaceAuthRequest(embedding)
        )

        if (response.isSuccessful && response.body()?.success == true) {
            val userId = response.body()?.userId ?: 0
            onSuccess(userId)
        } else {
            onFailure()
        }

    } catch (e: Exception) {
        onError()
    }
}