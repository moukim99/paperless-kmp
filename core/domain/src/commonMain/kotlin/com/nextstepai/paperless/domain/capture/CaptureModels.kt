package com.nextstepai.paperless.domain.capture

data class DocumentInput(
    val filename: String,
    val mimeType: String,
    val bytes: ByteArray,
    val pageCount: Int? = null,
    val suppliedOcrText: String? = null
)

data class ProcessedDocument(
    val filename: String,
    val mimeType: String,
    val bytes: ByteArray,
    val ocrText: String,
    val checksum: String,
    val pageCount: Int?
)
