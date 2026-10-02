@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.nextstepai.paperless.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.nextstepai.paperless.documents.presentation.DocumentUiModel
import com.nextstepai.paperless.documents.presentation.DocumentsUiEvent
import com.nextstepai.paperless.documents.presentation.DocumentsUiState
import com.nextstepai.paperless.documents.presentation.ExpiryState
import com.nextstepai.paperless.domain.model.Correspondent
import com.nextstepai.paperless.domain.model.DocumentType
import com.nextstepai.paperless.domain.model.StoragePath
import com.nextstepai.paperless.domain.model.Tag
import com.nextstepai.paperless.ui.DocumentsScreen
import com.nextstepai.paperless.ui.components.CatalogDeleteDialog
import com.nextstepai.paperless.ui.components.ClassificationEditDialog
import com.nextstepai.paperless.ui.components.StoragePathEditDialog
import com.nextstepai.paperless.ui.components.TagEditDialog
import com.nextstepai.paperless.ui.theme.PaperlessDimensions

@Composable
fun AppShell(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
) {
    var destination by rememberSaveable { mutableStateOf(AppDestination.Documents) }
    val compact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val showingDocumentDetail = compact && state.selected != null && destination == AppDestination.Documents
    val handleEvent: (DocumentsUiEvent) -> Unit = { event ->
        if (event is DocumentsUiEvent.Select || (event is DocumentsUiEvent.SearchChanged && destination == AppDestination.Tags)) {
            destination = AppDestination.Documents
        }
        onEvent(event)
    }

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
                AppDestinationContent(destination, state, handleEvent)
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
                    onEvent = handleEvent,
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
        AppDestination.Tags -> CatalogManagementScreen(state, onEvent, modifier)
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
private fun CatalogManagementScreen(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    var editingTag by remember { mutableStateOf<Tag?>(null) }
    var showTagDialog by remember { mutableStateOf(false) }

    var editingCorrespondent by remember { mutableStateOf<Correspondent?>(null) }
    var showCorrespondentDialog by remember { mutableStateOf(false) }

    var editingDocumentType by remember { mutableStateOf<DocumentType?>(null) }
    var showDocumentTypeDialog by remember { mutableStateOf(false) }

    var editingStoragePath by remember { mutableStateOf<StoragePath?>(null) }
    var showStoragePathDialog by remember { mutableStateOf(false) }

    var itemToDelete by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(title = { Text("Catalog Management") })
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Tags") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Correspondents") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Document Types") })
                    Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Storage Paths") })
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> { editingTag = null; showTagDialog = true }
                        1 -> { editingCorrespondent = null; showCorrespondentDialog = true }
                        2 -> { editingDocumentType = null; showDocumentTypeDialog = true }
                        3 -> { editingStoragePath = null; showStoragePathDialog = true }
                    }
                }
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> TagsList(
                    tags = state.tags,
                    onSearchTag = { onEvent(DocumentsUiEvent.SearchChanged("tag:" + it.name)) },
                    onEdit = { editingTag = it; showTagDialog = true },
                    onDelete = { itemToDelete = "Tag '${it.name}'" to { onEvent(DocumentsUiEvent.DeleteTag(it.id)) } }
                )
                1 -> CorrespondentsList(
                    correspondents = state.correspondents,
                    onSearch = { onEvent(DocumentsUiEvent.SearchChanged("correspondent:" + it.name)) },
                    onEdit = { editingCorrespondent = it; showCorrespondentDialog = true },
                    onDelete = { itemToDelete = "Correspondent '${it.name}'" to { onEvent(DocumentsUiEvent.DeleteCorrespondent(it.id)) } }
                )
                2 -> DocumentTypesList(
                    types = state.documentTypes,
                    onSearch = { onEvent(DocumentsUiEvent.SearchChanged("type:" + it.name)) },
                    onEdit = { editingDocumentType = it; showDocumentTypeDialog = true },
                    onDelete = { itemToDelete = "Document Type '${it.name}'" to { onEvent(DocumentsUiEvent.DeleteDocumentType(it.id)) } }
                )
                3 -> StoragePathsList(
                    paths = state.storagePaths,
                    onEdit = { editingStoragePath = it; showStoragePathDialog = true },
                    onDelete = { itemToDelete = "Storage Path '${it.name}'" to { onEvent(DocumentsUiEvent.DeleteStoragePath(it.id)) } }
                )
            }
        }
    }

    if (showTagDialog) {
        TagEditDialog(
            tag = editingTag,
            availableTags = state.tags,
            onSave = { tag ->
                showTagDialog = false
                onEvent(DocumentsUiEvent.SaveTag(tag))
            },
            onDismiss = { showTagDialog = false }
        )
    }

    if (showCorrespondentDialog) {
        ClassificationEditDialog(
            title = "Correspondent",
            initialName = editingCorrespondent?.name.orEmpty(),
            initialMatch = editingCorrespondent?.match.orEmpty(),
            initialAlgorithm = editingCorrespondent?.matchingAlgorithm ?: 1,
            initialInsensitive = editingCorrespondent?.insensitive ?: true,
            onSave = { name, match, algo, insensitive ->
                showCorrespondentDialog = false
                onEvent(
                    DocumentsUiEvent.SaveCorrespondent(
                        Correspondent(
                            id = editingCorrespondent?.id ?: 0,
                            remoteId = editingCorrespondent?.remoteId,
                            name = name,
                            match = match,
                            matchingAlgorithm = algo,
                            insensitive = insensitive
                        )
                    )
                )
            },
            onDismiss = { showCorrespondentDialog = false }
        )
    }

    if (showDocumentTypeDialog) {
        ClassificationEditDialog(
            title = "Document Type",
            initialName = editingDocumentType?.name.orEmpty(),
            initialMatch = editingDocumentType?.match.orEmpty(),
            initialAlgorithm = editingDocumentType?.matchingAlgorithm ?: 1,
            initialInsensitive = editingDocumentType?.insensitive ?: true,
            onSave = { name, match, algo, insensitive ->
                showDocumentTypeDialog = false
                onEvent(
                    DocumentsUiEvent.SaveDocumentType(
                        DocumentType(
                            id = editingDocumentType?.id ?: 0,
                            remoteId = editingDocumentType?.remoteId,
                            name = name,
                            match = match,
                            matchingAlgorithm = algo,
                            insensitive = insensitive
                        )
                    )
                )
            },
            onDismiss = { showDocumentTypeDialog = false }
        )
    }

    if (showStoragePathDialog) {
        StoragePathEditDialog(
            storagePath = editingStoragePath,
            onSave = { path ->
                showStoragePathDialog = false
                onEvent(DocumentsUiEvent.SaveStoragePath(path))
            },
            onDismiss = { showStoragePathDialog = false }
        )
    }

    itemToDelete?.let { (title, deleteAction) ->
        CatalogDeleteDialog(
            itemTitle = title,
            onConfirm = {
                deleteAction()
                itemToDelete = null
            },
            onDismiss = { itemToDelete = null }
        )
    }
}

@Composable
private fun TagsList(
    tags: List<Tag>,
    onSearchTag: (Tag) -> Unit,
    onEdit: (Tag) -> Unit,
    onDelete: (Tag) -> Unit,
) {
    if (tags.isEmpty()) {
        BoxedEmptyCollection("Tags", modifier = Modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PaperlessDimensions.lg),
            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
        ) {
            items(tags, key = { it.id }) { tag ->
                val tagColor = runCatching { parseHexColor(tag.color) }.getOrDefault(MaterialTheme.colorScheme.primary)
                Card(
                    onClick = { onSearchTag(tag) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(tagColor)
                        )
                        Spacer(Modifier.width(PaperlessDimensions.lg))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs)) {
                                Text(tag.name, style = MaterialTheme.typography.titleMedium)
                                if (tag.isInbox) {
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = { Text("Inbox") }
                                    )
                                }
                            }
                            if (tag.match.isNotBlank()) {
                                Text("Match: ${tag.match}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onEdit(tag) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Tag")
                        }
                        IconButton(onClick = { onDelete(tag) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete Tag", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CorrespondentsList(
    correspondents: List<Correspondent>,
    onSearch: (Correspondent) -> Unit,
    onEdit: (Correspondent) -> Unit,
    onDelete: (Correspondent) -> Unit,
) {
    if (correspondents.isEmpty()) {
        BoxedEmptyCollection("Correspondents", modifier = Modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PaperlessDimensions.lg),
            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
        ) {
            items(correspondents, key = { it.id }) { correspondent ->
                Card(
                    onClick = { onSearch(correspondent) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.FolderOpen, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(PaperlessDimensions.lg))
                        Column(Modifier.weight(1f)) {
                            Text(correspondent.name, style = MaterialTheme.typography.titleMedium)
                            if (correspondent.match.isNotBlank()) {
                                Text("Match: ${correspondent.match}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onEdit(correspondent) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Correspondent")
                        }
                        IconButton(onClick = { onDelete(correspondent) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete Correspondent", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentTypesList(
    types: List<DocumentType>,
    onSearch: (DocumentType) -> Unit,
    onEdit: (DocumentType) -> Unit,
    onDelete: (DocumentType) -> Unit,
) {
    if (types.isEmpty()) {
        BoxedEmptyCollection("Document Types", modifier = Modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PaperlessDimensions.lg),
            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
        ) {
            items(types, key = { it.id }) { type ->
                Card(
                    onClick = { onSearch(type) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(PaperlessDimensions.lg))
                        Column(Modifier.weight(1f)) {
                            Text(type.name, style = MaterialTheme.typography.titleMedium)
                            if (type.match.isNotBlank()) {
                                Text("Match: ${type.match}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onEdit(type) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Document Type")
                        }
                        IconButton(onClick = { onDelete(type) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete Document Type", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StoragePathsList(
    paths: List<StoragePath>,
    onEdit: (StoragePath) -> Unit,
    onDelete: (StoragePath) -> Unit,
) {
    if (paths.isEmpty()) {
        BoxedEmptyCollection("Storage Paths", modifier = Modifier.fillMaxSize())
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(PaperlessDimensions.lg),
            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
        ) {
            items(paths, key = { it.id }) { path ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.FolderOpen, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(PaperlessDimensions.lg))
                        Column(Modifier.weight(1f)) {
                            Text(path.name, style = MaterialTheme.typography.titleMedium)
                            if (path.path.isNotBlank()) {
                                Text("Path: ${path.path}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (path.match.isNotBlank()) {
                                Text("Match: ${path.match}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        IconButton(onClick = { onEdit(path) }) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Edit Storage Path")
                        }
                        IconButton(onClick = { onDelete(path) }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete Storage Path", tint = MaterialTheme.colorScheme.error)
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

private fun parseHexColor(hex: String): Color {
    val clean = hex.trim().removePrefix("#")
    val colorLong = clean.toLongOrNull(16) ?: 0xA6CEE3L
    return if (clean.length == 6) {
        Color(colorLong or 0xFF000000L)
    } else {
        Color(colorLong)
    }
}
