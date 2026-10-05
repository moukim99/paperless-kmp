package com.nextstepai.paperless.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.settings.components.AutoSyncCard
import com.nextstepai.paperless.ui.settings.components.CloudflareServerCard
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onIntent: (SettingsUiIntent) -> Unit,
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
            // العنوان الرئيسي
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                    Text("Preferences, remote Cloudflare synchronization & archival storage.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }

            // بطاقة خادم Cloudflare
            item {
                CloudflareServerCard(
                    uiState = uiState,
                    onUrlChange = { onIntent(SettingsUiIntent.UpdateServerUrl(it)) },
                    onTokenChange = { onIntent(SettingsUiIntent.UpdateAuthToken(it)) },
                    onToggleVisibility = { onIntent(SettingsUiIntent.ToggleTokenVisibility) },
                    onTestConnection = { onIntent(SettingsUiIntent.TestConnection) },
                    onSave = { onIntent(SettingsUiIntent.SaveSettings) }
                )
            }

            // بطاقة سلوك المزامنة
            item {
                AutoSyncCard(
                    syncOnWifi = uiState.syncOnWifiOnly,
                    selectedInterval = uiState.syncInterval,
                    onToggleWifi = { onIntent(SettingsUiIntent.ToggleSyncOnWifi(it)) },
                    onSelectInterval = { onIntent(SettingsUiIntent.SelectSyncInterval(it)) }
                )
            }

            // بطاقة اختيار اللغة
            item {
                com.nextstepai.paperless.ui.settings.components.LanguageSelectionCard(
                    selectedLanguage = uiState.selectedLanguage,
                    onSelectLanguage = { onIntent(SettingsUiIntent.SelectLanguage(it)) }
                )
            }

            // بطاقة التخزين المؤقت (Storage & Cache)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.FolderSpecial, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                            }
                            Text(stringResource(Res.string.storage_and_cache), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stringResource(Res.string.local_document_cache), style = MaterialTheme.typography.bodySmall, color = OnSurface)
                            Text("${uiState.cacheUsedMb} MB / ${uiState.cacheMaxMb.toInt()} MB used", style = MaterialTheme.typography.labelSmall, color = TerracottaPrimary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uiState.cacheUsedMb / uiState.cacheMaxMb },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = TerracottaPrimary,
                            trackColor = SurfaceContainerHigh
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                if (uiState.isConfirmingClearCache) {
                                    onIntent(SettingsUiIntent.ConfirmClearCache)
                                } else {
                                    onIntent(SettingsUiIntent.RequestClearCache)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = if (uiState.isConfirmingClearCache) {
                                ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.error)
                            } else {
                                ButtonDefaults.outlinedButtonColors(contentColor = OnSurface)
                            }
                        ) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isConfirmingClearCache) "${stringResource(Res.string.confirm_clear)} (${uiState.cacheUsedMb} MB)?" else stringResource(Res.string.clear_cache),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // بطاقة التعرف الضوئي OCR والذكاء الاصطناعي
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Outlined.DocumentScanner, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                                }
                                Text(stringResource(Res.string.ocr_indexing), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                            }
                            Surface(color = TerracottaSecondaryContainer, shape = RoundedCornerShape(6.dp)) {
                                Text("v2.4 Core", fontSize = 10.sp, color = TerracottaOnSecondaryContainer, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(Res.string.ocr_indexing_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(stringResource(Res.string.installed_lexicons), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            uiState.installedLanguages.forEach { lang ->
                                Surface(
                                    color = SurfaceContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f)))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Outlined.Translate, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(13.dp))
                                        Text(lang, fontSize = 11.sp, color = OnSurface)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.35f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(stringResource(Res.string.offline_ocr), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = OnSurface)
                                Text(stringResource(Res.string.offline_ocr_desc), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                            Switch(
                                checked = uiState.offlineOcrEnabled,
                                onCheckedChange = { onIntent(SettingsUiIntent.ToggleOfflineOcr(it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TerracottaOnPrimary,
                                    checkedTrackColor = TerracottaPrimary
                                )
                            )
                        }
                    }
                }
            }

            // تذييل الصفحة الأرشيفي
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(Res.string.editorial_build), style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant.copy(alpha = 0.7f))
                    Text(stringResource(Res.string.crafted_for_scholars), style = MaterialTheme.typography.bodySmall, color = OutlineVariant)
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
