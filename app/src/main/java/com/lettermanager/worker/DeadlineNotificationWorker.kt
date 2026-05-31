package com.lettermanager.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lettermanager.MainActivity
import com.lettermanager.R
import com.lettermanager.data.local.dao.LetterDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DeadlineNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val letterDao: LetterDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val letterId = inputData.getInt(KEY_LETTER_ID, -1)
        val daysRemaining = inputData.getInt(KEY_DAYS_REMAINING, 0)
        val letterNumber = inputData.getString(KEY_LETTER_NUMBER) ?: return Result.failure()

        if (letterId == -1) return Result.failure()

        val letter = letterDao.getLetterById(letterId) ?: return Result.success()
        if (letter.status == "archived") return Result.success()

        val message = when (daysRemaining) {
            3 -> "نامه $letterNumber — ۳ روز تا سررسید"
            1 -> "نامه $letterNumber — فردا سررسید است"
            0 -> "نامه $letterNumber — امروز سررسید است"
            else -> "نامه $letterNumber — اقدام لازم است"
        }

        sendNotification(letterId, letterNumber, message)
        return Result.success()
    }

    private fun sendNotification(letterId: Int, letterNumber: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "یادآوری سررسید نامه",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "اعلان‌های مربوط به سررسید نامه‌ها"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("letter_id", letterId)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            letterId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("یادآوری سررسید")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(letterId * 10 + (id.hashCode() % 10), notification)
    }

    companion object {
        const val CHANNEL_ID = "deadline_notifications"
        const val KEY_LETTER_ID = "letter_id"
        const val KEY_DAYS_REMAINING = "days_remaining"
        const val KEY_LETTER_NUMBER = "letter_number"
    }
}
