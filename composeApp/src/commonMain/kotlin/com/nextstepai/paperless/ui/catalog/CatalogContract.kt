package com.nextstepai.paperless.ui.catalog

import androidx.compose.ui.graphics.Color

enum class CatalogTab(val title: String, val count: Int? = null) {
    TAGS("Tags", 12),
    CORRESPONDENTS("Correspondents"),
    DOCUMENT_TYPES("Document Types"),
    STORAGE_PATHS("Storage Paths")
}

enum class TagSortFilter(val label: String) {
    ALPHABETICAL("Alphabetical"),
    MOST_USED("Most Used"),
    COLOR_CODED("Color Coded")
}

data class TagItem(
    val id: String,
    val name: String,
    val documentCount: Int,
    val color: Color,
    val categoryLabel: String? = null,
    val iconName: String? = null,
    val subtitle: String? = null,
    val isSpanFull: Boolean = false // لتحديد ما إذا كانت البطاقة تمتد بعرض كامل (ColSpan 2) أو نصف العرض
)

data class CatalogUiState(
    val selectedTab: CatalogTab = CatalogTab.TAGS,
    val selectedFilter: TagSortFilter = TagSortFilter.ALPHABETICAL,
    val searchQuery: String = "",
    val tags: List<TagItem> = emptyList(),
    val categorizedCount: Int = 6,
    val isLoading: Boolean = false
)

sealed interface CatalogUiIntent {
    data class SelectTab(val tab: CatalogTab) : CatalogUiIntent
    data class SelectFilter(val filter: TagSortFilter) : CatalogUiIntent
    data class SearchQueryChanged(val query: String) : CatalogUiIntent
    data class TagClicked(val tagId: String) : CatalogUiIntent
    data class TagOptionsClicked(val tagId: String) : CatalogUiIntent
    data object CreateNewTag : CatalogUiIntent
}
