package com.nextstepai.paperless

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.nextstepai.paperless.ui.navigation.AppShell
import com.nextstepai.paperless.ui.theme.PaperlessTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.nextstepai.paperless.app.AppContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        enableHighRefreshRate()
        AppContainer.initialize(this)
        AppContainer.startBackgroundSync()
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
        setContent {
            val state by AppContainer.documentsViewModel.state.collectAsState()
            PaperlessTheme { Surface { AppShell(state, AppContainer.documentsViewModel::onEvent) } }
        }
    }

    private fun enableHighRefreshRate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val display = window.context.display
            val maxRefreshRateMode = display.supportedModes?.maxByOrNull { it.refreshRate }
            maxRefreshRateMode?.let { mode ->
                window.attributes = window.attributes.apply {
                    preferredDisplayModeId = mode.modeId
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.attributes = window.attributes.apply {
                @Suppress("DEPRECATION")
                preferredRefreshRate = 120f
            }
        }
    }
}
