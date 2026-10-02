package com.nextstepai.paperless.app

import com.nextstepai.paperless.BuildConfig
import android.content.Context
import com.nextstepai.paperless.database.buildDatabase
import com.nextstepai.paperless.database.getDatabaseBuilder
import com.nextstepai.paperless.database.initializeDatabaseContext
import com.nextstepai.paperless.domain.auth.AuthTokenProvider
import com.nextstepai.paperless.domain.capture.ChecksumGenerator
import com.nextstepai.paperless.domain.ocr.AndroidMlKitOcrEngine
import com.nextstepai.paperless.domain.sync.AndroidDocumentFileStore
import com.nextstepai.paperless.domain.sync.ProcessDocumentCaptureUseCase
import com.nextstepai.paperless.domain.usecase.DeleteDocumentUseCase
import com.nextstepai.paperless.domain.usecase.UpdateDocumentMetadataUseCase
import com.nextstepai.paperless.platform.*
import com.nextstepai.paperless.documents.data.*
import com.nextstepai.paperless.documents.presentation.DocumentsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

actual object AppContainer {
    private var initialized = false
    private lateinit var _viewModel: DocumentsViewModel
    private var scheduler: AndroidSyncScheduler? = null
    actual val documentsViewModel: DocumentsViewModel get() = _viewModel

    actual fun initialize(context: Any?) {
        if (initialized) return
        val app = requireNotNull(context as? Context) { "Android Context is required" }.applicationContext
        initializeDatabaseContext(app)
        val db = buildDatabase(getDatabaseBuilder())
        val repository = RoomDocumentRepository(db.documentDao(), db.syncOperationDao())
        val enqueuer = EnqueueSyncOperationUseCase(db.syncOperationDao())
        val fileStore = AndroidDocumentFileStore(app)
        androidPreviewContext = app
        androidPreviewStore = fileStore
        val capture = ProcessDocumentCaptureUseCase(repository, AndroidMlKitOcrEngine(), fileStore, checksumGenerator = AndroidChecksumGenerator(), enqueuer = enqueuer)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        _viewModel = DocumentsViewModel(repository, RoomCatalogRepository(db.catalogDao()), capture, DeleteDocumentUseCase(repository, enqueuer), UpdateDocumentMetadataUseCase(repository, enqueuer), PlatformDocumentPreviewer, AndroidReminderScheduler(app), scope, syncEnqueuer = enqueuer, onTriggerSync = { startBackgroundSync() })
        val token = BuildConfig.API_TOKEN.takeIf { it.isNotBlank() }
        val baseUrl = BuildConfig.API_BASE_URL.takeIf { it.isNotBlank() && !it.contains("example.invalid") }
        if (baseUrl != null && token != null) {
            val remote = KtorRemoteDocumentDataSource(baseUrl, StaticTokenProvider(token), createJsonClient())
            AndroidSyncRuntime.processor = SyncQueueProcessor(db.syncOperationDao(), db.documentFileDao(), repository, remote, fileStore)
            scheduler = AndroidSyncScheduler(app)
        }
        initialized = true
    }

    actual fun startBackgroundSync() { scheduler?.schedule() }
}

private class StaticTokenProvider(private val value: String) : AuthTokenProvider { override suspend fun accessToken(): String = value }
private class AndroidChecksumGenerator : ChecksumGenerator {
    override fun sha256(bytes: ByteArray): String = java.security.MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
