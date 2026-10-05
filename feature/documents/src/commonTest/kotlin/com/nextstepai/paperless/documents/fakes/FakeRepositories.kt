package com.nextstepai.paperless.documents.fakes

import com.nextstepai.paperless.domain.model.Correspondent
import com.nextstepai.paperless.domain.model.CustomField
import com.nextstepai.paperless.domain.model.CustomFieldValue
import com.nextstepai.paperless.domain.model.Document
import com.nextstepai.paperless.domain.model.DocumentType
import com.nextstepai.paperless.domain.model.DocumentWithRelations
import com.nextstepai.paperless.domain.model.StoragePath
import com.nextstepai.paperless.domain.model.Tag
import com.nextstepai.paperless.domain.repository.CatalogRepository
import com.nextstepai.paperless.domain.repository.DocumentRepository
import com.nextstepai.paperless.domain.platform.DocumentPreviewer
import com.nextstepai.paperless.domain.platform.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class FakeDocumentRepository : DocumentRepository {
    val docsState = MutableStateFlow<List<Document>>(emptyList())
    val docRelationsState = MutableStateFlow<Map<Long, DocumentWithRelations>>(emptyMap())
    val syncErrorsState = MutableStateFlow<Map<Long, String>>(emptyMap())

    override fun observeDocuments(): Flow<List<Document>> = docsState
    override fun observeDocument(id: Long): Flow<DocumentWithRelations?> = docRelationsState.map { it[id] }
    override fun observeVersions(rootId: Long): Flow<List<Document>> = docsState.map { list ->
        list.filter { (it.rootDocumentId == rootId || it.id == rootId) && !it.isDeleted }
    }
    override fun observeLatestSyncError(documentId: Long): Flow<String?> = syncErrorsState.map { it[documentId] }
    override fun observeExpiring(beforeEpochMillis: Long): Flow<List<Document>> = docsState.map { list ->
        list.filter { d -> d.expiresAt != null && d.expiresAt!!.toEpochMilliseconds() <= beforeEpochMillis && !d.isDeleted }
    }

    override suspend fun getByChecksum(checksum: String): Document? = docsState.value.firstOrNull { it.checksum == checksum }
    override suspend fun getById(id: Long): Document? = docsState.value.firstOrNull { it.id == id }
    override suspend fun getWithRelations(id: Long): DocumentWithRelations? = docRelationsState.value[id]
    override suspend fun save(document: Document): Long {
        val id = if (document.id == 0L) (docsState.value.maxOfOrNull { it.id } ?: 0L) + 1L else document.id
        val updatedDoc = document.copy(id = id)
        docsState.value = docsState.value.filterNot { it.id == id } + updatedDoc
        val existingRel = docRelationsState.value[id]
        docRelationsState.value = docRelationsState.value + (id to (existingRel?.copy(document = updatedDoc) ?: DocumentWithRelations(updatedDoc, null, null, null, emptyList())))
        return id
    }
    override suspend fun delete(id: Long) {
        docsState.value = docsState.value.map { if (it.id == id) it.copy(isDeleted = true) else it }
    }
    override suspend fun markSynced(id: Long, remoteId: String, serverVersion: Long, syncedModified: Long) {
        docsState.value = docsState.value.map { if (it.id == id) it.copy(remoteId = remoteId, serverVersion = serverVersion, syncState = "SYNCED") else it }
    }
    override suspend fun markSyncState(id: Long, state: String, error: String?) {
        docsState.value = docsState.value.map { if (it.id == id) it.copy(syncState = state) else it }
        if (error != null) {
            syncErrorsState.value = syncErrorsState.value + (id to error)
        }
    }
    override suspend fun updateMetadata(
        id: Long,
        title: String,
        content: String,
        versionLabel: String?,
        created: Long,
        archiveSerialNumber: Long?,
        expiresAt: Long?,
        reminderDaysBeforeExpiry: Int?
    ) {
        docsState.value = docsState.value.map { d ->
            if (d.id == id) {
                d.copy(
                    title = title,
                    content = content,
                    versionLabel = versionLabel,
                    created = Instant.fromEpochMilliseconds(created),
                    archiveSerialNumber = archiveSerialNumber,
                    expiresAt = expiresAt?.let { Instant.fromEpochMilliseconds(it) },
                    reminderDaysBeforeExpiry = reminderDaysBeforeExpiry
                )
            } else d
        }
        val rel = docRelationsState.value[id]
        if (rel != null) {
            val updatedDoc = docsState.value.first { it.id == id }
            docRelationsState.value = docRelationsState.value + (id to rel.copy(document = updatedDoc))
        }
    }
    override suspend fun search(query: String): List<Document> {
        return docsState.value.filter { it.title.contains(query, ignoreCase = true) || it.content.contains(query, ignoreCase = true) }
    }
}

class FakeCatalogRepository : CatalogRepository {
    val tagsState = MutableStateFlow<List<Tag>>(emptyList())
    val correspondentsState = MutableStateFlow<List<Correspondent>>(emptyList())
    val docTypesState = MutableStateFlow<List<DocumentType>>(emptyList())
    val storagePathsState = MutableStateFlow<List<StoragePath>>(emptyList())
    val customFieldsState = MutableStateFlow<List<CustomField>>(emptyList())
    val customFieldValuesState = MutableStateFlow<Map<Long, List<CustomFieldValue>>>(emptyMap())

    override fun observeTags(): Flow<List<Tag>> = tagsState
    override fun observeCorrespondents(): Flow<List<Correspondent>> = correspondentsState
    override fun observeDocumentTypes(): Flow<List<DocumentType>> = docTypesState
    override fun observeStoragePaths(): Flow<List<StoragePath>> = storagePathsState
    override fun observeCustomFields(): Flow<List<CustomField>> = customFieldsState
    override fun observeCustomFieldValues(documentId: Long): Flow<List<CustomFieldValue>> =
        customFieldValuesState.map { it[documentId].orEmpty() }

    override suspend fun saveTag(tag: Tag): Long {
        val id = if (tag.id == 0L) (tagsState.value.maxOfOrNull { it.id } ?: 0L) + 1L else tag.id
        val updated = tag.copy(id = id)
        tagsState.value = tagsState.value.filterNot { it.id == id } + updated
        return id
    }
    override suspend fun deleteTag(id: Long) { tagsState.value = tagsState.value.filterNot { it.id == id } }

    override suspend fun saveCorrespondent(correspondent: Correspondent): Long {
        val id = if (correspondent.id == 0L) (correspondentsState.value.maxOfOrNull { it.id } ?: 0L) + 1L else correspondent.id
        val updated = correspondent.copy(id = id)
        correspondentsState.value = correspondentsState.value.filterNot { it.id == id } + updated
        return id
    }
    override suspend fun deleteCorrespondent(id: Long) { correspondentsState.value = correspondentsState.value.filterNot { it.id == id } }

    override suspend fun saveDocumentType(documentType: DocumentType): Long {
        val id = if (documentType.id == 0L) (docTypesState.value.maxOfOrNull { it.id } ?: 0L) + 1L else documentType.id
        val updated = documentType.copy(id = id)
        docTypesState.value = docTypesState.value.filterNot { it.id == id } + updated
        return id
    }
    override suspend fun deleteDocumentType(id: Long) { docTypesState.value = docTypesState.value.filterNot { it.id == id } }

    override suspend fun saveStoragePath(storagePath: StoragePath): Long {
        val id = if (storagePath.id == 0L) (storagePathsState.value.maxOfOrNull { it.id } ?: 0L) + 1L else storagePath.id
        val updated = storagePath.copy(id = id)
        storagePathsState.value = storagePathsState.value.filterNot { it.id == id } + updated
        return id
    }
    override suspend fun deleteStoragePath(id: Long) { storagePathsState.value = storagePathsState.value.filterNot { it.id == id } }

    override suspend fun addCorrespondent(name: String): Long = saveCorrespondent(Correspondent(0, null, name))
    override suspend fun addDocumentType(name: String): Long = saveDocumentType(DocumentType(0, null, name))
    override suspend fun addStoragePath(name: String, path: String): Long = saveStoragePath(StoragePath(0, null, name, path))

    override suspend fun setDocumentTags(documentId: Long, tagIds: List<Long>) {}
    override suspend fun updateDocumentClassification(documentId: Long, correspondentId: Long?, documentTypeId: Long?, storagePathId: Long?) {}
    override suspend fun saveCustomFieldValue(value: CustomFieldValue) {
        val list = customFieldValuesState.value[value.documentId].orEmpty().filterNot { it.fieldId == value.fieldId }
        customFieldValuesState.value = customFieldValuesState.value + (value.documentId to (list + value))
    }
}

class FakeDocumentPreviewer : DocumentPreviewer {
    var lastOpenedDocumentId: Long? = null
    override suspend fun open(documentId: Long): Result<Unit> {
        lastOpenedDocumentId = documentId
        return Result.success(Unit)
    }
    override suspend fun readBytes(documentId: Long): ByteArray? = ByteArray(0)
    override suspend fun getLocalPath(documentId: Long): String? = "/tmp/$documentId"
}

class FakeReminderScheduler : ReminderScheduler {
    val scheduledReminders = mutableListOf<Long>()
    val cancelledReminders = mutableListOf<Long>()
    override fun schedule(documentId: Long, title: String, expiryEpochMillis: Long, daysBeforeExpiry: Int) {
        scheduledReminders.add(documentId)
    }
    override fun cancel(documentId: Long) {
        cancelledReminders.add(documentId)
    }
}

class FakeOcrEngine : com.nextstepai.paperless.domain.ocr.OcrEngine {
    override suspend fun extractText(bytes: ByteArray, mimeType: String): String = "Extracted text"
}

class FakeDocumentFileStore : com.nextstepai.paperless.domain.sync.DocumentFileStore {
    override suspend fun read(documentId: Long): ByteArray = ByteArray(0)
    override suspend fun save(documentId: Long, filename: String, bytes: ByteArray) {}
    override suspend fun localPath(documentId: Long): String? = "/tmp/$documentId"
    override suspend fun cacheSize(): Long = 1024L
    override suspend fun clearCache() {}
}

class FakeChecksumGenerator : com.nextstepai.paperless.domain.capture.ChecksumGenerator {
    override fun sha256(bytes: ByteArray): String = "chk_" + bytes.contentHashCode()
}
