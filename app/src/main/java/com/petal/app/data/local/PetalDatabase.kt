package com.petal.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.petal.app.data.model.CycleEntry
import com.petal.app.data.model.OnboardingData
import com.petal.app.data.model.User

@Database(
    entities = [
        User::class,
        CycleEntry::class,
        OnboardingData::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PetalDatabase : RoomDatabase() {
    abstract fun cycleEntryDao(): CycleEntryDao
    abstract fun userDao(): UserDao

    companion object {
        const val DATABASE_NAME = "petal_database"

        @Volatile
        private var INSTANCE: PetalDatabase? = null

        /**
         * Returns a singleton instance of the database.
         * Used by components outside of Hilt DI (widgets, receivers).
         * Hilt-managed components should use the injected instance instead.
         */
        fun getInstance(context: Context): PetalDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PetalDatabase::class.java,
                    DATABASE_NAME
                ).fallbackToDestructiveMigration().build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
