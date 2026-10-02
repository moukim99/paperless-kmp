package com.nextstepai.paperless.documents.data

import com.nextstepai.paperless.domain.auth.AuthTokenProvider
import com.nextstepai.paperless.domain.sync.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

expect fun createHttpClient(): HttpClient

@Serializable private data class RemoteMetadataPayload(
    val remoteId: String?, val title: String, val content: String, val mimeType: String, val checksum: String,
    val archiveChecksum: String?, val pageCount: Int?, val created: Long, val modified: Long, val added: Long,
    val filename: String?, val originalFilename: String?, val expiresAt: Long?, val reminderDaysBeforeExpiry: Int?,
    val r2Key: String?, val baseVersion: Long,
    val correspondent: CatalogPayload? = null, val documentType: CatalogPayload? = null, val tags: List<CatalogPayload> = emptyList()
)
@Serializable private data class CatalogPayload(val name: String)
@Serializable private data class UploadResponse(val key: String, val sizeBytes: Long, val mimeType: String)
@Serializable private data class SyncResponse(val remoteId: String, val r2Key: String? = null, val serverVersion: Long = 1, val modified: Long? = null)
@Serializable private data class ErrorResponse(val error: String? = null, val serverVersion: Long? = null)

class KtorRemoteDocumentDataSource(
    private val baseUrl: String,
    private val tokenProvider: AuthTokenProvider,
    private val client: HttpClient
) : RemoteDocumentDataSource {
    private suspend fun HttpRequestBuilder.auth() {
        tokenProvider.accessToken()?.let { bearerAuth(it) }
    }

    override suspend fun uploadFile(documentId: String, filename: String, mimeType: String, bytes: ByteArray): String {
        val response = client.submitFormWithBinaryData(
            url = "$baseUrl/api/files",
            formData = formData {
                append("documentId", documentId)
                append("file", bytes, Headers.build { append(HttpHeaders.ContentType, mimeType); append(HttpHeaders.ContentDisposition, "filename=\"$filename\"") })
            }
        ) { auth() }
        return response.body<UploadResponse>().key
    }

    override suspend fun upsertDocument(metadata: RemoteDocumentMetadata): SyncResult {
        val response = client.post("$baseUrl/api/documents/sync") {
            auth(); contentType(ContentType.Application.Json)
            setBody(RemoteMetadataPayload(metadata.remoteId, metadata.title, metadata.content, metadata.mimeType, metadata.checksum, metadata.archiveChecksum, metadata.pageCount, metadata.created, metadata.modified, metadata.added, metadata.filename, metadata.originalFilename, metadata.expiresAt, metadata.reminderDaysBeforeExpiry, metadata.r2Key, metadata.baseVersion,
                metadata.correspondentName?.let(::CatalogPayload), metadata.documentTypeName?.let(::CatalogPayload), metadata.tagNames.map(::CatalogPayload)))
        }
        if (response.status == HttpStatusCode.Conflict) {
            val body = response.body<ErrorResponse>()
            throw SyncConflictException(body.serverVersion ?: 0L, body.error ?: "Remote document changed")
        }
        return response.body<SyncResponse>().let { SyncResult(it.remoteId, it.r2Key, it.serverVersion, it.modified) }
    }

    override suspend fun deleteDocument(remoteId: String) {
        client.delete("$baseUrl/api/documents/$remoteId") { auth() }
    }
}

fun createJsonClient(): HttpClient = createHttpClient().config {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; encodeDefaults = true }) }
}
