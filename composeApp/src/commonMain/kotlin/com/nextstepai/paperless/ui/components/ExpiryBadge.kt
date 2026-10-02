package com.nextstepai.paperless.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstepai.paperless.documents.presentation.ExpiryState
import com.nextstepai.paperless.ui.theme.LocalPaperlessSemanticColors

@Composable
fun ExpiryBadge(
    state: ExpiryState,
    label: String,
) {
    val semantic = LocalPaperlessSemanticColors.current
    val color = when (state) {
        ExpiryState.None -> MaterialTheme.colorScheme.surfaceVariant
        ExpiryState.Upcoming -> semantic.warning
        ExpiryState.Today, ExpiryState.Expired -> semantic.critical
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label) },
        border = null,
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            disabledLabelColor = color,
            disabledContainerColor = color.copy(alpha = 0.12f),
        ),
    )
}
