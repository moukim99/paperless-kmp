package com.nextstepai.paperless.platform

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.nextstepai.paperless.domain.capture.DocumentInput

@Composable
actual fun DocumentPickerButton(onPicked: (DocumentInput) -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@rememberLauncherForActivityResult
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val name = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val index = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst() && index >= 0) c.getString(index) else null
        } ?: "document"
        onPicked(DocumentInput(name, mime, bytes))
    }
    Button(onClick = { launcher.launch(arrayOf("application/pdf", "image/*", "text/plain", "application/octet-stream")) }) {
        Text("Add document")
    }
}
