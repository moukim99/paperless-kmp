package com.nextstepai.paperless.ui.catalog.storagepath

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nextstepai.paperless.ui.catalog.documenttype.MatchingAlgorithm
import com.nextstepai.paperless.ui.theme.*

@Composable
fun AddStoragePathDialog(
    uiState: AddStoragePathUiState,
    onIntent: (AddStoragePathIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var algorithmMenuExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onIntent(AddStoragePathIntent.Dismiss) },
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
                    .widthIn(max = 412.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.5f))
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // مقبض السحب المرئي العلوي
                    Box(
                        modifier = Modifier
                            .size(width = 40.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(OutlineVariant.copy(alpha = 0.6f))
                            .align(Alignment.CenterHorizontally)
                    )

                    // ترويسة النافذة
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "DOCVAULT CATALOG",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TerracottaPrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Add Storage Path",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Medium,
                                color = OnSurface
                            )
                        }

                        IconButton(
                            onClick = { onIntent(AddStoragePathIntent.Dismiss) },
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

                    // الحقل 1: اسم المسار (Name)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text("Name ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                            Text("*", color = TerracottaPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = uiState.name,
                            onValueChange = { onIntent(AddStoragePathIntent.NameChanged(it)) },
                            placeholder = { Text("e.g., Invoices & Receipts", fontSize = 13.sp, color = OutlineVariant) },
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

                    // الحقل 2: قالب المسار (Path template)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row {
                            Text("Path template ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OnSurface)
                            Text("*", color = TerracottaPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedTextField(
                            value = uiState.pathTemplate,
                            onValueChange = { onIntent(AddStoragePathIntent.PathTemplateChanged(it)) },
                            placeholder = { Text("{created_year}/{correspondent}/{title}", fontSize = 12.sp, color = OutlineVariant) },
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
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

                        // شريط التلميحات للوسوم المتاحة (Format tags)
                        Row(
                            modifier = Modifier.padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Format tags: ", fontSize = 11.sp, color = OnSurfaceVariant)
                            val tags = listOf("{created_year}", "{correspondent}", "{title}")
                            tags.forEachIndexed { index, tag ->
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    color = TerracottaPrimary,
                                    modifier = Modifier.clickable { onIntent(AddStoragePathIntent.InsertFormatTag(tag)) }
                                )
                                if (index < tags.size - 1) Text(",", fontSize = 11.sp, color = OnSurfaceVariant)
                            }
                        }
                    }

                    // بطاقة القواعد والمطابقة التلقائية (Auto-matching rules)
                    Surface(
                        color = TerracottaPrimaryContainer.copy(alpha = 0.08f),
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
                                    .clickable { onIntent(AddStoragePathIntent.ToggleRulesExpanded) },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Bolt,
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
                                    // نمط المطابقة (Matching pattern)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Matching pattern",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnSurface
                                        )
                                        OutlinedTextField(
                                            value = uiState.matchingPattern,
                                            onValueChange = { onIntent(AddStoragePathIntent.MatchingPatternChanged(it)) },
                                            placeholder = { Text("bill, statement, tax", fontSize = 12.sp, color = OutlineVariant) },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = SurfaceContainerLowest,
                                                unfocusedContainerColor = SurfaceContainerLowest,
                                                focusedBorderColor = TerracottaPrimary,
                                                unfocusedBorderColor = OutlineVariant.copy(alpha = 0.5f)
                                            )
                                        )
                                    }

                                    // خوارزمية المطابقة (Matching algorithm)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Matching algorithm",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OnSurface
                                        )
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(SurfaceContainerLowest)
                                                    .border(1.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                                    .clickable { algorithmMenuExpanded = true }
                                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = uiState.matchingAlgorithm.displayName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = OnSurface
                                                )
                                                Icon(
                                                    imageVector = Icons.Outlined.UnfoldMore,
                                                    contentDescription = null,
                                                    tint = OnSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            DropdownMenu(
                                                expanded = algorithmMenuExpanded,
                                                onDismissRequest = { algorithmMenuExpanded = false },
                                                modifier = Modifier.background(SurfaceContainerLowest)
                                            ) {
                                                MatchingAlgorithm.entries.forEach { algo ->
                                                    DropdownMenuItem(
                                                        text = { Text(algo.displayName, fontSize = 12.sp, color = OnSurface) },
                                                        onClick = {
                                                            onIntent(AddStoragePathIntent.AlgorithmSelected(algo))
                                                            algorithmMenuExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // تجاهل حالة الأحرف (Ignore case toggle)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Ignore case (case-insensitive)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = OnSurface
                                        )

                                        Switch(
                                            checked = uiState.ignoreCase,
                                            onCheckedChange = { onIntent(AddStoragePathIntent.ToggleIgnoreCase(it)) },
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

                    // أزرار الحفظ والإلغاء (Action Bar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onIntent(AddStoragePathIntent.Dismiss) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp, color = OnSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onIntent(AddStoragePathIntent.SavePath) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            enabled = uiState.name.isNotBlank() && uiState.pathTemplate.isNotBlank() && !uiState.isSaving
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
                                    Text("Save Path", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaOnPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
