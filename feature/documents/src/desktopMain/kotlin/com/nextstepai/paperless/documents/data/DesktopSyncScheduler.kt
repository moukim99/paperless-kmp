package com.nextstepai.paperless.documents.data

import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.minutes

class DesktopSyncScheduler(private val processor: SyncQueueProcessor, private val scope: CoroutineScope) : SyncScheduler {
    override fun schedule() {
        scope.launch { while (isActive) { runCatching { processor.process() }; delay(15.minutes) } }
    }
}
