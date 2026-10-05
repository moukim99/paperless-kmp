package com.nextstepai.paperless.ui.expiring

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.expiring.components.ExpiringDocumentCard
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpiringScreen(
    uiState: ExpiringUiState,
    onIntent: (ExpiringUiIntent) -> Unit,
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
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = TerracottaPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBackground.copy(alpha = 0.95f))
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
            // ترويسة الشاشة
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(Res.string.expiring_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text(stringResource(Res.string.expiring_subtitle), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }

            // شريط شرائح التصفية (Filter Chips Carousel)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpiryFilter.entries.forEach { filter ->
                        val isSelected = filter == uiState.selectedFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { onIntent(ExpiringUiIntent.SelectFilter(filter)) },
                            label = { Text(filter.label, fontSize = 12.sp) },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TerracottaPrimary,
                                selectedLabelColor = TerracottaOnPrimary,
                                containerColor = SurfaceContainer,
                                labelColor = OnSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = OutlineVariant.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            // لافتة التنبيه ذات الأولوية العاجلة (Warm Amber Alert Banner)
            item {
                Surface(
                    color = TerracottaTertiaryContainer.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TerracottaTertiaryContainer.copy(alpha = 0.35f))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(TerracottaTertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Warning, contentDescription = null, tint = TerracottaOnTertiaryContainer, modifier = Modifier.size(18.dp))
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("IMMEDIATE PRIORITY", color = AmberTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                PulsingDot()
                            }
                            Text(
                                text = "${uiState.urgentCount} contracts expire this week — review now",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurface
                            )
                            Text(
                                text = "Lease and Cloud services mandate renewal notices to avoid operational lapse.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // قائمة البطاقات
            items(uiState.documents, key = { it.id }) { doc ->
                ExpiringDocumentCard(
                    doc = doc,
                    onExtendDate = { onIntent(ExpiringUiIntent.ExtendDate(it)) },
                    onRenewed = { onIntent(ExpiringUiIntent.MarkRenewed(it)) },
                    onView = { onIntent(ExpiringUiIntent.ViewDocument(it)) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(600), repeatMode = RepeatMode.Reverse)
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(TerracottaPrimary.copy(alpha = alpha))
    )
}
