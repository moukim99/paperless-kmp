package com.nextstepai.paperless.documents.presentation

import com.nextstepai.paperless.domain.capture.DocumentInput
import com.nextstepai.paperless.domain.model.*
import com.nextstepai.paperless.domain.repository.CatalogRepository
import com.nextstepai.paperless.domain.repository.DocumentRepository
import com.nextstepai.paperless.domain.sync.ProcessDocumentCaptureUseCase
import com.nextstepai.paperless.domain.sync.SyncOperationEnqueuer
import com.nextstepai.paperless.domain.usecase.DeleteDocumentUseCase
import com.nextstepai.paperless.domain.usecase.UpdateDocumentMetadataUseCase
import com.nextstepai.paperless.domain.platform.DocumentPreviewer
import com.nextstepai.paperless.domain.platform.ReminderScheduler
import io.ktor.client.request.get
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*

enum class ExpiryState { None, Upcoming, Today, Expired }
enum class ConnectionTestStatus { Idle, Testing, Success, Error }

data class DocumentUiModel(
    val id: Long,
    val title: String,
    val createdEpochMillis: Long,
    val createdInput: String,
    val expiresAtEpochMillis: Long?,
    val pageCount: Int?,
    val mimeType: String,
    val syncState: String,
    val content: String,
    val filename: String?,
    val reminderDaysBeforeExpiry: Int?,
    val correspondentId: Long?,
    val documentTypeId: Long?,
    val storagePathId: Long?,
    val archiveSerialNumber: Long?,
    val tagIds: Set<Long>,
    val rootDocumentId: Long? = null,
    val versionIndex: Int? = 1,
    val versionLabel: String? = null,
    val expiryState: ExpiryState = ExpiryState.None,
    val expiryLabel: String? = null,
    val expiryInput: String = "",
    val lastSyncError: String? = null,
) {
    companion object {
        fun fromDomain(d: Document, tagIds: Set<Long> = emptySet()): DocumentUiModel {
            val expiry = d.expiresAt?.toEpochMilliseconds()
            val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
            val expiryInput = d.expiresAt?.toLocalDateTime(TimeZone.UTC)?.date?.toString().orEmpty()
            val createdInput = d.created.toLocalDateTime(TimeZone.UTC).date.toString()
            val state = expiry?.let {
                val days = ((it - now) / 86_400_000L).toInt()
                when {
                    days < 0 -> ExpiryState.Expired
                    days == 0 -> ExpiryState.Today
                    else -> ExpiryState.Upcoming
                }
            } ?: ExpiryState.None
            val label = expiry?.let {
                val days = ((it - now) / 86_400_000L).toInt()
                when {
                    days < 0 -> "Expired"
                    days == 0 -> "Expires today"
                    days == 1 -> "Expires tomorrow"
                    else -> "Expires in $days days"
                }
            }
            return DocumentUiModel(
                id = d.id,
                title = d.title,
                createdEpochMillis = d.created.toEpochMilliseconds(),
                createdInput = createdInput,
                expiresAtEpochMillis = expiry,
                pageCount = d.pageCount,
                mimeType = d.mimeType,
                syncState = d.syncState,
                content = d.content,
                filename = d.filename,
                reminderDaysBeforeExpiry = d.reminderDaysBeforeExpiry,
                correspondentId = d.correspondentId,
                documentTypeId = d.documentTypeId,
                storagePathId = d.storagePathId,
                archiveSerialNumber = d.archiveSerialNumber,
                tagIds = tagIds,
                rootDocumentId = d.rootDocumentId,
                versionIndex = d.versionIndex ?: 1,
                versionLabel = d.versionLabel,
                expiryState = state,
                expiryLabel = label,
                expiryInput = expiryInput
            )
        }
    }
}

data class DocumentsUiState(
    val documents: List<DocumentUiModel> = emptyList(),
    val query: String = "",
    val selectedId: Long? = null,
    val selected: DocumentUiModel? = null,
    val editTitle: String = "",
    val editContent: String = "",
    val editVersionLabel: String = "",
    val editCreatedDate: String = "",
    val editExpiry: String = "",
    val editArchiveSerialNumber: String = "",
    val editReminderDays: String = "30",
    val correspondents: List<Correspondent> = emptyList(),
    val documentTypes: List<DocumentType> = emptyList(),
    val storagePaths: List<StoragePath> = emptyList(),
    val tags: List<Tag> = emptyList(),
    val documentVersions: List<DocumentUiModel> = emptyList(),
    val selectedCorrespondentId: Long? = null,
    val selectedDocumentTypeId: Long? = null,
    val selectedStoragePathId: Long? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val customFields: List<CustomField> = emptyList(),
    val customFieldValues: Map<Long, String> = emptyMap(),
    val initialCustomFieldValues: Map<Long, String> = emptyMap(),
    val showCreatedDatePicker: Boolean = false,
    val showExpiryDatePicker: Boolean = false,
    val showAddCorrespondentDialog: Boolean = false,
    val showAddDocumentTypeDialog: Boolean = false,
    val showAddStoragePathDialog: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
    val importing: Boolean = false,
    val saving: Boolean = false,
    val serverUrl: String = "https://example.invalid",
    val authToken: String = "",
    val syncIntervalMinutes: Int = 15,
    val syncOnWifiOnly: Boolean = false,
    val connectionTestStatus: ConnectionTestStatus = ConnectionTestStatus.Idle,
    val connectionTestMessage: String? = null,
    val showClearCacheDialog: Boolean = false,
    val cacheSizeFormatted: String = "0 KB"
)

sealed interface DocumentsUiEvent {
    data class SearchChanged(val value: String): DocumentsUiEvent
    data object ClearSelection: DocumentsUiEvent
    data class Select(val id: Long): DocumentsUiEvent
    data class SelectVersion(val documentId: Long): DocumentsUiEvent
    data class UploadNewVersion(val rootDocumentId: Long, val input: DocumentInput): DocumentsUiEvent
    data class Delete(val id: Long): DocumentsUiEvent
    data class Import(val input: DocumentInput): DocumentsUiEvent
    data class TitleChanged(val value: String): DocumentsUiEvent
    data class ContentChanged(val value: String): DocumentsUiEvent
    data class VersionLabelChanged(val value: String): DocumentsUiEvent
    data class CreatedDateChanged(val value: String): DocumentsUiEvent
    data class ExpiryChanged(val value: String): DocumentsUiEvent
    data class ArchiveSerialNumberChanged(val value: String): DocumentsUiEvent
    data class ReminderDaysChanged(val value: String): DocumentsUiEvent
    data class CorrespondentChanged(val id: Long?): DocumentsUiEvent
    data class DocumentTypeChanged(val id: Long?): DocumentsUiEvent
    data class StoragePathChanged(val id: Long?): DocumentsUiEvent
    data class TagToggled(val id: Long): DocumentsUiEvent
    data class CustomFieldChanged(val fieldId: Long, val value: String): DocumentsUiEvent
    data class ToggleCreatedDatePicker(val show: Boolean): DocumentsUiEvent
    data class ToggleExpiryDatePicker(val show: Boolean): DocumentsUiEvent
    data class ToggleAddCorrespondentDialog(val show: Boolean): DocumentsUiEvent
    data class ToggleAddDocumentTypeDialog(val show: Boolean): DocumentsUiEvent
    data class ToggleAddStoragePathDialog(val show: Boolean): DocumentsUiEvent
    data class AddCorrespondent(val name: String): DocumentsUiEvent
    data class AddDocumentType(val name: String): DocumentsUiEvent
    data class AddStoragePath(val name: String, val path: String = ""): DocumentsUiEvent
    data class SaveTag(val tag: Tag): DocumentsUiEvent
    data class DeleteTag(val id: Long): DocumentsUiEvent
    data class SaveCorrespondent(val correspondent: Correspondent): DocumentsUiEvent
    data class DeleteCorrespondent(val id: Long): DocumentsUiEvent
    data class SaveDocumentType(val documentType: DocumentType): DocumentsUiEvent
    data class DeleteDocumentType(val id: Long): DocumentsUiEvent
    data class SaveStoragePath(val storagePath: StoragePath): DocumentsUiEvent
    data class DeleteStoragePath(val id: Long): DocumentsUiEvent
    data class RetrySync(val documentId: Long): DocumentsUiEvent
    data object SaveMetadata: DocumentsUiEvent
    data object OpenFile: DocumentsUiEvent
    data object Refresh: DocumentsUiEvent
    data class ServerUrlChanged(val value: String): DocumentsUiEvent
    data class AuthTokenChanged(val value: String): DocumentsUiEvent
    data class SyncIntervalChanged(val minutes: Int): DocumentsUiEvent
    data class SyncOnWifiOnlyToggled(val enabled: Boolean): DocumentsUiEvent
    data object TestConnection: DocumentsUiEvent
    data object SaveSyncSettings: DocumentsUiEvent
    data class ToggleClearCacheDialog(val show: Boolean): DocumentsUiEvent
    data object ClearLocalCache: DocumentsUiEvent
}

class DocumentsViewModel(
    private val repository: DocumentRepository,
    private val catalog: CatalogRepository,
    private val capture: ProcessDocumentCaptureUseCase,
    private val delete: DeleteDocumentUseCase,
    private val updateMetadata: UpdateDocumentMetadataUseCase,
    private val previewer: DocumentPreviewer,
    private val reminders: ReminderScheduler,
    private val scope: CoroutineScope,
    private val syncEnqueuer: SyncOperationEnqueuer? = null,
    private val onTriggerSync: (() -> Unit)? = null,
    private val fileStore: com.nextstepai.paperless.domain.sync.DocumentFileStore? = null
) {
    private val _state = MutableStateFlow(DocumentsUiState())
    private var searchJob: Job? = null
    val state: StateFlow<DocumentsUiState> = _state.asStateFlow()

    init {
        scope.launch { repository.observeDocuments().collectLatest { docs -> _state.update { state -> if (state.query.isBlank()) state.copy(documents = docs.map { d -> DocumentUiModel.fromDomain(d) }, loading = false) else state.copy(loading = false) } } }
        scope.launch { catalog.observeCorrespondents().collect { v -> _state.update { it.copy(correspondents = v) } } }
        scope.launch { catalog.observeDocumentTypes().collect { v -> _state.update { it.copy(documentTypes = v) } } }
        scope.launch { catalog.observeStoragePaths().collect { v -> _state.update { it.copy(storagePaths = v) } } }
        scope.launch { catalog.observeTags().collect { v -> _state.update { it.copy(tags = v) } } }
        scope.launch { catalog.observeCustomFields().collect { v -> _state.update { it.copy(customFields = v) } } }
        refreshCacheSize()
    }

    fun onEvent(event: DocumentsUiEvent) {
        when (event) {
            is DocumentsUiEvent.SearchChanged -> {
                _state.update { it.copy(query = event.value, error = null) }
                searchJob?.cancel()
                searchJob = scope.launch {
                    delay(250)
                    if (event.value.isBlank()) return@launch
                    runCatching { repository.search(event.value) }
                        .onSuccess { docs ->
                            if (_state.value.query == event.value) {
                                _state.update { it.copy(documents = docs.map { d -> DocumentUiModel.fromDomain(d) }) }
                            }
                        }
                        .onFailure { e ->
                            if (_state.value.query == event.value) {
                                _state.update { it.copy(error = e.message) }
                            }
                        }
                }
            }
            is DocumentsUiEvent.Select -> select(event.id)
            is DocumentsUiEvent.SelectVersion -> select(event.documentId)
            is DocumentsUiEvent.UploadNewVersion -> uploadNewVersion(event.rootDocumentId, event.input)
            DocumentsUiEvent.ClearSelection -> _state.update { it.copy(selectedId = null, selected = null) }
            is DocumentsUiEvent.Delete -> scope.launch { runCatching { delete(event.id); if (_state.value.selectedId == event.id) _state.update { it.copy(selectedId = null, selected = null) } }.onFailure { e -> _state.update { it.copy(error = e.message) } } }
            is DocumentsUiEvent.Import -> scope.launch { _state.update { it.copy(importing = true, error = null) }; runCatching { capture(event.input) }.onFailure { e -> _state.update { it.copy(error = e.message) } }; _state.update { it.copy(importing = false) } }
            is DocumentsUiEvent.TitleChanged -> _state.update { it.copy(editTitle = event.value) }
            is DocumentsUiEvent.ContentChanged -> _state.update { it.copy(editContent = event.value) }
            is DocumentsUiEvent.VersionLabelChanged -> _state.update { it.copy(editVersionLabel = event.value) }
            is DocumentsUiEvent.CreatedDateChanged -> _state.update { it.copy(editCreatedDate = event.value) }
            is DocumentsUiEvent.ExpiryChanged -> _state.update { it.copy(editExpiry = event.value) }
            is DocumentsUiEvent.ArchiveSerialNumberChanged -> _state.update { it.copy(editArchiveSerialNumber = event.value.filter(Char::isDigit)) }
            is DocumentsUiEvent.ReminderDaysChanged -> _state.update { it.copy(editReminderDays = event.value.filter(Char::isDigit)) }
            is DocumentsUiEvent.CorrespondentChanged -> _state.update { it.copy(selectedCorrespondentId = event.id) }
            is DocumentsUiEvent.DocumentTypeChanged -> _state.update { it.copy(selectedDocumentTypeId = event.id) }
            is DocumentsUiEvent.StoragePathChanged -> _state.update { it.copy(selectedStoragePathId = event.id) }
            is DocumentsUiEvent.TagToggled -> _state.update { s -> s.copy(selectedTagIds = s.selectedTagIds.toMutableSet().also { if (!it.add(event.id)) it.remove(event.id) }) }
            is DocumentsUiEvent.CustomFieldChanged -> _state.update { it.copy(customFieldValues = it.customFieldValues + (event.fieldId to event.value)) }
            is DocumentsUiEvent.ToggleCreatedDatePicker -> _state.update { it.copy(showCreatedDatePicker = event.show) }
            is DocumentsUiEvent.ToggleExpiryDatePicker -> _state.update { it.copy(showExpiryDatePicker = event.show) }
            is DocumentsUiEvent.ToggleAddCorrespondentDialog -> _state.update { it.copy(showAddCorrespondentDialog = event.show) }
            is DocumentsUiEvent.ToggleAddDocumentTypeDialog -> _state.update { it.copy(showAddDocumentTypeDialog = event.show) }
            is DocumentsUiEvent.ToggleAddStoragePathDialog -> _state.update { it.copy(showAddStoragePathDialog = event.show) }
            is DocumentsUiEvent.AddCorrespondent -> scope.launch {
                runCatching {
                    val id = catalog.addCorrespondent(event.name)
                    _state.update { it.copy(selectedCorrespondentId = id, showAddCorrespondentDialog = false) }
                }.onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.AddDocumentType -> scope.launch {
                runCatching {
                    val id = catalog.addDocumentType(event.name)
                    _state.update { it.copy(selectedDocumentTypeId = id, showAddDocumentTypeDialog = false) }
                }.onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.AddStoragePath -> scope.launch {
                runCatching {
                    val id = catalog.addStoragePath(event.name, event.path)
                    _state.update { it.copy(selectedStoragePathId = id, showAddStoragePathDialog = false) }
                }.onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.SaveTag -> scope.launch {
                runCatching { catalog.saveTag(event.tag) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.DeleteTag -> scope.launch {
                runCatching { catalog.deleteTag(event.id) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.SaveCorrespondent -> scope.launch {
                runCatching { catalog.saveCorrespondent(event.correspondent) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.DeleteCorrespondent -> scope.launch {
                runCatching { catalog.deleteCorrespondent(event.id) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.SaveDocumentType -> scope.launch {
                runCatching { catalog.saveDocumentType(event.documentType) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.DeleteDocumentType -> scope.launch {
                runCatching { catalog.deleteDocumentType(event.id) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.SaveStoragePath -> scope.launch {
                runCatching { catalog.saveStoragePath(event.storagePath) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.DeleteStoragePath -> scope.launch {
                runCatching { catalog.deleteStoragePath(event.id) }
                    .onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            is DocumentsUiEvent.RetrySync -> scope.launch {
                runCatching {
                    repository.markSyncState(event.documentId, "PENDING_UPLOAD")
                    syncEnqueuer?.enqueue(event.documentId, "UPLOAD")
                    onTriggerSync?.invoke()
                    select(event.documentId)
                }.onFailure { e -> _state.update { it.copy(error = e.message) } }
            }
            DocumentsUiEvent.SaveMetadata -> saveMetadata()
            DocumentsUiEvent.OpenFile -> scope.launch { _state.value.selectedId?.let { id -> previewer.open(id).onFailure { e -> _state.update { it.copy(error = e.message) } } } }
            DocumentsUiEvent.Refresh -> Unit
            is DocumentsUiEvent.ServerUrlChanged -> _state.update { it.copy(serverUrl = event.value) }
            is DocumentsUiEvent.AuthTokenChanged -> _state.update { it.copy(authToken = event.value) }
            is DocumentsUiEvent.SyncIntervalChanged -> _state.update { it.copy(syncIntervalMinutes = event.minutes) }
            is DocumentsUiEvent.SyncOnWifiOnlyToggled -> _state.update { it.copy(syncOnWifiOnly = event.enabled) }
            DocumentsUiEvent.TestConnection -> testConnection()
            DocumentsUiEvent.SaveSyncSettings -> saveSyncSettings()
            is DocumentsUiEvent.ToggleClearCacheDialog -> _state.update { it.copy(showClearCacheDialog = event.show) }
            DocumentsUiEvent.ClearLocalCache -> clearCache()
        }
    }

    private fun select(id: Long) = scope.launch {
        repository.observeDocument(id).firstOrNull()?.let { rel ->
            val d = rel.document
            val lastSyncError = repository.observeLatestSyncError(id).firstOrNull()
            val ui = DocumentUiModel.fromDomain(d, rel.tags.map(Tag::id).toSet()).copy(lastSyncError = lastSyncError)
            val values = catalog.observeCustomFieldValues(id).firstOrNull()?.associate {
                it.fieldId to (it.text ?: it.longText ?: it.select ?: it.documentIdsJson ?: it.boolean?.toString() ?: it.dateEpochDay?.let { ep -> LocalDate.fromEpochDays(ep.toInt()).toString() } ?: it.int?.toString() ?: it.float?.toString() ?: it.monetary ?: "")
            }.orEmpty()
            _state.update {
                it.copy(
                    selectedId = id,
                    selected = ui,
                    editTitle = ui.title,
                    editContent = ui.content,
                    editVersionLabel = ui.versionLabel.orEmpty(),
                    editCreatedDate = ui.createdInput,
                    editExpiry = ui.expiryInput,
                    editArchiveSerialNumber = ui.archiveSerialNumber?.toString().orEmpty(),
                    editReminderDays = (ui.reminderDaysBeforeExpiry ?: 30).toString(),
                    selectedCorrespondentId = rel.correspondent?.id,
                    selectedDocumentTypeId = rel.documentType?.id,
                    selectedStoragePathId = rel.storagePath?.id,
                    selectedTagIds = ui.tagIds,
                    customFieldValues = values,
                    initialCustomFieldValues = values
                )
            }
            val rootId = ui.rootDocumentId ?: ui.id
            repository.observeVersions(rootId).firstOrNull()?.let { versions ->
                _state.update { s -> s.copy(documentVersions = versions.map { DocumentUiModel.fromDomain(it) }) }
            }
        }
    }

    private fun saveMetadata() = scope.launch {
        val s = _state.value; val id = s.selectedId ?: return@launch
        _state.update { it.copy(saving = true, error = null) }
        runCatching {
            val createdInstant: kotlin.time.Instant = s.editCreatedDate.trim().takeIf { it.isNotBlank() }
                ?.let { runCatching { LocalDate.parse(it).atStartOfDayIn(TimeZone.UTC) }.getOrNull() }
                ?: kotlin.time.Instant.fromEpochMilliseconds(s.selected?.createdEpochMillis ?: kotlin.time.Clock.System.now().toEpochMilliseconds())
            val archiveSn = s.editArchiveSerialNumber.toLongOrNull()
            val expiry: kotlin.time.Instant? = s.editExpiry.trim().takeIf { it.isNotBlank() }
                ?.let { runCatching { LocalDate.parse(it).atStartOfDayIn(TimeZone.UTC) }.getOrNull() }
            val days = s.editReminderDays.toIntOrNull()?.coerceIn(0, 3650)

            updateMetadata(id, s.editTitle, s.editContent, s.editVersionLabel, createdInstant, archiveSn, expiry, days)
            catalog.updateDocumentClassification(id, s.selectedCorrespondentId, s.selectedDocumentTypeId, s.selectedStoragePathId)
            catalog.setDocumentTags(id, s.selectedTagIds.toList())

            s.customFields.forEach { field ->
                val value = s.customFieldValues[field.id].orEmpty().trim().takeIf { it.isNotEmpty() }
                val customValue = when (field.type) {
                    CustomFieldType.BOOLEAN -> CustomFieldValue(0, id, field.id, null, value?.toBooleanStrictOrNull(), null, null, null, null, null, null, null)
                    CustomFieldType.DATE -> CustomFieldValue(0, id, field.id, null, null, value?.let { runCatching { LocalDate.parse(it) }.getOrNull()?.toEpochDays()?.toLong() }, null, null, null, null, null, null)
                    CustomFieldType.INTEGER -> CustomFieldValue(0, id, field.id, null, null, null, value?.toLongOrNull(), null, null, null, null, null)
                    CustomFieldType.FLOAT -> CustomFieldValue(0, id, field.id, null, null, null, null, value?.toDoubleOrNull(), null, null, null, null)
                    CustomFieldType.MONETARY -> CustomFieldValue(0, id, field.id, null, null, null, null, null, value, null, null, null)
                    CustomFieldType.SELECT -> CustomFieldValue(0, id, field.id, null, null, null, null, null, null, null, value, null)
                    CustomFieldType.LONG_TEXT -> CustomFieldValue(0, id, field.id, null, null, null, null, null, null, null, null, value)
                    CustomFieldType.DOCUMENT_LINK -> CustomFieldValue(0, id, field.id, null, null, null, null, null, null, value, null, null)
                    else -> CustomFieldValue(0, id, field.id, value, null, null, null, null, null, null, null, null)
                }
                catalog.saveCustomFieldValue(customValue)
            }
            if (expiry != null && days != null) reminders.schedule(id, s.editTitle, expiry.toEpochMilliseconds(), days) else reminders.cancel(id)
            val updated = repository.observeDocument(id).firstOrNull()
            if (updated != null) {
                val updatedUi = DocumentUiModel.fromDomain(updated.document, updated.tags.map(Tag::id).toSet())
                _state.update { it.copy(selected = updatedUi, selectedTagIds = updatedUi.tagIds, initialCustomFieldValues = s.customFieldValues) }
            }
        }.onFailure { e -> _state.update { it.copy(error = e.message) } }
        _state.update { it.copy(saving = false) }
    }

    private fun uploadNewVersion(rootDocumentId: Long, input: DocumentInput) = scope.launch {
        _state.update { it.copy(importing = true, error = null) }
        runCatching {
            val selectedDoc = _state.value.selected
            val targetRootId = if (selectedDoc?.rootDocumentId != null && selectedDoc.rootDocumentId != 0L) {
                selectedDoc.rootDocumentId
            } else {
                rootDocumentId
            }

            val currentVersions = repository.observeVersions(targetRootId).firstOrNull().orEmpty()
            val maxVersion = currentVersions.maxOfOrNull { it.versionIndex ?: 1 } ?: selectedDoc?.versionIndex ?: 1
            val nextVersionIndex = maxVersion + 1
            val versionLabel = "v$nextVersionIndex"

            val resultId = capture(
                input = input,
                rootDocumentId = targetRootId,
                versionIndex = nextVersionIndex,
                versionLabel = versionLabel,
                correspondentId = selectedDoc?.correspondentId,
                documentTypeId = selectedDoc?.documentTypeId,
                storagePathId = selectedDoc?.storagePathId
            ).getOrThrow()

            if (selectedDoc != null && selectedDoc.tagIds.isNotEmpty()) {
                catalog.setDocumentTags(resultId, selectedDoc.tagIds.toList())
            }

            select(resultId)
        }.onFailure { e ->
            _state.update { it.copy(error = e.message) }
        }
        _state.update { it.copy(importing = false) }
    }

    fun refreshCacheSize() = scope.launch {
        fileStore?.cacheSize()?.let { bytes ->
            val formatted = when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
            _state.update { it.copy(cacheSizeFormatted = formatted) }
        }
    }

    private fun testConnection() = scope.launch {
        _state.update { it.copy(connectionTestStatus = ConnectionTestStatus.Testing, connectionTestMessage = null) }
        val url = _state.value.serverUrl.trim()
        if (url.isBlank() || url.contains("example.invalid")) {
            _state.update {
                it.copy(
                    connectionTestStatus = ConnectionTestStatus.Error,
                    connectionTestMessage = "Please configure a valid Cloudflare Worker URL."
                )
            }
            return@launch
        }
        runCatching {
            val client = io.ktor.client.HttpClient()
            val response = client.get(if (url.endsWith("/")) "${url}api/documents" else "$url/api/documents")
            client.close()
            if (response.status.value in 200..299) {
                _state.update {
                    it.copy(
                        connectionTestStatus = ConnectionTestStatus.Success,
                        connectionTestMessage = "Connection successful! Server responded HTTP ${response.status.value}"
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        connectionTestStatus = ConnectionTestStatus.Error,
                        connectionTestMessage = "Server returned status HTTP ${response.status.value}"
                    )
                }
            }
        }.onFailure { e ->
            _state.update {
                it.copy(
                    connectionTestStatus = ConnectionTestStatus.Error,
                    connectionTestMessage = e.message ?: "Connection failed."
                )
            }
        }
    }

    private fun saveSyncSettings() = scope.launch {
        _state.update { it.copy(connectionTestMessage = "Sync settings saved.") }
        onTriggerSync?.invoke()
    }

    private fun clearCache() = scope.launch {
        fileStore?.clearCache()
        refreshCacheSize()
        _state.update { it.copy(showClearCacheDialog = false) }
    }
}
