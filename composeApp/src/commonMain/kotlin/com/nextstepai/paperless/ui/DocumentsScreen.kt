package com.nextstepai.paperless.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstepai.paperless.documents.presentation.*
import com.nextstepai.paperless.platform.DocumentPickerButton
import com.nextstepai.paperless.platform.DocumentScannerButton

@Composable
fun DocumentsScreen(state: DocumentsUiState, onEvent: (DocumentsUiEvent) -> Unit) {
    Row(Modifier.fillMaxSize().padding(16.dp)) {
        Column(Modifier.weight(0.40f).fillMaxHeight()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = state.query, onValueChange = { onEvent(DocumentsUiEvent.SearchChanged(it)) }, modifier = Modifier.weight(1f), label = { Text("Search documents") }, singleLine = true)
                DocumentPickerButton { onEvent(DocumentsUiEvent.Import(it)) }
                DocumentScannerButton { onEvent(DocumentsUiEvent.Import(it)) }
            }
            if (state.importing) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp)) }
            Spacer(Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.documents, key = { it.id }) { doc ->
                    ElevatedCard(onClick = { onEvent(DocumentsUiEvent.Select(doc.id)) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(doc.title, style = MaterialTheme.typography.titleMedium)
                            Text(doc.filename ?: doc.mimeType, style = MaterialTheme.typography.bodySmall)
                            Text(doc.syncState, style = MaterialTheme.typography.labelSmall)
                            doc.expiresAtEpochMillis?.let { Text("Expiry: $it", style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.width(16.dp)); VerticalDivider(); Spacer(Modifier.width(16.dp))
        Box(Modifier.weight(0.60f).fillMaxHeight()) {
            val selected = state.selected
            if (selected == null) Text("Select a document", style = MaterialTheme.typography.headlineSmall)
            else LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Document details", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(state.editTitle, { onEvent(DocumentsUiEvent.TitleChanged(it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(state.editExpiry, { onEvent(DocumentsUiEvent.ExpiryChanged(it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Expiry date (YYYY-MM-DD)") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(state.editReminderDays, { onEvent(DocumentsUiEvent.ReminderDaysChanged(it)) }, modifier = Modifier.fillMaxWidth(), label = { Text("Reminder days before expiry") }, singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    Text("Correspondent", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = state.selectedCorrespondentId == null, onClick = { onEvent(DocumentsUiEvent.CorrespondentChanged(null)) }, label = { Text("None") })
                        state.correspondents.take(8).forEach { item -> FilterChip(selected = state.selectedCorrespondentId == item.id, onClick = { onEvent(DocumentsUiEvent.CorrespondentChanged(item.id)) }, label = { Text(item.name) }) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Document type", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = state.selectedDocumentTypeId == null, onClick = { onEvent(DocumentsUiEvent.DocumentTypeChanged(null)) }, label = { Text("None") })
                        state.documentTypes.take(8).forEach { item -> FilterChip(selected = state.selectedDocumentTypeId == item.id, onClick = { onEvent(DocumentsUiEvent.DocumentTypeChanged(item.id)) }, label = { Text(item.name) }) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Tags", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        state.tags.take(16).forEach { tag -> FilterChip(selected = tag.id in state.selectedTagIds, onClick = { onEvent(DocumentsUiEvent.TagToggled(tag.id)) }, label = { Text(tag.name) }) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onEvent(DocumentsUiEvent.SaveMetadata) }, enabled = !state.saving) { Text(if (state.saving) "Saving…" else "Save") }
                        OutlinedButton(onClick = { onEvent(DocumentsUiEvent.OpenFile) }) { Text("Open file") }
                        OutlinedButton(onClick = { onEvent(DocumentsUiEvent.Delete(selected.id)) }) { Text("Delete") }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Custom fields", style = MaterialTheme.typography.titleMedium)
                }
                items(state.customFields, key = { it.id }) { field ->
                    OutlinedTextField(
                        value = state.customFieldValues[field.id].orEmpty(),
                        onValueChange = { onEvent(DocumentsUiEvent.CustomFieldChanged(field.id, it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("${field.name} (${field.type.name.lowercase()})") },
                        minLines = if (field.type.name == "LONG_TEXT") 3 else 1
                    )
                }
                item {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("OCR / extracted text", style = MaterialTheme.typography.titleMedium)
                    Surface(tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) { Text(selected.content.ifBlank { "No OCR text available." }, modifier = Modifier.padding(12.dp)) }
                }
            }
        }
    }
}
