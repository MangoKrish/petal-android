package com.petal.app.data.remote.dto

import kotlinx.serialization.Serializable

// ---- Auth ----

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val securityQuestion: String = "",
    val securityAnswer: String = "",
    /** PHASE_6_7_PLAN.md §6A.1 — "primary" | "supporter". Default "primary". */
    val role: String? = null
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
    val createdAt: String,
    /** Returned by API on signup/register (PHASE_6_7_PLAN.md §6A.1). */
    val role: String? = null,
    val username: String? = null,
    val displayName: String? = null
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

// ---- Partner Messaging ----

@Serializable
data class PartnerThreadDto(
    val threadId: String,
    val partnerId: String,
    val partnerName: String,
    val lastMessageAt: String? = null
)

@Serializable
data class PartnerMessageDto(
    val id: String,
    val threadId: String,
    val senderId: String,
    val content: String,
    val sentAt: String,
    val readAt: String? = null
)

@Serializable
data class SendMessageRequest(
    val content: String
)

@Serializable
data class MarkReadResponse(
    val updated: Int
)

// ---- API envelopes ----

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val data: T? = null
)

// ---- Moderation (PHASE_6_7_PLAN.md §6B.1) ----

@Serializable
data class BlockedUserDto(
    val id: String,
    val blockedUserId: String,
    val username: String? = null,
    val displayName: String? = null,
    val blockedAt: String,
)

@Serializable
data class BlockHandleRequest(
    val username: String,
)

@Serializable
data class ReportRequest(
    /** snake_case identifier — e.g. "handle", "partner_connection", "story_share" */
    val context: String,
    val reason: String,
    val details: String? = null,
    val reportedUsername: String? = null,
    val reportedUserId: String? = null,
)

@Serializable
data class ReportSubmittedDto(
    val id: String,
    val status: String,
    val createdAt: String,
)

// ---- Friend groups (PHASE_6_7_PLAN.md §6B.3) ----

@Serializable
data class FriendGroupSummaryDto(
    val id: String,
    val name: String,
    val emoji: String? = null,
    val joinCode: String,
    val createdBy: String? = null,
    val createdAt: String,
    val maxMembers: Int,
    val memberCount: Int,
    val myShareLevel: String,
    val myReceiveUnwellPings: Boolean,
)

@Serializable
data class FriendGroupMemberDto(
    val userId: String,
    val username: String? = null,
    val displayName: String? = null,
    val joinedAt: String,
    val shareLevel: String,
    val receiveUnwellPings: Boolean,
)

@Serializable
data class ScoreboardEntryDto(
    val userId: String,
    val username: String? = null,
    val displayName: String? = null,
    val hydrationScore: Int,
    val sleepScore: Int,
    val exerciseScore: Int,
    /** PHASE_6_7_PLAN.md §6B.4 — quiz contribution (accuracy × engagement). */
    val quizScore: Int = 0,
    val totalScore: Int,
    val hydrationStreakDays: Int,
    val avgSleepHours: Double,
    val totalExerciseMinutes: Int,
    val quizCorrect: Int = 0,
    val quizAnswered: Int = 0,
)

@Serializable
data class CreateGroupRequest(
    val name: String,
    val emoji: String? = null,
)

@Serializable
data class JoinGroupRequest(
    val joinCode: String,
)

@Serializable
data class PatchMembershipRequest(
    val shareLevel: String? = null,
    val receiveUnwellPings: Boolean? = null,
)

@Serializable
data class UnwellPingRequest(
    val message: String? = null,
)

@Serializable
data class UnwellPingResponseDto(
    val id: String,
    val recipientCount: Int,
)

// ---- Notifications / device registration ----

@Serializable
data class RegisterDeviceRequest(
    val deviceToken: String,
    val platform: String = "android",
    val deviceName: String,
    val appVersion: String,
)

@Serializable
data class RegisterDeviceResponse(
    val deviceId: String? = null,
)

// ---- Quiz (PHASE_6_7_PLAN.md §6B.4) ----

@Serializable
data class QuizQuestionOptionDto(
    val key: String,
    val text: String,
)

@Serializable
data class QuizAttemptDto(
    val selectedKey: String,
    val correct: Boolean,
    val correctKey: String,
    val explanation: String,
)

@Serializable
data class DailyQuizQuestionDto(
    val id: String,
    val category: String,
    val difficulty: String,
    val prompt: String,
    val options: List<QuizQuestionOptionDto>,
    val source: String? = null,
    val audience: String,
    val attempt: QuizAttemptDto? = null,
)

@Serializable
data class DailyQuizSetDto(
    val forDate: String,
    val questions: List<DailyQuizQuestionDto>,
    val completed: Boolean,
    val answeredCount: Int,
)

@Serializable
data class QuizStatsDto(
    val lifetimeCorrect: Int,
    val lifetimeAnswered: Int,
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val todayCompleted: Boolean,
)

@Serializable
data class AnswerQuizRequest(
    val questionId: String,
    val selectedKey: String,
)

@Serializable
data class AnswerQuizResponseDto(
    val correct: Boolean,
    val correctKey: String,
    val explanation: String,
    val setCompleted: Boolean,
)

@Serializable
data class QuizHistoryEntryDto(
    val attemptedAt: String,
    val category: String,
    val prompt: String,
    val selectedKey: String,
    val correct: Boolean,
    val correctKey: String,
)
