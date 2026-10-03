package com.nextstepai.paperless.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nextstepai.paperless.domain.model.Correspondent
import com.nextstepai.paperless.domain.model.DocumentType
import com.nextstepai.paperless.domain.model.StoragePath
import com.nextstepai.paperless.domain.model.Tag
import com.nextstepai.paperless.ui.theme.PaperlessDimensions

private val presetColors = listOf(
    "#a6cee3", "#1f78b4", "#b2df8a", "#33a02c",
    "#fb9a99", "#e31a1c", "#fdbf6f", "#ff7f00",
    "#cab2d6", "#6a3d9a", "#ffff99", "#b15928"
)

val matchingAlgorithms = mapOf(
    1 to "Any word",
    2 to "All words",
    3 to "Exact match",
    4 to "Regular expression",
    5 to "Fuzzy match",
    6 to "Auto"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagEditDialog(
    tag: Tag?,
    availableTags: List<Tag>,
    onSave: (Tag) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(tag) { mutableStateOf(tag?.name.orEmpty()) }
    var color by remember(tag) { mutableStateOf(tag?.color.orEmpty().ifBlank { "#a6cee3" }) }
    var isInbox by remember(tag) { mutableStateOf(tag?.isInbox ?: false) }
    var parentId by remember(tag) { mutableStateOf(tag?.parentId) }
    var match by remember(tag) { mutableStateOf(tag?.match.orEmpty()) }
    var algorithm by remember(tag) { mutableStateOf(tag?.matchingAlgorithm ?: 1) }
    var insensitive by remember(tag) { mutableStateOf(tag?.insensitive ?: true) }

    var showAutoMatching by remember { mutableStateOf(match.isNotBlank()) }
    var parentDropdownExpanded by remember { mutableStateOf(false) }
    var algorithmDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (tag == null) "Add Tag" else "Edit Tag") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.md)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tag name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs)) {
                    Text("Color", style = MaterialTheme.typography.labelMedium)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(presetColors) { hex ->
                            val parseColor = runCatching { parseHexColor(hex) }.getOrDefault(Color.Gray)
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parseColor)
                                    .border(
                                        width = if (color.equals(hex, ignoreCase = true)) 3.dp else 1.dp,
                                        color = if (color.equals(hex, ignoreCase = true)) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { color = hex }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Hex color code") },
                        leadingIcon = { Icon(Icons.Outlined.ColorLens, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Inbox tag", style = MaterialTheme.typography.bodyMedium)
                        Text("New documents are assigned this tag automatically", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isInbox, onCheckedChange = { isInbox = it })
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    val parentName = availableTags.firstOrNull { it.id == parentId }?.name ?: "None"
                    OutlinedButton(
                        onClick = { parentDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                            Text("Parent tag", style = MaterialTheme.typography.labelMedium)
                            Text(parentName)
                        }
                    }
                    DropdownMenu(
                        expanded = parentDropdownExpanded,
                        onDismissRequest = { parentDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None") },
                            onClick = {
                                parentId = null
                                parentDropdownExpanded = false
                            }
                        )
                        availableTags.filterNot { it.id == tag?.id }.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    parentId = item.id
                                    parentDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showAutoMatching = !showAutoMatching },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-matching rules", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showAutoMatching = !showAutoMatching }) {
                        Icon(
                            if (showAutoMatching) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                if (showAutoMatching) {
                    OutlinedTextField(
                        value = match,
                        onValueChange = { match = it },
                        label = { Text("Matching pattern") },
                        placeholder = { Text("e.g. Invoice|Bill") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { algorithmDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text("Matching algorithm", style = MaterialTheme.typography.labelMedium)
                                Text(matchingAlgorithms[algorithm] ?: "Any word")
                            }
                        }
                        DropdownMenu(
                            expanded = algorithmDropdownExpanded,
                            onDismissRequest = { algorithmDropdownExpanded = false }
                        ) {
                            matchingAlgorithms.forEach { (algoKey, algoLabel) ->
                                DropdownMenuItem(
                                    text = { Text(algoLabel) },
                                    onClick = {
                                        algorithm = algoKey
                                        algorithmDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ignore case (insensitive)", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = insensitive, onCheckedChange = { insensitive = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Tag(
                                id = tag?.id ?: 0,
                                remoteId = tag?.remoteId,
                                name = name.trim(),
                                color = color.trim().ifBlank { "#a6cee3" },
                                parentId = parentId,
                                isInbox = isInbox,
                                match = match.trim(),
                                matchingAlgorithm = algorithm,
                                insensitive = insensitive
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationEditDialog(
    title: String,
    initialName: String,
    initialMatch: String,
    initialAlgorithm: Int,
    initialInsensitive: Boolean,
    onSave: (name: String, match: String, algorithm: Int, insensitive: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var match by remember { mutableStateOf(initialMatch) }
    var algorithm by remember { mutableStateOf(initialAlgorithm) }
    var insensitive by remember { mutableStateOf(initialInsensitive) }

    var showAutoMatching by remember { mutableStateOf(match.isNotBlank()) }
    var algorithmDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialName.isBlank()) "Add $title" else "Edit $title") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.md)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("$title name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showAutoMatching = !showAutoMatching },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-matching rules", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showAutoMatching = !showAutoMatching }) {
                        Icon(
                            if (showAutoMatching) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                if (showAutoMatching) {
                    OutlinedTextField(
                        value = match,
                        onValueChange = { match = it },
                        label = { Text("Matching pattern") },
                        placeholder = { Text("e.g. Telecom|Vodafone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { algorithmDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text("Matching algorithm", style = MaterialTheme.typography.labelMedium)
                                Text(matchingAlgorithms[algorithm] ?: "Any word")
                            }
                        }
                        DropdownMenu(
                            expanded = algorithmDropdownExpanded,
                            onDismissRequest = { algorithmDropdownExpanded = false }
                        ) {
                            matchingAlgorithms.forEach { (algoKey, algoLabel) ->
                                DropdownMenuItem(
                                    text = { Text(algoLabel) },
                                    onClick = {
                                        algorithm = algoKey
                                        algorithmDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ignore case (insensitive)", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = insensitive, onCheckedChange = { insensitive = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), match.trim(), algorithm, insensitive)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoragePathEditDialog(
    storagePath: StoragePath?,
    onSave: (StoragePath) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(storagePath) { mutableStateOf(storagePath?.name.orEmpty()) }
    var path by remember(storagePath) { mutableStateOf(storagePath?.path.orEmpty()) }
    var match by remember(storagePath) { mutableStateOf(storagePath?.match.orEmpty()) }
    var algorithm by remember(storagePath) { mutableStateOf(storagePath?.matchingAlgorithm ?: 1) }
    var insensitive by remember(storagePath) { mutableStateOf(storagePath?.insensitive ?: true) }

    var showAutoMatching by remember { mutableStateOf(match.isNotBlank()) }
    var algorithmDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (storagePath == null) "Add Storage Path" else "Edit Storage Path") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.md)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("Path template") },
                    placeholder = { Text("{created_year}/{correspondent}/{title}") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("Format: {created_year}/{correspondent}/{title}") }
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showAutoMatching = !showAutoMatching },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Auto-matching rules", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { showAutoMatching = !showAutoMatching }) {
                        Icon(
                            if (showAutoMatching) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = null
                        )
                    }
                }

                if (showAutoMatching) {
                    OutlinedTextField(
                        value = match,
                        onValueChange = { match = it },
                        label = { Text("Matching pattern") },
                        placeholder = { Text("e.g. Invoices|Bills") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { algorithmDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
                                Text("Matching algorithm", style = MaterialTheme.typography.labelMedium)
                                Text(matchingAlgorithms[algorithm] ?: "Any word")
                            }
                        }
                        DropdownMenu(
                            expanded = algorithmDropdownExpanded,
                            onDismissRequest = { algorithmDropdownExpanded = false }
                        ) {
                            matchingAlgorithms.forEach { (algoKey, algoLabel) ->
                                DropdownMenuItem(
                                    text = { Text(algoLabel) },
                                    onClick = {
                                        algorithm = algoKey
                                        algorithmDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Ignore case (insensitive)", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = insensitive, onCheckedChange = { insensitive = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            StoragePath(
                                id = storagePath?.id ?: 0,
                                remoteId = storagePath?.remoteId,
                                name = name.trim(),
                                path = path.trim(),
                                match = match.trim(),
                                matchingAlgorithm = algorithm,
                                insensitive = insensitive
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CatalogDeleteDialog(
    itemTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.DeleteOutline, contentDescription = null) },
        title = { Text("Delete $itemTitle?") },
        text = { Text("This will remove '$itemTitle' from your catalog.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
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
