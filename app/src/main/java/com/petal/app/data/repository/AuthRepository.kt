package com.petal.app.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.petal.app.data.local.UserDao
import com.petal.app.data.model.User
import com.petal.app.data.remote.AuthInterceptor
import com.petal.app.data.remote.PetalApiService
import com.petal.app.data.remote.dto.LoginRequest
import com.petal.app.data.remote.dto.RegisterRequest
import com.petal.app.data.remote.dto.ForgotPasswordRequest
import com.petal.app.data.remote.dto.ResetPasswordRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val apiService: PetalApiService,
    private val dataStore: DataStore<Preferences>,
    private val authInterceptor: AuthInterceptor
) {
    val isLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[AuthInterceptor.TOKEN_KEY] != null
    }

    val currentUserId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[AuthInterceptor.USER_ID_KEY]
    }

    val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE_KEY] ?: false
    }

    fun observeCurrentUser(): Flow<User?> = userDao.observeCurrentUser()

    suspend fun getCurrentUser(): User? = userDao.getCurrentUser()

    suspend fun getCurrentUserId(): String? =
        dataStore.data.map { it[AuthInterceptor.USER_ID_KEY] }.first()

    suspend fun register(
        name: String,
        email: String,
        password: String,
        securityQuestion: String,
        securityAnswer: String
    ): Result<User> = try {
        val response = apiService.register(
            RegisterRequest(
                name = name.trim(),
                email = email.trim().lowercase(),
                password = password,
                securityQuestion = securityQuestion,
                securityAnswer = securityAnswer
            )
        )
        if (response.isSuccessful) {
            val body = response.body()!!
            val user = User(
                id = body.userId,
                name = body.name,
                email = body.email,
                createdAt = body.createdAt
            )
            userDao.insertUser(user)
            authInterceptor.updateToken(body.token)
            dataStore.edit { prefs ->
                prefs[AuthInterceptor.TOKEN_KEY] = body.token
                prefs[AuthInterceptor.USER_ID_KEY] = body.userId
                prefs[AuthInterceptor.SESSION_ID_KEY] = body.sessionId
            }
            Result.success(user)
        } else {
            val errorBody = response.errorBody()?.string() ?: ""
            val errorMsg = try {
                val json = org.json.JSONObject(errorBody)
                json.optJSONObject("error")?.optString("message") ?: json.optString("message", "Registration failed. Please try again.")
            } catch (_: Exception) { "Registration failed (${response.code()}). Please try again." }
            Result.failure(Exception(errorMsg))
        }
    } catch (e: java.io.IOException) {
        Result.failure(Exception("Network error. Check your connection and try again."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun login(email: String, password: String): Result<User> = try {
        val response = apiService.login(
            LoginRequest(email = email.trim().lowercase(), password = password)
        )
        if (response.isSuccessful) {
            val body = response.body()!!
            val user = User(
                id = body.userId,
                name = body.name,
                email = body.email,
                createdAt = body.createdAt
            )
            userDao.insertUser(user)
            authInterceptor.updateToken(body.token)
            dataStore.edit { prefs ->
                prefs[AuthInterceptor.TOKEN_KEY] = body.token
                prefs[AuthInterceptor.USER_ID_KEY] = body.userId
                prefs[AuthInterceptor.SESSION_ID_KEY] = body.sessionId
            }
            Result.success(user)
        } else {
            val errorBody = response.errorBody()?.string() ?: ""
            val errorMsg = try {
                val json = org.json.JSONObject(errorBody)
                json.optJSONObject("error")?.optString("message") ?: json.optString("message", "Incorrect email or password.")
            } catch (_: Exception) { "Login failed (${response.code()}). Please try again." }
            Result.failure(Exception(errorMsg))
        }
    } catch (e: java.io.IOException) {
        Result.failure(Exception("Network error. Check your connection and try again."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun logout() {
        authInterceptor.clearToken()
        try {
            apiService.logout()
        } catch (_: java.io.IOException) { /* offline – server session will expire */ }
        dataStore.edit { prefs ->
            prefs.remove(AuthInterceptor.TOKEN_KEY)
            prefs.remove(AuthInterceptor.USER_ID_KEY)
            prefs.remove(AuthInterceptor.SESSION_ID_KEY)
            prefs.remove(ONBOARDING_COMPLETE_KEY)
        }
    }

    suspend fun getSecurityQuestion(email: String): Result<String> = try {
        val response = apiService.forgotPassword(ForgotPasswordRequest(email.trim().lowercase()))
        if (response.isSuccessful) {
            Result.success(response.body()!!.securityQuestion)
        } else {
            Result.failure(Exception("No account found with that email."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun resetPassword(
        email: String,
        securityAnswer: String,
        newPassword: String
    ): Result<Unit> = try {
        val response = apiService.resetPassword(
            ResetPasswordRequest(
                email = email.trim().lowercase(),
                securityAnswer = securityAnswer,
                newPassword = newPassword
            )
        )
        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Incorrect security answer."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE_KEY] = true
        }
    }

    suspend fun deleteAccount() {
        try {
            apiService.deleteAccount()
        } catch (_: Exception) { }
        val userId = getCurrentUserId()
        if (userId != null) {
            userDao.deleteUser(userId)
            userDao.deleteOnboarding(userId)
        }
        dataStore.edit { it.clear() }
    }

    companion object {
        val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
    }
}
