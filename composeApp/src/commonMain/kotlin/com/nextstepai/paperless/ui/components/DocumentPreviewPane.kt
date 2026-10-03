package com.nextstepai.paperless.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.nextstepai.paperless.domain.platform.DocumentPreviewer
import com.nextstepai.paperless.ui.theme.PaperlessDimensions

@Composable
fun DocumentPreviewPane(
    documentId: Long,
    mimeType: String,
    title: String,
    previewer: DocumentPreviewer,
    onOpenExternal: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var bytes by remember(documentId) { mutableStateOf<ByteArray?>(null) }
    var loading by remember(documentId) { mutableStateOf(true) }

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offset += offsetChange
    }

    LaunchedEffect(documentId) {
        loading = true
        bytes = previewer.readBytes(documentId)
        loading = false
    }

    val imageBitmap: ImageBitmap? = remember(bytes) {
        bytes?.toImageBitmap()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                loading -> {
                    LoadingState(Modifier.fillMaxSize())
                }
                imageBitmap != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.large)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = transformState),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(PaperlessDimensions.md),
                            modifier = Modifier.padding(PaperlessDimensions.xl)
                        ) {
                            Surface(
                                modifier = Modifier.size(72.dp),
                                shape = MaterialTheme.shapes.extraLarge,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Description,
                                        contentDescription = null,
                                        modifier = Modifier.size(36.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Text(title.ifBlank { "Document Preview" }, style = MaterialTheme.typography.titleLarge)
                            Text(
                                mimeType.uppercase() + if (bytes != null) " · ${(bytes!!.size / 1024)} KB" else "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (onOpenExternal != null) {
                                FilledTonalButton(onClick = onOpenExternal) {
                                    Icon(Icons.Outlined.OpenInNew, null)
                                    Spacer(Modifier.width(PaperlessDimensions.sm))
                                    Text("Open file")
                                }
                            }
                        }
                    }
                }
            }

            // Floating Control Bar
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(PaperlessDimensions.lg)
                    .animateContentSize(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = PaperlessDimensions.sm, vertical = PaperlessDimensions.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PaperlessDimensions.xs)
                ) {
                    IconButton(
                        onClick = { scale = (scale / 1.25f).coerceAtLeast(0.5f) }
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Zoom out")
                    }

                    TextButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        }
                    ) {
                        Text("${(scale * 100).toInt()}%")
                    }

                    IconButton(
                        onClick = { scale = (scale * 1.25f).coerceAtMost(5f) }
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Zoom in")
                    }

                    if (scale != 1f || offset != Offset.Zero) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        ) {
                            Icon(Icons.Outlined.RestartAlt, contentDescription = "Reset zoom")
                        }
                    }

                    if (onOpenExternal != null) {
                        IconButton(onClick = onOpenExternal) {
                            Icon(Icons.Outlined.OpenInNew, contentDescription = "Open externally")
                        }
                    }

                    if (onClose != null) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Outlined.Close, contentDescription = "Close preview")
                        }
                    }
                }
            }
        }
    }
}
