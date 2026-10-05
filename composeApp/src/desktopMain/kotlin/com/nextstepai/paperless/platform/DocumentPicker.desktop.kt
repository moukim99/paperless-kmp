package com.nextstepai.paperless.platform

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstepai.paperless.domain.capture.DocumentInput
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun DocumentPickerButton(onPicked: (DocumentInput) -> Unit) {
    val launch = rememberDocumentPickerLauncher(onPicked)
    Button(onClick = launch) { Text("Add document") }
}

@Composable
actual fun rememberDocumentPickerLauncher(onPicked: (DocumentInput) -> Unit): () -> Unit {
    return {
        val dialog = FileDialog(null as Frame?, "Select document", FileDialog.LOAD)
        dialog.isVisible = true
        val file = dialog.file
        val dir = dialog.directory
        if (file != null && dir != null) {
            val selected = File(dir, file)
            val mime = when (selected.extension.lowercase()) {
                "pdf" -> "application/pdf"
                "png" -> "image/png"
                "jpg", "jpeg" -> "image/jpeg"
                "txt" -> "text/plain"
                else -> "application/octet-stream"
            }
            onPicked(DocumentInput(selected.name, mime, selected.readBytes()))
        }
    }
}
