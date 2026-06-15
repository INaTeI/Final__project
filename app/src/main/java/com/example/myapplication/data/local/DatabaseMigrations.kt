package com.example.myapplication.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_profiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS browse_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                profileId INTEGER NOT NULL,
                countryCode TEXT NOT NULL,
                countryName TEXT NOT NULL,
                countryFlag TEXT NOT NULL,
                viewedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_browse_history_profileId_countryCode
            ON browse_history(profileId, countryCode)
            """.trimIndent()
        )
        addColumnIfMissing(db, "countries", "cachedAt", "INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS collections (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                profileId INTEGER NOT NULL,
                name TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS collection_countries (
                collectionId INTEGER NOT NULL,
                countryCode TEXT NOT NULL,
                PRIMARY KEY(collectionId, countryCode)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS country_notes (
                profileId INTEGER NOT NULL,
                countryCode TEXT NOT NULL,
                text TEXT NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(profileId, countryCode)
            )
            """.trimIndent()
        )
    }
}

private fun addColumnIfMissing(
    db: SupportSQLiteDatabase,
    tableName: String,
    columnName: String,
    definition: String
) {
    db.query("PRAGMA table_info($tableName)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == columnName) return
        }
    }
    db.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $definition")
}
