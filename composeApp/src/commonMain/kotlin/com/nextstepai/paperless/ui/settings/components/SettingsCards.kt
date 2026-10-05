package com.nextstepai.paperless.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.settings.*
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@Composable
fun CloudflareServerCard(
    uiState: SettingsUiState,
    onUrlChange: (String) -> Unit,
    onTokenChange: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onTestConnection: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.CloudSync, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                }
                Text("Cloudflare Worker Sync Server", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Configure your R2 & D1 Cloudflare Worker backend for seamless cross-device sync.",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // حقل Server URL
            OutlinedTextField(
                value = uiState.serverUrl,
                onValueChange = onUrlChange,
                label = { Text("Server URL", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TerracottaPrimary,
                    focusedLabelColor = TerracottaPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // حقل API Auth Token
            OutlinedTextField(
                value = uiState.authToken,
                onValueChange = onTokenChange,
                label = { Text("API Auth Token", fontSize = 12.sp) },
                singleLine = true,
                visualTransformation = if (uiState.isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (uiState.isTokenVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            contentDescription = "Toggle token",
                            tint = OutlineVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TerracottaPrimary,
                    focusedLabelColor = TerracottaPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // أزرار اختبار الاتصال والحفظ
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onTestConnection,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.connectionStatus !is ConnectionStatus.Testing
                ) {
                    when (uiState.connectionStatus) {
                        is ConnectionStatus.Testing -> {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = TerracottaSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(Res.string.connecting), fontSize = 12.sp, color = OnSurface)
                        }
                        is ConnectionStatus.Success -> {
                            Icon(Icons.Outlined.Verified, contentDescription = null, tint = TerracottaSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Online (${(uiState.connectionStatus as ConnectionStatus.Success).latencyMs}ms)", fontSize = 11.sp, color = TerracottaSecondary)
                        }
                        else -> {
                            Icon(Icons.Outlined.Cable, contentDescription = null, tint = TerracottaSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(Res.string.settings_test_connection), fontSize = 12.sp, color = OnSurface)
                        }
                    }
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Icon(
                        imageVector = if (uiState.isSaveSuccess) Icons.Outlined.DoneAll else Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TerracottaOnPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (uiState.isSaveSuccess) stringResource(Res.string.settings_saved) else stringResource(Res.string.settings_save), fontSize = 12.sp, color = TerracottaOnPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun AutoSyncCard(
    syncOnWifi: Boolean,
    selectedInterval: SyncInterval,
    onToggleWifi: (Boolean) -> Unit,
    onSelectInterval: (SyncInterval) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Wifi, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                }
                Text(stringResource(Res.string.settings_auto_sync), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(Res.string.settings_sync_wifi), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = OnSurface)
                    Text(stringResource(Res.string.sync_wifi_desc), style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
                Switch(
                    checked = syncOnWifi,
                    onCheckedChange = onToggleWifi,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TerracottaOnPrimary,
                        checkedTrackColor = TerracottaPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.35f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Text(stringResource(Res.string.settings_sync_interval), style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SyncInterval.entries.forEach { interval ->
                    val isSelected = interval == selectedInterval
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) TerracottaPrimary else SurfaceContainerLow)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else OutlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelectInterval(interval) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = interval.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TerracottaOnPrimary else OnSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageSelectionCard(
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(SurfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Translate, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(20.dp))
                }
                Text(stringResource(Res.string.settings_language_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                stringResource(Res.string.settings_language_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppLanguage.entries.forEach { language ->
                    val isSelected = language == selectedLanguage
                    val label = when (language) {
                        AppLanguage.SYSTEM -> stringResource(Res.string.language_system)
                        AppLanguage.ENGLISH -> stringResource(Res.string.language_english)
                        AppLanguage.ARABIC -> stringResource(Res.string.language_arabic)
                        AppLanguage.FRENCH -> stringResource(Res.string.language_french)
                    }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectLanguage(language) },
                        color = if (isSelected) SurfaceContainerHigh else SurfaceContainerLow,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, TerracottaPrimary) else null,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TerracottaPrimary else OnSurface
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
