package com.nextstepai.paperless.ui.catalog.correspondent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nextstepai.paperless.ui.catalog.documenttype.MatchingAlgorithm
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@Composable
fun AddCorrespondentDialog(
    uiState: AddCorrespondentUiState,
    onIntent: (AddCorrespondentIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var algorithmMenuExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onIntent(AddCorrespondentIntent.Dismiss) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // شريط الإبراز العلوي الأنيق المتدرج
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        TerracottaPrimary.copy(alpha = 0.7f),
                                        TerracottaPrimary,
                                        TerracottaPrimary.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // عنوان النافذة والوصف
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.add_correspondent),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(Res.string.correspondent_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                            IconButton(
                                onClick = { onIntent(AddCorrespondentIntent.Dismiss) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerLow)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Close",
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // حقل اسم جهة الاتصال (Correspondent Name)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CORRESPONDENT NAME",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            OutlinedTextField(
                                value = uiState.name,
                                onValueChange = { onIntent(AddCorrespondentIntent.NameChanged(it)) },
                                placeholder = {
                                    Text(
                                        "e.g. Acme Studio, Inland Revenue",
                                        fontSize = 13.sp,
                                        color = OutlineVariant
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceBackground,
                                    unfocusedContainerColor = SurfaceBackground,
                                    focusedBorderColor = TerracottaPrimary,
                                    unfocusedBorderColor = OutlineVariant.copy(alpha = 0.6f)
                                )
                            )
                        }

                        // بطاقة القواعد والمطابقة التلقائية (Auto-matching rules)
                        Surface(
                            color = SurfaceContainerLow.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(20.dp),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.5f))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                val rotationState by animateFloatAsState(
                                    targetValue = if (uiState.isRulesExpanded) 180f else 0f
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onIntent(AddCorrespondentIntent.ToggleRulesExpanded) },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.AutoAwesome,
                                            contentDescription = null,
                                            tint = TerracottaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Auto-matching rules",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnSurface
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.ExpandMore,
                                            contentDescription = "Toggle",
                                            tint = OnSurfaceVariant,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .rotate(rotationState)
                                        )
                                    }
                                }

                                AnimatedVisibility(
                                    visible = uiState.isRulesExpanded,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // حقل نمط المطابقة
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "Pattern text or keyword",
                                                fontSize = 11.sp,
                                                color = OnSurfaceVariant
                                            )
                                            OutlinedTextField(
                                                value = uiState.matchingPattern,
                                                onValueChange = { onIntent(AddCorrespondentIntent.MatchingPatternChanged(it)) },
                                                placeholder = { Text("Matching pattern", fontSize = 13.sp, color = OutlineVariant) },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(14.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedContainerColor = SurfaceContainerLowest,
                                                    unfocusedContainerColor = SurfaceContainerLowest,
                                                    focusedBorderColor = TerracottaPrimary,
                                                    unfocusedBorderColor = OutlineVariant.copy(alpha = 0.5f)
                                                )
                                            )
                                        }

                                        // اختيار خوارزمية المطابقة
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "Algorithm logic",
                                                fontSize = 11.sp,
                                                color = OnSurfaceVariant
                                            )
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(SurfaceContainerLowest)
                                                        .border(1.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                                        .clickable { algorithmMenuExpanded = true }
                                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "MATCHING ALGORITHM",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = TerracottaPrimary
                                                        )
                                                        Text(
                                                            text = uiState.matchingAlgorithm.displayName,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = OnSurface
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Outlined.UnfoldMore,
                                                        contentDescription = null,
                                                        tint = OnSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                DropdownMenu(
                                                    expanded = algorithmMenuExpanded,
                                                    onDismissRequest = { algorithmMenuExpanded = false },
                                                    modifier = Modifier.background(SurfaceContainerLowest)
                                                ) {
                                                    MatchingAlgorithm.entries.forEach { algo ->
                                                        DropdownMenuItem(
                                                            text = { Text(algo.displayName, fontSize = 13.sp, color = OnSurface) },
                                                            onClick = {
                                                                onIntent(AddCorrespondentIntent.AlgorithmSelected(algo))
                                                                algorithmMenuExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // تبديل تجاهل حالة الأحرف (Ignore case toggle)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 2.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Ignore case (insensitive)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = OnSurface
                                                )
                                                Text(
                                                    text = "Match both uppercase & lowercase",
                                                    fontSize = 11.sp,
                                                    color = OnSurfaceVariant
                                                )
                                            }

                                            Switch(
                                                checked = uiState.ignoreCase,
                                                onCheckedChange = { onIntent(AddCorrespondentIntent.ToggleIgnoreCase(it)) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = TerracottaOnPrimary,
                                                    checkedTrackColor = TerracottaPrimary,
                                                    uncheckedTrackColor = SurfaceContainerHighest
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // أزرار الحفظ والإلغاء
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { onIntent(AddCorrespondentIntent.Dismiss) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text(stringResource(Res.string.action_cancel), fontSize = 13.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onIntent(AddCorrespondentIntent.Save) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                                enabled = uiState.name.isNotBlank() && !uiState.isSaving
                            ) {
                                if (uiState.isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = TerracottaOnPrimary
                                    )
                                } else {
                                    Text(stringResource(Res.string.action_save), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TerracottaOnPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
