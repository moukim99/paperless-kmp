package com.nextstepai.paperless.domain.sync

import android.content.Context
import java.io.File

class AndroidDocumentFileStore(private val context: Context) : DocumentFileStore {
    private val root get() = File(context.filesDir, "documents").apply { mkdirs() }

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
}
