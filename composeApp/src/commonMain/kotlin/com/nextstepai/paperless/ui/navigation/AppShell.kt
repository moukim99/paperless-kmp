package com.nextstepai.paperless.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import com.nextstepai.paperless.documents.presentation.DocumentsUiEvent
import com.nextstepai.paperless.documents.presentation.DocumentsUiState
import com.nextstepai.paperless.ui.DocumentsScreen

@Composable
fun AppShell(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Documents) }
    val widthClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
    val compact = widthClass == WindowWidthSizeClass.COMPACT

    if (compact) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    AppDestination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = { Text(item.symbol) },
                            label = { Text(item.label) },
                        )
                    }
                }
            },
        ) { padding ->
            Surface(Modifier.fillMaxSize()) {
                AppDestinationContent(destination, state, onEvent)
            }
        }
    } else {
        NavigationRailScaffold(
            selected = destination,
            onSelect = { destination = it },
            content = { AppDestinationContent(destination, state, onEvent) },
        )
    }
}

@Composable
private fun NavigationRailScaffold(
    selected: AppDestination,
    onSelect: (AppDestination) -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Row(Modifier.fillMaxSize()) {
            NavigationRail {
                AppDestination.entries.forEach { item ->
                    NavigationRailItem(
                        selected = selected == item,
                        onClick = { onSelect(item) },
                        icon = { Text(item.symbol) },
                        label = { Text(item.label) },
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun AppDestinationContent(
    destination: AppDestination,
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
) {
    when (destination) {
        AppDestination.Documents -> DocumentsScreen(state, onEvent)
        AppDestination.Inbox -> PlaceholderDestination("Inbox")
        AppDestination.Expiring -> PlaceholderDestination("Expiring")
        AppDestination.Tags -> PlaceholderDestination("Tags")
        AppDestination.Settings -> PlaceholderDestination("Settings")
    }
}

@Composable
private fun PlaceholderDestination(title: String) {
    Surface(Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "This section is ready for its feature implementation.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
