package com.nextstepai.paperless.ui.documents.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.documents.DocumentListItem
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@Composable
fun DocumentListCard(
    doc: DocumentListItem,
    onClick: (String) -> Unit,
    onOptionsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(doc.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // مصغرة المعاينة (Vellum / Warm Tint Thumbnail)
            Box(
                modifier = Modifier
                    .size(width = 54.dp, height = 54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TerracottaPrimaryContainer.copy(alpha = 0.12f))
                    .border(1.dp, TerracottaPrimaryContainer.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // التفاصيل والشارات وحالة المعالجة
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onOptionsClick(doc.id) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Options",
                            tint = OnSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // الشارات الأرشيفية (Badges: JPEG, 1p, Pending_upload)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // شارة الامتداد
                    Surface(
                        color = SurfaceContainerLow,
                        shape = RoundedCornerShape(6.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TerracottaPrimaryContainer.copy(alpha = 0.3f)))
                    ) {
                        Text(
                            text = doc.extension.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TerracottaPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // شارة عدد الصفحات
                    Surface(
                        color = SurfaceContainerHigh.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${doc.pageCount}p",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // شارة حالة المزامنة السحابية (Pending Upload)
                    if (doc.isPendingUpload) {
                        Surface(
                            color = TerracottaTertiaryContainer.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(TerracottaTertiaryContainer.copy(alpha = 0.35f)))
                        ) {
                            Text(
                                text = stringResource(Res.string.pending_upload),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = AmberTertiary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // حالة المعالجة بـ OCR
                if (doc.ocrStatusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AmberTertiary)
                        )
                        Text(
                            text = doc.ocrStatusMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = OnSurfaceVariant.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
