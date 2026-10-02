package com.nextstepai.paperless

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nextstepai.paperless.app.AppContainer
import com.nextstepai.paperless.ui.DocumentsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(this)
        AppContainer.startBackgroundSync()
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        setContent {
            val state by AppContainer.documentsViewModel.state.collectAsState()
            MaterialTheme { Surface { DocumentsScreen(state, AppContainer.documentsViewModel::onEvent) } }
        }
    }
}
