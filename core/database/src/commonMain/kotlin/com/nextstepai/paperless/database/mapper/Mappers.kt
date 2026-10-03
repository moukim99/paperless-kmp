@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.nextstepai.paperless.database.mapper

import com.nextstepai.paperless.database.entity.*
import com.nextstepai.paperless.database.relation.DocumentWithRelationsEntity
import com.nextstepai.paperless.domain.model.*
import kotlin.time.Instant

fun CorrespondentEntity.toDomain() = Correspondent(id, remoteId, name, match, matchingAlgorithm, insensitive)
fun Correspondent.toEntity() = CorrespondentEntity(id, remoteId, null, name, match, matchingAlgorithm, insensitive)

fun TagEntity.toDomain() = Tag(id, remoteId, name, color, parentId, isInbox, match, matchingAlgorithm, insensitive)
fun Tag.toEntity() = TagEntity(id, remoteId, null, name, match, matchingAlgorithm, insensitive, color, parentId, isInbox)

fun DocumentTypeEntity.toDomain() = DocumentType(id, remoteId, name, match, matchingAlgorithm, insensitive)
fun DocumentType.toEntity() = DocumentTypeEntity(id, remoteId, null, name, match, matchingAlgorithm, insensitive)

fun StoragePathEntity.toDomain() = StoragePath(id, remoteId, name, path, match, matchingAlgorithm, insensitive)
fun StoragePath.toEntity() = StoragePathEntity(id, remoteId, null, name, match, matchingAlgorithm, insensitive, path)
fun DocumentEntity.toDomain() = Document(id, remoteId, ownerId, correspondentId, storagePathId, title, content, mimeType, checksum, archiveChecksum, pageCount, Instant.fromEpochMilliseconds(created), Instant.fromEpochMilliseconds(modified), Instant.fromEpochMilliseconds(added), filename, archiveFilename, originalFilename, archiveSerialNumber, rootDocumentId, versionIndex, versionLabel, documentTypeId, expiresAt?.let(Instant::fromEpochMilliseconds), reminderDaysBeforeExpiry, isDeleted, syncState, serverVersion, lastSyncedModified?.let(Instant::fromEpochMilliseconds))
fun Document.toEntity(now: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()) = DocumentEntity(id, remoteId, ownerId, correspondentId, storagePathId, title, content, content.length.toLong(), mimeType, checksum, archiveChecksum, pageCount, created.toEpochMilliseconds(), modified.toEpochMilliseconds(), added.toEpochMilliseconds(), filename, archiveFilename, originalFilename, archiveSerialNumber, rootDocumentId, versionIndex, versionLabel, documentTypeId, expiresAt?.toEpochMilliseconds(), reminderDaysBeforeExpiry, isDeleted, syncState, now, serverVersion, lastSyncedModified?.toEpochMilliseconds())
fun DocumentWithRelationsEntity.toDomain() = DocumentWithRelations(document.toDomain(), correspondent?.toDomain(), documentType?.toDomain(), storagePath?.toDomain(), tags.map { it.toDomain() })
