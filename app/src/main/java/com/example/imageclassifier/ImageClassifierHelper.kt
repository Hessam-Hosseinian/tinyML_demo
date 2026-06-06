package com.example.imageclassifier

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

data class ClassificationResult(
    val label: String,
    val score: Float
)

data class ClassificationOutput(
    val results: List<ClassificationResult>,
    val inferenceTimeMs: Long
)

class ImageClassifierHelper(
    private val context: Context
) {
    private var classifier: ImageClassifier? = null

    private val labels: List<String> by lazy {
        loadLabels()
    }

    private fun setupClassifier() {
        if (classifier != null) return

        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setMaxResults(3)
            .setScoreThreshold(0.1f)
            .build()

        classifier = ImageClassifier.createFromFileAndOptions(
            context,
            "mobilenet_v1_1.0_224_quant.tflite",
            options
        )
    }

    fun classify(bitmap: Bitmap): ClassificationOutput {
        setupClassifier()

        val startTime = SystemClock.uptimeMillis()

        val tensorImage = TensorImage.fromBitmap(bitmap)
        val classificationResults = classifier?.classify(tensorImage).orEmpty()

        val inferenceTime = SystemClock.uptimeMillis() - startTime

        val results = classificationResults
            .flatMap { it.categories }
            .sortedByDescending { it.score }
            .take(3)
            .map { category ->
                val index = category.index

                val labelName = if (index >= 0 && index < labels.size) {
                    labels[index]
                } else {
                    "Class $index"
                }

                ClassificationResult(
                    label = labelName,
                    score = category.score
                )
            }

        return ClassificationOutput(
            results = results,
            inferenceTimeMs = inferenceTime
        )
    }

    private fun loadLabels(): List<String> {
        return context.assets.open("labels.txt")
            .bufferedReader()
            .useLines { lines ->
                lines.map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .filter { it != "background" }
                    .toList()
            }
    }
}