package com.nextstepai.paperless.database.dao

import androidx.room.*
import com.nextstepai.paperless.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {
    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE") fun observeTags(): Flow<List<TagEntity>>
    @Query("SELECT * FROM correspondents ORDER BY name COLLATE NOCASE") fun observeCorrespondents(): Flow<List<CorrespondentEntity>>
    @Query("SELECT * FROM document_types ORDER BY name COLLATE NOCASE") fun observeDocumentTypes(): Flow<List<DocumentTypeEntity>>
    @Query("SELECT * FROM custom_fields ORDER BY name COLLATE NOCASE") fun observeCustomFields(): Flow<List<CustomFieldEntity>>
    @Query("SELECT * FROM custom_field_instances WHERE documentId = :documentId AND isDeleted = 0 ORDER BY fieldId") fun observeCustomFieldValues(documentId: Long): Flow<List<CustomFieldInstanceEntity>>

    @Query("SELECT tagId FROM document_tags WHERE documentId = :documentId") suspend fun tagIds(documentId: Long): List<Long>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertDocumentTag(value: DocumentTagCrossRef)
    @Query("DELETE FROM document_tags WHERE documentId = :documentId") suspend fun clearDocumentTags(documentId: Long)
    @Query("UPDATE documents SET correspondentId = :correspondentId, documentTypeId = :documentTypeId, modified = :modified, syncState = 'PENDING_UPDATE' WHERE id = :documentId") suspend fun updateClassification(documentId: Long, correspondentId: Long?, documentTypeId: Long?, modified: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertCustomFieldValue(value: CustomFieldInstanceEntity)
}
