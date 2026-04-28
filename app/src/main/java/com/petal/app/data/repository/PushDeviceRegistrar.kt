package com.petal.app.data.repository

import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import com.petal.app.BuildConfig
import com.petal.app.data.local.PetalPreferences
import com.petal.app.data.remote.PetalApiService
import com.petal.app.data.remote.dto.RegisterDeviceRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pulls the FCM token and registers the device with PetalAPI.
 * Safe to call repeatedly: stores last-sent token in DataStore and only
 * re-sends if the token has changed or never been sent.
 */
@Singleton
class PushDeviceRegistrar @Inject constructor(
    private val api: PetalApiService,
    private val prefs: PetalPreferences,
    @ApplicationContext private val context: Context,
) {
    suspend fun registerIfNeeded(): Result<Unit> = runCatching {
        val token = FirebaseMessaging.getInstance().token.await()
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
        val response = api.registerDevice(
            RegisterDeviceRequest(
                deviceToken = token,
                platform = "android",
                deviceName = deviceName.ifBlank { "Android device" },
                appVersion = BuildConfig.VERSION_NAME ?: "0.0.0",
            )
        )
        if (response.isSuccessful) {
            prefs.setFcmToken(token)
        }
        Unit
    }
}
