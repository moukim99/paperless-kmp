package com.nextstepai.paperless.database.relation

import androidx.room.*
import com.nextstepai.paperless.database.entity.*

data class DocumentWithRelationsEntity(
    @Embedded val document: DocumentEntity,
    @Relation(parentColumn = "correspondentId", entityColumn = "id") val correspondent: CorrespondentEntity?,
    @Relation(parentColumn = "documentTypeId", entityColumn = "id") val documentType: DocumentTypeEntity?,
    @Relation(parentColumn = "storagePathId", entityColumn = "id") val storagePath: StoragePathEntity?,
    @Relation(entity = TagEntity::class, parentColumn = "id", entityColumn = "id", associateBy = Junction(DocumentTagCrossRef::class, parentColumn = "documentId", entityColumn = "tagId")) val tags: List<TagEntity>
)
