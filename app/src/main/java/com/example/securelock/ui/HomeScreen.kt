package com.example.securelock.ui

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.example.securelock.network.ApiClient
import com.example.securelock.network.ApiService
import com.example.securelock.network.FingerprintAuthRequest
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SecureLock",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Scegli il metodo di accesso",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // FACE
                Button(
                    onClick = { navController.navigate(Routes.FACE_AUTH) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Riconoscimento facciale")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FINGERPRINT - TODO
                OutlinedButton(
                    onClick = {
                        val activity = context as FragmentActivity
                        val executor = ContextCompat.getMainExecutor(context)

                        val biometricPrompt = BiometricPrompt(
                            activity,
                            executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(
                                    result: BiometricPrompt.AuthenticationResult
                                ) {
                                    scope.launch {
                                        try {
                                            val api = ApiClient.retrofit.create(ApiService::class.java)
                                            val response = api.authWithFingerprint(
                                                FingerprintAuthRequest(userId = 1, drawerId = 1)
                                            )
                                            if (response.isSuccessful && response.body()?.success == true) {
                                                Toast.makeText(context, "Cassettino aperto!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Accesso negato", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Errore connessione", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }

                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    Toast.makeText(context, "Errore: $errString", Toast.LENGTH_SHORT).show()
                                }

                                override fun onAuthenticationFailed() {
                                    Toast.makeText(context, "Impronta non riconosciuta", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Autenticazione richiesta")
                            .setSubtitle("Usa la tua impronta")
                            .setNegativeButtonText("Annulla")
                            .build()

                        biometricPrompt.authenticate(promptInfo)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Impronta digitale")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // LOGIN
                TextButton(
                    onClick = { navController.navigate(Routes.LOGIN) }
                ) {
                    Text("Login classico")
                }
            }
        }
    }
}