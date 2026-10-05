package com.nextstepai.paperless.ui.catalog.documenttype

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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nextstepai.paperless.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentTypeDialog(
    uiState: AddDocumentTypeUiState,
    onIntent: (AddDocumentTypeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var algorithmMenuExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onIntent(AddDocumentTypeIntent.Dismiss) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // الحاوية الكلية مع تعتيم وتحديد أقصى عرض للشاشة
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
                    // الشريط اللوني المتدرج العلوي (Terracotta Gradient Bar)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        TerracottaPrimary.copy(alpha = 0.7f),
                                        TerracottaPrimary,
                                        TerracottaPrimary.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // ترويسة النافذة
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CATALOG SETUP",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Step 1 of 1",
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = OnSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Add Document Type",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // حقل اسم نوع المستند (Document Type Name)
                        Text(
                            text = "DOCUMENT TYPE NAME",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { onIntent(AddDocumentTypeIntent.NameChanged(it)) },
                            placeholder = {
                                Text(
                                    "e.g. Invoice, Contract, Tax Slip",
                                    fontSize = 13.sp,
                                    color = OutlineVariant
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceBackground,
                                unfocusedContainerColor = SurfaceBackground,
                                focusedBorderColor = TerracottaPrimary,
                                unfocusedBorderColor = OutlineVariant.copy(alpha = 0.6f)
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // بطاقة القواعد والمطابقة التلقائية (Collapsible Auto-matching rules)
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

                                // شريط رأس القائمة القابل للطي
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onIntent(AddDocumentTypeIntent.ToggleRulesExpanded) },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(TerracottaPrimary)
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
                                            contentDescription = "Expand",
                                            tint = OnSurfaceVariant,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .rotate(rotationState)
                                        )
                                    }
                                }

                                // تفاصيل القواعد القابلة للطي
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
                                        // حقل نمط المطابقة (Matching pattern)
                                        OutlinedTextField(
                                            value = uiState.matchingPattern,
                                            onValueChange = { onIntent(AddDocumentTypeIntent.MatchingPatternChanged(it)) },
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

                                        // قائمة اختيار الخوارزمية (Matching algorithm Dropdown)
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
                                                            onIntent(AddDocumentTypeIntent.AlgorithmSelected(algo))
                                                            algorithmMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // مفتاح تبديل تجاهل حالة الأحرف (Ignore case toggle)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 4.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Ignore case ",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = OnSurface
                                                )
                                                Text(
                                                    text = "(insensitive)",
                                                    fontSize = 12.sp,
                                                    fontStyle = FontStyle.Italic,
                                                    color = OnSurfaceVariant
                                                )
                                            }

                                            Switch(
                                                checked = uiState.ignoreCase,
                                                onCheckedChange = { onIntent(AddDocumentTypeIntent.ToggleIgnoreCase(it)) },
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

                        Spacer(modifier = Modifier.height(20.dp))

                        // أزرار الحفظ والإلغاء (Dialog Actions)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { onIntent(AddDocumentTypeIntent.Dismiss) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                            ) {
                                Text("Cancel", fontSize = 13.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Medium)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { onIntent(AddDocumentTypeIntent.Save) },
                                shape = CircleShape,
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
                                    Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TerracottaOnPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
