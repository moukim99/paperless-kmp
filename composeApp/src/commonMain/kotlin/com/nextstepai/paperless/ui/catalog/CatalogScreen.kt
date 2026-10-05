package com.nextstepai.paperless.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.nextstepai.paperless.ui.catalog.components.FullWidthTagCard
import com.nextstepai.paperless.ui.catalog.components.SquareTagCard
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    uiState: CatalogUiState,
    onIntent: (CatalogUiIntent) -> Unit,
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
                        Text("DocVault", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TerracottaPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = OnSurfaceVariant)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Tune", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBackground.copy(alpha = 0.95f))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onIntent(CatalogUiIntent.CreateNewTag) },
                icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text(stringResource(Res.string.add_tag), fontWeight = FontWeight.SemiBold) },
                containerColor = TerracottaPrimary,
                contentColor = TerracottaOnPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ترويسة الصفحة
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(Res.string.catalog_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(stringResource(Res.string.catalog_subtitle), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }

            // تبويبات الفهرس الأفقية (Horizontal Category Tabs)
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    CatalogTab.entries.forEach { tab ->
                        val isSelected = tab == uiState.selectedTab
                        val tabTitle = when (tab) {
                            CatalogTab.TAGS -> stringResource(Res.string.catalog_tags)
                            CatalogTab.CORRESPONDENTS -> stringResource(Res.string.catalog_correspondents)
                            CatalogTab.DOCUMENT_TYPES -> stringResource(Res.string.catalog_document_types)
                            CatalogTab.STORAGE_PATHS -> stringResource(Res.string.catalog_storage_paths)
                        }
                        Column(
                            modifier = Modifier
                                .clickable { onIntent(CatalogUiIntent.SelectTab(tab)) }
                                .padding(bottom = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = tabTitle,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TerracottaPrimary else OnSurfaceVariant
                                )
                                if (tab.count != null) {
                                    Surface(
                                        color = SurfaceContainer,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = "${tab.count}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TerracottaPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(modifier = Modifier.width(36.dp).height(2.dp).background(TerracottaPrimary))
                            }
                        }
                    }
                }
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), thickness = 1.dp)
            }

            // شريط البحث المخصص (Search Bar)
            item(span = { GridItemSpan(2) }) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { onIntent(CatalogUiIntent.SearchQueryChanged(it)) },
                    placeholder = { Text(stringResource(Res.string.search_tags), style = MaterialTheme.typography.bodySmall, color = OutlineVariant) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = OutlineVariant) },
                    trailingIcon = { Icon(Icons.Outlined.FilterList, contentDescription = null, tint = OutlineVariant) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedBorderColor = TerracottaPrimary,
                        unfocusedBorderColor = OutlineVariant.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
            }

            // أزرار شرائح الفرز (Sort Chips)
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TagSortFilter.entries.forEach { filter ->
                        val isSelected = filter == uiState.selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onIntent(CatalogUiIntent.SelectFilter(filter)) },
                            label = { Text(filter.label, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SurfaceContainer,
                                selectedLabelColor = TerracottaPrimary,
                                containerColor = SurfaceContainerLowest,
                                labelColor = OnSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) TerracottaPrimary.copy(alpha = 0.3f) else OutlineVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }

            // إحصائيات وعنوان القسم
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(Res.string.active_tags), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                    Text("${uiState.categorizedCount} categorized", style = MaterialTheme.typography.labelSmall, color = OutlineVariant)
                }
            }

            // بطاقات الـ Bento Grid
            items(
                items = uiState.tags,
                key = { it.id },
                span = { tag -> GridItemSpan(if (tag.isSpanFull) 2 else 1) }
            ) { tag ->
                if (tag.isSpanFull) {
                    FullWidthTagCard(
                        tag = tag,
                        icon = if (tag.name.contains("ID", ignoreCase = true)) Icons.Outlined.Badge else Icons.Outlined.Label,
                        onClick = { onIntent(CatalogUiIntent.TagClicked(it)) },
                        onOptionsClick = { onIntent(CatalogUiIntent.TagOptionsClicked(it)) }
                    )
                } else {
                    val bottomIcon = when {
                        tag.name.contains("Contract", ignoreCase = true) -> Icons.Outlined.Description
                        tag.name.contains("Tax", ignoreCase = true) -> Icons.Outlined.CalendarToday
                        tag.name.contains("Health", ignoreCase = true) -> Icons.Outlined.HealthAndSafety
                        else -> Icons.Outlined.ReceiptLong
                    }
                    SquareTagCard(
                        tag = tag,
                        bottomIcon = bottomIcon,
                        onClick = { onIntent(CatalogUiIntent.TagClicked(it)) },
                        onOptionsClick = { onIntent(CatalogUiIntent.TagOptionsClicked(it)) }
                    )
                }
            }

            // بطاقة التلميح الأرشيفية (Smart Auto-Tagging Enabled)
            item(span = { GridItemSpan(2) }) {
                Surface(
                    color = SurfaceContainerLow,
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f))),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Outlined.Lightbulb, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                        Column {
                            Text("Smart Auto-Tagging Enabled", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "New OCR scans matching keywords and VAT numbers will automatically file under their respective tags.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(70.dp)) // مساحة فارغة لضمان عدم حجب المحتوى بالـ FAB وشريط التنقل السفلي
            }
        }
    }
}
