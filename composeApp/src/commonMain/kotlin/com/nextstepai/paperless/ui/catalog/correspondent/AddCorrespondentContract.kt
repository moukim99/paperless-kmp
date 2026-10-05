package com.nextstepai.paperless.ui.catalog.correspondent

import com.nextstepai.paperless.ui.catalog.documenttype.MatchingAlgorithm

data class AddCorrespondentUiState(
    val name: String = "",
    val isRulesExpanded: Boolean = true,
    val matchingPattern: String = "",
    val matchingAlgorithm: MatchingAlgorithm = MatchingAlgorithm.ANY_WORD,
    val ignoreCase: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddCorrespondentIntent {
    data class NameChanged(val name: String) : AddCorrespondentIntent
    data object ToggleRulesExpanded : AddCorrespondentIntent
    data class MatchingPatternChanged(val pattern: String) : AddCorrespondentIntent
    data class AlgorithmSelected(val algorithm: MatchingAlgorithm) : AddCorrespondentIntent
    data class ToggleIgnoreCase(val ignore: Boolean) : AddCorrespondentIntent
    data object Dismiss : AddCorrespondentIntent
    data object Save : AddCorrespondentIntent
}
