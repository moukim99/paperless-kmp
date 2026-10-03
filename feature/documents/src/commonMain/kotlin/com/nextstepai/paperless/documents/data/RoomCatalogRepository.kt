@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.documents.data

import com.nextstepai.paperless.database.dao.CatalogDao
import com.nextstepai.paperless.database.entity.CorrespondentEntity
import com.nextstepai.paperless.database.entity.CustomFieldInstanceEntity
import com.nextstepai.paperless.database.entity.DocumentTagCrossRef
import com.nextstepai.paperless.database.entity.DocumentTypeEntity
import com.nextstepai.paperless.database.entity.StoragePathEntity
import com.nextstepai.paperless.database.entity.TagEntity
import com.nextstepai.paperless.database.mapper.*
import com.nextstepai.paperless.domain.model.Correspondent
import com.nextstepai.paperless.domain.model.CustomFieldValue
import com.nextstepai.paperless.domain.model.DocumentType
import com.nextstepai.paperless.domain.model.StoragePath
import com.nextstepai.paperless.domain.model.Tag
import com.nextstepai.paperless.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCatalogRepository(private val dao: CatalogDao) : CatalogRepository {
    override fun observeTags() = dao.observeTags().map { it.map { e -> e.toDomain() } }
    override fun observeCorrespondents() = dao.observeCorrespondents().map { it.map { e -> e.toDomain() } }
    override fun observeDocumentTypes() = dao.observeDocumentTypes().map { it.map { e -> e.toDomain() } }
    override fun observeStoragePaths() = dao.observeStoragePaths().map { it.map { e -> e.toDomain() } }
    override fun observeCustomFields() = dao.observeCustomFields().map { it.map { e -> com.nextstepai.paperless.domain.model.CustomField(e.id, e.name, runCatching { com.nextstepai.paperless.domain.model.CustomFieldType.valueOf(e.dataType) }.getOrDefault(com.nextstepai.paperless.domain.model.CustomFieldType.STRING), e.extraDataJson) } }
    override fun observeCustomFieldValues(documentId: Long) = dao.observeCustomFieldValues(documentId).map { list -> list.map { e -> CustomFieldValue(e.id, e.documentId, e.fieldId, e.valueText ?: e.valueUrl, e.valueBool, e.valueDate, e.valueInt, e.valueFloat, e.valueMonetary, e.valueDocumentIdsJson, e.valueSelect, e.valueLongText) } }
    override suspend fun saveTag(tag: Tag): Long = dao.insertTag(tag.toEntity())
    override suspend fun deleteTag(id: Long) = dao.deleteTag(id)
    override suspend fun saveCorrespondent(correspondent: Correspondent): Long = dao.insertCorrespondent(correspondent.toEntity())
    override suspend fun deleteCorrespondent(id: Long) = dao.deleteCorrespondent(id)
    override suspend fun saveDocumentType(documentType: DocumentType): Long = dao.insertDocumentType(documentType.toEntity())
    override suspend fun deleteDocumentType(id: Long) = dao.deleteDocumentType(id)
    override suspend fun saveStoragePath(storagePath: StoragePath): Long = dao.insertStoragePath(storagePath.toEntity())
    override suspend fun deleteStoragePath(id: Long) = dao.deleteStoragePath(id)
    override suspend fun addCorrespondent(name: String): Long = dao.insertCorrespondent(CorrespondentEntity(remoteId = null, ownerId = null, name = name.trim(), match = "", matchingAlgorithm = 1, insensitive = true))
    override suspend fun addDocumentType(name: String): Long = dao.insertDocumentType(DocumentTypeEntity(remoteId = null, ownerId = null, name = name.trim(), match = "", matchingAlgorithm = 1, insensitive = true))
    override suspend fun addStoragePath(name: String, path: String): Long = dao.insertStoragePath(StoragePathEntity(remoteId = null, ownerId = null, name = name.trim(), match = "", matchingAlgorithm = 1, insensitive = true, path = path.trim()))
    override suspend fun setDocumentTags(documentId: Long, tagIds: List<Long>) { dao.clearDocumentTags(documentId); tagIds.distinct().forEach { dao.insertDocumentTag(DocumentTagCrossRef(documentId, it)) } }
    override suspend fun updateDocumentClassification(documentId: Long, correspondentId: Long?, documentTypeId: Long?, storagePathId: Long?) = dao.updateClassification(documentId, correspondentId, documentTypeId, storagePathId, kotlin.time.Clock.System.now().toEpochMilliseconds())
    override suspend fun saveCustomFieldValue(value: CustomFieldValue) = dao.upsertCustomFieldValue(CustomFieldInstanceEntity(value.id, value.documentId, value.fieldId, kotlin.time.Clock.System.now().toEpochMilliseconds(), value.text, value.boolean, null, value.dateEpochDay, value.int, value.float, value.monetary, value.documentIdsJson, value.select, value.longText, false))
}
