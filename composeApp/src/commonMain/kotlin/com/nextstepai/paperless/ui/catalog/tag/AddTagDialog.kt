package com.nextstepai.paperless.ui.catalog.tag

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nextstepai.paperless.ui.catalog.tag.components.TagColorPaletteSection
import com.nextstepai.paperless.ui.theme.*

@Composable
fun AddTagDialog(
    uiState: AddTagUiState,
    onIntent: (AddTagIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = { onIntent(AddTagIntent.Dismiss) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .widthIn(max = 410.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.6f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ترويسة النافذة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Add Tag",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                                Text(
                                    text = "DOCVAULT",
                                    fontSize = 10.sp,
                                    color = OnSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "Organize documents with tailored metadata",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onIntent(AddTagIntent.Dismiss) },
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

                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f), thickness = 1.dp)

                    // حقل اسم الوسم (Tag Name)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "TAG NAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        OutlinedTextField(
                            value = uiState.tagName,
                            onValueChange = { onIntent(AddTagIntent.TagNameChanged(it)) },
                            placeholder = { Text("e.g., Tax Receipts 2026", fontSize = 13.sp, color = OutlineVariant) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerLowest,
                                unfocusedContainerColor = SurfaceContainerLowest,
                                focusedBorderColor = TerracottaPrimary,
                                unfocusedBorderColor = OutlineVariant.copy(alpha = 0.6f)
                            )
                        )
                    }

                    // قسم لوحة الألوان (Tag Palette & Hex Input)
                    TagColorPaletteSection(
                        selectedColor = uiState.selectedColor,
                        hexCode = uiState.hexCode,
                        onColorSelected = { onIntent(AddTagIntent.ColorSelected(it)) },
                        onHexCodeChanged = { onIntent(AddTagIntent.HexCodeChanged(it)) }
                    )

                    // مفتاح وسم صندوق الوارد الافتراضي (Inbox Tag Switch)
                    Surface(
                        color = SurfaceContainerLow.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(16.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(
                                        imageVector = Icons.Outlined.Inbox,
                                        contentDescription = null,
                                        tint = TerracottaPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Inbox tag",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnSurface
                                    )
                                }
                                Text(
                                    text = "New incoming documents are assigned this tag automatically",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }

                            Switch(
                                checked = uiState.isInboxTag,
                                onCheckedChange = { onIntent(AddTagIntent.ToggleInboxTag(it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TerracottaOnPrimary,
                                    checkedTrackColor = TerracottaPrimary,
                                    uncheckedTrackColor = SurfaceContainerHighest
                                )
                            )
                        }
                    }

                    // محدد الوسم الأب (Parent Tag Selector)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceContainerLowest)
                                .border(1.dp, OutlineVariant.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                .clickable { onIntent(AddTagIntent.SelectParentTagClicked) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PARENT TAG",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceVariant
                                )
                                Text(
                                    text = uiState.parentTagName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainerLow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // شريط الأكورديون لقواعد المطابقة (Auto-matching rules)
                    Column {
                        val rotationState by animateFloatAsState(
                            targetValue = if (uiState.isAutoMatchingExpanded) 180f else 0f
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onIntent(AddTagIntent.ToggleAutoMatchingExpanded) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                                Text("Auto-matching rules", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = OnSurface)
                            }
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(18.dp).rotate(rotationState)
                            )
                        }

                        Text(
                            text = "Assign based on text detection, correspondents or barcode",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(start = 22.dp)
                        )

                        AnimatedVisibility(
                            visible = uiState.isAutoMatchingExpanded,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 10.dp, start = 22.dp)) {
                                Text(
                                    text = "Regex & keyword pattern rules will execute during OCR ingestion.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.4f), thickness = 1.dp)

                    // أزرار الحفظ والإلغاء (Modal Actions)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onIntent(AddTagIntent.Dismiss) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                        ) {
                            Text("Cancel", fontSize = 13.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onIntent(AddTagIntent.SaveTag) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            enabled = uiState.tagName.isNotBlank() && !uiState.isSaving
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = TerracottaOnPrimary
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Save Tag", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TerracottaOnPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
