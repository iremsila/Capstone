package com.iremsilayildirim.capstone.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Mevcut tabloyu silmek istiyorsanız:
        database.execSQL("DROP TABLE IF EXISTS users")

        // Yeni tabloyu oluşturun:
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                age TEXT NOT NULL,
                email TEXT NOT NULL,
                password TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}
