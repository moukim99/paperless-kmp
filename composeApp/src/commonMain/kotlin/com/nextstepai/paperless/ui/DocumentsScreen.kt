package com.nextstepai.paperless.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass
import com.nextstepai.paperless.documents.presentation.*
import com.nextstepai.paperless.platform.DocumentPickerButton
import com.nextstepai.paperless.platform.DocumentScannerButton
import com.nextstepai.paperless.ui.components.*

@OptIn(androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun DocumentsScreen(state: DocumentsUiState, onEvent: (DocumentsUiEvent) -> Unit) {
    val adaptiveInfo = androidx.compose.material3.adaptive.currentWindowAdaptiveInfo()
    val compact = adaptiveInfo.windowSizeClass.windowWidthSizeClass ==
        androidx.window.core.layout.WindowWidthSizeClass.COMPACT

    if (compact && state.selected != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Document") },
                    navigationIcon = {
                        TextButton(onClick = { onEvent(DocumentsUiEvent.ClearSelection) }) {
                            Text("Back")
                        }
                    }
                )
            }
        ) { padding ->
            DocumentDetails(
                state = state,
                selected = state.selected,
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        }
        return
    }

    val scaffoldState = androidx.compose.material3.adaptive.layout.calculateListDetailPaneScaffoldState(
        currentPaneDestination = if (state.selected == null) {
            androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole.List
        } else {
            androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole.Detail
        }
    )

    androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold(
        scaffoldState = scaffoldState,
        modifier = Modifier.fillMaxSize(),
        listPane = {
            androidx.compose.material3.adaptive.layout.AnimatedPane {
                DocumentsList(
                    state = state,
                    onEvent = onEvent,
                    onSelect = { onEvent(DocumentsUiEvent.Select(it)) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        detailPane = {
            androidx.compose.material3.adaptive.layout.AnimatedPane {
                state.selected?.let {
                    DocumentDetails(
                        state = state,
                        selected = it,
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: EmptyState(
                    "Select a document",
                    "Choose a document from the list to view its details."
                )
            }
        }
    )
}

@Composable
private fun DocumentsList(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var addOpen by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Documents") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { addOpen = true }) { Text("+", style = MaterialTheme.typography.headlineSmall) }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(DocumentsUiEvent.SearchChanged(it)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                label = { Text("Search documents") },
                supportingText = { Text("tag: · type: · correspondent: · after: · before: · expiry:") },
                singleLine = true
            )
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            }
            if (state.importing) LinearProgressIndicator(Modifier.fillMaxWidth())
            when {
                state.loading -> LoadingState(Modifier.fillMaxSize())
                state.documents.isEmpty() -> EmptyState(
                    if (state.query.isBlank()) "No documents yet" else "No matching documents",
                    if (state.query.isBlank()) "Add a file or scan a document to build your library." else "Try a different search or filter."
                )
                else -> LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.documents, key = { it.id }) { doc ->
                        DocumentCard(
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
        AlertDialog(
            onDismissRequest = { addOpen = false },
            title = { Text("Add document") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DocumentPickerButton { addOpen = false; onEvent(DocumentsUiEvent.Import(it)) }
                    DocumentScannerButton { addOpen = false; onEvent(DocumentsUiEvent.Import(it)) }
                }
            },
            confirmButton = { TextButton(onClick = { addOpen = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DocumentCard(
    document: DocumentUiModel,
    correspondent: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(document.title.ifBlank { "Untitled document" }, style = MaterialTheme.typography.titleMedium)
            correspondent?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            Spacer(Modifier.height(8.dp))
            Text(
                document.mimeType.substringAfterLast('/').uppercase() + " · " + (document.pageCount ?: 1) + " pages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AssistChip(onClick = {}, enabled = false, label = {
                    Text(document.syncState.lowercase().replaceFirstChar { it.uppercase() })
                })
                document.expiryLabel?.let { ExpiryBadge(document.expiryState, it) }
            }
        }
    }
}

@Composable
private fun DocumentDetails(
    state: DocumentsUiState,
    selected: DocumentUiModel,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier
) {
    var confirmDelete by remember(selected.id) { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current
    val originalExpiry = selected.expiresAtEpochMillis?.let {
        kotlinx.datetime.Instant.fromEpochMilliseconds(it).toString().take(10)
    }.orEmpty()
    val dirty = state.editTitle != selected.title ||
        state.editExpiry != originalExpiry ||
        state.editReminderDays != (selected.reminderDaysBeforeExpiry ?: 30).toString() ||
        state.selectedCorrespondentId != selected.correspondentId ||
        state.selectedDocumentTypeId != selected.documentTypeId

    LazyColumn(
        modifier,
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(selected.title.ifBlank { "Untitled document" }, style = MaterialTheme.typography.headlineSmall)
            Text(
                selected.mimeType.substringAfterLast('/').uppercase() + " · " + (selected.pageCount ?: 1) + " pages",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            selected.expiryLabel?.let { ExpiryBadge(selected.expiryState, it) }

            SectionHeader("Information")
            OutlinedTextField(
                value = state.editTitle,
                onValueChange = { onEvent(DocumentsUiEvent.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true
            )
            CatalogChips("Correspondent", state.selectedCorrespondentId, state.correspondents.map { it.id to it.name }) {
                onEvent(DocumentsUiEvent.CorrespondentChanged(it))
            }
            CatalogChips("Document type", state.selectedDocumentTypeId, state.documentTypes.map { it.id to it.name }) {
                onEvent(DocumentsUiEvent.DocumentTypeChanged(it))
            }

            SectionHeader("Expiry")
            OutlinedTextField(
                value = state.editExpiry,
                onValueChange = { onEvent(DocumentsUiEvent.ExpiryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expiry date (YYYY-MM-DD)") },
                singleLine = true
            )
            OutlinedTextField(
                value = state.editReminderDays,
                onValueChange = { onEvent(DocumentsUiEvent.ReminderDaysChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Reminder days before expiry") },
                singleLine = true
            )

            SectionHeader("Tags")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                state.tags.take(32).forEach { tag ->
                    FilterChip(
                        selected = tag.id in state.selectedTagIds,
                        onClick = { onEvent(DocumentsUiEvent.TagToggled(tag.id)) },
                        label = { Text(tag.name) }
                    )
                }
            }

            SectionHeader("Actions")
            if (dirty) {
                Text("Unsaved changes", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = { onEvent(DocumentsUiEvent.SaveMetadata) }, enabled = !state.saving) {
                    Text(if (state.saving) "Saving…" else "Save changes")
                }
            }
            TextButton(onClick = { onEvent(DocumentsUiEvent.OpenFile) }) { Text("Open document") }
            TextButton(onClick = { confirmDelete = true }) {
                Text("Delete document", color = MaterialTheme.colorScheme.error)
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionHeader("Custom fields")
        }

        items(state.customFields, key = { it.id }) { field ->
            OutlinedTextField(
                value = state.customFieldValues[field.id].orEmpty(),
                onValueChange = { onEvent(DocumentsUiEvent.CustomFieldChanged(field.id, it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(field.name) },
                minLines = if (field.type.name == "LONG_TEXT") 3 else 1
            )
        }

        item {
            SectionHeader("Extracted text")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { clipboard.setText(AnnotatedString(selected.content)) }) { Text("Copy") }
                TextButton(onClick = {
                    onEvent(DocumentsUiEvent.SearchChanged(selected.content.take(120)))
                }) { Text("Search") }
            }
            Surface(
                tonalElevation = 1.dp,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selected.content.ifBlank { "No OCR text available." }, Modifier.padding(16.dp))
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete document?") },
            text = { Text("This removes the document from the local library.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onEvent(DocumentsUiEvent.Delete(selected.id))
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CatalogChips(
    title: String,
    selected: Long?,
    items: List<Pair<Long, String>>,
    onSelect: (Long?) -> Unit
) {
    SectionHeader(title)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(selected == null, { onSelect(null) }, label = { Text("None") })
        items.take(24).forEach { (id, name) ->
            FilterChip(selected == id, { onSelect(id) }, label = { Text(name) })
        }
    }
}
