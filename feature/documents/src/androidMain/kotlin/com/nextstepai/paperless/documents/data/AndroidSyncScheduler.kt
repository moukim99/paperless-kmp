package com.nextstepai.paperless.documents.data

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

object AndroidSyncRuntime { @Volatile var processor: SyncQueueProcessor? = null }

class AndroidSyncScheduler(private val context: Context) : SyncScheduler {
    override fun schedule() {
        val request = PeriodicWorkRequestBuilder<PaperlessSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork("paperless-sync", ExistingPeriodicWorkPolicy.UPDATE, request)
    }
}

class PaperlessSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            val processor = AndroidSyncRuntime.processor ?: return Result.failure()
            processor.process()
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}
