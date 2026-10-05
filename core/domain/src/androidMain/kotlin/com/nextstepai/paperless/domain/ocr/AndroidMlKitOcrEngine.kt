package com.nextstepai.paperless.domain.ocr

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class AndroidMlKitOcrEngine : OcrEngine {
    override suspend fun extractText(bytes: ByteArray, mimeType: String): String = withContext(Dispatchers.IO) {
        if (mimeType.equals("text/plain", ignoreCase = true)) {
            return@withContext runCatching { bytes.decodeToString() }.getOrDefault("")
        }
        val bitmap = runCatching { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }.getOrNull()
            ?: return@withContext ""
        runCatching {
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(InputImage.fromBitmap(bitmap, 0))
                .await()
                .text
        }.getOrDefault("")
    }
}
