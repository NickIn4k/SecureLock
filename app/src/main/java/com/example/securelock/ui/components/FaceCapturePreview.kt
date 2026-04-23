package com.example.securelock.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.securelock.auth.FaceNetHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun FaceCapturePreview(
    modifier: Modifier = Modifier,
    onStatusChange: (String) -> Unit = {},
    onEmbeddingCaptured: (List<Float>) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isProcessing by remember { mutableStateOf(false) }
    var alreadyCaptured by remember { mutableStateOf(false) }
    var embeddingSamples by remember { mutableStateOf<List<List<Float>>>(emptyList()) }

    val faceNetHelper = remember { FaceNetHelper(context) }

    val faceDetector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setMinFaceSize(0.3f)
                .build()
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            faceNetHelper.close()
            faceDetector.close()
        }
    }

    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("Permesso fotocamera non concesso")
        }
        return
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = Executors.newSingleThreadExecutor()
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().apply {
                        surfaceProvider = previewView.surfaceProvider
                    }

                    val analyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analyzer.setAnalyzer(executor) { imageProxy ->

                        if (isProcessing || alreadyCaptured) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val mediaImage = imageProxy.image ?: run {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        faceDetector.process(inputImage)
                            .addOnSuccessListener { faces ->

                                if (faces.isEmpty()) {
                                    onStatusChange("Nessun volto rilevato")
                                    imageProxy.close()
                                    return@addOnSuccessListener
                                }

                                // Controlli qualità
                                val face = faces[0]
                                val imageWidth = inputImage.width
                                val imageHeight = inputImage.height

                                val faceArea = face.boundingBox.width() * face.boundingBox.height()
                                val imageArea = imageWidth * imageHeight

                                val margin = 20
                                val box = face.boundingBox

                                val isFullyInside =
                                    box.left > margin &&
                                    box.top > margin &&
                                    box.right < inputImage.width - margin &&
                                    box.bottom < inputImage.height - margin


                                val isValid =
                                    face.headEulerAngleY in -10f..10f &&
                                    face.headEulerAngleX in -10f..10f &&
                                    face.headEulerAngleZ in -10f..10f &&
                                    faceArea > imageArea * 0.05f &&
                                    isFullyInside

                                if (!isValid) {
                                    onStatusChange("Guarda dritto e avvicinati")
                                    imageProxy.close()
                                    return@addOnSuccessListener
                                }

                                isProcessing = true
                                onStatusChange("Acquisizione in corso...")

                                val bitmap = imageProxy.toBitmap()
                                val faceBitmap = cropFace(bitmap, face.boundingBox)

                                if (faceBitmap == null) {
                                    isProcessing = false
                                    imageProxy.close()
                                    return@addOnSuccessListener
                                }

                                try {
                                    val embedding = faceNetHelper.getEmbedding(faceBitmap)

                                    val newSamples = embeddingSamples + listOf(embedding)
                                    embeddingSamples = newSamples

                                    // Raccolta campioni
                                    if (newSamples.size < 7) {
                                        onStatusChange("Rimani fermo (${newSamples.size}/5)")
                                        isProcessing = false
                                        imageProxy.close()
                                        return@addOnSuccessListener
                                    }

                                    // Media finale
                                    val finalEmbedding = averageEmbeddings(newSamples)

                                    alreadyCaptured = true
                                    onStatusChange("Volto acquisito correttamente")
                                    onEmbeddingCaptured(finalEmbedding)

                                } catch (e: Exception) {
                                    Log.e("FaceCapture", "Errore embedding", e)
                                    onStatusChange("Errore analisi volto")
                                } finally {
                                    isProcessing = false
                                    imageProxy.close()
                                }
                            }
                            .addOnFailureListener {
                                isProcessing = false
                                imageProxy.close()
                            }
                    }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        analyzer
                    )

                } catch (e: Exception) {
                    Log.e("FaceCapture", "Errore camera", e)
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

fun averageEmbeddings(samples: List<List<Float>>): List<Float> {
    val size = samples[0].size
    val result = MutableList(size) { 0f }

    for (sample in samples) {
        for (i in 0 until size) {
            result[i] += sample[i]
        }
    }

    return result.map { it / samples.size }
}

fun cropFace(bitmap: Bitmap, box: Rect): Bitmap? {
    val left = maxOf(0, box.left)
    val top = maxOf(0, box.top)
    val width = minOf(bitmap.width - left, box.width())
    val height = minOf(bitmap.height - top, box.height())

    return if (width > 0 && height > 0) {
        Bitmap.createBitmap(bitmap, left, top, width, height)
    } else {
        null
    }
}