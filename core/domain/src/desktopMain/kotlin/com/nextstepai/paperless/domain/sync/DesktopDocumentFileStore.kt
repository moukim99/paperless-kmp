package com.nextstepai.paperless.domain.sync

import java.io.File

class DesktopDocumentFileStore(private val root: File) : DocumentFileStore {
    init { root.mkdirs() }

    override suspend fun save(documentId: Long, filename: String, bytes: ByteArray) {
        File(root, documentId.toString()).apply { mkdirs() }
            .resolve(filename)
            .writeBytes(bytes)
    }

    override suspend fun localPath(documentId: Long): String? = File(root, documentId.toString()).listFiles()?.firstOrNull()?.absolutePath

    override suspend fun read(documentId: Long): ByteArray {
        val directory = File(root, documentId.toString())
        val file = directory.listFiles()?.firstOrNull() ?: error("Local file for document $documentId not found")
        return file.readBytes()
    }

    override suspend fun cacheSize(): Long = runCatching {
        root.walkTopDown().filter { it.isFile }.sumOf { it.length() }
    }.getOrDefault(0L)

    override suspend fun clearCache() {
        runCatching { root.listFiles()?.forEach { it.deleteRecursively() } }
    }
}
