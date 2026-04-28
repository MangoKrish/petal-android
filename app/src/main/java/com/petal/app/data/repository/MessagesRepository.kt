package com.petal.app.data.repository

import com.petal.app.data.remote.PetalApiService
import com.petal.app.data.remote.dto.PartnerMessageDto
import com.petal.app.data.remote.dto.PartnerThreadDto
import com.petal.app.data.remote.dto.SendMessageRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessagesRepository @Inject constructor(
    private val api: PetalApiService
) {
    suspend fun getThread(): Result<PartnerThreadDto?> = try {
        val r = api.getMessagingThread()
        if (r.isSuccessful) Result.success(r.body()?.data)
        else Result.failure(Exception("Couldn't reach soft talks."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun listMessages(limit: Int = 100): Result<List<PartnerMessageDto>> = try {
        val r = api.listMessages(limit)
        if (r.isSuccessful) Result.success(r.body()?.data ?: emptyList())
        else Result.failure(Exception("Couldn't load messages."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun send(content: String): Result<PartnerMessageDto> = try {
        val r = api.sendMessage(SendMessageRequest(content = content))
        val data = r.body()?.data
        if (r.isSuccessful && data != null) Result.success(data)
        else Result.failure(Exception("Couldn't send message."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markRead(): Result<Int> = try {
        val r = api.markMessagesRead()
        if (r.isSuccessful) Result.success(r.body()?.data?.updated ?: 0)
        else Result.failure(Exception("Couldn't mark read."))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun delete(id: String): Result<Unit> = try {
        val r = api.deleteMessage(id)
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Couldn't delete message."))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
