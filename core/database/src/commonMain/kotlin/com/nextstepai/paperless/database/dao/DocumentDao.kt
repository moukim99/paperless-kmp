package com.nextstepai.paperless.database.dao

import androidx.room.*
import androidx.room.RoomRawQuery
import com.nextstepai.paperless.database.entity.*
import com.nextstepai.paperless.database.relation.DocumentWithRelationsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE isDeleted = 0 ORDER BY created DESC, id DESC")
    fun observeAll(): Flow<List<DocumentEntity>>
    @Transaction @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun observeWithRelations(id: Long): Flow<DocumentWithRelationsEntity?>
    @Transaction @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun findWithRelations(id: Long): DocumentWithRelationsEntity?
    @Query("SELECT * FROM documents WHERE checksum = :checksum LIMIT 1")
    suspend fun findByChecksum(checksum: String): DocumentEntity?
    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): DocumentEntity?
    @RawQuery
    suspend fun searchFts(query: RoomRawQuery): List<DocumentEntity>
    @Query("SELECT * FROM documents WHERE expiresAt IS NOT NULL AND expiresAt <= :beforeEpochMillis AND isDeleted = 0 ORDER BY expiresAt ASC")
    fun observeExpiring(beforeEpochMillis: Long): Flow<List<DocumentEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: DocumentEntity): Long
    @Query("UPDATE documents SET isDeleted = 1, syncState = 'PENDING_DELETE', modified = :modified WHERE id = :id") suspend fun softDelete(id: Long, modified: Long)
    @Query("UPDATE documents SET remoteId = :remoteId, serverVersion = :serverVersion, syncedAt = :syncedAt, lastSyncedModified = :syncedModified, syncState = 'SYNCED' WHERE id = :id")
    suspend fun markSynced(id: Long, remoteId: String, serverVersion: Long, syncedAt: Long, syncedModified: Long)
    @Query("UPDATE documents SET syncState = :state WHERE id = :id") suspend fun markSyncState(id: Long, state: String)
    @Query("UPDATE documents SET title = :title, expiresAt = :expiresAt, reminderDaysBeforeExpiry = :reminderDaysBeforeExpiry, modified = :modified, syncState = 'PENDING_UPDATE' WHERE id = :id")
    suspend fun updateMetadata(id: Long, title: String, expiresAt: Long?, reminderDaysBeforeExpiry: Int?, modified: Long)
}

@Dao
interface DocumentTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entity: DocumentTagCrossRef)
}
