package com.nextstepai.paperless.ui.documentdetail

import androidx.compose.ui.graphics.Color

data class TagBadge(
    val id: String,
    val name: String,
    val color: Color
)

data class DocumentDetailUiState(
    val id: String = "doc-1",
    val title: String = "2026-08-29-14-37-55-565",
    val extension: String = "JPEG",
    val pageCount: Int = 1,
    val version: String = "v1.0",
    val isIndexed: Boolean = true,
    // Form Inputs
    val correspondents: List<String> = listOf("None (Unassigned)", "Federal Tax Authority", "Ministry of Justice", "Health Insurance Provider"),
    val selectedCorrespondent: String = "None (Unassigned)",
    val documentTypes: List<String> = listOf("None", "Invoice / Receipt", "Contract & Agreement", "Legal Notice", "Bank Statement"),
    val selectedDocumentType: String = "None",
    val storagePaths: List<String> = listOf("None (Root / Inbox)", "/Archives/2026/Personal", "/Tax_Filings/Q3"),
    val selectedStoragePath: String = "None (Root / Inbox)",
    val asn: String = "",
    val versionLabel: String = "",
    // Versions
    val currentVersionDate: String = "2026-10-02",
    val currentVersionSize: String = "1.4 MB",
    // Dates & Reminders
    val documentDate: String = "2026-10-02",
    val expiryDate: String = "",
    val reminderDays: Int = 30,
    // Tags
    val tags: List<TagBadge> = listOf(
        TagBadge("1", "Tax-2026", Color(0xFFD97706)),
        TagBadge("2", "Receipt", Color(0xFFC85A32))
    ),
    // OCR Text
    val ocrText: String = "RECEIPT #2026-98124\nDATE: 2026-10-02 14:37:55\nTRANSACTION TOTAL: USD 142.50\nSTATUS: PAID / VALIDATED",
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false
)

sealed interface DocumentDetailIntent {
    data object NavigateBack : DocumentDetailIntent
    data object ShareClicked : DocumentDetailIntent
    data class TitleChanged(val title: String) : DocumentDetailIntent
    data class CorrespondentSelected(val correspondent: String) : DocumentDetailIntent
    data class DocumentTypeSelected(val type: String) : DocumentDetailIntent
    data class StoragePathSelected(val path: String) : DocumentDetailIntent
    data class AsnChanged(val asn: String) : DocumentDetailIntent
    data class VersionLabelChanged(val label: String) : DocumentDetailIntent
    data class DocumentDateChanged(val date: String) : DocumentDetailIntent
    data class ExpiryDateChanged(val date: String) : DocumentDetailIntent
    data class ReminderDaysChanged(val days: Int) : DocumentDetailIntent
    data object UploadNewVersion : DocumentDetailIntent
    data class DownloadVersion(val versionId: String) : DocumentDetailIntent
    data object AddTagClicked : DocumentDetailIntent
    data class RemoveTag(val tagId: String) : DocumentDetailIntent
    data object EditOcrText : DocumentDetailIntent
    data object CopyOcrText : DocumentDetailIntent
    data object InAppPreviewClicked : DocumentDetailIntent
    data object OpenExternalClicked : DocumentDetailIntent
    data object DeleteDocumentClicked : DocumentDetailIntent
    data object DiscardChanges : DocumentDetailIntent
    data object SaveChanges : DocumentDetailIntent
}
