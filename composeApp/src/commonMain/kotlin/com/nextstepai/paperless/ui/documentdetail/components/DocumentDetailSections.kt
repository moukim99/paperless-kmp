package com.nextstepai.paperless.ui.documentdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.theme.*

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    badgeCount: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(TerracottaPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TerracottaPrimary, modifier = Modifier.size(16.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
            if (badgeCount != null) {
                Surface(color = SurfaceContainerHigh, shape = CircleShape) {
                    Text(badgeCount, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariant, letterSpacing = 0.5.sp)
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceContainerLowest)
                    .border(1.dp, OutlineVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedValue, fontSize = 13.sp, color = OnSurface)
                Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(SurfaceContainerLowest)
            ) {
                options.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(item, fontSize = 13.sp, color = OnSurface) },
                        onClick = {
                            onSelect(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
