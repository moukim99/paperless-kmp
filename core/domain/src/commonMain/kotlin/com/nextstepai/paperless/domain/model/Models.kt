@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.domain.model

import kotlinx.datetime.Instant

data class Correspondent(val id: Long, val remoteId: String?, val name: String, val match: String = "", val matchingAlgorithm: Int = 1, val insensitive: Boolean = true)
data class Tag(val id: Long, val remoteId: String?, val name: String, val color: String = "#a6cee3", val parentId: Long?, val isInbox: Boolean = false)
data class DocumentType(val id: Long, val remoteId: String?, val name: String, val match: String = "", val matchingAlgorithm: Int = 1, val insensitive: Boolean = true)
data class StoragePath(val id: Long, val remoteId: String?, val name: String, val path: String)
data class Document(
    val id: Long, val remoteId: String?, val ownerId: String?, val correspondentId: Long?, val storagePathId: Long?,
    val title: String, val content: String, val mimeType: String, val checksum: String, val archiveChecksum: String?,
    val pageCount: Int?, val created: Instant, val modified: Instant, val added: Instant,
    val filename: String?, val archiveFilename: String?, val originalFilename: String?, val archiveSerialNumber: Long?,
    val rootDocumentId: Long?, val versionIndex: Int?, val versionLabel: String?,
    val expiresAt: Instant?, val reminderDaysBeforeExpiry: Int?, val isDeleted: Boolean = false, val syncState: String = "PENDING_UPLOAD", val serverVersion: Long = 0, val lastSyncedModified: Instant? = null
)
enum class CustomFieldType { STRING, URL, DATE, BOOLEAN, INTEGER, FLOAT, MONETARY, DOCUMENT_LINK, SELECT, LONG_TEXT }
data class CustomField(val id: Long, val name: String, val type: CustomFieldType, val extraDataJson: String?)
data class CustomFieldValue(val id: Long, val documentId: Long, val fieldId: Long, val text: String?, val boolean: Boolean?, val dateEpochDay: Long?, val int: Long?, val float: Double?, val monetary: String?, val documentIdsJson: String?, val select: String?, val longText: String?)
data class DocumentWithRelations(val document: Document, val correspondent: Correspondent?, val documentType: DocumentType?, val storagePath: StoragePath?, val tags: List<Tag>)
