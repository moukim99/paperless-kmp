package com.nextstepai.paperless.domain.repository

import com.nextstepai.paperless.domain.model.Correspondent
import com.nextstepai.paperless.domain.model.CustomField
import com.nextstepai.paperless.domain.model.CustomFieldValue
import com.nextstepai.paperless.domain.model.DocumentType
import com.nextstepai.paperless.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun observeTags(): Flow<List<Tag>>
    fun observeCorrespondents(): Flow<List<Correspondent>>
    fun observeDocumentTypes(): Flow<List<DocumentType>>
    fun observeCustomFields(): Flow<List<CustomField>>
    fun observeCustomFieldValues(documentId: Long): Flow<List<CustomFieldValue>>
    suspend fun setDocumentTags(documentId: Long, tagIds: List<Long>)
    suspend fun updateDocumentClassification(documentId: Long, correspondentId: Long?, documentTypeId: Long?)
    suspend fun saveCustomFieldValue(value: CustomFieldValue)
}
