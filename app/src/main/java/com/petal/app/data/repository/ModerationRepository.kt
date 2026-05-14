package com.petal.app.data.repository

import com.petal.app.data.remote.PetalApiService
import com.petal.app.data.remote.dto.BlockHandleRequest
import com.petal.app.data.remote.dto.BlockedUserDto
import com.petal.app.data.remote.dto.ReportRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PHASE_6_7_PLAN.md §6B.1 — moderation surface.
 * Wraps the API endpoints with friendlier error envelopes; mirrors the
 * EducationBookmarksRepository pattern. Local cache could be added here
 * later if blocked-list fetches become a bottleneck.
 */
@Singleton
class ModerationRepository @Inject constructor(
    private val api: PetalApiService
) {
    suspend fun listBlocks(): Result<List<BlockedUserDto>> = try {
        val response = api.listBlocks()
        if (response.isSuccessful) {
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Result.failure(Exception("Couldn't load blocks (${response.code()})."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun blockHandle(handle: String): Result<BlockedUserDto> = try {
        val response = api.blockHandle(BlockHandleRequest(username = handle.trim().lowercase()))
        val body = response.body()?.data
        if (response.isSuccessful && body != null) {
            Result.success(body)
        } else {
            Result.failure(Exception("Couldn't block — handle may not exist."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun removeBlock(blockId: String): Result<Unit> = try {
        val response = api.removeBlock(blockId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Couldn't unblock (${response.code()})."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun submitReport(
        context: String,
        reason: String,
        details: String? = null,
        reportedUsername: String? = null,
        reportedUserId: String? = null,
    ): Result<String> = try {
        val response = api.submitReport(
            ReportRequest(
                context = context,
                reason = reason,
                details = details?.takeIf { it.isNotBlank() },
                reportedUsername = reportedUsername?.takeIf { it.isNotBlank() },
                reportedUserId = reportedUserId?.takeIf { it.isNotBlank() },
            )
        )
        val id = response.body()?.data?.id
        if (response.isSuccessful && id != null) {
            Result.success(id)
        } else {
            Result.failure(Exception("Couldn't file the report (${response.code()})."))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
