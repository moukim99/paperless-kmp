package com.nextstepai.paperless.domain.ocr

interface OcrEngine {
    suspend fun extractText(bytes: ByteArray, mimeType: String): String
}
