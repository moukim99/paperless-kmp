package com.nextstepai.paperless.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.nextstepai.paperless.documents.presentation.DocumentUiModel
import com.nextstepai.paperless.documents.presentation.DocumentsUiEvent
import com.nextstepai.paperless.documents.presentation.DocumentsUiState
import com.nextstepai.paperless.platform.DocumentPickerButton
import com.nextstepai.paperless.platform.DocumentScannerButton
import com.nextstepai.paperless.ui.components.CustomFieldInput
import com.nextstepai.paperless.ui.components.EmptyState
import com.nextstepai.paperless.ui.components.ExpiryBadge
import com.nextstepai.paperless.ui.components.LoadingState
import com.nextstepai.paperless.ui.theme.PaperlessDimensions
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = state.selected
    val compact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    if (compact && selected != null) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            selected.title.ifBlank { "Document" },
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
                selected = selected,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(padding),
                showTopBar = false,
            )
        }
        return
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
    val currentSelectedId = state.selectedId
    LaunchedEffect(currentSelectedId) {
        if (currentSelectedId != null) {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
        } else {
            navigator.navigateTo(ListDetailPaneScaffoldRole.List)
        }
    }

    ListDetailPaneScaffold(
        directive = navigator.scaffoldDirective,
        value = navigator.scaffoldValue,
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
                selected?.let {
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
    val isPdf = document.mimeType.contains("pdf", ignoreCase = true)
    val isImage = document.mimeType.contains("image", ignoreCase = true)
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val thumbColor = when {
        isPdf -> MaterialTheme.colorScheme.errorContainer
        isImage -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val thumbIconColor = when {
        isPdf -> MaterialTheme.colorScheme.onErrorContainer
        isImage -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val thumbIcon = when {
        isImage -> Icons.Outlined.Image
        else -> Icons.Outlined.Description
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = MaterialTheme.shapes.medium,
                color = thumbColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        thumbIcon,
                        contentDescription = null,
                        tint = thumbIconColor,
                        modifier = Modifier.size(26.dp)
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
                if (!correspondent.isNullOrBlank()) {
                    Text(
                        correspondent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(document.mimeType.substringAfterLast('/').uppercase()) }
                    )
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text("${document.pageCount ?: 1}p") }
                    )
                    document.expiryLabel?.let { ExpiryBadge(document.expiryState, it) }
                    SyncBadge(document.syncState)
                }
            }
        }
    }
}

@Composable
private fun SyncBadge(syncState: String) {
    val isError = syncState.equals("FAILED", true) || syncState.equals("CONFLICT", true) || syncState.equals("ERROR", true)
    AssistChip(
        onClick = {},
        enabled = false,
        label = {
            Text(syncState.lowercase().replaceFirstChar { it.uppercase() })
        },
        leadingIcon = if (isError) {
            {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    contentDescription = "Sync error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else null,
        colors = if (isError) {
            AssistChipDefaults.assistChipColors(
                disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                disabledLabelColor = MaterialTheme.colorScheme.onErrorContainer
            )
        } else AssistChipDefaults.assistChipColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var editOcrMode by remember(selected.id) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val availableDocs = remember(state.documents) {
        state.documents.map { it.id to it.title.ifBlank { "Untitled document #${it.id}" } }
    }

    val dirty = state.editTitle != selected.title ||
        state.editContent != selected.content ||
        state.editVersionLabel != selected.versionLabel.orEmpty() ||
        state.editCreatedDate != selected.createdInput ||
        state.editExpiry != selected.expiryInput ||
        state.editArchiveSerialNumber != selected.archiveSerialNumber?.toString().orEmpty() ||
        state.editReminderDays != (selected.reminderDaysBeforeExpiry ?: 30).toString() ||
        state.selectedCorrespondentId != selected.correspondentId ||
        state.selectedDocumentTypeId != selected.documentTypeId ||
        state.selectedStoragePathId != selected.storagePathId ||
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
            AnimatedVisibility(
                visible = dirty,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
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
            if (selected.syncState.equals("FAILED", true) || selected.syncState.equals("CONFLICT", true) || selected.syncState.equals("ERROR", true) || selected.lastSyncError != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(PaperlessDimensions.lg),
                            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(Modifier.width(PaperlessDimensions.sm))
                                Text(
                                    if (selected.syncState.equals("CONFLICT", true)) "Sync Conflict" else "Sync Error",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                selected.lastSyncError?.ifBlank { "Synchronization failed. Check your network or server connection." }
                                    ?: "Synchronization failed. Check your network or server connection.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            FilledTonalButton(
                                onClick = { onEvent(DocumentsUiEvent.RetrySync(selected.id)) },
                                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onError,
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Outlined.Refresh, contentDescription = null)
                                Spacer(Modifier.width(PaperlessDimensions.sm))
                                Text("Retry sync")
                            }
                        }
                    }
                }
            }
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
                    SearchableCatalogDropdown(
                        title = "Correspondent",
                        selectedId = state.selectedCorrespondentId,
                        items = state.correspondents.map { it.id to it.name },
                        onSelect = { onEvent(DocumentsUiEvent.CorrespondentChanged(it)) },
                        onAddNew = { onEvent(DocumentsUiEvent.ToggleAddCorrespondentDialog(true)) }
                    )
                    Spacer(Modifier.height(PaperlessDimensions.md))
                    SearchableCatalogDropdown(
                        title = "Document type",
                        selectedId = state.selectedDocumentTypeId,
                        items = state.documentTypes.map { it.id to it.name },
                        onSelect = { onEvent(DocumentsUiEvent.DocumentTypeChanged(it)) },
                        onAddNew = { onEvent(DocumentsUiEvent.ToggleAddDocumentTypeDialog(true)) }
                    )
                    Spacer(Modifier.height(PaperlessDimensions.md))
                    SearchableCatalogDropdown(
                        title = "Storage path",
                        selectedId = state.selectedStoragePathId,
                        items = state.storagePaths.map { it.id to it.name },
                        onSelect = { onEvent(DocumentsUiEvent.StoragePathChanged(it)) },
                        onAddNew = { onEvent(DocumentsUiEvent.ToggleAddStoragePathDialog(true)) }
                    )
                    Spacer(Modifier.height(PaperlessDimensions.md))
                    OutlinedTextField(
                        value = state.editArchiveSerialNumber,
                        onValueChange = { onEvent(DocumentsUiEvent.ArchiveSerialNumberChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Archive serial number (ASN)") },
                        placeholder = { Text("e.g. 10042") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(Modifier.height(PaperlessDimensions.md))
                    OutlinedTextField(
                        value = state.editVersionLabel,
                        onValueChange = { onEvent(DocumentsUiEvent.VersionLabelChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Version label") },
                        placeholder = { Text("e.g. Draft, Signed Final") },
                        singleLine = true
                    )
                }
            }

            if (state.documentVersions.size > 1) {
                item {
                    DetailCard(title = "Document versions (${state.documentVersions.size})", icon = Icons.Outlined.FolderOpen) {
                        Column(verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs)) {
                            state.documentVersions.forEach { ver ->
                                val isCurrent = ver.id == selected.id
                                Surface(
                                    onClick = { if (!isCurrent) onEvent(DocumentsUiEvent.SelectVersion(ver.id)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.medium,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                                ) {
                                    Row(
                                        modifier = Modifier.padding(PaperlessDimensions.md),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                "Version ${ver.versionIndex ?: 1}" + if (!ver.versionLabel.isNullOrBlank()) " - ${ver.versionLabel}" else "",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                "Created: ${ver.createdInput}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (isCurrent) {
                                            AssistChip(
                                                onClick = {},
                                                enabled = false,
                                                label = { Text("Current") }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                DetailCard(title = "Dates & reminders", icon = Icons.Outlined.CalendarMonth) {
                    OutlinedTextField(
                        value = state.editCreatedDate,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { onEvent(DocumentsUiEvent.ToggleCreatedDatePicker(true)) },
                        enabled = false,
                        readOnly = true,
                        label = { Text("Document date") },
                        placeholder = { Text("YYYY-MM-DD") },
                        trailingIcon = {
                            IconButton(onClick = { onEvent(DocumentsUiEvent.ToggleCreatedDatePicker(true)) }) {
                                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Select document date")
                            }
                        },
                        singleLine = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(Modifier.height(PaperlessDimensions.md))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.md)
                    ) {
                        OutlinedTextField(
                            value = state.editExpiry,
                            onValueChange = {},
                            modifier = Modifier.weight(1f).clickable { onEvent(DocumentsUiEvent.ToggleExpiryDatePicker(true)) },
                            enabled = false,
                            readOnly = true,
                            label = { Text("Expiry date") },
                            placeholder = { Text("YYYY-MM-DD") },
                            trailingIcon = {
                                IconButton(onClick = { onEvent(DocumentsUiEvent.ToggleExpiryDatePicker(true)) }) {
                                    Icon(Icons.Outlined.CalendarMonth, contentDescription = "Select expiry date")
                                }
                            },
                            singleLine = true,
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        OutlinedTextField(
                            value = state.editReminderDays,
                            onValueChange = { onEvent(DocumentsUiEvent.ReminderDaysChanged(it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Reminder days") },
                            leadingIcon = { Icon(Icons.Outlined.Notifications, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                                CustomFieldInput(
                                    field = field,
                                    value = state.customFieldValues[field.id].orEmpty(),
                                    onValueChange = { onEvent(DocumentsUiEvent.CustomFieldChanged(field.id, it)) },
                                    availableDocuments = availableDocs
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
                            onClick = { editOcrMode = !editOcrMode }
                        ) {
                            Text(if (editOcrMode) "Done editing" else "Edit text")
                        }
                        FilledTonalButton(
                            onClick = { clipboard.setText(AnnotatedString(state.editContent)) },
                            enabled = state.editContent.isNotBlank()
                        ) {
                            Icon(Icons.Outlined.ContentCopy, null)
                            Spacer(Modifier.width(PaperlessDimensions.sm))
                            Text("Copy")
                        }
                        OutlinedButton(
                            onClick = {
                                val query = state.editContent
                                    .trim()
                                    .split(Regex("\\s+"))
                                    .take(6)
                                    .joinToString(" ")
                                if (query.isNotBlank()) onEvent(DocumentsUiEvent.SearchChanged(query))
                            },
                            enabled = state.editContent.isNotBlank()
                        ) {
                            Icon(Icons.Outlined.Search, null)
                            Spacer(Modifier.width(PaperlessDimensions.sm))
                            Text("Search text")
                        }
                    }

                    Spacer(Modifier.height(PaperlessDimensions.md))

                    if (editOcrMode) {
                        OutlinedTextField(
                            value = state.editContent,
                            onValueChange = { onEvent(DocumentsUiEvent.ContentChanged(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Extracted OCR text / Notes") },
                            minLines = 6,
                            maxLines = 15
                        )
                    } else if (state.editContent.isBlank()) {
                        Text(
                            "No OCR text available.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            if (ocrExpanded) state.editContent else state.editContent.take(320) + if (state.editContent.length > 320) "…" else "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (state.editContent.length > 320) {
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

    if (state.showCreatedDatePicker) {
        PaperlessDatePickerDialog(
            title = "Document date",
            initialDateString = state.editCreatedDate,
            onDateSelected = { onEvent(DocumentsUiEvent.CreatedDateChanged(it)) },
            onDismiss = { onEvent(DocumentsUiEvent.ToggleCreatedDatePicker(false)) }
        )
    }

    if (state.showExpiryDatePicker) {
        PaperlessDatePickerDialog(
            title = "Expiry date",
            initialDateString = state.editExpiry,
            onDateSelected = { onEvent(DocumentsUiEvent.ExpiryChanged(it)) },
            onDismiss = { onEvent(DocumentsUiEvent.ToggleExpiryDatePicker(false)) }
        )
    }

    if (state.showAddCorrespondentDialog) {
        AddItemDialog(
            title = "Correspondent",
            onConfirm = { onEvent(DocumentsUiEvent.AddCorrespondent(it)) },
            onDismiss = { onEvent(DocumentsUiEvent.ToggleAddCorrespondentDialog(false)) }
        )
    }

    if (state.showAddDocumentTypeDialog) {
        AddItemDialog(
            title = "Document type",
            onConfirm = { onEvent(DocumentsUiEvent.AddDocumentType(it)) },
            onDismiss = { onEvent(DocumentsUiEvent.ToggleAddDocumentTypeDialog(false)) }
        )
    }

    if (state.showAddStoragePathDialog) {
        AddItemDialog(
            title = "Storage path",
            onConfirm = { onEvent(DocumentsUiEvent.AddStoragePath(it)) },
            onDismiss = { onEvent(DocumentsUiEvent.ToggleAddStoragePathDialog(false)) }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaperlessDatePickerDialog(
    title: String,
    initialDateString: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val initialMillis = remember(initialDateString) {
        initialDateString.trim().takeIf { it.isNotBlank() }?.let {
            runCatching {
                LocalDate.parse(it).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            }.getOrNull()
        } ?: kotlin.time.Clock.System.now().toEpochMilliseconds()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val selectedMs = datePickerState.selectedDateMillis
                    if (selectedMs != null) {
                        val formatted = Instant.fromEpochMilliseconds(selectedMs)
                            .toLocalDateTime(TimeZone.UTC)
                            .date
                            .toString()
                        onDateSelected(formatted)
                    }
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = { Text(title, modifier = Modifier.padding(start = 24.dp, top = 16.dp)) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableCatalogDropdown(
    title: String,
    selectedId: Long?,
    items: List<Pair<Long, String>>,
    onSelect: (Long?) -> Unit,
    onAddNew: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val selectedLabel = items.firstOrNull { it.first == selectedId }?.second ?: "None"

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    Box {
        OutlinedButton(
            onClick = {
                searchQuery = ""
                expanded = true
            },
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = PaperlessDimensions.sm, vertical = PaperlessDimensions.xs),
                placeholder = { Text("Search " + title.lowercase()) },
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) }
            )
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    expanded = false
                    onSelect(null)
                }
            )
            HorizontalDivider()
            filteredItems.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        expanded = false
                        onSelect(id)
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Add, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(PaperlessDimensions.sm))
                        Text("Add new " + title.lowercase(), color = MaterialTheme.colorScheme.primary)
                    }
                },
                onClick = {
                    expanded = false
                    onAddNew()
                }
            )
        }
    }
}

@Composable
private fun AddItemDialog(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add new " + title.lowercase()) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(title + " name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs)
            ) {
                Text(
                    selected.mimeType.substringAfterLast('/').uppercase() + " · " +
                        (selected.pageCount ?: 1) +
                        if ((selected.pageCount ?: 1) == 1) " page" else " pages",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text("v${selected.versionIndex ?: 1}" + if (!selected.versionLabel.isNullOrBlank()) ": ${selected.versionLabel}" else "")
                    }
                )
            }
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
            Modifier.fillMaxWidth().animateContentSize().padding(PaperlessDimensions.lg),
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
