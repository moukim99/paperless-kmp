package com.nextstepai.paperless.database.dao

import androidx.room.*
import com.nextstepai.paperless.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentFileDao {
    @Query("SELECT * FROM document_files WHERE documentId = :documentId LIMIT 1")
    suspend fun findByDocumentId(documentId: Long): DocumentFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DocumentFileEntity): Long

    @Query("UPDATE document_files SET r2Key = :r2Key, uploadedAt = :uploadedAt, uploadState = 'UPLOADED' WHERE documentId = :documentId")
    suspend fun markUploaded(documentId: Long, r2Key: String, uploadedAt: Long)
}

@Dao
interface SyncOperationDao {
    @Query("SELECT * FROM sync_operations WHERE state = 'PENDING' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun nextPending(limit: Int = 20): List<SyncOperationEntity>

    @Query("UPDATE sync_operations SET state = 'PENDING', updatedAt = :now WHERE state = 'UPLOADING' AND updatedAt < :cutoff")
    suspend fun recoverStaleUploading(cutoff: Long, now: Long)

    @Query("SELECT * FROM sync_operations WHERE documentId = :documentId AND state = 'PENDING' ORDER BY createdAt DESC LIMIT 1")
    suspend fun pendingForDocument(documentId: Long): SyncOperationEntity?

    @Insert
    suspend fun insert(entity: SyncOperationEntity): Long

    @Update
    suspend fun update(entity: SyncOperationEntity)

    @Query("DELETE FROM sync_operations WHERE documentId = :documentId AND operation = :operation")
    suspend fun deleteOperation(documentId: Long, operation: String)
}
