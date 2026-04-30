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
import com.example.securelock.network.AuthResponse
import com.example.securelock.network.FaceAuthRequest
import com.example.securelock.ui.components.FaceCapturePreview
import kotlinx.coroutines.launch

@Composable
fun FaceAuthScreen(navController: NavController) {

    val scope = rememberCoroutineScope()

    var statusMessage by remember {
        mutableStateOf("Posiziona il viso davanti alla fotocamera")
    }

    // 🔴 evita chiamate multiple (fondamentale)
    var alreadySent by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {

        FaceCapturePreview(
            modifier = Modifier.fillMaxSize(),

            onStatusChange = {
                statusMessage = it
            },

            onEmbeddingCaptured = { embedding ->

                // evita doppie chiamate
                if (alreadySent) return@FaceCapturePreview
                alreadySent = true

                statusMessage = "Invio embedding al server..."

                scope.launch {
                    sendToBackend(
                        embedding = embedding,

                        onSuccess = { body ->
                            statusMessage = "Login riuscito"

                            val userId = body.userId ?: 0
                            val role = body.userRole

                            when (role) {
                                "superadmin" -> {
                                    navController.navigate("setup/$userId") {
                                        popUpTo(Routes.FACE_AUTH) { inclusive = true }
                                    }
                                }

                                "admin" -> {
                                    navController.navigate("welcome/$userId/true") {
                                        popUpTo(Routes.FACE_AUTH) { inclusive = true }
                                    }
                                }

                                else -> {
                                    navController.navigate("welcome/$userId/false") {
                                        popUpTo(Routes.FACE_AUTH) { inclusive = true }
                                    }
                                }
                            }
                        },

                        onFailure = {
                            alreadySent = false
                            statusMessage = "Volto non riconosciuto"
                        },

                        onError = {
                            alreadySent = false
                            statusMessage = "Errore connessione"
                        }
                    )
                }
            }
        )

        // 🔵 UI messaggi sotto
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

                TextButton(
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Text("Annulla")
                }
            }
        }
    }
}

private suspend fun sendToBackend(
    embedding: List<Float>,
    onSuccess: (AuthResponse) -> Unit,
    onFailure: () -> Unit,
    onError: () -> Unit
) {
    try {
        val response = ApiClient.api.authWithFace(
            FaceAuthRequest(embedding)
        )

        val body = response.body()

        if (response.isSuccessful && body?.success == true) {
            onSuccess(body)
        } else {
            onFailure()
        }

    } catch (e: Exception) {
        onError()
    }
}