package com.petal.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PetalApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val periodChannel = NotificationChannel(
            CHANNEL_PERIOD_REMINDERS,
            "Period Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders about upcoming periods and cycle events"
        }

        val dailyChannel = NotificationChannel(
            CHANNEL_DAILY_INSIGHTS,
            "Daily Insights",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily cycle insights and wellness tips"
        }

        val partnerChannel = NotificationChannel(
            CHANNEL_PARTNER_UPDATES,
            "Partner Updates",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Updates for partners and caregivers"
        }

        val syncChannel = NotificationChannel(
            CHANNEL_SYNC,
            "Sync",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background data synchronization"
        }

        manager.createNotificationChannels(
            listOf(periodChannel, dailyChannel, partnerChannel, syncChannel)
        )
    }

    companion object {
        const val CHANNEL_PERIOD_REMINDERS = "period_reminders"
        const val CHANNEL_DAILY_INSIGHTS = "daily_insights"
        const val CHANNEL_PARTNER_UPDATES = "partner_updates"
        const val CHANNEL_SYNC = "sync"
    }
}
