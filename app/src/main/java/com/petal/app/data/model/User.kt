package com.petal.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val dateOfBirth: String? = null,
    val createdAt: String,
    val isPartnerAccount: Boolean = false
)

@Serializable
data class Session(
    val id: String,
    val userId: String,
    val token: String,
    val createdAt: String,
    val expiresAt: String
)

@Entity(tableName = "onboarding")
data class OnboardingData(
    @PrimaryKey val userId: String,
    val lastPeriodStart: String,
    val periodLength: Int = 5,
    val cycleLength: Int = 28,
    val goals: List<String> = emptyList(),
    val symptoms: List<String> = emptyList(),
    val completedAt: String? = null
)

data class NotificationPreferences(
    val upcomingCycleEnabled: Boolean = true,
    val upcomingCycleLeadDays: Int = 2,
    val dailySymptomEnabled: Boolean = false,
    val dailySymptomTime: String = "09:00",
    val frequency: ReminderFrequency = ReminderFrequency.Daily,
    val inAppEnabled: Boolean = true,
    val quietMode: Boolean = false
)

enum class ReminderFrequency(val display: String) {
    Daily("Daily"),
    EveryOtherDay("Every other day"),
    Weekly("Weekly");
}
