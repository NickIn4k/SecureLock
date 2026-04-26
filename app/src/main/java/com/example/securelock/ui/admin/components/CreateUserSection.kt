package com.example.securelock.ui.admin.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.securelock.network.ApiClient
import com.example.securelock.network.CreateUserRequest
import com.example.securelock.network.FaceCheckRequest
import com.example.securelock.network.SaveFaceRequest
import com.example.securelock.ui.components.FaceCapturePreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CreateUserSection(
    currentUserId: Int,
    onUserCreated: () -> Unit = {}
) {

    // -------- STATE --------
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var faceEmbedding by remember { mutableStateOf<List<Float>?>(null) }
    var isFaceCheckedOk by remember { mutableStateOf(false) } // Cerca volti duplicati nel db

    var selectedSlots by remember { mutableStateOf(setOf<Int>()) }

    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val shape = RoundedCornerShape(18.dp)

    Column(
        modifier = Modifier.fillMaxWidth()
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        Text(
            text = "Crea nuovo utente",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF16324F)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -------- DATI --------
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Nome e cognome") },
            shape = shape,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            shape = shape,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            shape = shape,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(18.dp))

        // -------- FACE --------
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF9FBFF)
            ),
            border = BorderStroke(1.dp, Color(0x22000000))
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Riconoscimento facciale")

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    when {
                        faceEmbedding == null -> "Nessun volto acquisito"
                        isFaceCheckedOk -> "Volto acquisito e verificato"
                        else -> "Volto acquisito"
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { isScanning = true },
                    enabled = !isScanning && faceEmbedding == null,
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                    .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7EA8FF),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Text("Acquisisci volto")
                }

                if (isScanning) {
                    Spacer(modifier = Modifier.height(10.dp))

                    FaceCapturePreview(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),

                        onStatusChange = {
                            message = it
                        },

                        onEmbeddingCaptured = { embedding ->
                            scope.launch {
                                isScanning = false
                                message = "Controllo volto nel database..."

                                try {
                                    val response = ApiClient.api.checkFace(
                                        FaceCheckRequest(faceEmbedding = embedding)
                                    )

                                    val body = response.body()

                                    if (response.code() == 409 || body?.duplicate == true) {
                                        message = body?.message ?: "Volto già registrato"
                                        faceEmbedding = null
                                        isFaceCheckedOk = false
                                        return@launch
                                    }

                                    if (response.isSuccessful && body?.success == true) {
                                        faceEmbedding = embedding
                                        isFaceCheckedOk = true
                                        message = "Volto libero, pronto per il salvataggio"
                                    } else {
                                        message = body?.message ?: "Errore controllo volto"
                                        faceEmbedding = null
                                        isFaceCheckedOk = false
                                    }
                                } catch (e: Exception) {
                                    message = "Errore connessione: ${e.message}"
                                    faceEmbedding = null
                                    isFaceCheckedOk = false
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // -------- CASSETTI --------
        Card(
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF9FBFF)
            ),
            border = BorderStroke(1.dp, Color(0x22000000))
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Cassetti assegnati")

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Selezionati: ${selectedSlots.size}",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(10.dp))

                val slots = listOf(
                    1 to "Cassetto 1",
                    2 to "Cassetto 2",
                    3 to "Cassetto 3"
                )

                slots.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name)

                        Checkbox(
                            checked = selectedSlots.contains(id),
                            onCheckedChange = { checked ->
                                selectedSlots = if (checked)
                                    selectedSlots + id
                                else
                                    selectedSlots - id
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // -------- SALVA --------
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7EA8FF),
                contentColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp
            ),
            onClick = {

                if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                    message = "Compila tutti i campi"
                    return@Button
                }

                isLoading = true
                message = ""

                scope.launch {
                    try {
                        val createResponse = ApiClient.api.createUser(
                            CreateUserRequest(
                                adminUserId = currentUserId,
                                fullName = fullName,
                                username = username,
                                password = password,
                                slotIds = selectedSlots.toList()
                            )
                        )

                        val createBody = createResponse.body()

                        when {
                            createResponse.code() == 409 -> {
                                message = createBody?.message ?: "Username già esistente"
                                return@launch
                            }

                            createResponse.code() == 403 -> {
                                message = createBody?.message ?: "Non autorizzato"
                                return@launch
                            }

                            !createResponse.isSuccessful || createBody?.success != true -> {
                                message = createBody?.message ?: "Errore creazione utente"
                                return@launch
                            }
                        }

                        val createdUserId = createBody?.userId
                        if (createdUserId == null) {
                            message = "Utente creato ma userId mancante"
                            return@launch
                        }

                        // Se il volto non è stato acquisito, finisco qui
                        if (faceEmbedding == null) {
                            message = "Utente creato correttamente"
                            delay(800)
                            onUserCreated()
                            return@launch
                        }

                        // Se il volto era stato acquisito e controllato prima, lo salvo ora
                        if (!isFaceCheckedOk) {
                            message = "Volto non ancora validato"
                            return@launch
                        }

                        val saveFaceResponse = ApiClient.api.saveFace(
                            SaveFaceRequest(
                                userId = createdUserId,
                                faceEmbedding = faceEmbedding!!
                            )
                        )

                        val saveFaceBody = saveFaceResponse.body()

                        if (saveFaceResponse.isSuccessful && saveFaceBody?.success == true) {
                            message = "Utente e volto salvati correttamente"
                            delay(800)
                            onUserCreated()
                        } else {
                            message = saveFaceBody?.message ?: "Errore salvataggio volto"
                        }

                    } catch (e: Exception) {
                        message = "Errore: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            shape = shape
        ) {
            Text("Salva utente")
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (message.isNotBlank()) {
            Text(message)
        }
    }
}