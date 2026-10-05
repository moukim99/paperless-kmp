package com.nextstepai.paperless.ui.documents.add.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.theme.*

@Composable
fun AddOptionCard(
    title: String,
    badgeText: String,
    badgeColor: Color,
    badgeTextColor: Color,
    description: String,
    icon: ImageVector,
    buttonText: String,
    buttonContainerColor: Color,
    buttonContentColor: Color,
    buttonIcon: ImageVector,
    buttonBorder: BorderStroke? = null,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // حاوية الأيقونة الدافئة
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TerracottaPrimaryContainer.copy(alpha = 0.12f))
                        .border(1.dp, TerracottaPrimaryContainer.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // نصوص العنوان والوصف والشارة
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = OnSurface
                        )
                        Surface(
                            color = badgeColor,
                            shape = CircleShape,
                            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = badgeTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // زر الإجراء الرئيسي للبطاقة
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonContainerColor,
                    contentColor = buttonContentColor
                ),
                border = buttonBorder,
                contentPadding = PaddingValues(vertical = 12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
