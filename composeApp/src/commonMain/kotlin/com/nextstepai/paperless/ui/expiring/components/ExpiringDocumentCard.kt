package com.nextstepai.paperless.ui.expiring.components

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.expiring.DocumentCategory
import com.nextstepai.paperless.ui.expiring.ExpiringDocumentItem
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@Composable
fun ExpiringDocumentCard(
    doc: ExpiringDocumentItem,
    onExtendDate: (String) -> Unit,
    onRenewed: (String) -> Unit,
    onView: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isUrgent = doc.daysRemaining <= 7
    val spineColor = if (isUrgent) TerracottaPrimary else Color.Transparent

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView(doc.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isUrgent) {
                // الشريط الجانبي التنبيهي للمستندات العاجلة
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .width(4.dp)
                        .background(spineColor)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // مصغرة المعاينة الأرشيفية
                    DocumentThumbnailSpecimen(doc = doc)

                    // المحتوى الأساسي للبطاقة
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryBadge(category = doc.category)
                            ExpiryIndicator(days = doc.daysRemaining)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = doc.issuer,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSurfaceVariant
                            )
                            if (doc.annualCost != null) {
                                Text(
                                    text = doc.annualCost,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.35f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // شريط الحالة السفلية والأزرار
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = doc.noticeWindowText ?: if (doc.isAutoRenewScheduled) "Auto-renew scheduled" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { onExtendDate(doc.id) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(stringResource(Res.string.extend_date), fontSize = 11.sp, color = OnSurface)
                        }

                        if (isUrgent) {
                            Button(
                                onClick = { onRenewed(doc.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(stringResource(Res.string.renewed), fontSize = 11.sp, color = TerracottaOnPrimary, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            FilledTonalButton(
                                onClick = { onView(doc.id) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(stringResource(Res.string.view), fontSize = 11.sp, color = OnSurface)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBadge(category: DocumentCategory) {
    val (bg, textColor) = when (category) {
        DocumentCategory.CONTRACTS -> TerracottaPrimary.copy(alpha = 0.12f) to TerracottaPrimary
        DocumentCategory.SUBSCRIPTIONS -> TerracottaTertiaryContainer.copy(alpha = 0.2f) to AmberTertiary
        DocumentCategory.INSURANCE -> TerracottaSecondaryContainer to TerracottaOnSecondaryContainer
        DocumentCategory.DOMAINS -> SurfaceContainerHighest to OnSurfaceVariant
    }

    Surface(color = bg, shape = RoundedCornerShape(12.dp)) {
        Text(
            text = category.displayName,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun ExpiryIndicator(days: Int) {
    val color = when {
        days <= 4 -> TerracottaPrimary
        days <= 7 -> AmberTertiary
        else -> OnSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Icon(
            imageVector = if (days <= 7) Icons.Outlined.Alarm else Icons.Outlined.CalendarToday,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = "Expires in $days days",
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (days <= 7) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DocumentThumbnailSpecimen(doc: ExpiringDocumentItem) {
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainerHigh)
            .border(1.dp, OutlineVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(doc.docType, fontSize = 9.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                Icon(Icons.Outlined.Gavel, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(12.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(OutlineVariant.copy(alpha = 0.6f)))
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(2.dp).background(OutlineVariant.copy(alpha = 0.6f)))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(2.dp).background(OutlineVariant.copy(alpha = 0.6f)))
            }
            Text(doc.pageOrDetail, fontSize = 8.sp, color = OnSurfaceVariant)
        }
    }
}
