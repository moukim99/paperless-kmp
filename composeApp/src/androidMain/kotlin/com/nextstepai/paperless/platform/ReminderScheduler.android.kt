package com.nextstepai.paperless.platform

import android.app.*
import android.content.Context
import androidx.work.*
import com.nextstepai.paperless.domain.platform.ReminderScheduler
import java.util.concurrent.TimeUnit

class AndroidReminderScheduler(private val context: Context) : ReminderScheduler {
    private val workManager = WorkManager.getInstance(context)
    override fun schedule(documentId: Long, title: String, expiresAtEpochMillis: Long, daysBeforeExpiry: Int) {
        val trigger = expiresAtEpochMillis - TimeUnit.DAYS.toMillis(daysBeforeExpiry.toLong())
        val delay = (trigger - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ExpiryReminderWorker>()
            .setInputData(workDataOf("documentId" to documentId, "title" to title))
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork("expiry-$documentId", ExistingWorkPolicy.REPLACE, request)
    }
    override fun cancel(documentId: Long) { workManager.cancelUniqueWork("expiry-$documentId") }
}

class ExpiryReminderWorker(appContext: Context, params: WorkerParameters) : Worker(appContext, params) {
    override fun doWork(): Result {
        val channelId = "document-expiry"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel(channelId, "Document expiry", NotificationManager.IMPORTANCE_DEFAULT))
        val title = inputData.getString("title") ?: "Document expiry reminder"
        val notification = Notification.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Document expiry reminder")
            .setContentText("$title is approaching its expiry date.")
            .setAutoCancel(true).build()
        manager.notify(inputData.getLong("documentId", System.currentTimeMillis()).toInt(), notification)
        return Result.success()
    }
}
