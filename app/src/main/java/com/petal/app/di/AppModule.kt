package com.petal.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.petal.app.data.local.CycleEntryDao
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
    // No-op v1→v2 placeholder so a future schema bump has a real upgrade path
    // instead of falling back to destructive migration (which wipes user data).
    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Intentionally empty: schema unchanged in v2.
            // When real changes land, replace this with explicit DDL.
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
        .addMigrations(MIGRATION_1_2)
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
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore
}
