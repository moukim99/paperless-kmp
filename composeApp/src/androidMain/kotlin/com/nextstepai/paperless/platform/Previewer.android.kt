package com.nextstepai.paperless.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.nextstepai.paperless.domain.platform.DocumentPreviewer
import com.nextstepai.paperless.domain.sync.AndroidDocumentFileStore
import java.io.File

internal lateinit var androidPreviewContext: Context
internal var androidPreviewStore: AndroidDocumentFileStore? = null

actual object PlatformDocumentPreviewer : DocumentPreviewer {
    override suspend fun open(documentId: Long): Result<Unit> = runCatching {
        val path = androidPreviewStore?.localPath(documentId) ?: error("Local file not found")
        val file = File(path)
        val uri = FileProvider.getUriForFile(androidPreviewContext, "${androidPreviewContext.packageName}.files", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, androidPreviewContext.contentResolver.getType(uri) ?: "application/octet-stream")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        androidPreviewContext.startActivity(intent)
    }
}
