package com.example

import android.content.Context
import android.graphics.Bitmap
import kotlin.math.sqrt
// import org.tensorflow.lite.Interpreter

class FaceAuthManager(private val context: Context) {
    // private var interpreter: Interpreter? = null

    init {
        // interpreter = Interpreter(FileUtil.loadMappedFile(context, "mobile_facenet.tflite"))
    }

    fun extractEmbedding(bitmap: Bitmap): FloatArray {
        // Placeholder: Preprocess 112x112 bitmap and run TFLite inference
        // val inputBuffer = convertBitmapToByteBuffer(bitmap)
        // val outputBuffer = Array(1) { FloatArray(128) }
        // interpreter?.run(inputBuffer, outputBuffer)
        // return normalizeL2(outputBuffer[0])
        
        val dummy = FloatArray(128) { 0.5f }
        return normalizeL2(dummy)
    }

    fun computeCosineSimilarity(embeddingA: FloatArray, embeddingB: FloatArray): Float {
        if (embeddingA.size != embeddingB.size) return 0f
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in embeddingA.indices) {
            dotProduct += embeddingA[i] * embeddingB[i]
            normA += embeddingA[i] * embeddingA[i]
            normB += embeddingB[i] * embeddingB[i]
        }
        if (normA == 0f || normB == 0f) return 0f
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }

    private fun normalizeL2(embedding: FloatArray): FloatArray {
        var sum = 0f
        for (v in embedding) sum += v * v
        val norm = sqrt(sum)
        if (norm == 0f) return embedding
        for (i in embedding.indices) embedding[i] /= norm
        return embedding
    }
}
