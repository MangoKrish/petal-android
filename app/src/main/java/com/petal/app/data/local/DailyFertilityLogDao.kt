package com.petal.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petal.app.data.model.DailyFertilityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyFertilityLogDao {

    @Query("SELECT * FROM daily_fertility_logs WHERE userId = :userId AND logDate = :logDate")
    suspend fun getForDate(userId: String, logDate: String): DailyFertilityLog?

    @Query("SELECT * FROM daily_fertility_logs WHERE userId = :userId ORDER BY logDate ASC")
    fun observeForUser(userId: String): Flow<List<DailyFertilityLog>>

    @Query("SELECT * FROM daily_fertility_logs WHERE userId = :userId AND logDate >= :sinceDate ORDER BY logDate ASC")
    suspend fun getSince(userId: String, sinceDate: String): List<DailyFertilityLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: DailyFertilityLog)

    @Query("DELETE FROM daily_fertility_logs WHERE userId = :userId AND logDate = :logDate")
    suspend fun deleteForDate(userId: String, logDate: String)

    @Query("DELETE FROM daily_fertility_logs WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("SELECT * FROM daily_fertility_logs WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsynced(userId: String): List<DailyFertilityLog>

    @Query("UPDATE daily_fertility_logs SET isSynced = 1 WHERE userId = :userId AND logDate = :logDate")
    suspend fun markSynced(userId: String, logDate: String)
}
