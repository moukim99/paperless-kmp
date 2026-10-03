package com.nextstepai.paperless.documents.presentation

import com.nextstepai.paperless.documents.fakes.FakeCatalogRepository
import com.nextstepai.paperless.documents.fakes.FakeChecksumGenerator
import com.nextstepai.paperless.documents.fakes.FakeDocumentFileStore
import com.nextstepai.paperless.documents.fakes.FakeDocumentPreviewer
import com.nextstepai.paperless.documents.fakes.FakeDocumentRepository
import com.nextstepai.paperless.documents.fakes.FakeOcrEngine
import com.nextstepai.paperless.documents.fakes.FakeReminderScheduler
import com.nextstepai.paperless.domain.model.Correspondent
import com.nextstepai.paperless.domain.model.CustomField
import com.nextstepai.paperless.domain.model.CustomFieldType
import com.nextstepai.paperless.domain.model.Document
import com.nextstepai.paperless.domain.model.DocumentWithRelations
import com.nextstepai.paperless.domain.model.Tag
import com.nextstepai.paperless.domain.sync.ProcessDocumentCaptureUseCase
import com.nextstepai.paperless.domain.usecase.DeleteDocumentUseCase
import com.nextstepai.paperless.domain.usecase.UpdateDocumentMetadataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DocumentsViewModelTest {

    private lateinit var repository: FakeDocumentRepository
    private lateinit var catalog: FakeCatalogRepository
    private lateinit var previewer: FakeDocumentPreviewer
    private lateinit var reminders: FakeReminderScheduler
    private lateinit var viewModel: DocumentsViewModel
    private lateinit var viewModelScope: kotlinx.coroutines.CoroutineScope
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val now = kotlin.time.Clock.System.now()

    private val sampleDoc = Document(
        id = 1L,
        remoteId = "rem_1",
        ownerId = "owner_1",
        correspondentId = 1L,
        storagePathId = null,
        title = "Invoice March 2025",
        content = "Total amount due is 1500 USD for services rendered.",
        mimeType = "application/pdf",
        checksum = "chk_123",
        archiveChecksum = null,
        pageCount = 2,
        created = now,
        modified = now,
        added = now,
        filename = "invoice.pdf",
        archiveFilename = null,
        originalFilename = "invoice.pdf",
        archiveSerialNumber = 1001L,
        rootDocumentId = 1L,
        versionIndex = 1,
        versionLabel = "Final Draft",
        documentTypeId = 1L,
        expiresAt = null,
        reminderDaysBeforeExpiry = 30,
        isDeleted = false,
        syncState = "SYNCED"
    )

    private val sampleDocV2 = Document(
        id = 2L,
        remoteId = "rem_2",
        ownerId = "owner_1",
        correspondentId = 1L,
        storagePathId = null,
        title = "Invoice March 2025 - Signed",
        content = "Total amount due is 1500 USD - Approved and Signed.",
        mimeType = "application/pdf",
        checksum = "chk_124",
        archiveChecksum = null,
        pageCount = 2,
        created = now,
        modified = now,
        added = now,
        filename = "invoice_signed.pdf",
        archiveFilename = null,
        originalFilename = "invoice_signed.pdf",
        archiveSerialNumber = 1002L,
        rootDocumentId = 1L,
        versionIndex = 2,
        versionLabel = "Signed Copy",
        documentTypeId = 1L,
        expiresAt = null,
        reminderDaysBeforeExpiry = 30,
        isDeleted = false,
        syncState = "SYNCED"
    )

    @BeforeTest
    fun setup() {
        repository = FakeDocumentRepository()
        catalog = FakeCatalogRepository()
        previewer = FakeDocumentPreviewer()
        reminders = FakeReminderScheduler()

        repository.docsState.value = listOf(sampleDoc, sampleDocV2)
        repository.docRelationsState.value = mapOf(
            1L to DocumentWithRelations(sampleDoc, Correspondent(1L, null, "Acme Corp"), null, null, listOf(Tag(1L, null, "Finance", parentId = null))),
            2L to DocumentWithRelations(sampleDocV2, Correspondent(1L, null, "Acme Corp"), null, null, listOf(Tag(1L, null, "Finance", parentId = null)))
        )
        catalog.correspondentsState.value = listOf(Correspondent(1L, null, "Acme Corp"))
        catalog.tagsState.value = listOf(Tag(1L, null, "Finance", parentId = null))
        catalog.customFieldsState.value = listOf(
            CustomField(1L, "Linked Documents", CustomFieldType.DOCUMENT_LINK, null)
        )

        val deleteUseCase = DeleteDocumentUseCase(repository)
        val updateUseCase = UpdateDocumentMetadataUseCase(repository)
        val fakeFileStore = FakeDocumentFileStore()
        val captureUseCase = ProcessDocumentCaptureUseCase(
            repository = repository,
            ocrEngine = FakeOcrEngine(),
            fileStore = fakeFileStore,
            clock = kotlin.time.Clock.System,
            checksumGenerator = FakeChecksumGenerator()
        )

        viewModelScope = CoroutineScope(testDispatcher + Job())

        viewModel = DocumentsViewModel(
            repository = repository,
            catalog = catalog,
            capture = captureUseCase,
            delete = deleteUseCase,
            updateMetadata = updateUseCase,
            previewer = previewer,
            reminders = reminders,
            scope = viewModelScope,
            fileStore = fakeFileStore
        )
    }

    @AfterTest
    fun tearDown() {
        viewModelScope.cancel()
    }

    @Test
    fun testInitialStateLoading() = testScope.runTest {
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(2, state.documents.size)
        assertEquals(1, state.correspondents.size)
        assertEquals("Acme Corp", state.correspondents.first().name)
        assertEquals(1, state.customFields.size)
        assertEquals("Linked Documents", state.customFields.first().name)
    }

    @Test
    fun testSelectDocumentLoadsDetailsAndVersions() = testScope.runTest {
        advanceUntilIdle()
        viewModel.onEvent(DocumentsUiEvent.Select(1L))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(1L, state.selectedId)
        assertNotNull(state.selected)
        assertEquals("Invoice March 2025", state.editTitle)
        assertEquals("Total amount due is 1500 USD for services rendered.", state.editContent)
        assertEquals("Final Draft", state.editVersionLabel)
        assertEquals(2, state.documentVersions.size)
    }

    @Test
    fun testEditContentAndVersionLabelThenSaveMetadata() = testScope.runTest {
        advanceUntilIdle()
        viewModel.onEvent(DocumentsUiEvent.Select(1L))
        advanceUntilIdle()

        viewModel.onEvent(DocumentsUiEvent.TitleChanged("Invoice March 2025 Updated"))
        viewModel.onEvent(DocumentsUiEvent.ContentChanged("Corrected OCR Text"))
        viewModel.onEvent(DocumentsUiEvent.VersionLabelChanged("v1 Final Corrected"))
        viewModel.onEvent(DocumentsUiEvent.CustomFieldChanged(1L, "[2]"))

        viewModel.onEvent(DocumentsUiEvent.SaveMetadata)
        advanceUntilIdle()

        val updatedDoc = repository.getById(1L)
        assertNotNull(updatedDoc)
        assertEquals("Invoice March 2025 Updated", updatedDoc.title)
        assertEquals("Corrected OCR Text", updatedDoc.content)
        assertEquals("v1 Final Corrected", updatedDoc.versionLabel)

        val savedCustomValues = catalog.customFieldValuesState.value[1L]
        assertNotNull(savedCustomValues)
        assertTrue(savedCustomValues.any { it.fieldId == 1L && it.documentIdsJson == "[2]" })
    }

    @Test
    fun testOpenFileTriggersPreviewer() = testScope.runTest {
        advanceUntilIdle()
        viewModel.onEvent(DocumentsUiEvent.Select(1L))
        advanceUntilIdle()

        viewModel.onEvent(DocumentsUiEvent.OpenFile)
        advanceUntilIdle()

        assertEquals(1L, previewer.lastOpenedDocumentId)
    }

    @Test
    fun testRetrySyncMarksDocumentPendingUpload() = testScope.runTest {
        advanceUntilIdle()
        viewModel.onEvent(DocumentsUiEvent.RetrySync(1L))
        advanceUntilIdle()

        val doc = repository.getById(1L)
        assertNotNull(doc)
        assertEquals("PENDING_UPLOAD", doc.syncState)
    }

    @Test
    fun testUploadNewVersionCreatesNextVersionIndexAndRootId() = testScope.runTest {
        advanceUntilIdle()
        viewModel.onEvent(DocumentsUiEvent.Select(1L))
        advanceUntilIdle()

        val newVersionInput = com.nextstepai.paperless.domain.capture.DocumentInput(
            bytes = "v3 content".encodeToByteArray(),
            filename = "invoice_v3.pdf",
            mimeType = "application/pdf"
        )

        viewModel.onEvent(DocumentsUiEvent.UploadNewVersion(1L, newVersionInput))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.selected)
        val newDocId = state.selectedId
        assertNotNull(newDocId)

        val newDoc = repository.getById(newDocId)
        assertNotNull(newDoc)
        assertEquals(1L, newDoc.rootDocumentId)
        assertEquals(3, newDoc.versionIndex)
        assertEquals("v3", newDoc.versionLabel)
        assertEquals(1L, newDoc.correspondentId)
    }

    @Test
    fun testSyncSettingsStateAndConnectionTest() = testScope.runTest {
        advanceUntilIdle()

        viewModel.onEvent(DocumentsUiEvent.ServerUrlChanged("https://example.invalid"))
        viewModel.onEvent(DocumentsUiEvent.AuthTokenChanged("secret_token_123"))
        viewModel.onEvent(DocumentsUiEvent.SyncIntervalChanged(30))
        viewModel.onEvent(DocumentsUiEvent.SyncOnWifiOnlyToggled(true))

        val state = viewModel.state.value
        assertEquals("https://example.invalid", state.serverUrl)
        assertEquals("secret_token_123", state.authToken)
        assertEquals(30, state.syncIntervalMinutes)
        assertTrue(state.syncOnWifiOnly)

        viewModel.onEvent(DocumentsUiEvent.TestConnection)
        advanceUntilIdle()

        val updatedState = viewModel.state.value
        assertEquals(ConnectionTestStatus.Error, updatedState.connectionTestStatus)
        assertEquals("Please configure a valid Cloudflare Worker URL.", updatedState.connectionTestMessage)
    }

    @Test
    fun testClearLocalCache() = testScope.runTest {
        advanceUntilIdle()

        viewModel.onEvent(DocumentsUiEvent.ToggleClearCacheDialog(true))
        assertTrue(viewModel.state.value.showClearCacheDialog)

        viewModel.onEvent(DocumentsUiEvent.ClearLocalCache)
        advanceUntilIdle()

        assertEquals(false, viewModel.state.value.showClearCacheDialog)
    }
}
