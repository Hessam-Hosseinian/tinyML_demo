package com.example.imageclassifier

import android.graphics.Bitmap
import android.graphics.ImageDecoder

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.Composable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TinyMLClassifierScreen()
                }
            }
        }
    }
}

@Composable
fun TinyMLClassifierScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var output by remember { mutableStateOf<ClassificationOutput?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val classifierHelper = remember {
        ImageClassifierHelper(context)
    }

    fun classifyBitmap(bitmap: Bitmap) {
        scope.launch {
            isLoading = true

            try {
                selectedBitmap = bitmap

                output = withContext(Dispatchers.Default) {
                    classifierHelper.classify(bitmap)
                }
            } finally {
                isLoading = false
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            isLoading = true

            try {
                val bitmap = withContext(Dispatchers.IO) {
                    loadBitmapFromUri(context, uri)
                }

                selectedBitmap = bitmap

                output = withContext(Dispatchers.Default) {
                    classifierHelper.classify(bitmap)
                }
            } finally {
                isLoading = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap == null) return@rememberLauncherForActivityResult

        classifyBitmap(bitmap)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "TinyML On-device Image Classification",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Runs locally on device using TensorFlow Lite",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        selectedBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Selected image",
                modifier = Modifier
                    .size(260.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    imagePicker.launch("image/*")
                }
            ) {
                Text("Choose Image")
            }

            Button(
                onClick = {
                    cameraLauncher.launch(null)
                }
            ) {
                Text("Take Photo")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        output?.let { result ->
            ResultCard(result)
        }
    }
}

@Composable
fun ResultCard(output: ClassificationOutput) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Result",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            output.results.forEachIndexed { index, result ->
                val normalizedScore = if (result.score > 1f) {
                    result.score / 255f
                } else {
                    result.score
                }

                val percent = normalizedScore * 100

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${index + 1}. ${result.label}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "%.1f%%".format(percent),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Inference time: ${output.inferenceTimeMs} ms",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Execution: On-device / Offline",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun loadBitmapFromUri(
    context: android.content.Context,
    uri: Uri
): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder: ImageDecoder, _: ImageDecoder.ImageInfo, _: ImageDecoder.Source ->
            decoder.isMutableRequired = true
        }
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}
