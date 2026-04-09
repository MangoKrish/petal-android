package com.petal.app.domain

import android.content.Context
import androidx.work.*
import com.petal.app.worker.NotificationWorker
import com.petal.app.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule a notification for upcoming period.
     * Fires [leadDays] days before the predicted period start.
     */
    fun schedulePeriodReminder(nextPeriodDate: java.time.LocalDate, leadDays: Int = 2) {
        val reminderDate = nextPeriodDate.minusDays(leadDays.toLong())
        val now = LocalDateTime.now()
        val reminderDateTime = reminderDate.atTime(9, 0) // 9 AM

        val delayMillis = Duration.between(now, reminderDateTime).toMillis()
        if (delayMillis <= 0) return // Date already passed

        val data = workDataOf(
            NotificationWorker.KEY_NOTIFICATION_TYPE to NotificationWorker.TYPE_PERIOD_REMINDER,
            NotificationWorker.KEY_TITLE to "Period coming soon",
            NotificationWorker.KEY_BODY to "Your period is expected in about $leadDays days. Time to prepare!",
            NotificationWorker.KEY_DAYS_UNTIL to leadDays
        )

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(TAG_PERIOD_REMINDER)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            WORK_PERIOD_REMINDER,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Schedule daily symptom logging reminders.
     */
    fun scheduleDailyReminder(time: LocalTime = LocalTime.of(9, 0)) {
        val now = LocalDateTime.now()
        var nextRun = now.toLocalDate().atTime(time)
        if (nextRun.isBefore(now)) {
            nextRun = nextRun.plusDays(1)
        }

        val delayMillis = Duration.between(now, nextRun).toMillis()

        val data = workDataOf(
            NotificationWorker.KEY_NOTIFICATION_TYPE to NotificationWorker.TYPE_DAILY_REMINDER,
            NotificationWorker.KEY_TITLE to "How are you feeling today?",
            NotificationWorker.KEY_BODY to "Take a moment to log your symptoms and flow."
        )

        val request = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(TAG_DAILY_REMINDER)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_DAILY_REMINDER,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Schedule partner notification about partner's cycle.
     */
    fun schedulePartnerUpdate(partnerName: String, phase: String, cycleDay: Int) {
        val data = workDataOf(
            NotificationWorker.KEY_NOTIFICATION_TYPE to NotificationWorker.TYPE_PARTNER_UPDATE,
            NotificationWorker.KEY_TITLE to "$partnerName's cycle update",
            NotificationWorker.KEY_BODY to "$partnerName is on day $cycleDay ($phase phase). Check the app for ways to support them.",
            NotificationWorker.KEY_PARTNER_NAME to partnerName
        )

        val request = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInputData(data)
            .addTag(TAG_PARTNER_UPDATE)
            .build()

        workManager.enqueueUniqueWork(
            WORK_PARTNER_UPDATE,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Schedule periodic background data sync.
     */
    fun scheduleSync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
            .addTag(TAG_SYNC)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Cancel all scheduled notifications.
     */
    fun cancelAll() {
        workManager.cancelAllWorkByTag(TAG_PERIOD_REMINDER)
        workManager.cancelAllWorkByTag(TAG_DAILY_REMINDER)
        workManager.cancelAllWorkByTag(TAG_PARTNER_UPDATE)
    }

    /**
     * Cancel just the daily reminder.
     */
    fun cancelDailyReminder() {
        workManager.cancelUniqueWork(WORK_DAILY_REMINDER)
    }

    companion object {
        private const val TAG_PERIOD_REMINDER = "period_reminder"
        private const val TAG_DAILY_REMINDER = "daily_reminder"
        private const val TAG_PARTNER_UPDATE = "partner_update"
        private const val TAG_SYNC = "sync"

        private const val WORK_PERIOD_REMINDER = "work_period_reminder"
        private const val WORK_DAILY_REMINDER = "work_daily_reminder"
        private const val WORK_PARTNER_UPDATE = "work_partner_update"
        private const val WORK_SYNC = "work_sync"
    }
}
