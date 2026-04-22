package com.example.securelock.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.securelock.auth.FaceNetHelper
import com.example.securelock.network.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.*
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

// utilizzo di API sperimentali (imageProxy.image)
@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun FaceAuthScreen(navController: NavController) {
    val context = LocalContext.current
    // Lega camera e lifecycle
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    // Scopo per le chiamate async
    val scope = rememberCoroutineScope()

    // Variabili di stato (remember - crea una sola volta)
    var statusMessage by remember { mutableStateOf("Posiziona il viso davanti alla fotocamera") }
    var isProcessing by remember { mutableStateOf(false) }

    // Apertura => carica il modello
    // Chiusura => libera la memoria
    val faceNetHelper = remember { FaceNetHelper(context) }
    DisposableEffect(Unit) {
        onDispose { faceNetHelper.close() }
    }

    // ML Kit - crea il face DETECTOR nella camera
    // Privilegia la velocità
    // Ignora facce di sfondo - (minFaceSize)
    val faceDetector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setMinFaceSize(0.25f)
                .build()
        )
    }
    DisposableEffect(Unit) {
        onDispose { faceDetector.close() }
    }

    // Check dei permessi Android da AndroidManifest.xml
    val hasPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permesso fotocamera non concesso")
        }
        return
    }

    // UI Principale
    Box(Modifier.fillMaxSize()) {
        AndroidView(
            // Factory => creazione di View personalizzata
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                // Executor per analisi frame in background
                val executor = Executors.newSingleThreadExecutor()
                // Provider della fotocamera
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                // Evento => appena è pronta
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    // L'utente vede ciò che mostra la telecamera
                    val preview = Preview.Builder().build().apply {
                        surfaceProvider = previewView.surfaceProvider
                    }

                    // Analisi:
                    // 1. Evita di accumulare frame
                    // 2. Tieni solo l'ultimo
                    val analyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    // Ciclo di analisi (per ogni frame)
                    analyzer.setAnalyzer(executor) { imageProxy ->
                        // Check stato => salta frame se in corso
                        if (isProcessing) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        // Prendi l'immagine raw della fotocamera
                        val mediaImage = imageProxy.image ?: run {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        // Conversione in InputImage (per ML Kit)
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        // Cerca i volti nell'immagine (ML Kit)
                        // Se li trova restituisce una lista di "facce"
                        faceDetector.process(inputImage)
                            .addOnSuccessListener { faces ->
                                // Se non trova volti => salta frame
                                if (faces.isEmpty()) {
                                    imageProxy.close()
                                    return@addOnSuccessListener
                                }

                                // Set stato di analisi (blocco altri frame)
                                isProcessing = true
                                statusMessage = "Analisi volto..."

                                // Conversione in bitmap
                                // Crop del volto (il primo della lista)
                                val bitmap = imageProxy.toBitmap()
                                val faceBitmap = cropFace(bitmap, faces[0].boundingBox)

                                // Check validità volto
                                if (faceBitmap == null) {
                                    isProcessing = false
                                    imageProxy.close()
                                    return@addOnSuccessListener
                                }

                                // Calcola embedding (metodo in FaceNetHelper)
                                val embedding = faceNetHelper.getEmbedding(faceBitmap)

                                // Chiamata al server (funzione scritta sotto)
                                // Scope.launch per le chiamate async
                                scope.launch {
                                    sendToBackend(
                                        embedding,
                                        // In caso di successo, apri la schermata con i cassetti correlati
                                        onSuccess = {
                                            statusMessage = "Accesso autorizzato!"
                                            isProcessing = false
                                        },
                                        // In caso di successo, apri la schermata con i cassetti correlati
                                        onFailure = {
                                            statusMessage = "Volto non riconosciuto"
                                            isProcessing = false
                                        },
                                        // In caso di errore, mostra errore
                                        onError = {
                                            statusMessage = "Errore connessione"
                                            isProcessing = false
                                        }
                                    )
                                }
                                //Chiusura frame
                                imageProxy.close()
                            }
                            // In caso di errore ML Kit
                            .addOnFailureListener {
                                imageProxy.close()
                            }
                    }

                    try {
                        // Fai partire la fotocamera, attacca preview e analyzer
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            analyzer
                        )
                    } catch (e: Exception) {
                        Log.e("FaceAuth", "Errore camera", e)
                    }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // UI overlay - messaggi per l'utente
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

                if (isProcessing) {
                    CircularProgressIndicator(Modifier.size(24.dp))
                    Spacer(Modifier.height(8.dp))
                }

                Text(statusMessage, fontSize = 16.sp)

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Annulla")
                }
            }
        }
    }
}

// Funzioni helper

// Bitmap: immagine da analizzare (camera)
// Box: rettangolo del volto (ML Kit)
fun cropFace(bitmap: Bitmap, box: Rect): Bitmap? {
    // Nuove dimensioni immagine
    // Evita valori negativi con a=0
    val left = maxOf(0, box.left)
    val top = maxOf(0, box.top)
    val width = minOf(bitmap.width - left, box.width())
    val height = minOf(bitmap.height - top, box.height())

    // Ritorna immagine ritagliata
    return if (width > 0 && height > 0)
        Bitmap.createBitmap(bitmap, left, top, width, height)
    else
        null
}

// Invio face embedding al server
// Gestione eventi settati a Unit
private suspend fun sendToBackend(
    embedding: List<Float>,
    onSuccess: () -> Unit,  // Unit => void in Java
    onFailure: () -> Unit,
    onError: () -> Unit
) {
    try {
        val api = ApiClient.retrofit.create(ApiService::class.java)

        val response = api.authWithFace(
            FaceAuthRequest(
                embedding = embedding,
                drawerId = -1 // TO DO
            )
        )

        // HTTP OK (200)
        if (response.isSuccessful && response.body()?.success == true) {
            onSuccess() // Lancia l'evento con successo
        } else {
            onFailure() // Lancia l'evento il fallimento
        }
    } catch (e: Exception) {
        Log.e("FaceAuth", "Errore backend", e)
        onError()
    }
}