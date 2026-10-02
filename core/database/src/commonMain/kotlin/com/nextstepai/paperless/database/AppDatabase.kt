package com.nextstepai.paperless.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nextstepai.paperless.database.dao.*
import com.nextstepai.paperless.database.entity.*

@Database(entities = [DocumentEntity::class, TagEntity::class, DocumentTagCrossRef::class, CorrespondentEntity::class, DocumentTypeEntity::class, StoragePathEntity::class, CustomFieldEntity::class, CustomFieldInstanceEntity::class, DocumentFileEntity::class, SyncOperationEntity::class], version = 5, exportSchema = true)
abstract class AppDatabase : RoomDatabase() { abstract fun documentDao(): DocumentDao; abstract fun documentTagDao(): DocumentTagDao; abstract fun catalogDao(): CatalogDao
    abstract fun documentFileDao(): DocumentFileDao
    abstract fun syncOperationDao(): SyncOperationDao
}
