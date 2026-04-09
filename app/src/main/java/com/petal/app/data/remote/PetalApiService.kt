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
}
