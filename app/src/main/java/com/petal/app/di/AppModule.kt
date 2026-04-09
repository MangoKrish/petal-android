package com.petal.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
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

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PetalDatabase = Room.databaseBuilder(
        context,
        PetalDatabase::class.java,
        PetalDatabase.DATABASE_NAME
    ).fallbackToDestructiveMigration().build()

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
