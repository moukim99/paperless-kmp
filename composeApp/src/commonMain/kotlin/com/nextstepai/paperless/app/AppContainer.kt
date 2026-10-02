package com.nextstepai.paperless.app

import com.nextstepai.paperless.documents.presentation.DocumentsViewModel

expect object AppContainer {
    val documentsViewModel: DocumentsViewModel
    fun initialize(context: Any? = null)
    fun startBackgroundSync()
}
