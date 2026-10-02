package com.nextstepai.paperless.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstepai.paperless.documents.presentation.DocumentUiModel
import com.nextstepai.paperless.documents.presentation.DocumentsUiEvent
import com.nextstepai.paperless.documents.presentation.DocumentsUiState
import com.nextstepai.paperless.platform.DocumentPickerButton
import com.nextstepai.paperless.platform.DocumentScannerButton

private val MobileBreakpoint = 700.dp

@Composable
fun DocumentsScreen(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isMobile = maxWidth < MobileBreakpoint
        var showDetails by remember { mutableStateOf(false) }

        LaunchedEffect(state.selectedId) {
            if (state.selectedId == null) showDetails = false
        }

        if (isMobile) {
            if (showDetails && state.selected != null) {
                DocumentDetailsScreen(
                    state = state,
                    selected = state.selected,
                    onEvent = onEvent,
                    onBack = { showDetails = false }
                )
            } else {
                DocumentsList(
                    state = state,
                    onEvent = onEvent,
                    onSelect = {
                        onEvent(DocumentsUiEvent.Select(it))
                        showDetails = true
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Row(
                Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DocumentsList(
                    state = state,
                    onEvent = onEvent,
                    onSelect = { onEvent(DocumentsUiEvent.Select(it)) },
                    modifier = Modifier.weight(0.42f).fillMaxHeight()
                )
                VerticalDivider()
                Box(
                    Modifier.weight(0.58f).fillMaxHeight()
                ) {
                    state.selected?.let {
                        DocumentDetails(
                            state = state,
                            selected = it,
                            onEvent = onEvent,
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: EmptySelection()
                }
            }
        }
    }
}

@Composable
private fun DocumentsList(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    onSelect: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        TopAppBar(title = { Text("Documents") })
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(DocumentsUiEvent.SearchChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search documents") },
                singleLine = true
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DocumentPickerButton { onEvent(DocumentsUiEvent.Import(it)) }
                DocumentScannerButton { onEvent(DocumentsUiEvent.Import(it)) }
            }
            if (state.importing) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.documents.isEmpty() -> EmptyDocumentsState()
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                items(state.documents, key = { it.id }) { doc ->
                    DocumentListItem(
                        document = doc,
                        selected = doc.id == state.selectedId,
                        onClick = { onSelect(doc.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentListItem(
    document: DocumentUiModel,
    selected: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = if (selected) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        } else CardDefaults.elevatedCardColors()
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Text(
                document.title.ifBlank { "Untitled document" },
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                document.filename ?: document.mimeType,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(document.syncState, style = MaterialTheme.typography.labelSmall)
                document.expiresAtEpochMillis?.let {
                    Text("Expires: ${formatEpochDate(it)}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun DocumentDetailsScreen(
    state: DocumentsUiState,
    selected: DocumentUiModel,
    onEvent: (DocumentsUiEvent) -> Unit,
    onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Document details") },
            navigationIcon = {
                TextButton(onClick = onBack) { Text("Back") }
            }
        )
        DocumentDetails(
            state = state,
            selected = selected,
            onEvent = onEvent,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun DocumentDetails(
    state: DocumentsUiState,
    selected: DocumentUiModel,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp
        )
    ) {
        item {
            OutlinedTextField(
                value = state.editTitle,
                onValueChange = { onEvent(DocumentsUiEvent.TitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.editExpiry,
                onValueChange = { onEvent(DocumentsUiEvent.ExpiryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Expiry date (YYYY-MM-DD)") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = state.editReminderDays,
                onValueChange = { onEvent(DocumentsUiEvent.ReminderDaysChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Reminder days before expiry") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Text("Correspondent", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = state.selectedCorrespondentId == null,
                    onClick = { onEvent(DocumentsUiEvent.CorrespondentChanged(null)) },
                    label = { Text("None") }
                )
                state.correspondents.take(16).forEach { item ->
                    FilterChip(
                        selected = state.selectedCorrespondentId == item.id,
                        onClick = { onEvent(DocumentsUiEvent.CorrespondentChanged(item.id)) },
                        label = { Text(item.name) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Document type", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = state.selectedDocumentTypeId == null,
                    onClick = { onEvent(DocumentsUiEvent.DocumentTypeChanged(null)) },
                    label = { Text("None") }
                )
                state.documentTypes.take(16).forEach { item ->
                    FilterChip(
                        selected = state.selectedDocumentTypeId == item.id,
                        onClick = { onEvent(DocumentsUiEvent.DocumentTypeChanged(item.id)) },
                        label = { Text(item.name) }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text("Tags", style = MaterialTheme.typography.titleSmall)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.tags.take(24).forEach { tag ->
                    FilterChip(
                        selected = tag.id in state.selectedTagIds,
                        onClick = { onEvent(DocumentsUiEvent.TagToggled(tag.id)) },
                        label = { Text(tag.name) }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { onEvent(DocumentsUiEvent.SaveMetadata) },
                    enabled = !state.saving
                ) {
                    Text(if (state.saving) "Saving…" else "Save")
                }
                OutlinedButton(onClick = { onEvent(DocumentsUiEvent.OpenFile) }) {
                    Text("Open file")
                }
                OutlinedButton(onClick = { onEvent(DocumentsUiEvent.Delete(selected.id)) }) {
                    Text("Delete")
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("Custom fields", style = MaterialTheme.typography.titleMedium)
        }

        items(state.customFields, key = { it.id }) { field ->
            OutlinedTextField(
                value = state.customFieldValues[field.id].orEmpty(),
                onValueChange = {
                    onEvent(DocumentsUiEvent.CustomFieldChanged(field.id, it))
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("${field.name} (${field.type.name.lowercase()})") },
                minLines = if (field.type.name == "LONG_TEXT") 3 else 1
            )
        }

        item {
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            Text("OCR / extracted text", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Text(
                    selected.content.ifBlank { "No OCR text available." },
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyDocumentsState() {
    Box(
        Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No documents", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                "Import a file or scan a document to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptySelection() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Select a document", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                "Choose a document from the list to edit its metadata.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatEpochDate(epochMillis: Long): String =
    kotlinx.datetime.Instant.fromEpochMilliseconds(epochMillis).toString().take(10)
