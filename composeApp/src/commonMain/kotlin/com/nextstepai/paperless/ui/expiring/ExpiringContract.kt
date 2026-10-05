package com.nextstepai.paperless.ui.expiring

enum class ExpiryFilter(val label: String) {
    ALL("All Expiring"),
    DUE_7_DAYS("Due in 7 Days"),
    DUE_30_DAYS("Due in 30 Days"),
    EXPIRED("Expired")
}

enum class DocumentCategory(val displayName: String) {
    CONTRACTS("Contracts"),
    SUBSCRIPTIONS("Subscriptions"),
    INSURANCE("Insurance"),
    DOMAINS("Domains")
}

data class ExpiringDocumentItem(
    val id: String,
    val title: String,
    val issuer: String,
    val category: DocumentCategory,
    val daysRemaining: Int,
    val docType: String = "PDF",
    val pageOrDetail: String,
    val annualCost: String? = null,
    val noticeWindowText: String? = null,
    val isAutoRenewScheduled: Boolean = false,
    val r2Key: String? = null
)

data class ExpiringUiState(
    val selectedFilter: ExpiryFilter = ExpiryFilter.ALL,
    val urgentCount: Int = 2,
    val documents: List<ExpiringDocumentItem> = emptyList(),
    val totalCount: Int = 0,
    val isLoading: Boolean = false
)

sealed interface ExpiringUiIntent {
    data class SelectFilter(val filter: ExpiryFilter) : ExpiringUiIntent
    data class ExtendDate(val docId: String) : ExpiringUiIntent
    data class MarkRenewed(val docId: String) : ExpiringUiIntent
    data class ViewDocument(val docId: String) : ExpiringUiIntent
}
