package com.nextstepai.paperless.domain.sync

interface SyncOperationEnqueuer { suspend fun enqueue(documentId: Long, operation: String) }
