package com.nextstepai.paperless.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.inbox.components.InboxDocumentCard
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    uiState: InboxUiState,
    onIntent: (InboxUiIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = TerracottaPrimary)
                        Text(
                            text = "DocVault",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceBackground.copy(alpha = 0.95f)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ترويسة الصفحة
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.inbox_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = stringResource(Res.string.inbox_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Tune, contentDescription = "Filter", tint = OnSurfaceVariant)
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.Checklist, contentDescription = "Select Multiple", tint = OnSurfaceVariant)
                        }
                    }
                }
            }

            // شريط الإجراءات التكتيكية المجمعة (Batch Action Strip)
            item {
                Surface(
                    color = SurfaceContainerLow,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onIntent(InboxUiIntent.SelectAll) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceContainer),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.SelectAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Select All", fontSize = 12.sp, color = OnSurfaceVariant)
                            }

                            FilledTonalButton(
                                onClick = { onIntent(InboxUiIntent.MarkReviewed) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = SurfaceContainer),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Outlined.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp), tint = OnSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark Reviewed", fontSize = 12.sp, color = OnSurfaceVariant)
                            }
                        }

                        Button(
                            onClick = { onIntent(InboxUiIntent.ProcessQueue) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimaryContainer),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = TerracottaOnPrimaryContainer)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Process Queue", fontSize = 12.sp, color = TerracottaOnPrimaryContainer)
                        }
                    }
                }
            }

            // قائمة بطاقات المستندات
            items(uiState.documents, key = { it.id }) { doc ->
                InboxDocumentCard(
                    document = doc,
                    onAssignMetadata = { onIntent(InboxUiIntent.AssignMetadata(it)) },
                    onDelete = { onIntent(InboxUiIntent.DeleteDocument(it)) },
                    onClick = { onIntent(InboxUiIntent.OpenDocumentDetail(it)) }
                )
            }

            // شريط الإشعار بالأرشفة التلقائية (Auto-ingestion Banner)
            item {
                Surface(
                    color = SurfaceContainerLow.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TerracottaPrimaryContainer.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Auto-ingestion active", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                            Text(
                                "Incoming scanned documents from your sync folder are automatically staged here for classification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
