package com.petal.app.data.remote

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Intercepts OkHttp requests to attach the Bearer token.
 *
 * Uses an in-memory [AtomicReference] for the cached token so we never call
 * [runBlocking] on OkHttp's dispatcher thread (which can deadlock or trigger ANRs).
 * The cache is updated whenever [updateToken] / [clearToken] are called from a
 * coroutine context (e.g. after login or logout).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : Interceptor {

    /** Thread-safe, non-blocking token cache. */
    private val cachedToken = AtomicReference<String?>(null)

    /** Call from a coroutine after login/register to warm the in-memory cache. */
    suspend fun warmCache() {
        cachedToken.set(
            dataStore.data.map { it[TOKEN_KEY] }.first()
        )
    }

    /** Update both DataStore *and* the in-memory cache. */
    fun updateToken(token: String?) {
        cachedToken.set(token)
    }

    /** Clear token on logout. */
    fun clearToken() {
        cachedToken.set(null)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = cachedToken.get()

        val request = chain.request().newBuilder().apply {
            addHeader("Content-Type", "application/json")
            addHeader("Accept", "application/json")
            if (token != null) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(request)
    }

    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val SESSION_ID_KEY = stringPreferencesKey("session_id")
    }
}
