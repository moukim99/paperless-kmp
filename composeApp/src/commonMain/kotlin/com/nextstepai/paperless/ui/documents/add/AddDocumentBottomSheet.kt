package com.nextstepai.paperless.ui.documents.add

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstepai.paperless.ui.documents.add.components.AddOptionCard
import com.nextstepai.paperless.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import paperless_kmp.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentBottomSheet(
    onIntent: (AddDocumentIntent) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = { onIntent(AddDocumentIntent.Dismiss) },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = SurfaceContainerLowest,
        scrimColor = Color(0xFF1F1917).copy(alpha = 0.5f),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // مؤشر السحب اليدوي العلوي (Interactive Drag Pill)
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 5.dp)
                    .clip(CircleShape)
                    .background(OnSurface.copy(alpha = 0.2f))
            )

            // زر الإغلاق الدائري العلوي
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = { onIntent(AddDocumentIntent.Dismiss) },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainerHigh)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = OnSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ترويسة العنوان والوصف
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = stringResource(Res.string.add_to_library),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.add_to_library_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            // الخيار 1: استيراد ملف (Import File)
            AddOptionCard(
                title = stringResource(Res.string.import_file),
                badgeText = "Max 50MB",
                badgeColor = SurfaceContainer,
                badgeTextColor = OnSurfaceVariant,
                description = stringResource(Res.string.import_file_desc),
                icon = Icons.Outlined.UploadFile,
                buttonText = stringResource(Res.string.add_document),
                buttonContainerColor = TerracottaPrimary,
                buttonContentColor = TerracottaOnPrimary,
                buttonIcon = Icons.Outlined.Add,
                onActionClick = { onIntent(AddDocumentIntent.ImportFileClicked) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // الخيار 2: مسح ضوئي بالكاميرا (Scan Document)
            AddOptionCard(
                title = stringResource(Res.string.scan_document),
                badgeText = "Auto-crop OCR",
                badgeColor = TerracottaPrimaryContainer.copy(alpha = 0.12f),
                badgeTextColor = TerracottaPrimary,
                description = stringResource(Res.string.scan_document_desc),
                icon = Icons.Outlined.DocumentScanner,
                buttonText = stringResource(Res.string.scan_document),
                buttonContainerColor = SurfaceContainerLow,
                buttonContentColor = OnSurface,
                buttonIcon = Icons.Outlined.PhotoCamera,
                buttonBorder = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                onActionClick = { onIntent(AddDocumentIntent.ScanDocumentClicked) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // التذييل الأمني الأرشيفي (Encrypted AES-256 Storage)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = TerracottaPrimary.copy(alpha = 0.85f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(Res.string.encrypted_storage),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Text("•", color = OutlineVariant, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(Res.string.help_guide),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TerracottaPrimary,
                    modifier = Modifier.clickable { onIntent(AddDocumentIntent.HelpGuideClicked) }
                )
            }
        }
    }
}
