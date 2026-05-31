package com.lettermanager.service

import android.content.Context
import androidx.work.*
import com.lettermanager.domain.model.Letter
import com.lettermanager.worker.DeadlineNotificationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleDeadlineNotifications(letter: Letter) {
        val now = System.currentTimeMillis()
        val deadlineMs = letter.deadlineMiladi

        // 3 days before
        scheduleNotification(letter, daysRemaining = 3, deadlineMs = deadlineMs, now = now)
        // 1 day before
        scheduleNotification(letter, daysRemaining = 1, deadlineMs = deadlineMs, now = now)
        // Deadline day
        scheduleNotification(letter, daysRemaining = 0, deadlineMs = deadlineMs, now = now)
    }

    private fun scheduleNotification(letter: Letter, daysRemaining: Int, deadlineMs: Long, now: Long) {
        val triggerMs = deadlineMs - (daysRemaining * 24 * 60 * 60 * 1000L)
        val delayMs = triggerMs - now
        if (delayMs <= 0) return

        val data = workDataOf(
            DeadlineNotificationWorker.KEY_LETTER_ID to letter.id,
            DeadlineNotificationWorker.KEY_DAYS_REMAINING to daysRemaining,
            DeadlineNotificationWorker.KEY_LETTER_NUMBER to letter.autoNumber
        )

        val request = OneTimeWorkRequestBuilder<DeadlineNotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("letter_${letter.id}")
            .build()

        workManager.enqueueUniqueWork(
            "deadline_${letter.id}_${daysRemaining}d",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelDeadlineNotifications(letterId: Int) {
        workManager.cancelAllWorkByTag("letter_$letterId")
    }
}
