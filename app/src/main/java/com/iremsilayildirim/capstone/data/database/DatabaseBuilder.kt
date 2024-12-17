package com.iremsilayildirim.capstone.data.database

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context): AppDatabase {
        if (INSTANCE == null) {
            synchronized(AppDatabase::class) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "homework1.db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration() // Optional for development
                    .build()
            }
        }
        return INSTANCE!!
    }

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
