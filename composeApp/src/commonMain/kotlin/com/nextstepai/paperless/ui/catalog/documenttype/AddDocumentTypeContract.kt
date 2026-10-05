package com.nextstepai.paperless.ui.catalog.documenttype

enum class MatchingAlgorithm(val displayName: String, val apiValue: Int) {
    ANY_WORD("Any word", 1),
    ALL_WORDS("All words", 2),
    EXACT_MATCH("Exact match", 3),
    REGULAR_EXPRESSION("Regular expression", 4),
    FUZZY("Fuzzy match", 5),
    AUTO("Automatic", 6)
}

data class AddDocumentTypeUiState(
    val name: String = "",
    val isRulesExpanded: Boolean = true,
    val matchingPattern: String = "",
    val matchingAlgorithm: MatchingAlgorithm = MatchingAlgorithm.ANY_WORD,
    val ignoreCase: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface AddDocumentTypeIntent {
    data class NameChanged(val name: String) : AddDocumentTypeIntent
    data object ToggleRulesExpanded : AddDocumentTypeIntent
    data class MatchingPatternChanged(val pattern: String) : AddDocumentTypeIntent
    data class AlgorithmSelected(val algorithm: MatchingAlgorithm) : AddDocumentTypeIntent
    data class ToggleIgnoreCase(val ignore: Boolean) : AddDocumentTypeIntent
    data object Dismiss : AddDocumentTypeIntent
    data object Save : AddDocumentTypeIntent
}
