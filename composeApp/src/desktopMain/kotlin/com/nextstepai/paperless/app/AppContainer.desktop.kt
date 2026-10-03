package com.nextstepai.paperless.app

import com.nextstepai.paperless.database.buildDatabase
import com.nextstepai.paperless.database.getDatabaseBuilder
import com.nextstepai.paperless.domain.auth.AuthTokenProvider
import com.nextstepai.paperless.domain.capture.ChecksumGenerator
import com.nextstepai.paperless.domain.ocr.DesktopOcrEngine
import com.nextstepai.paperless.domain.sync.DesktopDocumentFileStore
import com.nextstepai.paperless.domain.sync.ProcessDocumentCaptureUseCase
import com.nextstepai.paperless.domain.usecase.DeleteDocumentUseCase
import com.nextstepai.paperless.domain.usecase.UpdateDocumentMetadataUseCase
import com.nextstepai.paperless.platform.DesktopReminderScheduler
import com.nextstepai.paperless.platform.PlatformDocumentPreviewer
import com.nextstepai.paperless.platform.desktopPreviewStore
import com.nextstepai.paperless.documents.data.*
import com.nextstepai.paperless.documents.presentation.DocumentsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

actual object AppContainer {
    private var initialized = false
    private lateinit var _viewModel: DocumentsViewModel
    private var scheduler: DesktopSyncScheduler? = null
    actual val documentsViewModel: DocumentsViewModel get() = _viewModel

    actual fun initialize(context: Any?) {
        if (initialized) return
        val db = buildDatabase(getDatabaseBuilder())
        val repository = RoomDocumentRepository(db.documentDao(), db.syncOperationDao())
        val enqueuer = EnqueueSyncOperationUseCase(db.syncOperationDao())
        val fileStore = DesktopDocumentFileStore(File(System.getProperty("user.home"), ".nextstepai-paperless/files"))
        desktopPreviewStore = fileStore
        val capture = ProcessDocumentCaptureUseCase(repository, DesktopOcrEngine(), fileStore, checksumGenerator = DesktopChecksumGenerator(), enqueuer = enqueuer)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        _viewModel = DocumentsViewModel(repository, RoomCatalogRepository(db.catalogDao()), capture, DeleteDocumentUseCase(repository, enqueuer), UpdateDocumentMetadataUseCase(repository, enqueuer), PlatformDocumentPreviewer, DesktopReminderScheduler(), scope, syncEnqueuer = enqueuer, onTriggerSync = { startBackgroundSync() })
        val baseUrl = System.getenv("PAPERLESS_API_URL")?.takeIf { it.isNotBlank() }
        val token = System.getenv("PAPERLESS_API_TOKEN")?.takeIf { it.isNotBlank() }
        if (baseUrl != null && token != null) {
            val remote = KtorRemoteDocumentDataSource(baseUrl, StaticTokenProvider(token), createJsonClient())
            val processor = SyncQueueProcessor(db.syncOperationDao(), db.documentFileDao(), repository, remote, fileStore)
            scheduler = DesktopSyncScheduler(processor, scope)
        }
        initialized = true
    }
    actual fun startBackgroundSync() { scheduler?.schedule() }
}
private class StaticTokenProvider(private val value: String) : AuthTokenProvider { override suspend fun accessToken(): String = value }
private class DesktopChecksumGenerator : ChecksumGenerator {
    override fun sha256(bytes: ByteArray): String = java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
