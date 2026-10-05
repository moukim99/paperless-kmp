package com.nextstepai.paperless.platform

import androidx.compose.runtime.Composable
import com.nextstepai.paperless.domain.capture.DocumentInput

@Composable
expect fun DocumentPickerButton(onPicked: (DocumentInput) -> Unit)

@Composable
expect fun rememberDocumentPickerLauncher(onPicked: (DocumentInput) -> Unit): () -> Unit
