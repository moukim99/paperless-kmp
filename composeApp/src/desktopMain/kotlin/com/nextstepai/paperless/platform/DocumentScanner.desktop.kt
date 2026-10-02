package com.nextstepai.paperless.platform

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstepai.paperless.domain.capture.DocumentInput

@Composable
actual fun DocumentScannerButton(onScanned: (DocumentInput) -> Unit) {
    OutlinedButton(onClick = {}, enabled = false) { Text("Scanner: Android only") }
}
