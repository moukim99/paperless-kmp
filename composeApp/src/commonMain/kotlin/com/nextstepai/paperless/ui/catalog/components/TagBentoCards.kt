package com.nextstepai.paperless.ui.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.catalog.TagItem
import com.nextstepai.paperless.ui.theme.*

/**
 * بطاقة ممتدة بعرض كامل (Featured Wide Bento Card)
 */
@Composable
fun FullWidthTagCard(
    tag: TagItem,
    icon: ImageVector,
    onClick: (String) -> Unit,
    onOptionsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(tag.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tag.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tag.color, modifier = Modifier.size(20.dp))
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tag.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(tag.color)
                        )
                    }
                    Text(
                        text = tag.subtitle ?: "${tag.documentCount} documents cataloged",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { onOptionsClick(tag.id) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "Options", tint = OutlineVariant)
            }
        }
    }
}

/**
 * بطاقة نصفية مربعة (Square Grid Bento Card)
 */
@Composable
fun SquareTagCard(
    tag: TagItem,
    bottomIcon: ImageVector,
    onClick: (String) -> Unit,
    onOptionsClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(tag.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(tag.color)
                )
                IconButton(onClick = { onOptionsClick(tag.id) }, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "Options", tint = OutlineVariant, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${tag.documentCount} documents",
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.35f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tag.categoryLabel?.uppercase() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = tag.color,
                    fontSize = 10.sp
                )
                Icon(bottomIcon, contentDescription = null, tint = OutlineVariant, modifier = Modifier.size(15.dp))
            }
        }
    }
}
