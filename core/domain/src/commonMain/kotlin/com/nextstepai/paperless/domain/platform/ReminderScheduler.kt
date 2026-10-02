package com.nextstepai.paperless.domain.platform

interface ReminderScheduler {
    fun schedule(documentId: Long, title: String, expiresAtEpochMillis: Long, daysBeforeExpiry: Int)
    fun cancel(documentId: Long)
}
