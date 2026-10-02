package com.nextstepai.paperless.ui.components

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nextstepai.paperless.domain.model.CustomField
import com.nextstepai.paperless.domain.model.CustomFieldType
import com.nextstepai.paperless.ui.theme.PaperlessDimensions
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomFieldInput(
    field: CustomField,
    value: String,
    onValueChange: (String) -> Unit,
    availableDocuments: List<Pair<Long, String>> = emptyList(),
    modifier: Modifier = Modifier
) {
    when (field.type) {
        CustomFieldType.BOOLEAN -> {
            Row(
                modifier = modifier.fillMaxWidth().padding(vertical = PaperlessDimensions.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(field.name, style = MaterialTheme.typography.bodyLarge)
                    Text("Boolean", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = value.toBooleanStrictOrNull() ?: false,
                    onCheckedChange = { onValueChange(it.toString()) }
                )
            }
        }

        CustomFieldType.DATE -> {
            var showDatePicker by remember { mutableStateOf(false) }
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = modifier.fillMaxWidth().clickable { showDatePicker = true },
                enabled = false,
                readOnly = true,
                label = { Text(field.name) },
                placeholder = { Text("YYYY-MM-DD") },
                supportingText = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Outlined.CalendarMonth, contentDescription = "Select date")
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary
                )
            )

            if (showDatePicker) {
                CustomFieldDatePickerDialog(
                    title = field.name,
                    initialDateString = value,
                    onDateSelected = onValueChange,
                    onDismiss = { showDatePicker = false }
                )
            }
        }

        CustomFieldType.INTEGER -> {
            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    if (input.isBlank() || input == "-" || input.toLongOrNull() != null) {
                        onValueChange(input)
                    }
                },
                modifier = modifier.fillMaxWidth(),
                label = { Text(field.name) },
                supportingText = { Text("Integer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        CustomFieldType.FLOAT -> {
            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    if (input.isBlank() || input == "-" || input == "." || input == "-." || input.toDoubleOrNull() != null) {
                        onValueChange(input)
                    }
                },
                modifier = modifier.fillMaxWidth(),
                label = { Text(field.name) },
                supportingText = { Text("Number / Float") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        CustomFieldType.MONETARY -> {
            OutlinedTextField(
                value = value,
                onValueChange = { input ->
                    if (input.isBlank() || input.toDoubleOrNull() != null) {
                        onValueChange(input)
                    }
                },
                modifier = modifier.fillMaxWidth(),
                label = { Text(field.name) },
                supportingText = { Text("Monetary amount") },
                leadingIcon = {
                    Text(
                        "$",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = PaperlessDimensions.sm)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        CustomFieldType.SELECT -> {
            val options = remember(field.extraDataJson) {
                parseSelectOptions(field.extraDataJson)
            }
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                        Text(field.name, style = MaterialTheme.typography.labelMedium)
                        Text(
                            value.ifBlank { "Select option" },
                            color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
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
                            onValueChange("")
                        }
                    )
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                expanded = false
                                onValueChange(option)
                            }
                        )
                    }
                }
            }
        }

        CustomFieldType.URL -> {
            val uriHandler = LocalUriHandler.current
            val isValidUrl = remember(value) {
                value.isBlank() || value.startsWith("http://", ignoreCase = true) || value.startsWith("https://", ignoreCase = true)
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier.fillMaxWidth(),
                label = { Text(field.name) },
                placeholder = { Text("https://example.com") },
                supportingText = {
                    if (!isValidUrl) {
                        Text("URL should start with http:// or https://", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Web link (URL)")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                trailingIcon = {
                    if (value.isNotBlank() && isValidUrl) {
                        IconButton(onClick = { runCatching { uriHandler.openUri(value) } }) {
                            Icon(Icons.Outlined.OpenInNew, contentDescription = "Open link")
                        }
                    }
                },
                isError = !isValidUrl
            )
        }

        CustomFieldType.LONG_TEXT -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier.fillMaxWidth(),
                label = { Text(field.name) },
                supportingText = { Text("Long text") },
                minLines = 4,
                maxLines = 8
            )
        }

        CustomFieldType.DOCUMENT_LINK -> {
            var showPicker by remember { mutableStateOf(false) }
            val linkedIds = remember(value) { parseDocumentIds(value) }
            val linkedDocs = remember(linkedIds, availableDocuments) {
                linkedIds.mapNotNull { id ->
                    availableDocuments.firstOrNull { it.first == id } ?: (id to "Document #$id")
                }
            }

            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs)) {
                Text(field.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(linkedDocs) { (docId, title) ->
                        AssistChip(
                            onClick = {},
                            label = { Text(title, maxLines = 1) },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        val newIds = linkedIds.filterNot { it == docId }
                                        onValueChange(serializeDocumentIds(newIds))
                                    },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(Icons.Outlined.Close, contentDescription = "Remove document link")
                                }
                            }
                        )
                    }
                    item {
                        OutlinedButton(
                            onClick = { showPicker = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Link Document", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                if (linkedDocs.isEmpty()) {
                    Text("No linked documents", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (showPicker) {
                DocumentLinkPickerDialog(
                    title = "Link to " + field.name,
                    availableDocuments = availableDocuments.filterNot { it.first in linkedIds },
                    onDocumentSelected = { selectedDocId ->
                        val newIds = linkedIds + selectedDocId
                        onValueChange(serializeDocumentIds(newIds))
                    },
                    onDismiss = { showPicker = false }
                )
            }
        }

        else -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier.fillMaxWidth(),
                label = { Text(field.name) },
                supportingText = { Text(field.type.name.lowercase().replace('_', ' ')) },
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentLinkPickerDialog(
    title: String,
    availableDocuments: List<Pair<Long, String>>,
    onDocumentSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredDocs = remember(availableDocuments, searchQuery) {
        if (searchQuery.isBlank()) availableDocuments
        else availableDocuments.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().height(300.dp),
                verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.sm)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search documents") },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    singleLine = true
                )
                if (filteredDocs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No available documents to link", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs)) {
                        items(filteredDocs) { (id, title) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onDocumentSelected(id)
                                        onDismiss()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(12.dp))
                                Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomFieldDatePickerDialog(
    title: String,
    initialDateString: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(initialDateString) {
        initialDateString.trim().takeIf { it.isNotBlank() }?.let {
            runCatching {
                LocalDate.parse(it).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            }.getOrNull()
        } ?: Clock.System.now().toEpochMilliseconds()
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

private fun parseSelectOptions(rawJson: String?): List<String> {
    if (rawJson.isNullOrBlank()) return emptyList()
    val trimmed = rawJson.trim()
    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
        return trimmed.removePrefix("[").removeSuffix("]")
            .split(",")
            .map { it.trim().trim('"', '\'') }
            .filter { it.isNotBlank() }
    }
    return trimmed.split(",").map { it.trim() }.filter { it.isNotBlank() }
}

private fun parseDocumentIds(raw: String): List<Long> {
    if (raw.isBlank()) return emptyList()
    val clean = raw.trim().removePrefix("[").removeSuffix("]")
    return clean.split(",").mapNotNull { it.trim().trim('"', '\'').toLongOrNull() }
}

private fun serializeDocumentIds(ids: List<Long>): String {
    if (ids.isEmpty()) return ""
    return "[${ids.joinToString(",")}]"
}
