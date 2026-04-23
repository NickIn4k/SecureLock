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

    val faceNetHelper = remember { FaceNetHelper(context) }
    val faceDetector = remember {
        FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setMinFaceSize(0.25f)
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
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
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
                                    imageProxy.close()
                                    return@addOnSuccessListener
                                }

                                isProcessing = true
                                onStatusChange("Analisi volto...")

                                val bitmap = imageProxy.toBitmap()
                                val faceBitmap = cropFace(bitmap, faces[0].boundingBox)

                                if (faceBitmap == null) {
                                    isProcessing = false
                                    imageProxy.close()
                                    return@addOnSuccessListener
                                }

                                try {
                                    val embedding = faceNetHelper.getEmbedding(faceBitmap)
                                    alreadyCaptured = true
                                    onStatusChange("Volto acquisito correttamente")
                                    onEmbeddingCaptured(embedding)
                                } catch (e: Exception) {
                                    Log.e("FaceCapture", "Errore embedding", e)
                                    onStatusChange("Errore durante l'analisi del volto")
                                } finally {
                                    isProcessing = false
                                    imageProxy.close()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FaceCapture", "Errore ML Kit", e)
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