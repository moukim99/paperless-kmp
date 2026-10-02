package com.nextstepai.paperless.domain.ocr

class DesktopOcrEngine : OcrEngine {
    override suspend fun extractText(bytes: ByteArray, mimeType: String): String =
        ""
}
