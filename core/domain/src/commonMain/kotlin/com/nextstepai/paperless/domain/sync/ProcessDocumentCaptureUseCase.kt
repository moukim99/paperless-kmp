@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.domain.sync

import com.nextstepai.paperless.domain.capture.DocumentInput
import com.nextstepai.paperless.domain.ocr.OcrEngine
import com.nextstepai.paperless.domain.repository.DocumentRepository
import kotlinx.datetime.*
import com.nextstepai.paperless.domain.capture.ChecksumGenerator

class ProcessDocumentCaptureUseCase(
    private val repository: DocumentRepository,
    private val ocrEngine: OcrEngine,
    private val fileStore: DocumentFileStore,
    private val clock: Clock = kotlin.time.Clock.System,
    private val checksumGenerator: ChecksumGenerator,
    private val enqueuer: SyncOperationEnqueuer? = null
) {
    suspend operator fun invoke(input: DocumentInput): Result<Long> = runCatching {
        val now = clock.now()
        val content = input.suppliedOcrText ?: ocrEngine.extractText(input.bytes, input.mimeType)
        val checksum = checksumGenerator.sha256(input.bytes)
        val existing = repository.getByChecksum(checksum)
        if (existing != null) return@runCatching existing.id
        val id = repository.save(
            com.nextstepai.paperless.domain.model.Document(
                id = 0,
                remoteId = null,
                ownerId = null,
                correspondentId = null,
                storagePathId = null,
                title = input.filename.substringBeforeLast('.', input.filename),
                content = content,
                mimeType = input.mimeType,
                checksum = checksum,
                archiveChecksum = null,
                pageCount = input.pageCount,
                created = now,
                modified = now,
                added = now,
                filename = input.filename,
                archiveFilename = null,
                originalFilename = input.filename,
                archiveSerialNumber = null,
                rootDocumentId = null,
                versionIndex = null,
                versionLabel = null,
                expiresAt = null,
                reminderDaysBeforeExpiry = null,
                isDeleted = false
            )
        )
        fileStore.save(id, input.filename, input.bytes)
        enqueuer?.enqueue(id, "UPLOAD")
        id
    }

}
