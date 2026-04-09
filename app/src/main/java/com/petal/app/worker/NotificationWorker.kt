package com.petal.app.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.petal.app.MainActivity
import com.petal.app.PetalApplication
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val type = inputData.getString(KEY_NOTIFICATION_TYPE) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val body = inputData.getString(KEY_BODY) ?: return Result.failure()

        val channelId = when (type) {
            TYPE_PERIOD_REMINDER -> PetalApplication.CHANNEL_PERIOD_REMINDERS
            TYPE_DAILY_REMINDER -> PetalApplication.CHANNEL_DAILY_INSIGHTS
            TYPE_PARTNER_UPDATE -> PetalApplication.CHANNEL_PARTNER_UPDATES
            else -> PetalApplication.CHANNEL_DAILY_INSIGHTS
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(
                when (type) {
                    TYPE_PERIOD_REMINDER -> NotificationCompat.PRIORITY_HIGH
                    TYPE_PARTNER_UPDATE -> NotificationCompat.PRIORITY_DEFAULT
                    else -> NotificationCompat.PRIORITY_DEFAULT
                }
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val notificationId = when (type) {
                TYPE_PERIOD_REMINDER -> NOTIFICATION_ID_PERIOD
                TYPE_DAILY_REMINDER -> NOTIFICATION_ID_DAILY
                TYPE_PARTNER_UPDATE -> NOTIFICATION_ID_PARTNER
                else -> NOTIFICATION_ID_DAILY
            }
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }

        return Result.success()
    }

    companion object {
        const val KEY_NOTIFICATION_TYPE = "notification_type"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_DAYS_UNTIL = "days_until"
        const val KEY_PARTNER_NAME = "partner_name"

        const val TYPE_PERIOD_REMINDER = "period_reminder"
        const val TYPE_DAILY_REMINDER = "daily_reminder"
        const val TYPE_PARTNER_UPDATE = "partner_update"

        private const val NOTIFICATION_ID_PERIOD = 1001
        private const val NOTIFICATION_ID_DAILY = 1002
        private const val NOTIFICATION_ID_PARTNER = 1003
    }
}
