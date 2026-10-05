package com.nextstepai.paperless.platform

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.nextstepai.paperless.domain.capture.DocumentInput
import com.nextstepai.paperless.domain.ocr.AndroidMlKitOcrEngine
import kotlinx.coroutines.launch

@Composable
actual fun DocumentScannerButton(onScanned: (DocumentInput) -> Unit) {
    val launch = rememberDocumentScannerLauncher(onScanned)
    Button(onClick = launch) { Text("Scan document") }
}

@Composable
actual fun rememberDocumentScannerLauncher(onScanned: (DocumentInput) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val options = GmsDocumentScannerOptions.Builder()
        .setGalleryImportAllowed(true)
        .setPageLimit(50)
        .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
        .setScannerMode(SCANNER_MODE_FULL)
        .build()
    val scanner = GmsDocumentScanning.getClient(options)
    val launcher = rememberLauncherForActivityResult(StartIntentSenderForResult()) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val scan = GmsDocumentScanningResult.fromActivityResultIntent(result.data) ?: return@rememberLauncherForActivityResult
        scope.launch {
            val input = scan.toDocumentInput(context)
            if (input != null) onScanned(input)
        }
    }
    return {
        val activity = context as? android.app.Activity
        if (activity != null) {
            scanner.getStartScanIntent(activity)
                .addOnSuccessListener { sender -> launcher.launch(IntentSenderRequest.Builder(sender).build()) }
        }
    }
}

private suspend fun GmsDocumentScanningResult.toDocumentInput(context: Context): DocumentInput? {
    val pdf = getPdf() ?: return null
    val bytes = context.contentResolver.openInputStream(pdf.uri)?.use { it.readBytes() } ?: return null
    val pages = getPages().orEmpty()
    val ocr = AndroidMlKitOcrEngine()
    val text = buildString {
        pages.forEachIndexed { index, page ->
            val imageBytes = context.contentResolver.openInputStream(page.imageUri)?.use { it.readBytes() } ?: return@forEachIndexed
            val pageText = runCatching { ocr.extractText(imageBytes, "image/jpeg") }.getOrDefault("")
            if (pageText.isNotBlank()) {
                if (isNotEmpty()) append("\n\n")
                append("--- Page ${index + 1} ---\n")
                append(pageText)
            }
        }
    }
    return DocumentInput(
        filename = "scan-${System.currentTimeMillis()}.pdf",
        mimeType = "application/pdf",
        bytes = bytes,
        pageCount = pdf.pageCount,
        suppliedOcrText = text.ifBlank { null }
    )
}
