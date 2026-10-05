package com.nextstepai.paperless.ui.documentdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.documentdetail.components.SectionHeader
import com.nextstepai.paperless.ui.documentdetail.components.SelectorField
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailsScreen(
    uiState: DocumentDetailUiState,
    onIntent: (DocumentDetailIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceBackground,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onIntent(DocumentDetailIntent.NavigateBack) }) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = OnSurface)
                    }
                },
                title = {
                    Column {
                        Text(
                            text = uiState.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            maxLines = 1
                        )
                        Text(
                            text = "DocVault • Archive",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(DocumentDetailIntent.ShareClicked) }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = OnSurfaceVariant)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = OnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBackground.copy(alpha = 0.95f))
            )
        },
        bottomBar = {
            // شريط الحفظ والتجاهل السفلي (Sticky Save Actions Bar)
            Surface(
                color = SurfaceContainerLowest.copy(alpha = 0.95f),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.5f))),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onIntent(DocumentDetailIntent.DiscardChanges) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(Res.string.action_cancel), fontSize = 13.sp, color = OnSurface, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { onIntent(DocumentDetailIntent.SaveChanges) },
                        modifier = Modifier.weight(2f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = TerracottaOnPrimary)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text(stringResource(Res.string.action_save), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TerracottaOnPrimary)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner: المعاينة العلوية والشارات
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(TerracottaPrimaryContainer.copy(alpha = 0.15f))
                            .border(1.dp, TerracottaPrimaryContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Description, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(28.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${uiState.extension.uppercase()} • ${uiState.pageCount} page", fontSize = 11.sp, color = OnSurfaceVariant)
                            Surface(color = TerracottaPrimaryContainer.copy(alpha = 0.15f), shape = CircleShape) {
                                Text(uiState.version, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TerracottaPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                            }
                            if (uiState.isIndexed) {
                                Surface(color = Color(0xFFE8F5E9), shape = CircleShape) {
                                    Text(stringResource(Res.string.indexed), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 1. قسم المعلومات (Information Section)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(icon = Icons.Outlined.Article, title = stringResource(Res.string.section_info))

                        // حقل العنوان
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(Res.string.label_document_title), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                            OutlinedTextField(
                                value = uiState.title,
                                onValueChange = { onIntent(DocumentDetailIntent.TitleChanged(it)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // محدد الجهة
                        SelectorField(
                            label = stringResource(Res.string.label_correspondent),
                            selectedValue = uiState.selectedCorrespondent,
                            options = uiState.correspondents,
                            onSelect = { onIntent(DocumentDetailIntent.CorrespondentSelected(it)) }
                        )

                        // محدد نوع المستند
                        SelectorField(
                            label = stringResource(Res.string.label_document_type),
                            selectedValue = uiState.selectedDocumentType,
                            options = uiState.documentTypes,
                            onSelect = { onIntent(DocumentDetailIntent.DocumentTypeSelected(it)) }
                        )

                        // محدد مسار الحفظ
                        SelectorField(
                            label = stringResource(Res.string.label_storage_path),
                            selectedValue = uiState.selectedStoragePath,
                            options = uiState.storagePaths,
                            onSelect = { onIntent(DocumentDetailIntent.StoragePathSelected(it)) }
                        )

                        // صف الحقول الثنائية ASN و Version Label
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(Res.string.label_asn), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                                OutlinedTextField(
                                    value = uiState.asn,
                                    onValueChange = { onIntent(DocumentDetailIntent.AsnChanged(it)) },
                                    placeholder = { Text(stringResource(Res.string.placeholder_asn), fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(Res.string.label_version_label), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                                OutlinedTextField(
                                    value = uiState.versionLabel,
                                    onValueChange = { onIntent(DocumentDetailIntent.VersionLabelChanged(it)) },
                                    placeholder = { Text(stringResource(Res.string.placeholder_version), fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. قسم إصدارات الملف (Document Versions)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(icon = Icons.Outlined.FolderCopy, title = stringResource(Res.string.section_versions), badgeCount = "1")

                        // بطاقة الإصدار الحالي
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(TerracottaPrimaryContainer.copy(alpha = 0.08f))
                                .border(1.dp, TerracottaPrimaryContainer.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Version 1", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnSurface)
                                    Surface(color = TerracottaPrimary, shape = RoundedCornerShape(4.dp)) {
                                        Text(stringResource(Res.string.badge_current), color = TerracottaOnPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                                    }
                                }
                                Text("Created: ${uiState.currentVersionDate} • ${uiState.currentVersionSize}", fontSize = 11.sp, color = OnSurfaceVariant)
                            }
                            IconButton(onClick = { onIntent(DocumentDetailIntent.DownloadVersion("v1")) }) {
                                Icon(Icons.Outlined.Download, contentDescription = "Download", tint = TerracottaPrimary)
                            }
                        }

                        // زر رفع إصدار جديد
                        OutlinedButton(
                            onClick = { onIntent(DocumentDetailIntent.UploadNewVersion) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TerracottaPrimary.copy(alpha = 0.5f)))
                        ) {
                            Icon(Icons.Outlined.UploadFile, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(Res.string.upload_new_version), fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 3. قسم التواريخ والتنبيهات (Dates & Reminders)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(icon = Icons.Outlined.CalendarToday, title = stringResource(Res.string.section_dates_reminders))

                        // تاريخ المستند
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(Res.string.label_document_date), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                            OutlinedTextField(
                                value = uiState.documentDate,
                                onValueChange = { onIntent(DocumentDetailIntent.DocumentDateChanged(it)) },
                                trailingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = TerracottaPrimary) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // تاريخ الانتهاء والتذكير
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(Res.string.label_expiry_date), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                                OutlinedTextField(
                                    value = uiState.expiryDate,
                                    onValueChange = { onIntent(DocumentDetailIntent.ExpiryDateChanged(it)) },
                                    placeholder = { Text("YYYY-MM-DD", fontSize = 11.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(Res.string.label_reminder_days), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant)
                                OutlinedTextField(
                                    value = uiState.reminderDays.toString(),
                                    onValueChange = { it.toIntOrNull()?.let { days -> onIntent(DocumentDetailIntent.ReminderDaysChanged(days)) } },
                                    leadingIcon = { Icon(Icons.Outlined.Notifications, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. قسم الوسوم (Tags)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(icon = Icons.Outlined.Label, title = stringResource(Res.string.section_tags))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = { onIntent(DocumentDetailIntent.AddTagClicked) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = TerracottaPrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Res.string.add_tag), fontSize = 12.sp, color = TerracottaPrimary, fontWeight = FontWeight.SemiBold)
                            }

                            uiState.tags.forEach { tag ->
                                Surface(
                                    color = SurfaceContainer,
                                    shape = CircleShape,
                                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.6f)))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(tag.color))
                                        Text(tag.name, fontSize = 11.sp, color = OnSurface)
                                        Icon(
                                            imageVector = Icons.Outlined.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(12.dp).clickable { onIntent(DocumentDetailIntent.RemoveTag(tag.id)) },
                                            tint = OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. قسم النص المستخرج (Extracted Text OCR)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(icon = Icons.Outlined.DocumentScanner, title = stringResource(Res.string.section_ocr))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onIntent(DocumentDetailIntent.EditOcrText) },
                                modifier = Modifier.weight(1f).height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Res.string.action_edit), fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = { onIntent(DocumentDetailIntent.CopyOcrText) },
                                modifier = Modifier.weight(1f).height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Res.string.action_copy), fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier.weight(1f).height(32.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Outlined.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Res.string.search), fontSize = 11.sp)
                            }
                        }

                        Surface(
                            color = SurfaceBackground,
                            shape = RoundedCornerShape(10.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.5f))),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(stringResource(Res.string.ocr_preview_snippet), fontSize = 10.sp, fontStyle = FontStyle.Italic, color = OnSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(uiState.ocrText, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = OnSurface, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }

            // 6. قسم الإجراءات السريعة والحذف (Actions)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OutlineVariant.copy(alpha = 0.4f)))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(icon = Icons.Outlined.Bolt, title = stringResource(Res.string.section_actions))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onIntent(DocumentDetailIntent.InAppPreviewClicked) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaPrimary)
                            ) {
                                Icon(Icons.Outlined.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(Res.string.in_app_preview), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedButton(
                                onClick = { onIntent(DocumentDetailIntent.OpenExternalClicked) },
                                modifier = Modifier.weight(1f).height(38.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Outlined.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(Res.string.open_external), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        TextButton(
                            onClick = { onIntent(DocumentDetailIntent.DeleteDocumentClicked) },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(Res.string.action_delete), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
