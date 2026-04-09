package com.petal.app.data.repository

import com.petal.app.data.local.CycleEntryDao
import com.petal.app.data.model.*
import com.petal.app.data.remote.PetalApiService
import com.petal.app.data.remote.dto.CycleEntryRequest
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepository @Inject constructor(
    private val cycleEntryDao: CycleEntryDao,
    private val apiService: PetalApiService
) {
    fun observeEntries(userId: String): Flow<List<CycleEntry>> =
        cycleEntryDao.getEntriesForUser(userId)

    suspend fun getEntries(userId: String): List<CycleEntry> =
        cycleEntryDao.getEntriesForUserOnce(userId)

    suspend fun getEntry(userId: String, entryId: String): CycleEntry? =
        cycleEntryDao.getEntry(userId, entryId)

    suspend fun getLatestEntry(userId: String): CycleEntry? =
        cycleEntryDao.getLatestEntry(userId)

    suspend fun saveEntry(
        userId: String,
        entryId: String? = null,
        start: String,
        end: String,
        cycleLength: Int,
        flowIntensity: FlowIntensity,
        pain: SymptomLevel = SymptomLevel.None,
        cramps: SymptomLevel = SymptomLevel.None,
        cravings: SymptomLevel = SymptomLevel.None,
        mood: MoodLevel = MoodLevel.Calm,
        headaches: SymptomLevel = SymptomLevel.None
    ): CycleEntry {
        require(cycleLength in 15..45) { "Cycle length must be 15-45 days." }

        val startDate = LocalDate.parse(start)
        val endDate = LocalDate.parse(end)
        require(!endDate.isBefore(startDate)) { "End date must be on or after start date." }

        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val id = entryId ?: UUID.randomUUID().toString()

        val entry = CycleEntry(
            id = id,
            userId = userId,
            start = start,
            end = end,
            cycleLength = cycleLength,
            flowIntensity = flowIntensity,
            painLevel = pain,
            crampsLevel = cramps,
            cravingsLevel = cravings,
            moodLevel = mood,
            headachesLevel = headaches,
            createdAt = now,
            updatedAt = now,
            isSynced = false
        )

        cycleEntryDao.insertEntry(entry)

        // Attempt remote sync (fire and forget for offline-first)
        try {
            val request = CycleEntryRequest(
                startDate = start,
                endDate = end,
                cycleLength = cycleLength,
                flowIntensity = flowIntensity.display,
                pain = pain.display,
                cramps = cramps.display,
                cravings = cravings.display,
                mood = mood.display,
                headaches = headaches.display
            )
            val response = if (entryId != null) {
                apiService.updateCycleEntry(id, request)
            } else {
                apiService.createCycleEntry(request)
            }
            if (response.isSuccessful) {
                cycleEntryDao.markSynced(id)
            }
        } catch (_: Exception) {
            // Will sync later via WorkManager
        }

        return entry
    }

    suspend fun deleteEntry(userId: String, entryId: String) {
        cycleEntryDao.deleteById(userId, entryId)
        try {
            apiService.deleteCycleEntry(entryId)
        } catch (_: Exception) {
            // Will handle in background sync
        }
    }

    suspend fun syncWithRemote(userId: String) {
        try {
            val response = apiService.getCycleEntries()
            if (response.isSuccessful) {
                val remoteDtos = response.body() ?: return
                val remoteEntries = remoteDtos.map { dto ->
                    CycleEntry(
                        id = dto.id,
                        userId = userId,
                        start = dto.startDate,
                        end = dto.endDate,
                        cycleLength = dto.cycleLength,
                        flowIntensity = FlowIntensity.fromString(dto.flowIntensity),
                        painLevel = SymptomLevel.fromString(dto.pain),
                        crampsLevel = SymptomLevel.fromString(dto.cramps),
                        cravingsLevel = SymptomLevel.fromString(dto.cravings),
                        moodLevel = MoodLevel.fromString(dto.mood),
                        headachesLevel = SymptomLevel.fromString(dto.headaches),
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        isSynced = true
                    )
                }
                cycleEntryDao.insertEntries(remoteEntries)
            }

            // Upload unsynced local entries
            val unsynced = cycleEntryDao.getUnsyncedEntries(userId)
            for (entry in unsynced) {
                try {
                    val request = CycleEntryRequest(
                        startDate = entry.start,
                        endDate = entry.end,
                        cycleLength = entry.cycleLength,
                        flowIntensity = entry.flowIntensity.display,
                        pain = entry.painLevel.display,
                        cramps = entry.crampsLevel.display,
                        cravings = entry.cravingsLevel.display,
                        mood = entry.moodLevel.display,
                        headaches = entry.headachesLevel.display
                    )
                    val result = apiService.createCycleEntry(request)
                    if (result.isSuccessful) {
                        cycleEntryDao.markSynced(entry.id)
                    }
                } catch (_: Exception) {
                    break // Stop trying if network fails
                }
            }
        } catch (_: Exception) {
            // Offline — will retry later
        }
    }

    suspend fun getEntryCount(userId: String): Int =
        cycleEntryDao.getEntryCount(userId)

    suspend fun deleteAllForUser(userId: String) {
        cycleEntryDao.deleteAllForUser(userId)
    }
}
