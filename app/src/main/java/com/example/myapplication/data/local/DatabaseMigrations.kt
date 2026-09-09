package com.example.myapplication.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE ingreso
            ADD COLUMN estado TEXT NOT NULL DEFAULT 'NO_DISPONIBLE'
            """.trimIndent()
        )
    }
}