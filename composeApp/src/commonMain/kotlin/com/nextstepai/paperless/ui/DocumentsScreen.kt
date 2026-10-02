package com.nextstepai.paperless.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculateListDetailPaneScaffoldState
import androidx.window.core.layout.WindowWidthSizeClass
import com.nextstepai.paperless.documents.presentation.DocumentUiModel
import com.nextstepai.paperless.documents.presentation.DocumentsUiEvent
import com.nextstepai.paperless.documents.presentation.DocumentsUiState
import com.nextstepai.paperless.documents.presentation.ExpiryState
import com.nextstepai.paperless.platform.DocumentPickerButton
import com.nextstepai.paperless.platform.DocumentScannerButton
import com.nextstepai.paperless.ui.components.EmptyState
import com.nextstepai.paperless.ui.components.ExpiryBadge
import com.nextstepai.paperless.ui.components.LoadingState
import com.nextstepai.paperless.ui.theme.PaperlessDimensions

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val compact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    if (compact && state.selected != null) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            state.selected.title.ifBlank { "Document" },
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onEvent(DocumentsUiEvent.ClearSelection) }) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            DocumentDetails(
                state = state,
                selected = state.selected,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(padding),
                showTopBar = false,
            )
        }
        return
    }

    val scaffoldState = calculateListDetailPaneScaffoldState(
        currentPaneDestination = if (state.selected == null) {
            ListDetailPaneScaffoldRole.List
        } else {
            ListDetailPaneScaffoldRole.Detail
        }
    )

    ListDetailPaneScaffold(
        scaffoldState = scaffoldState,
        modifier = modifier.fillMaxSize(),
        listPane = {
            AnimatedPane {
                DocumentsList(
                    state = state,
                    onEvent = onEvent,
                    onSelect = { onEvent(DocumentsUiEvent.Select(it)) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        detailPane = {
            AnimatedPane {
                state.selected?.let {
                    DocumentDetails(
                        state = state,
                        selected = it,
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                        showTopBar = true,
                    )
                } ?: EmptyState(
                    "Your document details",
                    "Select a document to view metadata, expiry, tags and extracted text."
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentsList(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addOpen by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Documents")
                        Text(
                            state.documents.size.toString() + " in your library",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(DocumentsUiEvent.Refresh) }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { addOpen = true },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Add document") }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
        ) {
            SearchAndFilters(state, onEvent)

            state.error?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = PaperlessDimensions.lg),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        it,
                        modifier = Modifier.padding(PaperlessDimensions.lg),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.height(PaperlessDimensions.sm))
            }

            if (state.importing) {
                androidx.compose.material3.LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            when {
                state.loading -> LoadingState(Modifier.fillMaxSize())
                state.documents.isEmpty() -> EmptyState(
                    if (state.query.isBlank()) "No documents yet" else "No matching documents",
                    if (state.query.isBlank())
                        "Import a file or scan a document to start building your library."
                    else
                        "Try a different search or use one of the filters above."
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = PaperlessDimensions.lg,
                        top = PaperlessDimensions.sm,
                        end = PaperlessDimensions.lg,
                        bottom = 112.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
                ) {
                    items(state.documents, key = { it.id }) { doc ->
                        ModernDocumentCard(
                            document = doc,
                            correspondent = state.correspondents.firstOrNull { it.id == doc.correspondentId }?.name,
                            selected = doc.id == state.selectedId,
                            onClick = { onSelect(doc.id) }
                        )
                    }
                }
            }
        }
    }

    if (addOpen) {
        ModalBottomSheet(onDismissRequest = { addOpen = false }) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = PaperlessDimensions.xl)
                    .padding(bottom = PaperlessDimensions.xl),
                verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.md)
            ) {
                Text("Add to library", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Choose how you want to bring a document into Paperless.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.UploadFile, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(PaperlessDimensions.lg))
                        Column(Modifier.weight(1f)) {
                            Text("Import a file", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Choose an existing PDF or image from your device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DocumentPickerButton {
                        addOpen = false
                        onEvent(DocumentsUiEvent.Import(it))
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.DocumentScanner, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(PaperlessDimensions.lg))
                        Column(Modifier.weight(1f)) {
                            Text("Scan document", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Capture one or more pages with the document scanner.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    DocumentScannerButton {
                        addOpen = false
                        onEvent(DocumentsUiEvent.Import(it))
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchAndFilters(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = PaperlessDimensions.lg, vertical = PaperlessDimensions.sm),
        verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onEvent(DocumentsUiEvent.SearchChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            placeholder = { Text("Search your documents") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { onEvent(DocumentsUiEvent.SearchChanged("")) }) {
                        Icon(Icons.Outlined.Close, contentDescription = "Clear search")
                    }
                }
            },
            supportingText = {
                Text("Supports tag:, type:, correspondent:, after:, before:, expiry:")
            }
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)) {
            item {
                FilterChip(
                    selected = state.query.isBlank(),
                    onClick = { onEvent(DocumentsUiEvent.SearchChanged("")) },
                    label = { Text("All") }
                )
            }
            item {
                FilterChip(
                    selected = state.query == "expiry:soon",
                    onClick = { onEvent(DocumentsUiEvent.SearchChanged("expiry:soon")) },
                    label = { Text("Expiring") }
                )
            }
        }
    }
}

@Composable
private fun ModernDocumentCard(
    document: DocumentUiModel,
    correspondent: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Outlined.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(Modifier.width(PaperlessDimensions.lg))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    document.title.ifBlank { "Untitled document" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2
                )
                Text(
                    buildString {
                        correspondent?.let { append(it); append(" · ") }
                        append(document.mimeType.substringAfterLast('/').uppercase())
                        append(" · ")
                        append(document.pageCount ?: 1)
                        append(if ((document.pageCount ?: 1) == 1) " page" else " pages")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)) {
                    document.expiryLabel?.let { ExpiryBadge(document.expiryState, it) }
                    SyncBadge(document.syncState)
                }
            }
        }
    }
}

@Composable
private fun SyncBadge(syncState: String) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(syncState.lowercase().replaceFirstChar { it.uppercase() })
        }
    )
}

@Composable
private fun DocumentDetails(
    state: DocumentsUiState,
    selected: DocumentUiModel,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier,
    showTopBar: Boolean,
) {
    var confirmDelete by remember(selected.id) { mutableStateOf(false) }
    var ocrExpanded by rememberSaveable(selected.id) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    val dirty = state.editTitle != selected.title ||
        state.editExpiry != selected.expiryInput ||
        state.editReminderDays != (selected.reminderDaysBeforeExpiry ?: 30).toString() ||
        state.selectedCorrespondentId != selected.correspondentId ||
        state.selectedDocumentTypeId != selected.documentTypeId ||
        state.selectedTagIds != selected.tagIds ||
        state.customFieldValues != state.initialCustomFieldValues

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = if (showTopBar) {
            {
                TopAppBar(
                    title = { Text("Document details") },
                    actions = {
                        if (dirty) {
                            Text(
                                "Unsaved",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = PaperlessDimensions.lg)
                            )
                        }
                    }
                )
            }
        } else {
            {}
        },
        bottomBar = {
            if (dirty) {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Unsaved changes",
                            Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilledTonalButton(
                            onClick = { onEvent(DocumentsUiEvent.SaveMetadata) },
                            enabled = !state.saving
                        ) {
                            Text(if (state.saving) "Saving…" else "Save changes")
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                horizontal = PaperlessDimensions.xl,
                vertical = PaperlessDimensions.lg
            ),
            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.lg)
        ) {
            item {
                DocumentHeader(selected)
            }

            item {
                DetailCard(title = "Information", icon = Icons.Outlined.Description) {
                    OutlinedTextField(
                        value = state.editTitle,
                        onValueChange = { onEvent(DocumentsUiEvent.TitleChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(PaperlessDimensions.md))
                    CatalogDropdown(
                        title = "Correspondent",
                        selectedId = state.selectedCorrespondentId,
                        items = state.correspondents.map { it.id to it.name },
                        onSelect = { onEvent(DocumentsUiEvent.CorrespondentChanged(it)) }
                    )
                    Spacer(Modifier.height(PaperlessDimensions.md))
                    CatalogDropdown(
                        title = "Document type",
                        selectedId = state.selectedDocumentTypeId,
                        items = state.documentTypes.map { it.id to it.name },
                        onSelect = { onEvent(DocumentsUiEvent.DocumentTypeChanged(it)) }
                    )
                }
            }

            item {
                DetailCard(title = "Expiry & reminders", icon = Icons.Outlined.CalendarMonth) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.md)
                    ) {
                        OutlinedTextField(
                            value = state.editExpiry,
                            onValueChange = { onEvent(DocumentsUiEvent.ExpiryChanged(it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Expiry date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.editReminderDays,
                            onValueChange = { onEvent(DocumentsUiEvent.ReminderDaysChanged(it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Reminder days") },
                            leadingIcon = { Icon(Icons.Outlined.Notifications, null) },
                            singleLine = true
                        )
                    }
                    selected.expiryLabel?.let {
                        Spacer(Modifier.height(PaperlessDimensions.sm))
                        ExpiryBadge(selected.expiryState, it)
                    }
                }
            }

            item {
                TagsCard(
                    state = state,
                    onEvent = onEvent
                )
            }

            if (state.customFields.isNotEmpty()) {
                item {
                    DetailCard(title = "Custom fields", icon = Icons.Outlined.Label) {
                        Column(verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.md)) {
                            state.customFields.forEach { field ->
                                OutlinedTextField(
                                    value = state.customFieldValues[field.id].orEmpty(),
                                    onValueChange = { onEvent(DocumentsUiEvent.CustomFieldChanged(field.id, it)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(field.name) },
                                    supportingText = { Text(field.type.name.lowercase().replace('_', ' ')) },
                                    minLines = if (field.type.name == "LONG_TEXT") 3 else 1
                                )
                            }
                        }
                    }
                }
            }

            item {
                DetailCard(title = "Extracted text", icon = Icons.Outlined.FindInPage) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
                    ) {
                        FilledTonalButton(
                            onClick = { clipboard.setText(AnnotatedString(selected.content)) },
                            enabled = selected.content.isNotBlank()
                        ) {
                            Icon(Icons.Outlined.ContentCopy, null)
                            Spacer(Modifier.width(PaperlessDimensions.sm))
                            Text("Copy")
                        }
                        OutlinedButton(
                            onClick = {
                                val query = selected.content
                                    .trim()
                                    .split(Regex("\\s+"))
                                    .take(6)
                                    .joinToString(" ")
                                if (query.isNotBlank()) onEvent(DocumentsUiEvent.SearchChanged(query))
                            },
                            enabled = selected.content.isNotBlank()
                        ) {
                            Icon(Icons.Outlined.Search, null)
                            Spacer(Modifier.width(PaperlessDimensions.sm))
                            Text("Search text")
                        }
                    }

                    Spacer(Modifier.height(PaperlessDimensions.md))

                    if (selected.content.isBlank()) {
                        Text(
                            "No OCR text available.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            if (ocrExpanded) selected.content else selected.content.take(320) + if (selected.content.length > 320) "…" else "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (selected.content.length > 320) {
                            TextButton(onClick = { ocrExpanded = !ocrExpanded }) {
                                Text(if (ocrExpanded) "Show less" else "Show full text")
                            }
                        }
                    }
                }
            }

            item {
                DetailCard(title = "Actions", icon = Icons.Outlined.MoreVert) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(onClick = { onEvent(DocumentsUiEvent.OpenFile) }) {
                            Icon(Icons.Outlined.OpenInNew, null)
                            Spacer(Modifier.width(PaperlessDimensions.sm))
                            Text("Open file")
                        }
                        OutlinedButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Outlined.DeleteOutline, null)
                            Spacer(Modifier.width(PaperlessDimensions.sm))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
            title = { Text("Delete document?") },
            text = { Text("The document will be removed from the local library. This action cannot be undone here.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onEvent(DocumentsUiEvent.Delete(selected.id))
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DocumentHeader(selected: DocumentUiModel) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Outlined.Description,
                    null,
                    Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.width(PaperlessDimensions.lg))
        Column(Modifier.weight(1f)) {
            Text(
                selected.title.ifBlank { "Untitled document" },
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                selected.mimeType.substringAfterLast('/').uppercase() + " · " +
                    (selected.pageCount ?: 1) +
                    if ((selected.pageCount ?: 1) == 1) " page" else " pages",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            selected.expiryLabel?.let {
                Spacer(Modifier.height(PaperlessDimensions.sm))
                ExpiryBadge(selected.expiryState, it)
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm),
            content = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(PaperlessDimensions.sm))
                    Text(title, style = MaterialTheme.typography.titleLarge)
                }
                HorizontalDivider(Modifier.padding(vertical = PaperlessDimensions.sm))
                content()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CatalogDropdown(
    title: String,
    selectedId: Long?,
    items: List<Pair<Long, String>>,
    onSelect: (Long?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = items.firstOrNull { it.first == selectedId }?.second ?: "None"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                Text(title, style = MaterialTheme.typography.labelMedium)
                Text(selectedLabel)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    expanded = false
                    onSelect(null)
                }
            )
            items.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelect(id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagsCard(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedTags = state.tags.filter { it.id in state.selectedTagIds }

    DetailCard(title = "Tags", icon = Icons.Outlined.Label) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
        ) {
            selectedTags.forEach { tag ->
                FilterChip(
                    selected = true,
                    onClick = { onEvent(DocumentsUiEvent.TagToggled(tag.id)) },
                    label = { Text(tag.name) }
                )
            }

            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Icon(Icons.Outlined.Add, null)
                    Spacer(Modifier.width(PaperlessDimensions.sm))
                    Text("Add tag")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    val available = state.tags.filterNot { it.id in state.selectedTagIds }
                    if (available.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("All tags are selected") },
                            onClick = { expanded = false }
                        )
                    } else {
                        available.forEach { tag ->
                            DropdownMenuItem(
                                text = { Text(tag.name) },
                                onClick = {
                                    expanded = false
                                    onEvent(DocumentsUiEvent.TagToggled(tag.id))
                                }
                            )
                        }
                    }
                }
            }
        }
        if (selectedTags.isEmpty()) {
            Text(
                "No tags assigned",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
