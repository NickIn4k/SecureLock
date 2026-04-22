package com.example.securelock

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.securelock.ui.AppNavigation

class MainActivity : ComponentActivity() {
    var showPermissionDeniedDialog by mutableStateOf(false)
    // Gestione Permessi
    // Launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if(!isGranted)
            showPermissionDeniedDialog = true
    }

    // Check permessi
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Chiamata al launcher
    private fun requestCameraPermissionIfNeeded() {
        if (!hasCameraPermission()) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Lifecycle - eventi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCameraPermissionIfNeeded()
        setContent { App() }
    }

    // UI
    @androidx.compose.runtime.Composable
    private fun App() {
        MaterialTheme {
            Surface {
                // In caso di permesso negato => dialog di errore
                if (showPermissionDeniedDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermissionDeniedDialog = false },
                        title = { Text("Permesso negato") },
                        text = { Text("Permesso negato. Non posso far partire l'app") },
                        confirmButton = {
                            TextButton(onClick = {
                                showPermissionDeniedDialog = false
                            }) {
                                Text("OK")
                            }
                        }
                    )
                }
                AppNavigation()
            }
        }
    }
}