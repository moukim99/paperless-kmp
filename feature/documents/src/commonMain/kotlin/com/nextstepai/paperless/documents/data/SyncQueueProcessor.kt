@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.documents.data

import com.nextstepai.paperless.database.dao.SyncOperationDao
import com.nextstepai.paperless.database.entity.SyncOperationEntity
import com.nextstepai.paperless.domain.repository.DocumentRepository
import com.nextstepai.paperless.domain.sync.*

class SyncQueueProcessor(
    private val operations: SyncOperationDao,
    private val files: com.nextstepai.paperless.database.dao.DocumentFileDao,
    private val repository: DocumentRepository,
    private val remote: RemoteDocumentDataSource,
    private val fileStore: DocumentFileStore,
    private val clock: kotlin.time.Clock = kotlin.time.Clock.System
) {
    suspend fun process(limit: Int = 10): Int {
        val now = clock.now().toEpochMilliseconds()
        operations.recoverStaleUploading(now - 30 * 60 * 1000L, now)
        var processed = 0
        for (op in operations.nextPending(limit)) {
            try {
                val now = clock.now().toEpochMilliseconds()
                operations.update(op.copy(state = "UPLOADING", updatedAt = now))
                val document = repository.getById(op.documentId) ?: run { operations.deleteOperation(op.documentId, op.operation); continue }
                val relations = repository.getWithRelations(op.documentId)
                if (op.operation == "DELETE") {
                    document.remoteId?.let { remote.deleteDocument(it) }
                    operations.deleteOperation(op.documentId, op.operation)
                    processed++
                    continue
                }
                val bytes = fileStore.read(document.id)
                val key = remote.uploadFile(document.remoteId ?: document.id.toString(), document.filename ?: "document-${document.id}", document.mimeType, bytes)
                files.markUploaded(document.id, key, clock.now().toEpochMilliseconds())
                val result = remote.upsertDocument(RemoteDocumentMetadata(
                    document.remoteId, document.title, document.content, document.mimeType, document.checksum,
                    document.archiveChecksum, document.pageCount, document.created.toEpochMilliseconds(),
                    document.modified.toEpochMilliseconds(), document.added.toEpochMilliseconds(), document.filename,
                    document.originalFilename, document.expiresAt?.toEpochMilliseconds(), document.reminderDaysBeforeExpiry,
                    key, document.serverVersion,
                    relations?.correspondent?.name, relations?.documentType?.name, relations?.tags?.map { it.name } ?: emptyList()
                ))
                repository.markSynced(document.id, result.remoteId, result.serverVersion, document.modified.toEpochMilliseconds())
                operations.deleteOperation(op.documentId, op.operation)
                processed++
            } catch (e: SyncConflictException) {
                repository.markSyncState(op.documentId, "CONFLICT", e.message)
                operations.update(op.copy(state = "CONFLICT", lastError = e.message, attemptCount = op.attemptCount + 1, updatedAt = clock.now().toEpochMilliseconds()))
            } catch (e: Throwable) {
                repository.markSyncState(op.documentId, "FAILED", e.message)
                operations.update(op.copy(state = if (op.attemptCount >= 5) "FAILED" else "PENDING", lastError = e.message, attemptCount = op.attemptCount + 1, updatedAt = clock.now().toEpochMilliseconds()))
            }
        }
        return processed
    }
}

class EnqueueSyncOperationUseCase(private val operations: SyncOperationDao, private val clock: kotlin.time.Clock = kotlin.time.Clock.System) : com.nextstepai.paperless.domain.sync.SyncOperationEnqueuer {
    override suspend fun enqueue(documentId: Long, operation: String) {
        val now = clock.now().toEpochMilliseconds()
        operations.deleteOperation(documentId, operation)
        operations.insert(SyncOperationEntity(documentId = documentId, operation = operation, createdAt = now, updatedAt = now))
    }
}

