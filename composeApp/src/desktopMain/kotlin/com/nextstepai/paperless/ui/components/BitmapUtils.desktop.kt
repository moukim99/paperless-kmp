package com.nextstepai.paperless.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun ByteArray.toImageBitmap(): ImageBitmap? = runCatching {
    Image.makeFromEncoded(this).toComposeImageBitmap()
}.getOrNull()
