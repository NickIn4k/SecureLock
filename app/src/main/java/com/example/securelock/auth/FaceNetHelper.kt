package com.example.securelock.auth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import androidx.core.graphics.scale
import androidx.core.graphics.get

class FaceNetHelper(context: Context) {
    private val interpreter: Interpreter // Motore TensorFlow
    private val inputSize = 112 // FaceNet richiede img 112x112
    private val embeddingSize = 128 // FaceNet crea un vettore di 128 float

    // Carica facenet.tflite da /assets
    // Crea l'interpreter per il modello
    init {
        interpreter = Interpreter(loadModelFile(context))
    }

    private fun loadModelFile(context: Context): MappedByteBuffer {
        // Apri .tflite con stream e channel
        val fileDescriptor = context.assets.openFd("facenet.tflite")

        // Crea un canale per leggere il file
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        // Mappa in RAM per accesso veloce
        // Standard di TensorFlow
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    // Versione 1.0 - lenta/migliorabile
    fun getEmbedding(faceBitmap: Bitmap): List<Float> {
        require(faceBitmap.width > 0 && faceBitmap.height > 0) {
            "Bitmap non valida"
        }

        // Ridimensiona volto a 112x112
        val resized = faceBitmap.scale(inputSize, inputSize)

        // Conversione pixel (0-255) in float (-1,1) normalizzati
        // Struttura 4D - NHWC (Number, Height, Width, Channels):
        // - [1]: numero di immmagini
        // - [112]: height
        // - [112]: width
        // - [3]: canali colore (RGB)
        val input = Array(1) {
            Array(inputSize) {
                Array(inputSize) {
                    FloatArray(3)
                }
            }
        }

        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = resized[x, y]
                // Normalizzazione: (valore - 128) / 128 porta tutto tra -1 e 1
                input[0][y][x][0] = (Color.red(pixel) - 128f) / 128f
                input[0][y][x][1] = (Color.green(pixel) - 128f) / 128f
                input[0][y][x][2] = (Color.blue(pixel) - 128f) / 128f
            }
        }

        // Buffer di output (128 float)
        val output = Array(1) { FloatArray(embeddingSize) }

        // Esegui il modello
        interpreter.run(input, output)

        // Restituisce una lista
        return output[0].toList()
    }

    // Libera la memoria quando non serve più
    fun close() {
        interpreter.close()
    }
}