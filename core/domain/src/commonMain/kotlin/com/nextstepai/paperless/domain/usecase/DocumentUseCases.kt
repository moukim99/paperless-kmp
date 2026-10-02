@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.domain.usecase

import com.nextstepai.paperless.domain.model.Document
import com.nextstepai.paperless.domain.repository.DocumentRepository
import com.nextstepai.paperless.domain.sync.SyncOperationEnqueuer
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.*
import kotlin.time.Duration.Companion.days

class ObserveDocumentsUseCase(private val repository: DocumentRepository) { operator fun invoke(): Flow<List<Document>> = repository.observeDocuments() }
class SearchDocumentsUseCase(private val repository: DocumentRepository) { suspend operator fun invoke(query: String): List<Document> = repository.search(query) }
class GetExpiringDocumentsUseCase(private val repository: DocumentRepository, private val clock: Clock = kotlin.time.Clock.System) {
    operator fun invoke(days: Int): Flow<List<Document>> = repository.observeExpiring((clock.now() + days.days).toEpochMilliseconds())
}
class DeleteDocumentUseCase(private val repository: DocumentRepository, private val enqueuer: SyncOperationEnqueuer? = null) {
    suspend operator fun invoke(id: Long) { repository.delete(id); enqueuer?.enqueue(id, "DELETE") }
}
class UpdateDocumentMetadataUseCase(private val repository: DocumentRepository, private val enqueuer: SyncOperationEnqueuer? = null) {
    suspend operator fun invoke(id: Long, title: String, expiresAt: kotlinx.datetime.Instant?, reminderDaysBeforeExpiry: Int?) {
        repository.updateMetadata(id, title.trim().ifBlank { "Untitled document" }, expiresAt?.toEpochMilliseconds(), reminderDaysBeforeExpiry)
        enqueuer?.enqueue(id, "UPLOAD")
    }
}
