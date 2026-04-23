package com.example.securelock.auth

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

// Logica dell'impronta digitale.
class BiometricHelper(
    private val context: Context,
    private val onSuccess: (userId: Int) -> Unit,  // eventi (Unit (kotlin) => void (java))
    private val onError: (message: String) -> Unit
) {
    // BiometricPrompt richiede una FragmentActivity nullable (sub-activity)
    // Ogni fragment ha il suo layout e lifecycle => più panel
    fun authenticate() {
        val activity = context as? FragmentActivity
            ?: run {
                onError("Contesto non valido")
                return
            }

        // Executor => gestione callback sul thread principale
        val executor = ContextCompat.getMainExecutor(context)

        // Callback => gestione risposte dell'impronta digitale
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            // AuthenticationCallBack => risposta evento biometrico
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    // TODO manda la richiesta al server
                    onSuccess(-1)
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    // Errore di sistema — es. troppi tentativi,
                    // sensore non disponibile, utente ha premuto Annulla
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    // Impronta letta ma non corrisponde a nessuna salvata
                    Toast.makeText(
                        context,
                        "Impronta non riconosciuta, riprova",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        // Configurazione del dialogo che vede l'utente
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticazione richiesta")
            .setSubtitle("Usa la tua impronta per accedere")
            .setNegativeButtonText("Annulla")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}