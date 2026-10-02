package com.nextstepai.paperless.platform

import com.nextstepai.paperless.domain.platform.DocumentPreviewer
import java.awt.Desktop
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.nextstepai.paperless.domain.sync.DesktopDocumentFileStore

internal var desktopPreviewStore: DesktopDocumentFileStore? = null

actual object PlatformDocumentPreviewer : DocumentPreviewer {
    override suspend fun open(documentId: Long): Result<Unit> = runCatching {
        val path = desktopPreviewStore?.localPath(documentId) ?: error("Local file not found")
        withContext(Dispatchers.IO) { Desktop.getDesktop().open(File(path)) }
    }
}
