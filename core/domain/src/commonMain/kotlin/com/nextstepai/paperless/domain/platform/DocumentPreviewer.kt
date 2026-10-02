package com.nextstepai.paperless.domain.platform

interface DocumentPreviewer {
    suspend fun open(documentId: Long): Result<Unit>
}
