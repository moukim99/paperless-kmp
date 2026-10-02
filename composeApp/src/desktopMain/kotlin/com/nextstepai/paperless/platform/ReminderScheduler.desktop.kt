package com.nextstepai.paperless.platform

import com.nextstepai.paperless.domain.platform.ReminderScheduler

class DesktopReminderScheduler : ReminderScheduler {
    override fun schedule(documentId: Long, title: String, expiresAtEpochMillis: Long, daysBeforeExpiry: Int) = Unit
    override fun cancel(documentId: Long) = Unit
}
