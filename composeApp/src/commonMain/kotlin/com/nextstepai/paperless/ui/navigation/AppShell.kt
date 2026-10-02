package com.nextstepai.paperless.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.nextstepai.paperless.documents.presentation.DocumentUiModel
import com.nextstepai.paperless.documents.presentation.DocumentsUiEvent
import com.nextstepai.paperless.documents.presentation.DocumentsUiState
import com.nextstepai.paperless.documents.presentation.ExpiryState
import com.nextstepai.paperless.ui.DocumentsScreen
import com.nextstepai.paperless.ui.theme.PaperlessDimensions

@Composable
fun AppShell(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Documents) }
    val compact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val showingDocumentDetail = compact && state.selected != null && destination == AppDestination.Documents

    if (compact) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (!showingDocumentDetail) {
                    NavigationBar {
                        AppDestination.entries.forEach { item ->
                            NavigationBarItem(
                                selected = destination == item,
                                onClick = { destination = item },
                                icon = { DestinationIcon(item) },
                                label = { Text(item.label) },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Surface(Modifier.fillMaxSize().padding(padding)) {
                AppDestinationContent(destination, state, onEvent)
            }
        }
    } else {
        Surface(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail(
                    header = {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.padding(top = PaperlessDimensions.lg)
                        )
                    }
                ) {
                    AppDestination.entries.forEach { item ->
                        NavigationRailItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = { DestinationIcon(item) },
                            label = { Text(item.label) },
                        )
                    }
                }
                AppDestinationContent(
                    destination = destination,
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun DestinationIcon(item: AppDestination) {
    val icon = when (item) {
        AppDestination.Documents -> Icons.Outlined.Description
        AppDestination.Inbox -> Icons.Outlined.FolderOpen
        AppDestination.Expiring -> Icons.Outlined.Schedule
        AppDestination.Tags -> Icons.Outlined.Label
        AppDestination.Settings -> Icons.Outlined.Settings
    }
    Icon(icon, contentDescription = item.label)
}

@Composable
private fun AppDestinationContent(
    destination: AppDestination,
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (destination) {
        AppDestination.Documents -> DocumentsScreen(state, onEvent, modifier)
        AppDestination.Inbox -> CollectionScreen(
            title = "Inbox",
            subtitle = "Recently captured documents",
            documents = state.documents.sortedByDescending { it.createdEpochMillis }.take(20),
            state = state,
            onEvent = onEvent,
            modifier = modifier,
        )
        AppDestination.Expiring -> CollectionScreen(
            title = "Expiring",
            subtitle = "Documents that need attention",
            documents = state.documents
                .filter { it.expiryState != ExpiryState.None }
                .sortedWith(compareBy<DocumentUiModel> { it.expiryState != ExpiryState.Expired }.thenBy { it.expiresAtEpochMillis ?: Long.MAX_VALUE }),
            state = state,
            onEvent = onEvent,
            modifier = modifier,
        )
        AppDestination.Tags -> TagsScreen(state, onEvent, modifier)
        AppDestination.Settings -> SettingsScreen(modifier)
    }
}

@Composable
private fun CollectionScreen(
    title: String,
    subtitle: String,
    documents: List<DocumentUiModel>,
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title)
                        Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    ) { padding ->
        if (documents.isEmpty()) {
            BoxedEmptyCollection(title, modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(PaperlessDimensions.lg),
                verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
            ) {
                items(documents, key = { it.id }) { document ->
                    Card(
                        onClick = { onEvent(DocumentsUiEvent.Select(document.id)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (document.id == state.selectedId)
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Description, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.size(PaperlessDimensions.lg))
                            Column(Modifier.weight(1f)) {
                                Text(document.title.ifBlank { "Untitled document" }, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    document.expiryLabel ?: document.mimeType.substringAfterLast('/').uppercase(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxedEmptyCollection(title: String, modifier: Modifier = Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.FolderOpen, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(PaperlessDimensions.lg))
        Text("Nothing here yet", style = MaterialTheme.typography.titleLarge)
        Text(
            title + " will show relevant documents here.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TagsScreen(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Tags") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(PaperlessDimensions.lg),
            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
        ) {
            item {
                Text(
                    "Browse your library by tag",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(state.tags, key = { it.id }) { tag ->
                Card(
                    onClick = { onEvent(DocumentsUiEvent.SearchChanged("tag:" + tag.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Label, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.size(PaperlessDimensions.lg))
                        Column {
                            Text(tag.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Search documents with this tag",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Settings") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(PaperlessDimensions.lg),
            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.md)
        ) {
            item {
                Text("App preferences", style = MaterialTheme.typography.titleLarge)
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(PaperlessDimensions.lg)) {
                        Text("Appearance", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(PaperlessDimensions.sm))
                        Text("System theme and dynamic Material 3 colors are currently enabled.")
                        Text(
                            "Theme controls can be persisted here when preference storage is added.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(PaperlessDimensions.lg)) {
                        Text("Sync", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(PaperlessDimensions.sm))
                        Text("Background synchronization is managed by the app scheduler.")
                        Text(
                            "Server and account controls belong here in the next settings slice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Column(Modifier.padding(PaperlessDimensions.lg)) {
                        Text("Notifications", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(PaperlessDimensions.sm))
                        Text("Expiry reminders are supported.")
                        Text(
                            "Notification permission and reminder scheduling are handled by the Android layer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
