package com.nextstepai.paperless.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.documents.presentation.DocumentsUiState
import com.nextstepai.paperless.documents.presentation.DocumentsUiEvent
import com.nextstepai.paperless.documents.presentation.ExpiryState
import com.nextstepai.paperless.platform.rememberDocumentPickerLauncher
import com.nextstepai.paperless.platform.rememberDocumentScannerLauncher
import com.nextstepai.paperless.ui.documents.DocumentFilter
import com.nextstepai.paperless.ui.documents.DocumentListItem
import com.nextstepai.paperless.ui.documents.components.DocumentListCard
import com.nextstepai.paperless.ui.documents.add.AddDocumentBottomSheet
import com.nextstepai.paperless.ui.documents.add.AddDocumentIntent
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    state: DocumentsUiState,
    onEvent: (DocumentsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var selectedFilterState by remember { mutableStateOf(DocumentFilter.ALL) }

    val launchPicker = rememberDocumentPickerLauncher { input ->
        onEvent(DocumentsUiEvent.Import(input))
    }
    val launchScanner = rememberDocumentScannerLauncher { input ->
        onEvent(DocumentsUiEvent.Import(input))
    }

    val filteredDocs = remember(state.documents, selectedFilterState) {
        state.documents.filter { doc ->
            when (selectedFilterState) {
                DocumentFilter.ALL -> true
                DocumentFilter.EXPIRING -> doc.expiryState != ExpiryState.None
                DocumentFilter.RECENT -> true
                DocumentFilter.INVOICES -> doc.title.contains("invoice", ignoreCase = true) || doc.content.contains("invoice", ignoreCase = true)
            }
        }
    }

    val pendingSyncCount = remember(state.documents) {
        state.documents.count { it.syncState.contains("PENDING", ignoreCase = true) }
    }

    val documentListItems = remember(filteredDocs) {
        filteredDocs.map { doc ->
            val isPending = doc.syncState.contains("PENDING", ignoreCase = true)
            DocumentListItem(
                id = doc.id.toString(),
                title = doc.title,
                extension = doc.filename?.substringAfterLast('.', "JPEG")?.uppercase() ?: "JPEG",
                pageCount = doc.pageCount ?: 1,
                isPendingUpload = isPending,
                ocrStatusMessage = if (isPending) "Queued for cloud OCR indexing" else null
            )
        }
    }

    val uiState = remember(selectedFilterState, state.query, state.documents.size, pendingSyncCount, documentListItems) {
        com.nextstepai.paperless.ui.documents.DocumentsUiState(
            selectedFilter = selectedFilterState,
            searchQuery = state.query,
            totalCount = state.documents.size,
            pendingSyncCount = pendingSyncCount,
            documents = documentListItems
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerracottaPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "DocVault",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                color = OnSurfaceVariant
                            )
                            Surface(
                                color = TerracottaPrimaryContainer.copy(alpha = 0.15f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Archive",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(DocumentsUiEvent.Refresh) }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBackground.copy(alpha = 0.95f))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text(stringResource(Res.string.add_document), fontWeight = FontWeight.SemiBold) },
                containerColor = TerracottaPrimary,
                contentColor = TerracottaOnPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // العنوان وعدادات المزامنة
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = stringResource(Res.string.documents_title),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Medium,
                        color = OnSurface
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${uiState.totalCount} in your library",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Text("•", color = OutlineVariant, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            PulsingSyncDot()
                            Text(
                                text = "${uiState.pendingSyncCount} sync pending",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = TerracottaPrimary
                            )
                        }
                    }
                }
            }

            // مؤشر عملية الاستيراد الحالية (Importing Progress)
            if (state.importing) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = TerracottaPrimaryContainer.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = TerracottaPrimary)
                            Column {
                                Text(stringResource(Res.string.importing_indexing), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text(stringResource(Res.string.importing_desc), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // عرض الخطأ إن وجد
            val errorMsg = state.error
            if (errorMsg != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // شريط البحث الذكي مع أيقونة الـ Barcode Scanner وتلميحات الاستعلام
            item {
                Column {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { onEvent(DocumentsUiEvent.SearchChanged(it)) },
                        placeholder = { Text(stringResource(Res.string.documents_search_placeholder), style = MaterialTheme.typography.bodyMedium, color = OutlineVariant) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = OutlineVariant) },
                        trailingIcon = {
                            IconButton(onClick = { launchScanner() }) {
                                Icon(Icons.Outlined.QrCodeScanner, contentDescription = "Scan Barcode", tint = OutlineVariant)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLowest,
                            unfocusedContainerColor = SurfaceContainerLowest,
                            focusedBorderColor = TerracottaPrimary,
                            unfocusedBorderColor = OutlineVariant.copy(alpha = 0.6f)
                        ),
                        singleLine = true
                    )

                    // سطر التلميحات البحثية (Supports: tag:, type:, ...)
                    Text(
                        text = "Supports: tag:, type:, correspondent:, after:, expiry:",
                        fontSize = 11.sp,
                        color = OnSurfaceVariant.copy(alpha = 0.75f),
                        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                    )
                }
            }

            // شريط الفلاتر الحلقية (Filter Chips)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DocumentFilter.entries.forEach { filter ->
                        val isSelected = filter == uiState.selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterState = filter },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(filter.label, fontSize = 12.sp)
                                    if (filter.count != null) {
                                        Surface(
                                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else SurfaceContainer,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "${filter.count}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaPrimary,
                                selectedLabelColor = TerracottaOnPrimary,
                                containerColor = SurfaceContainerLowest,
                                labelColor = OnSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color.Transparent else OutlineVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            // قائمة المستندات
            items(uiState.documents, key = { it.id }) { doc ->
                DocumentListCard(
                    doc = doc,
                    onClick = { idStr ->
                        idStr.toLongOrNull()?.let { parsedId -> onEvent(DocumentsUiEvent.Select(parsedId)) }
                    },
                    onOptionsClick = { idStr ->
                        idStr.toLongOrNull()?.let { parsedId -> onEvent(DocumentsUiEvent.Select(parsedId)) }
                    }
                )
            }

            // تذييل نهاية الأرشفة الأنيق (End of indexed documents)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(TerracottaPrimaryContainer.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "End of indexed documents for August 2026",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = OnSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddDocumentBottomSheet(
            onIntent = { intent ->
                when (intent) {
                    is AddDocumentIntent.Dismiss -> showAddSheet = false
                    is AddDocumentIntent.ImportFileClicked -> {
                        showAddSheet = false
                        launchPicker()
                    }
                    is AddDocumentIntent.ScanDocumentClicked -> {
                        showAddSheet = false
                        launchScanner()
                    }
                    is AddDocumentIntent.HelpGuideClicked -> {
                        showAddSheet = false
                    }
                }
            }
        )
    }
}

@Composable
private fun PulsingSyncDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(700), repeatMode = RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(TerracottaPrimary.copy(alpha = alpha))
    )
}
