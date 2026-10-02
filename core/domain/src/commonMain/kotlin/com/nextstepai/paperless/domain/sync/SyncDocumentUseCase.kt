@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.domain.sync

import com.nextstepai.paperless.domain.repository.DocumentRepository

class SyncDocumentUseCase(
    private val repository: DocumentRepository,
    private val remote: RemoteDocumentDataSource,
    private val fileStore: DocumentFileStore
) {
    suspend operator fun invoke(documentId: Long): Result<SyncResult> = runCatching {
        val document = repository.getById(documentId)
            ?: error("Document $documentId not found")
        val r2Key = remote.uploadFile(
            documentId = document.remoteId ?: document.id.toString(),
            filename = document.filename ?: "document-${document.id}",
            mimeType = document.mimeType,
            bytes = fileStore.read(document.id)
        )
        remote.upsertDocument(
            RemoteDocumentMetadata(
                remoteId = document.remoteId,
                title = document.title,
                content = document.content,
                mimeType = document.mimeType,
                checksum = document.checksum,
                archiveChecksum = document.archiveChecksum,
                pageCount = document.pageCount,
                created = document.created.toEpochMilliseconds(),
                modified = document.modified.toEpochMilliseconds(),
                added = document.added.toEpochMilliseconds(),
                filename = document.filename,
                originalFilename = document.originalFilename,
                expiresAt = document.expiresAt?.toEpochMilliseconds(),
                reminderDaysBeforeExpiry = document.reminderDaysBeforeExpiry,
                r2Key = r2Key
            )
        )
    }
}
