package com.petal.app.data.repository

import com.petal.app.data.local.DailyFertilityLogDao
import com.petal.app.data.model.DailyFertilityLog
import com.petal.app.domain.FertilityFusion
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FertilityLogRepository @Inject constructor(
    private val dao: DailyFertilityLogDao
) {
    fun observeForUser(userId: String): Flow<List<DailyFertilityLog>> =
        dao.observeForUser(userId)

    suspend fun getForDate(userId: String, date: LocalDate): DailyFertilityLog? =
        dao.getForDate(userId, date.toString())

    suspend fun getRecent(userId: String, days: Int = 45): List<DailyFertilityLog> {
        val since = LocalDate.now().minusDays(days.toLong()).toString()
        return dao.getSince(userId, since)
    }

    suspend fun upsert(
        userId: String,
        date: LocalDate,
        temperature: Double? = null,
        cervicalMucus: String? = null,
        ovulationPain: Boolean = false,
        lhTestResult: String? = null,
        sexualActivity: Boolean = false
    ): DailyFertilityLog {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val existing = dao.getForDate(userId, date.toString())
        val merged = (existing ?: DailyFertilityLog(
            userId = userId,
            logDate = date.toString(),
            createdAt = now,
            updatedAt = now
        )).copy(
            // Only overwrite a field when the caller passed something explicit.
            // For the booleans the upstream UI always sends current state, so
            // we replace; numeric/text fields use null = "leave existing".
            temperature = temperature ?: existing?.temperature,
            cervicalMucus = cervicalMucus ?: existing?.cervicalMucus,
            ovulationPain = ovulationPain,
            lhTestResult = lhTestResult ?: existing?.lhTestResult,
            sexualActivity = sexualActivity,
            updatedAt = now,
            isSynced = false
        )
        dao.upsert(merged)
        return merged
    }

    /** Maps Room rows into the FertilityFusion math input shape. */
    fun toFusionLogs(rows: List<DailyFertilityLog>): List<FertilityFusion.FertilityLog> =
        rows.map { r ->
            FertilityFusion.FertilityLog(
                logDate = LocalDate.parse(r.logDate),
                temperature = r.temperature,
                cervicalMucus = r.cervicalMucus,
                ovulationPain = r.ovulationPain,
                lhTestResult = when (r.lhTestResult) {
                    "positive" -> FertilityFusion.LhTestResult.POSITIVE
                    "negative" -> FertilityFusion.LhTestResult.NEGATIVE
                    "inconclusive" -> FertilityFusion.LhTestResult.INCONCLUSIVE
                    else -> null
                }
            )
        }
}
