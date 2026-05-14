package com.petal.app.data.repository

import com.petal.app.data.remote.PetalApiService
import com.petal.app.data.remote.dto.AnswerQuizRequest
import com.petal.app.data.remote.dto.AnswerQuizResponseDto
import com.petal.app.data.remote.dto.DailyQuizSetDto
import com.petal.app.data.remote.dto.QuizStatsDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PHASE_6_7_PLAN.md §6B.4 — daily quiz repository.
 * Wraps PetalAPI quiz endpoints in Result<T> with kind error copy.
 */
@Singleton
class QuizRepository @Inject constructor(
    private val api: PetalApiService,
) {
    suspend fun fetchToday(): Result<DailyQuizSetDto> = try {
        val r = api.getQuizToday()
        val body = r.body()?.data
        if (r.isSuccessful && body != null) Result.success(body)
        else Result.failure(Exception("Couldn’t load today’s quiz."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun answer(questionId: String, selectedKey: String): Result<AnswerQuizResponseDto> = try {
        val r = api.answerQuiz(AnswerQuizRequest(questionId, selectedKey))
        val body = r.body()?.data
        if (r.isSuccessful && body != null) Result.success(body)
        else Result.failure(Exception("Couldn’t record that answer."))
    } catch (e: Exception) { Result.failure(e) }

    suspend fun stats(): Result<QuizStatsDto> = try {
        val r = api.getQuizStats()
        val body = r.body()?.data
        if (r.isSuccessful && body != null) Result.success(body)
        else Result.failure(Exception("Couldn’t load quiz stats."))
    } catch (e: Exception) { Result.failure(e) }
}
