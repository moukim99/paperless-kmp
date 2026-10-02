package com.nextstepai.paperless.domain.repository

import com.nextstepai.paperless.domain.model.Document
import com.nextstepai.paperless.domain.model.DocumentWithRelations
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocuments(): Flow<List<Document>>
    fun observeDocument(id: Long): Flow<DocumentWithRelations?>
    fun observeLatestSyncError(documentId: Long): Flow<String?>
    fun observeExpiring(beforeEpochMillis: Long): Flow<List<Document>>
    suspend fun getByChecksum(checksum: String): Document?
    suspend fun getById(id: Long): Document?
    suspend fun getWithRelations(id: Long): DocumentWithRelations?
    suspend fun save(document: Document): Long
    suspend fun delete(id: Long)
    suspend fun markSynced(id: Long, remoteId: String, serverVersion: Long, syncedModified: Long)
    suspend fun markSyncState(id: Long, state: String, error: String? = null)
    suspend fun updateMetadata(id: Long, title: String, created: Long, archiveSerialNumber: Long?, expiresAt: Long?, reminderDaysBeforeExpiry: Int?)
    suspend fun search(query: String): List<Document>
}
