@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.documents.data

import com.nextstepai.paperless.database.dao.CatalogDao
import com.nextstepai.paperless.database.entity.CustomFieldInstanceEntity
import com.nextstepai.paperless.database.entity.DocumentTagCrossRef
import com.nextstepai.paperless.database.mapper.*
import com.nextstepai.paperless.domain.model.CustomFieldValue
import com.nextstepai.paperless.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCatalogRepository(private val dao: CatalogDao) : CatalogRepository {
    override fun observeTags() = dao.observeTags().map { it.map { e -> e.toDomain() } }
    override fun observeCorrespondents() = dao.observeCorrespondents().map { it.map { e -> e.toDomain() } }
    override fun observeDocumentTypes() = dao.observeDocumentTypes().map { it.map { e -> e.toDomain() } }
    override fun observeCustomFields() = dao.observeCustomFields().map { it.map { e -> com.nextstepai.paperless.domain.model.CustomField(e.id, e.name, runCatching { com.nextstepai.paperless.domain.model.CustomFieldType.valueOf(e.dataType) }.getOrDefault(com.nextstepai.paperless.domain.model.CustomFieldType.STRING), e.extraDataJson) } }
    override fun observeCustomFieldValues(documentId: Long) = dao.observeCustomFieldValues(documentId).map { list -> list.map { e -> CustomFieldValue(e.id, e.documentId, e.fieldId, e.valueText ?: e.valueUrl, e.valueBool, e.valueDate, e.valueInt, e.valueFloat, e.valueMonetary, e.valueDocumentIdsJson, e.valueSelect, e.valueLongText) } }
    override suspend fun setDocumentTags(documentId: Long, tagIds: List<Long>) { dao.clearDocumentTags(documentId); tagIds.distinct().forEach { dao.insertDocumentTag(DocumentTagCrossRef(documentId, it)) } }
    override suspend fun updateDocumentClassification(documentId: Long, correspondentId: Long?, documentTypeId: Long?) = dao.updateClassification(documentId, correspondentId, documentTypeId, kotlin.time.Clock.System.now().toEpochMilliseconds())
    override suspend fun saveCustomFieldValue(value: CustomFieldValue) = dao.upsertCustomFieldValue(CustomFieldInstanceEntity(value.id, value.documentId, value.fieldId, kotlin.time.Clock.System.now().toEpochMilliseconds(), value.text, value.boolean, null, value.dateEpochDay, value.int, value.float, value.monetary, value.documentIdsJson, value.select, value.longText, false))
}
