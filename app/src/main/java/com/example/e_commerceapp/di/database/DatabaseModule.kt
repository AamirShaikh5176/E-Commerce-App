package com.example.e_commerceapp.di.database

import android.content.Context
import androidx.room.Room
import com.example.e_commerceapp.data.data_source.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE user_carts ADD COLUMN category_id TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE user_carts ADD COLUMN category_name TEXT NOT NULL DEFAULT ''")
        }
    }

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database",
        ).addMigrations(MIGRATION_3_4)
        .fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideAppDao(appDatabase: AppDatabase) = appDatabase.appDao()
}
