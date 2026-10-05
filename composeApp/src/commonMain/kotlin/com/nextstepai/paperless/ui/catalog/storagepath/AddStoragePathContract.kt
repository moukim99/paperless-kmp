package com.nextstepai.paperless.ui.catalog.storagepath

import com.nextstepai.paperless.ui.catalog.documenttype.MatchingAlgorithm

data class AddStoragePathUiState(
    val name: String = "Standard Filing Archive",
    val pathTemplate: String = "{created_year}/{correspondent}/{title}",
    val isRulesExpanded: Boolean = true,
    val matchingPattern: String = "invoice, receipt",
    val matchingAlgorithm: MatchingAlgorithm = MatchingAlgorithm.ANY_WORD,
    val ignoreCase: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddStoragePathIntent {
    data class NameChanged(val name: String) : AddStoragePathIntent
    data class PathTemplateChanged(val template: String) : AddStoragePathIntent
    data class InsertFormatTag(val tag: String) : AddStoragePathIntent
    data object ToggleRulesExpanded : AddStoragePathIntent
    data class MatchingPatternChanged(val pattern: String) : AddStoragePathIntent
    data class AlgorithmSelected(val algorithm: MatchingAlgorithm) : AddStoragePathIntent
    data class ToggleIgnoreCase(val ignore: Boolean) : AddStoragePathIntent
    data object Dismiss : AddStoragePathIntent
    data object SavePath : AddStoragePathIntent
}
