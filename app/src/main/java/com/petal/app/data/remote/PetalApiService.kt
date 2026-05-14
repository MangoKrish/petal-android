package com.petal.app.data.remote

import com.petal.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface PetalApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<SecurityQuestionResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    // Cycle entries
    @GET("cycles")
    suspend fun getCycleEntries(): Response<List<CycleEntryDto>>

    @POST("cycles")
    suspend fun createCycleEntry(@Body request: CycleEntryRequest): Response<CycleEntryDto>

    @PUT("cycles/{id}")
    suspend fun updateCycleEntry(
        @Path("id") id: String,
        @Body request: CycleEntryRequest
    ): Response<CycleEntryDto>

    @DELETE("cycles/{id}")
    suspend fun deleteCycleEntry(@Path("id") id: String): Response<Unit>

    // Onboarding
    @GET("onboarding")
    suspend fun getOnboarding(): Response<OnboardingDto>

    @POST("onboarding")
    suspend fun saveOnboarding(@Body request: OnboardingRequest): Response<OnboardingDto>

    // Sharing
    @GET("share/links")
    suspend fun getShareLinks(): Response<List<ShareLinkDto>>

    @POST("share/links")
    suspend fun createShareLink(@Body request: CreateShareLinkRequest): Response<ShareLinkDto>

    @DELETE("share/links/{id}")
    suspend fun revokeShareLink(@Path("id") id: String): Response<Unit>

    @GET("share/{token}")
    suspend fun getSharedData(@Path("token") token: String): Response<SharedDataDto>

    // Partner
    @GET("partner/connections")
    suspend fun getPartnerConnections(): Response<List<PartnerConnectionDto>>

    @POST("partner/invite")
    suspend fun invitePartner(@Body request: InvitePartnerRequest): Response<PartnerConnectionDto>

    @PUT("partner/connections/{id}")
    suspend fun updatePartnerConnection(
        @Path("id") id: String,
        @Body request: UpdatePartnerRequest
    ): Response<PartnerConnectionDto>

    @DELETE("partner/connections/{id}")
    suspend fun removePartner(@Path("id") id: String): Response<Unit>

    // PHASE_6_7_PLAN.md §6B.1 — block list + moderation reports
    @GET("users/me/blocks")
    suspend fun listBlocks(): Response<ApiEnvelope<List<BlockedUserDto>>>

    @POST("users/me/blocks")
    suspend fun blockHandle(@Body request: BlockHandleRequest): Response<ApiEnvelope<BlockedUserDto>>

    @DELETE("users/me/blocks/{id}")
    suspend fun removeBlock(@Path("id") id: String): Response<Unit>

    @POST("reports")
    suspend fun submitReport(@Body request: ReportRequest): Response<ApiEnvelope<ReportSubmittedDto>>

    // PHASE_6_7_PLAN.md §6B.3 — friend groups
    @GET("groups")
    suspend fun listGroups(): Response<ApiEnvelope<List<FriendGroupSummaryDto>>>

    @POST("groups")
    suspend fun createGroup(@Body request: CreateGroupRequest): Response<ApiEnvelope<FriendGroupSummaryDto>>

    @POST("groups/join")
    suspend fun joinGroup(@Body request: JoinGroupRequest): Response<ApiEnvelope<FriendGroupSummaryDto>>

    @GET("groups/{id}/members")
    suspend fun listGroupMembers(@Path("id") id: String): Response<ApiEnvelope<List<FriendGroupMemberDto>>>

    @GET("groups/{id}/scoreboard")
    suspend fun getGroupScoreboard(
        @Path("id") id: String,
        @Query("range") range: String,
    ): Response<ApiEnvelope<List<ScoreboardEntryDto>>>

    @PATCH("groups/{id}/membership")
    suspend fun updateGroupMembership(
        @Path("id") id: String,
        @Body request: PatchMembershipRequest,
    ): Response<Unit>

    @POST("groups/{id}/unwell")
    suspend fun unwellPing(
        @Path("id") id: String,
        @Body request: UnwellPingRequest,
    ): Response<ApiEnvelope<UnwellPingResponseDto>>

    @POST("groups/{id}/leave")
    suspend fun leaveGroup(@Path("id") id: String): Response<Unit>

    @DELETE("groups/{id}")
    suspend fun disbandGroup(@Path("id") id: String): Response<Unit>

    @DELETE("groups/{id}/members/{userId}")
    suspend fun removeGroupMember(
        @Path("id") id: String,
        @Path("userId") userId: String,
    ): Response<Unit>

    // User
    @GET("user/profile")
    suspend fun getProfile(): Response<UserProfileDto>

    @PUT("user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserProfileDto>

    @DELETE("user/account")
    suspend fun deleteAccount(): Response<Unit>

    // Sync
    @POST("sync")
    suspend fun syncEntries(@Body request: SyncRequest): Response<SyncResponse>

    // ---- Partner Messaging ----
    @GET("messages/thread")
    suspend fun getMessagingThread(): Response<ApiEnvelope<PartnerThreadDto?>>

    @GET("messages")
    suspend fun listMessages(@Query("limit") limit: Int = 100): Response<ApiEnvelope<List<PartnerMessageDto>>>

    @POST("messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<ApiEnvelope<PartnerMessageDto>>

    @POST("messages/read")
    suspend fun markMessagesRead(): Response<ApiEnvelope<MarkReadResponse>>

    @DELETE("messages/{id}")
    suspend fun deleteMessage(@Path("id") id: String): Response<ApiEnvelope<Unit>>

    // ---- Notifications / device registration ----
    @POST("notifications/register-device")
    suspend fun registerDevice(@Body request: RegisterDeviceRequest): Response<ApiEnvelope<RegisterDeviceResponse>>

    // PHASE_6_7_PLAN.md §6B.4 — daily quiz
    @GET("quiz/today")
    suspend fun getQuizToday(): Response<ApiEnvelope<DailyQuizSetDto>>

    @POST("quiz/answer")
    suspend fun answerQuiz(@Body request: AnswerQuizRequest): Response<ApiEnvelope<AnswerQuizResponseDto>>

    @GET("quiz/stats")
    suspend fun getQuizStats(): Response<ApiEnvelope<QuizStatsDto>>
}
