package com.nextstepai.paperless.documents.data

import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes

class DesktopSyncScheduler(private val processor: SyncQueueProcessor, private val scope: CoroutineScope) : SyncScheduler {
    private var syncJob: Job? = null

    override fun schedule() {
        syncJob?.cancel()
        syncJob = scope.launch {
            runCatching { processor.process() }
            while (isActive) {
                delay(15.minutes)
                runCatching { processor.process() }
            }
        }
    }
}
