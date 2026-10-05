package com.nextstepai.paperless.ui.documents.add

sealed interface AddDocumentIntent {
    data object Dismiss : AddDocumentIntent
    data object ImportFileClicked : AddDocumentIntent
    data object ScanDocumentClicked : AddDocumentIntent
    data object HelpGuideClicked : AddDocumentIntent
}
