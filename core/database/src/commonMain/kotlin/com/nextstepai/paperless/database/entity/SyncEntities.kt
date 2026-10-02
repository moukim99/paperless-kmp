package com.nextstepai.paperless.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "document_files",
    indices = [Index("documentId", unique = true), Index("r2Key", unique = true)]
)
data class DocumentFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val r2Key: String?,
    val localPath: String?,
    val sizeBytes: Long,
    val mimeType: String,
    val uploadedAt: Long?,
    val uploadState: String = "PENDING"
)

@Entity(
    tableName = "sync_operations",
    indices = [Index("documentId"), Index("state"), Index("createdAt")]
)
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val operation: String,
    val state: String = "PENDING",
    val attemptCount: Int = 0,
    val lastError: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
