package com.nextstepai.paperless.documents.presentation

import com.nextstepai.paperless.domain.capture.DocumentInput
import com.nextstepai.paperless.domain.model.*
import com.nextstepai.paperless.domain.repository.CatalogRepository
import com.nextstepai.paperless.domain.repository.DocumentRepository
import com.nextstepai.paperless.domain.sync.ProcessDocumentCaptureUseCase
import com.nextstepai.paperless.domain.usecase.DeleteDocumentUseCase
import com.nextstepai.paperless.domain.usecase.UpdateDocumentMetadataUseCase
import com.nextstepai.paperless.domain.platform.DocumentPreviewer
import com.nextstepai.paperless.domain.platform.ReminderScheduler
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*

enum class ExpiryState { None, Upcoming, Today, Expired }

data class DocumentUiModel(
    val id: Long,
    val title: String,
    val createdEpochMillis: Long,
    val expiresAtEpochMillis: Long?,
    val pageCount: Int?,
    val mimeType: String,
    val syncState: String,
    val content: String,
    val filename: String?,
    val reminderDaysBeforeExpiry: Int?,
    val correspondentId: Long?,
    val documentTypeId: Long?,
    val tagIds: Set<Long>,
    val expiryState: ExpiryState = ExpiryState.None,
    val expiryLabel: String? = null,
) {
    companion object {
        fun fromDomain(d: Document, tagIds: Set<Long> = emptySet()): DocumentUiModel {
            val expiry = d.expiresAt?.toEpochMilliseconds()
            val state = expiry?.let {
                val days = ((it - Clock.System.now().toEpochMilliseconds()) / 86_400_000L).toInt()
                when {
                    days < 0 -> ExpiryState.Expired
                    days == 0 -> ExpiryState.Today
                    else -> ExpiryState.Upcoming
                }
            } ?: ExpiryState.None
            val label = expiry?.let {
                val days = ((it - Clock.System.now().toEpochMilliseconds()) / 86_400_000L).toInt()
                when {
                    days < 0 -> "Expired"
                    days == 0 -> "Expires today"
                    days == 1 -> "Expires tomorrow"
                    else -> "Expires in $days days"
                }
            }
            return DocumentUiModel(
                d.id, d.title, d.created.toEpochMilliseconds(), expiry, d.pageCount, d.mimeType,
                d.syncState, d.content, d.filename, d.reminderDaysBeforeExpiry, d.correspondentId,
                d.documentTypeId, tagIds, state, label
            )
        }
    }
}

data class DocumentsUiState(
    val documents: List<DocumentUiModel> = emptyList(), val query: String = "", val selectedId: Long? = null,
    val selected: DocumentUiModel? = null, val editTitle: String = "", val editExpiry: String = "", val editReminderDays: String = "30",
    val correspondents: List<Correspondent> = emptyList(), val documentTypes: List<DocumentType> = emptyList(), val tags: List<Tag> = emptyList(),
    val selectedCorrespondentId: Long? = null, val selectedDocumentTypeId: Long? = null, val selectedTagIds: Set<Long> = emptySet(),
    val customFields: List<CustomField> = emptyList(), val customFieldValues: Map<Long, String> = emptyMap(),
    val loading: Boolean = true, val error: String? = null, val importing: Boolean = false, val saving: Boolean = false
)
sealed interface DocumentsUiEvent {
    data class SearchChanged(val value: String): DocumentsUiEvent
    data class Select(val id: Long): DocumentsUiEvent
    data class Delete(val id: Long): DocumentsUiEvent
    data class Import(val input: DocumentInput): DocumentsUiEvent
    data class TitleChanged(val value: String): DocumentsUiEvent
    data class ExpiryChanged(val value: String): DocumentsUiEvent
    data class ReminderDaysChanged(val value: String): DocumentsUiEvent
    data class CorrespondentChanged(val id: Long?): DocumentsUiEvent
    data class DocumentTypeChanged(val id: Long?): DocumentsUiEvent
    data class TagToggled(val id: Long): DocumentsUiEvent
    data class CustomFieldChanged(val fieldId: Long, val value: String): DocumentsUiEvent
    data object SaveMetadata: DocumentsUiEvent
    data object OpenFile: DocumentsUiEvent
    data object Refresh: DocumentsUiEvent
}

class DocumentsViewModel(
    private val repository: DocumentRepository,
    private val catalog: CatalogRepository,
    private val capture: ProcessDocumentCaptureUseCase,
    private val delete: DeleteDocumentUseCase,
    private val updateMetadata: UpdateDocumentMetadataUseCase,
    private val previewer: DocumentPreviewer,
    private val reminders: ReminderScheduler,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(DocumentsUiState())
    val state: StateFlow<DocumentsUiState> = _state.asStateFlow()
    init {
        scope.launch { repository.observeDocuments().collectLatest { docs -> _state.update { state -> state.copy(documents = docs.map { d -> DocumentUiModel.fromDomain(d) }, loading = false) } } }
        scope.launch { catalog.observeCorrespondents().collect { v -> _state.update { it.copy(correspondents = v) } } }
        scope.launch { catalog.observeDocumentTypes().collect { v -> _state.update { it.copy(documentTypes = v) } } }
        scope.launch { catalog.observeTags().collect { v -> _state.update { it.copy(tags = v) } } }
        scope.launch { catalog.observeCustomFields().collect { v -> _state.update { it.copy(customFields = v) } } }
    }
    fun onEvent(event: DocumentsUiEvent) {
        when (event) {
            is DocumentsUiEvent.SearchChanged -> { _state.update { it.copy(query = event.value, error = null) }; scope.launch { runCatching { repository.search(event.value) }.onSuccess { docs -> _state.update { it.copy(documents = docs.map { d -> DocumentUiModel.fromDomain(d) }) } }.onFailure { e -> _state.update { it.copy(error = e.message) } } } }
            is DocumentsUiEvent.Select -> select(event.id)
            is DocumentsUiEvent.Delete -> scope.launch { runCatching { delete(event.id); if (_state.value.selectedId == event.id) _state.update { it.copy(selectedId = null, selected = null) } }.onFailure { e -> _state.update { it.copy(error = e.message) } } }
            is DocumentsUiEvent.Import -> scope.launch { _state.update { it.copy(importing = true, error = null) }; runCatching { capture(event.input) }.onFailure { e -> _state.update { it.copy(error = e.message) } }; _state.update { it.copy(importing = false) } }
            is DocumentsUiEvent.TitleChanged -> _state.update { it.copy(editTitle = event.value) }
            is DocumentsUiEvent.ExpiryChanged -> _state.update { it.copy(editExpiry = event.value) }
            is DocumentsUiEvent.ReminderDaysChanged -> _state.update { it.copy(editReminderDays = event.value.filter(Char::isDigit)) }
            is DocumentsUiEvent.CorrespondentChanged -> _state.update { it.copy(selectedCorrespondentId = event.id) }
            is DocumentsUiEvent.DocumentTypeChanged -> _state.update { it.copy(selectedDocumentTypeId = event.id) }
            is DocumentsUiEvent.TagToggled -> _state.update { s -> s.copy(selectedTagIds = s.selectedTagIds.toMutableSet().also { if (!it.add(event.id)) it.remove(event.id) }) }
            is DocumentsUiEvent.CustomFieldChanged -> _state.update { it.copy(customFieldValues = it.customFieldValues + (event.fieldId to event.value)) }
            DocumentsUiEvent.SaveMetadata -> saveMetadata()
            DocumentsUiEvent.OpenFile -> scope.launch { _state.value.selectedId?.let { id -> previewer.open(id).onFailure { e -> _state.update { it.copy(error = e.message) } } } }
            DocumentsUiEvent.Refresh -> Unit
        }
    }
    private fun select(id: Long) = scope.launch {
        repository.observeDocument(id).first()?.let { rel ->
            val d = rel.document
            val ui = DocumentUiModel.fromDomain(d, rel.tags.map(Tag::id).toSet())
            val values = catalog.observeCustomFieldValues(id).first().associate { it.fieldId to (it.text ?: it.longText ?: it.select ?: it.boolean?.toString() ?: it.int?.toString() ?: it.float?.toString() ?: it.monetary ?: "") }
            _state.update { it.copy(selectedId=id, selected=ui, editTitle=ui.title, editExpiry=ui.expiresAtEpochMillis?.let { m -> Instant.fromEpochMilliseconds(m).toString().take(10) } ?: "", editReminderDays=(ui.reminderDaysBeforeExpiry ?: 30).toString(), selectedCorrespondentId=rel.correspondent?.id, selectedDocumentTypeId=rel.documentType?.id, selectedTagIds=ui.tagIds, customFieldValues=values) }
        }
    }
    private fun saveMetadata() = scope.launch {
        val s = _state.value; val id = s.selectedId ?: return@launch
        _state.update { it.copy(saving=true, error=null) }
        runCatching {
            val expiry = s.editExpiry.trim().takeIf { it.isNotBlank() }?.let { LocalDate.parse(it).atStartOfDayIn(TimeZone.UTC) }
            val days = s.editReminderDays.toIntOrNull()?.coerceIn(0, 3650)
            updateMetadata(id, s.editTitle, expiry, days)
            catalog.updateDocumentClassification(id, s.selectedCorrespondentId, s.selectedDocumentTypeId)
            catalog.setDocumentTags(id, s.selectedTagIds.toList())
            s.customFields.forEach { field -> catalog.saveCustomFieldValue(CustomFieldValue(0, id, field.id, s.customFieldValues[field.id], null, null, null, null, null, null, null, null)) }
            if (expiry != null && days != null) reminders.schedule(id, s.editTitle, expiry.toEpochMilliseconds(), days) else reminders.cancel(id)
        }.onFailure { e -> _state.update { it.copy(error=e.message) } }
        _state.update { it.copy(saving=false) }
    }
}
