package com.nextstepai.paperless.ui.inbox.components

import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.inbox.DocumentStatus
import com.nextstepai.paperless.ui.inbox.InboxDocumentItem
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@Composable
fun InboxDocumentCard(
    document: InboxDocumentItem,
    onAssignMetadata: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spineColor = when (document.status) {
        DocumentStatus.NEEDS_REVIEW -> TerracottaPrimary
        DocumentStatus.OCR_IN_PROGRESS -> TerracottaTertiaryContainer
        DocumentStatus.UNASSIGNED -> TerracottaSecondary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(document.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Visual Accent Spine (الخط العمودي الجانبي المميز للهوية الأرشيفية)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .width(4.dp)
                    .background(spineColor)
            )

            Row(
                modifier = Modifier
                    .padding(start = 12.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // مصغرة المعاينة (Specimen Thumbnail)
                Box(
                    modifier = Modifier
                        .size(width = 64.dp, height = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceContainer)
                        .border(1.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    // شارة نوع الامتداد المصغرة
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(OnSurface.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = document.extension.uppercase(),
                            color = SurfaceContainerLowest,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // تفاصيل المستند
                Column(modifier = Modifier.weight(1f)) {
                    // الحالة والوقت
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DocumentStatusIndicator(status = document.status)
                        Text(
                            text = "Uploaded ${document.uploadedAgo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant.copy(alpha = 0.75f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // اسم المستند
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // البيانات التقنية
                    Text(
                        text = "${document.extension.uppercase()} • ${document.pageCount} page • ${document.fileSizeFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )

                    // شريط المعالجة الحصري لـ OCR
                    if (document.status == DocumentStatus.OCR_IN_PROGRESS && document.ocrProgress != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { document.ocrProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(2.5.dp)),
                            color = TerracottaTertiaryContainer,
                            trackColor = SurfaceContainerHigh
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recognizing line items (${(document.ocrProgress * 100).toInt()}%)...",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberTertiary
                            )
                            IconButton(onClick = { onClick(document.id) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Outlined.Visibility, contentDescription = "View", tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // فاصل الأزرار والإجراءات
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = OutlineVariant.copy(alpha = 0.35f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // شريط الإجراءات السفلي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (document.status == DocumentStatus.UNASSIGNED) {
                            Button(
                                onClick = { onAssignMetadata(document.id) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaSecondaryContainer),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Outlined.NewLabel, contentDescription = null, tint = OnSecondaryFixed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(Res.string.assign_metadata), fontSize = 11.sp, color = OnSecondaryFixed, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                document.tags.forEach { tag ->
                                    Surface(
                                        color = SurfaceContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OnSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            if (document.status == DocumentStatus.UNASSIGNED) {
                                IconButton(onClick = { onDelete(document.id) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Outlined.EditNote, contentDescription = "Edit", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentStatusIndicator(status: DocumentStatus) {
    when (status) {
        DocumentStatus.NEEDS_REVIEW -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(TerracottaPrimary))
                Text(stringResource(Res.string.status_needs_review), color = TerracottaPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        DocumentStatus.OCR_IN_PROGRESS -> {
            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(animation = tween(800), repeatMode = RepeatMode.Reverse)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(TerracottaTertiaryContainer.copy(alpha = alpha)))
                Text(stringResource(Res.string.status_ocr_progress), color = AmberTertiary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        DocumentStatus.UNASSIGNED -> {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Outlined.Info, contentDescription = null, tint = TerracottaSecondary, modifier = Modifier.size(13.dp))
                Text(stringResource(Res.string.status_unassigned), color = TerracottaSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}
