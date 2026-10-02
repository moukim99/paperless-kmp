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
        require(mimeType.startsWith("image/")) { "Android ML Kit OCR currently accepts image captures; PDF page rasterization is handled by the scanner layer." }
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: error("Unable to decode image for OCR")
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(InputImage.fromBitmap(bitmap, 0))
            .await()
            .text
    }
}
