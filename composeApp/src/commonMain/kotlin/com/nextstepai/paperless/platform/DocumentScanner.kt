package com.nextstepai.paperless.platform

import androidx.compose.runtime.Composable
import com.nextstepai.paperless.domain.capture.DocumentInput

@Composable
expect fun DocumentScannerButton(onScanned: (DocumentInput) -> Unit)

@Composable
expect fun rememberDocumentScannerLauncher(onScanned: (DocumentInput) -> Unit): () -> Unit
