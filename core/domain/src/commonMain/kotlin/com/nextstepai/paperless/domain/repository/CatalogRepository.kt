package com.nextstepai.paperless.domain.repository

import com.nextstepai.paperless.domain.model.Correspondent
import com.nextstepai.paperless.domain.model.CustomField
import com.nextstepai.paperless.domain.model.CustomFieldValue
import com.nextstepai.paperless.domain.model.DocumentType
import com.nextstepai.paperless.domain.model.StoragePath
import com.nextstepai.paperless.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun observeTags(): Flow<List<Tag>>
    fun observeCorrespondents(): Flow<List<Correspondent>>
    fun observeDocumentTypes(): Flow<List<DocumentType>>
    fun observeStoragePaths(): Flow<List<StoragePath>>
    fun observeCustomFields(): Flow<List<CustomField>>
    fun observeCustomFieldValues(documentId: Long): Flow<List<CustomFieldValue>>
    suspend fun saveTag(tag: Tag): Long
    suspend fun deleteTag(id: Long)
    suspend fun saveCorrespondent(correspondent: Correspondent): Long
    suspend fun deleteCorrespondent(id: Long)
    suspend fun saveDocumentType(documentType: DocumentType): Long
    suspend fun deleteDocumentType(id: Long)
    suspend fun saveStoragePath(storagePath: StoragePath): Long
    suspend fun deleteStoragePath(id: Long)
    suspend fun addCorrespondent(name: String): Long
    suspend fun addDocumentType(name: String): Long
    suspend fun addStoragePath(name: String, path: String = ""): Long
    suspend fun setDocumentTags(documentId: Long, tagIds: List<Long>)
    suspend fun updateDocumentClassification(documentId: Long, correspondentId: Long?, documentTypeId: Long?, storagePathId: Long?)
    suspend fun saveCustomFieldValue(value: CustomFieldValue)
}
