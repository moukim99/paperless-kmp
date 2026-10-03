package com.nextstepai.paperless.domain.platform

interface DocumentPreviewer {
    suspend fun open(documentId: Long): Result<Unit>
    suspend fun readBytes(documentId: Long): ByteArray?
    suspend fun getLocalPath(documentId: Long): String?
}
