@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.documents.data

import androidx.room.RoomRawQuery
import com.nextstepai.paperless.database.dao.DocumentDao
import com.nextstepai.paperless.database.entity.DocumentEntity
import com.nextstepai.paperless.database.mapper.*
import com.nextstepai.paperless.domain.model.*
import com.nextstepai.paperless.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDocumentRepository(private val dao: DocumentDao) : DocumentRepository {
    override fun observeDocuments(): Flow<List<Document>> = dao.observeAll().map { it.map(DocumentEntity::toDomain) }
    override fun observeDocument(id: Long): Flow<DocumentWithRelations?> = dao.observeWithRelations(id).map { it?.toDomain() }
    override fun observeExpiring(beforeEpochMillis: Long): Flow<List<Document>> = dao.observeExpiring(beforeEpochMillis).map { it.map(DocumentEntity::toDomain) }
    override suspend fun getByChecksum(checksum: String): Document? = dao.findByChecksum(checksum)?.toDomain()
    override suspend fun getById(id: Long): Document? = dao.findById(id)?.toDomain()
    override suspend fun getWithRelations(id: Long): DocumentWithRelations? = dao.findWithRelations(id)?.toDomain()
    override suspend fun save(document: Document): Long = dao.upsert(document.toEntity())
    override suspend fun delete(id: Long) = dao.softDelete(id, kotlin.time.Clock.System.now().toEpochMilliseconds())
    override suspend fun markSynced(id: Long, remoteId: String, serverVersion: Long, syncedModified: Long) = dao.markSynced(id, remoteId, serverVersion, kotlin.time.Clock.System.now().toEpochMilliseconds(), syncedModified)
    override suspend fun markSyncState(id: Long, state: String, error: String?) = dao.markSyncState(id, state)
    override suspend fun updateMetadata(id: Long, title: String, expiresAt: Long?, reminderDaysBeforeExpiry: Int?) = dao.updateMetadata(id, title, expiresAt, reminderDaysBeforeExpiry, kotlin.time.Clock.System.now().toEpochMilliseconds())
    override suspend fun search(query: String): List<Document> {
        val parts = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        if (parts.isEmpty()) return emptyList()
        val fts = parts.filterNot { it.startsWith("tag:", true) || it.startsWith("type:", true) || it.startsWith("correspondent:", true) || it.startsWith("after:", true) || it.startsWith("before:", true) || it.startsWith("expiry:", true) }
        val filters = parts.filter { it.contains(':') }
        val args = mutableListOf<Any>()
        val where = mutableListOf("d.isDeleted = 0")
        if (fts.isNotEmpty()) {
            val match = fts.joinToString(" AND ") { token -> "\"${token.replace("\"", "\"\"")}\"*" }
            where += "f MATCH ?"; args += match
        }
        filters.forEach { raw ->
            val key = raw.substringBefore(':').lowercase(); val value = raw.substringAfter(':').trim()
            when (key) {
                "tag" -> { where += "EXISTS (SELECT 1 FROM document_tags dt JOIN tags t ON t.id = dt.tagId WHERE dt.documentId = d.id AND lower(t.name) = lower(?))"; args += value }
                "type" -> { where += "EXISTS (SELECT 1 FROM document_types t WHERE t.id = d.documentTypeId AND lower(t.name) = lower(?))"; args += value }
                "correspondent" -> { where += "EXISTS (SELECT 1 FROM correspondents c WHERE c.id = d.correspondentId AND lower(c.name) = lower(?))"; args += value }
                "after" -> { where += "d.created >= strftime('%s', ?) * 1000"; args += value }
                "before" -> { where += "d.created < strftime('%s', ?) * 1000"; args += value }
                "expiry" -> if (value.equals("soon", true)) { where += "d.expiresAt IS NOT NULL AND d.expiresAt <= (strftime('%s','now') + 30*86400) * 1000" }
            }
        }
        val sql = if (fts.isNotEmpty()) "SELECT d.* FROM documents d JOIN documents_fts f ON f.rowid = d.id WHERE ${where.joinToString(" AND ")} ORDER BY d.created DESC LIMIT 100" else "SELECT d.* FROM documents d WHERE ${where.joinToString(" AND ")} ORDER BY d.created DESC LIMIT 100"
        val raw = RoomRawQuery(sql = sql, onBindStatement = { stmt -> args.forEachIndexed { index, value -> if (value is String) stmt.bindText(index + 1, value) else stmt.bindLong(index + 1, (value as Number).toLong()) } })
        return dao.searchFts(raw).map(DocumentEntity::toDomain)
    }
}
