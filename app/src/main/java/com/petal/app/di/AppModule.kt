package com.petal.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.petal.app.data.local.CycleEntryDao
import com.petal.app.data.local.DailyFertilityLogDao
import com.petal.app.data.local.PetalDatabase
import com.petal.app.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "petal_preferences")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ─── Migrations ───────────────────────────────────────────────────────
    // v1→v2: PHASE_6_7_PLAN.md §6A.2. Adds the daily_fertility_logs table
    // (per-day BBT / mucus / ovulation pain / LH / sexual activity) and the
    // users.cycleMode column (tracking | ttc | avoiding). Mirrors PetalAPI
    // migration 004_fertility_fusion_fields.sql.
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS daily_fertility_logs (
                    userId TEXT NOT NULL,
                    logDate TEXT NOT NULL,
                    temperature REAL,
                    cervicalMucus TEXT,
                    ovulationPain INTEGER NOT NULL DEFAULT 0,
                    lhTestResult TEXT,
                    sexualActivity INTEGER NOT NULL DEFAULT 0,
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL,
                    isSynced INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(userId, logDate)
                )
            """.trimIndent())
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_dfl_user_date ON daily_fertility_logs(userId, logDate)")
            db.execSQL("ALTER TABLE users ADD COLUMN cycleMode TEXT NOT NULL DEFAULT 'tracking'")
        }
    }

    // v2→v3: PHASE_6_7_PLAN.md §6A.1. Adds users.role / users.username /
    // users.displayName so the supporter shell + handle-based connections
    // have somewhere to live. Mirrors PetalAPI migration 003.
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'primary'")
            db.execSQL("ALTER TABLE users ADD COLUMN username TEXT")
            db.execSQL("ALTER TABLE users ADD COLUMN displayName TEXT")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PetalDatabase = Room.databaseBuilder(
        context,
        PetalDatabase::class.java,
        PetalDatabase.DATABASE_NAME
    )
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
        // Last-resort safety net only on truly unhandled bumps. Prefer explicit
        // migrations above; this prevents installs from crash-looping if a dev
        // forgets to add one.
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    @Provides
    @Singleton
    fun provideCycleEntryDao(database: PetalDatabase): CycleEntryDao =
        database.cycleEntryDao()

    @Provides
    @Singleton
    fun provideUserDao(database: PetalDatabase): UserDao =
        database.userDao()

    @Provides
    @Singleton
    fun provideDailyFertilityLogDao(database: PetalDatabase): DailyFertilityLogDao =
        database.dailyFertilityLogDao()

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore
}
