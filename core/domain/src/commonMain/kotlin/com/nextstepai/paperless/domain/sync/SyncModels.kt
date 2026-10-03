package com.nextstepai.paperless.domain.sync

enum class SyncState { SYNCED, PENDING_UPLOAD, PENDING_UPDATE, PENDING_DELETE, UPLOADING, FAILED, CONFLICT }

data class SyncSettings(
    val serverUrl: String = "https://example.invalid",
    val authToken: String = "",
    val syncIntervalMinutes: Int = 15,
    val syncOnWifiOnly: Boolean = false
)

data class RemoteDocumentMetadata(
    val remoteId: String?, val title: String, val content: String, val mimeType: String,
    val checksum: String, val archiveChecksum: String?, val pageCount: Int?, val created: Long,
    val modified: Long, val added: Long, val filename: String?, val originalFilename: String?,
    val expiresAt: Long?, val reminderDaysBeforeExpiry: Int?, val r2Key: String?, val baseVersion: Long = 0,
    val correspondentName: String? = null, val documentTypeName: String? = null, val tagNames: List<String> = emptyList()
)
data class SyncResult(val remoteId: String, val r2Key: String?, val serverVersion: Long = 1, val modified: Long? = null)

class SyncConflictException(val remoteVersion: Long, message: String) : IllegalStateException(message)

interface RemoteDocumentDataSource {
    suspend fun uploadFile(documentId: String, filename: String, mimeType: String, bytes: ByteArray): String
    suspend fun upsertDocument(metadata: RemoteDocumentMetadata): SyncResult
    suspend fun deleteDocument(remoteId: String)
}
