package com.petal.app.data.remote.dto

import kotlinx.serialization.Serializable

// ---- Auth ----

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val securityQuestion: String = "",
    val securityAnswer: String = ""
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val userId: String,
    val name: String,
    val email: String,
    val token: String,
    val sessionId: String,
    val createdAt: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class SecurityQuestionResponse(
    val securityQuestion: String
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val securityAnswer: String,
    val newPassword: String
)

// ---- Cycle Entries ----

@Serializable
data class CycleEntryDto(
    val id: String,
    val startDate: String,
    val endDate: String,
    val cycleLength: Int,
    val flowIntensity: String,
    val pain: String = "None",
    val cramps: String = "None",
    val cravings: String = "None",
    val mood: String = "Calm",
    val headaches: String = "None",
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CycleEntryRequest(
    val startDate: String,
    val endDate: String,
    val cycleLength: Int,
    val flowIntensity: String,
    val pain: String = "None",
    val cramps: String = "None",
    val cravings: String = "None",
    val mood: String = "Calm",
    val headaches: String = "None"
)

// ---- Onboarding ----

@Serializable
data class OnboardingDto(
    val userId: String,
    val lastPeriodStart: String,
    val periodLength: Int,
    val cycleLength: Int,
    val goals: List<String> = emptyList(),
    val symptoms: List<String> = emptyList(),
    val completedAt: String? = null
)

@Serializable
data class OnboardingRequest(
    val lastPeriodStart: String,
    val periodLength: Int,
    val cycleLength: Int,
    val goals: List<String> = emptyList(),
    val symptoms: List<String> = emptyList()
)

// ---- Sharing ----

@Serializable
data class ShareLinkDto(
    val id: String,
    val token: String,
    val label: String,
    val showCycleLength: Boolean = true,
    val showNextPeriod: Boolean = true,
    val showSymptoms: Boolean = false,
    val showPhase: Boolean = true,
    val active: Boolean = true,
    val createdAt: String
)

@Serializable
data class CreateShareLinkRequest(
    val label: String,
    val showCycleLength: Boolean = true,
    val showNextPeriod: Boolean = true,
    val showSymptoms: Boolean = false,
    val showPhase: Boolean = true
)

@Serializable
data class SharedDataDto(
    val userName: String,
    val label: String,
    val permissions: SharedPermissionsDto,
    val entries: List<SharedEntryDto>,
    val onboarding: SharedOnboardingDto? = null
)

@Serializable
data class SharedPermissionsDto(
    val showCycleLength: Boolean,
    val showNextPeriod: Boolean,
    val showSymptoms: Boolean,
    val showPhase: Boolean
)

@Serializable
data class SharedEntryDto(
    val start: String,
    val end: String,
    val cycleLength: Int,
    val flowIntensity: String,
    val symptoms: SharedSymptomsDto
)

@Serializable
data class SharedSymptomsDto(
    val pain: String,
    val cramps: String,
    val cravings: String,
    val mood: String,
    val headaches: String
)

@Serializable
data class SharedOnboardingDto(
    val lastPeriodStart: String,
    val periodLength: Int,
    val cycleLength: Int
)

// ---- Partner ----

@Serializable
data class PartnerConnectionDto(
    val id: String,
    val partnerName: String,
    val partnerEmail: String,
    val note: String = "",
    val sharingEnabled: Boolean = true,
    val status: String = "Invited",
    val isCaregiver: Boolean = false,
    val permissions: SharedPermissionsDto,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class InvitePartnerRequest(
    val partnerName: String,
    val partnerEmail: String,
    val note: String = "",
    val isCaregiver: Boolean = false,
    val showCycleLength: Boolean = true,
    val showNextPeriod: Boolean = true,
    val showSymptoms: Boolean = false,
    val showPhase: Boolean = true
)

@Serializable
data class UpdatePartnerRequest(
    val sharingEnabled: Boolean? = null,
    val showCycleLength: Boolean? = null,
    val showNextPeriod: Boolean? = null,
    val showSymptoms: Boolean? = null,
    val showPhase: Boolean? = null
)

// ---- User ----

@Serializable
data class UserProfileDto(
    val id: String,
    val name: String,
    val email: String,
    val dateOfBirth: String? = null,
    val createdAt: String,
    val isPartnerAccount: Boolean = false
)

@Serializable
data class UpdateProfileRequest(
    val name: String? = null,
    val email: String? = null,
    val dateOfBirth: String? = null
)

// ---- Sync ----

@Serializable
data class SyncRequest(
    val entries: List<CycleEntryRequest>,
    val lastSyncAt: String? = null
)

@Serializable
data class SyncResponse(
    val updatedEntries: List<CycleEntryDto>,
    val deletedIds: List<String>,
    val syncedAt: String
)
