package com.nextstepai.paperless.ui.inbox

enum class DocumentStatus {
    NEEDS_REVIEW,
    OCR_IN_PROGRESS,
    UNASSIGNED
}

data class InboxDocumentItem(
    val id: String,
    val title: String,
    val extension: String,
    val pageCount: Int,
    val fileSizeFormatted: String,
    val uploadedAgo: String,
    val status: DocumentStatus,
    val ocrProgress: Float? = null, // e.g. 0.68f
    val r2ThumbnailUrl: String? = null,
    val tags: List<String> = emptyList()
)

data class InboxUiState(
    val documents: List<InboxDocumentItem> = emptyList(),
    val totalCount: Int = 0,
    val isAutoIngestionActive: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface InboxUiIntent {
    data object SelectAll : InboxUiIntent
    data object MarkReviewed : InboxUiIntent
    data object ProcessQueue : InboxUiIntent
    data class AssignMetadata(val docId: String) : InboxUiIntent
    data class DeleteDocument(val docId: String) : InboxUiIntent
    data class OpenDocumentDetail(val docId: String) : InboxUiIntent
}
