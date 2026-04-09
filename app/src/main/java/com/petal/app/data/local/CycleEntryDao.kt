package com.petal.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petal.app.data.model.CycleEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleEntryDao {

    @Query("SELECT * FROM cycle_entries WHERE userId = :userId ORDER BY start DESC")
    fun getEntriesForUser(userId: String): Flow<List<CycleEntry>>

    @Query("SELECT * FROM cycle_entries WHERE userId = :userId ORDER BY start DESC")
    suspend fun getEntriesForUserOnce(userId: String): List<CycleEntry>

    @Query("SELECT * FROM cycle_entries WHERE id = :entryId AND userId = :userId")
    suspend fun getEntry(userId: String, entryId: String): CycleEntry?

    @Query("SELECT * FROM cycle_entries WHERE userId = :userId ORDER BY start DESC LIMIT 1")
    suspend fun getLatestEntry(userId: String): CycleEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CycleEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<CycleEntry>)

    @Update
    suspend fun updateEntry(entry: CycleEntry)

    @Delete
    suspend fun deleteEntry(entry: CycleEntry)

    @Query("DELETE FROM cycle_entries WHERE id = :entryId AND userId = :userId")
    suspend fun deleteById(userId: String, entryId: String)

    @Query("SELECT * FROM cycle_entries WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsyncedEntries(userId: String): List<CycleEntry>

    @Query("UPDATE cycle_entries SET isSynced = 1 WHERE id = :entryId")
    suspend fun markSynced(entryId: String)

    @Query("SELECT COUNT(*) FROM cycle_entries WHERE userId = :userId")
    suspend fun getEntryCount(userId: String): Int

    @Query("SELECT * FROM cycle_entries WHERE userId = :userId AND start >= :fromDate ORDER BY start ASC")
    suspend fun getEntriesFrom(userId: String, fromDate: String): List<CycleEntry>

    @Query("DELETE FROM cycle_entries WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
}
