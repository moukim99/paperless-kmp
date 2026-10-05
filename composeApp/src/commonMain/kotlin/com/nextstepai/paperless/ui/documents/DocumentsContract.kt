package com.nextstepai.paperless.ui.documents

enum class DocumentFilter(val label: String, val count: Int? = null) {
    ALL("All", 1),
    EXPIRING("Expiring"),
    RECENT("Recent Scans"),
    INVOICES("Invoices")
}

data class DocumentListItem(
    val id: String,
    val title: String,
    val extension: String = "JPEG",
    val pageCount: Int = 1,
    val isPendingUpload: Boolean = true,
    val ocrStatusMessage: String? = "Queued for cloud OCR indexing",
    val r2Key: String? = null
)

data class DocumentsUiState(
    val selectedFilter: DocumentFilter = DocumentFilter.ALL,
    val searchQuery: String = "",
    val totalCount: Int = 1,
    val pendingSyncCount: Int = 1,
    val documents: List<DocumentListItem> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface DocumentsUiIntent {
    data class SearchQueryChanged(val query: String) : DocumentsUiIntent
    data class FilterSelected(val filter: DocumentFilter) : DocumentsUiIntent
    data object ScanBarcodeClicked : DocumentsUiIntent
    data object AddDocumentClicked : DocumentsUiIntent
    data class DocumentClicked(val docId: String) : DocumentsUiIntent
    data class DocumentOptionsClicked(val docId: String) : DocumentsUiIntent
}
