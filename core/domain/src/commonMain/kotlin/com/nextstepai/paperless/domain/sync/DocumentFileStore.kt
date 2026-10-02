package com.nextstepai.paperless.domain.sync

interface DocumentFileStore {
    suspend fun read(documentId: Long): ByteArray
    suspend fun save(documentId: Long, filename: String, bytes: ByteArray)
    suspend fun localPath(documentId: Long): String?
}
