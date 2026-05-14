package com.petal.app.data.repository

import com.petal.app.data.remote.PetalApiService
import com.petal.app.data.remote.dto.CreateGroupRequest
import com.petal.app.data.remote.dto.FriendGroupMemberDto
import com.petal.app.data.remote.dto.FriendGroupSummaryDto
import com.petal.app.data.remote.dto.JoinGroupRequest
import com.petal.app.data.remote.dto.PatchMembershipRequest
import com.petal.app.data.remote.dto.ScoreboardEntryDto
import com.petal.app.data.remote.dto.UnwellPingRequest
import com.petal.app.data.remote.dto.UnwellPingResponseDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PHASE_6_7_PLAN.md §6B.3 — friend groups + wellness scoreboard.
 * Wraps each API call in Result<T> with friendly error messages.
 */
@Singleton
class GroupsRepository @Inject constructor(
    private val api: PetalApiService,
) {
    suspend fun listGroups(): Result<List<FriendGroupSummaryDto>> = try {
        val r = api.listGroups()
        if (r.isSuccessful) Result.success(r.body()?.data ?: emptyList())
        else Result.failure(Exception("Couldn't load groups (${r.code()})."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun createGroup(name: String, emoji: String?): Result<FriendGroupSummaryDto> = try {
        val r = api.createGroup(CreateGroupRequest(name = name.trim(), emoji = emoji?.trim()?.takeIf { it.isNotEmpty() }))
        val body = r.body()?.data
        if (r.isSuccessful && body != null) Result.success(body)
        else Result.failure(Exception("Couldn't create group (${r.code()})."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun joinGroup(code: String): Result<FriendGroupSummaryDto> = try {
        val r = api.joinGroup(JoinGroupRequest(joinCode = code.trim().uppercase()))
        val body = r.body()?.data
        if (r.isSuccessful && body != null) Result.success(body)
        else Result.failure(Exception("Couldn't join — check the code."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun listMembers(groupId: String): Result<List<FriendGroupMemberDto>> = try {
        val r = api.listGroupMembers(groupId)
        if (r.isSuccessful) Result.success(r.body()?.data ?: emptyList())
        else Result.failure(Exception("Couldn't load members."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getScoreboard(groupId: String, range: String): Result<List<ScoreboardEntryDto>> = try {
        val r = api.getGroupScoreboard(groupId, range)
        if (r.isSuccessful) Result.success(r.body()?.data ?: emptyList())
        else Result.failure(Exception("Couldn't load scoreboard."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun updateMembership(
        groupId: String,
        shareLevel: String? = null,
        receiveUnwellPings: Boolean? = null,
    ): Result<Unit> = try {
        val r = api.updateGroupMembership(groupId, PatchMembershipRequest(shareLevel, receiveUnwellPings))
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Couldn't update."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun fireUnwellPing(groupId: String, message: String?): Result<UnwellPingResponseDto> = try {
        val r = api.unwellPing(groupId, UnwellPingRequest(message?.takeIf { it.isNotBlank() }))
        val body = r.body()?.data
        if (r.isSuccessful && body != null) Result.success(body)
        else Result.failure(Exception("Couldn't send."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun leaveGroup(groupId: String): Result<Unit> = try {
        val r = api.leaveGroup(groupId)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Couldn't leave."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun disbandGroup(groupId: String): Result<Unit> = try {
        val r = api.disbandGroup(groupId)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Couldn't disband."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun removeMember(groupId: String, targetUserId: String): Result<Unit> = try {
        val r = api.removeGroupMember(groupId, targetUserId)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Couldn't remove."))
    } catch (e: Exception) { Result.failure(e) }
}
