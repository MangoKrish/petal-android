package com.petal.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petal.app.data.model.OnboardingData
import com.petal.app.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUser(userId: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun observeUser(userId: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): User?

    @Query("SELECT * FROM users LIMIT 1")
    fun observeCurrentUser(): Flow<User?>

    // Cycle mode (PHASE_6_7_PLAN.md §6A.2)
    @Query("SELECT cycleMode FROM users WHERE id = :userId")
    suspend fun getCycleMode(userId: String): String?

    @Query("UPDATE users SET cycleMode = :mode WHERE id = :userId")
    suspend fun updateCycleMode(userId: String, mode: String)

    // Role / handle (PHASE_6_7_PLAN.md §6A.1)
    @Query("SELECT role FROM users WHERE id = :userId")
    suspend fun getRole(userId: String): String?

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateRole(userId: String, role: String)

    @Query("UPDATE users SET username = :username WHERE id = :userId")
    suspend fun updateUsername(userId: String, username: String)

    @Query("UPDATE users SET displayName = :displayName WHERE id = :userId")
    suspend fun updateDisplayName(userId: String, displayName: String)

    // Onboarding
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOnboarding(data: OnboardingData)

    @Query("SELECT * FROM onboarding WHERE userId = :userId")
    suspend fun getOnboarding(userId: String): OnboardingData?

    @Query("SELECT * FROM onboarding WHERE userId = :userId")
    fun observeOnboarding(userId: String): Flow<OnboardingData?>

    @Query("DELETE FROM onboarding WHERE userId = :userId")
    suspend fun deleteOnboarding(userId: String)
}
