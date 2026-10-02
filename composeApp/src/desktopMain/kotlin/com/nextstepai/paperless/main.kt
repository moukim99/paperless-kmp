package com.nextstepai.paperless

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nextstepai.paperless.app.AppContainer
import com.nextstepai.paperless.ui.DocumentsScreen

fun main() = application {
    AppContainer.initialize()
    AppContainer.startBackgroundSync()
    Window(onCloseRequest = ::exitApplication, title = "Paperless KMP") {
        val state by AppContainer.documentsViewModel.state.collectAsState()
        MaterialTheme { Surface { DocumentsScreen(state, AppContainer.documentsViewModel::onEvent) } }
    }
}
