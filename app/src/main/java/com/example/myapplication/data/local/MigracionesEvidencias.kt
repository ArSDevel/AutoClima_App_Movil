package com.example.myapplication.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Permite actualizar directamente desde la versión de diagnóstico
// hacia la versión que incorpora evidencias asociadas a ingresos.
val MIGRATION_6_9 = object : Migration(6, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        prepararEvidenciasVersion9(db)
    }
}

// Actualiza la versión incluida en el proyecto de cámara.
// Conserva las fotografías que ya estaban registradas.
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        prepararEvidenciasVersion9(db)
    }
}

private fun prepararEvidenciasVersion9(
    db: SupportSQLiteDatabase
) {
    // La versión anterior de diagnóstico no tenía fotoPath.
    // La versión 8 del proyecto ya incluye este campo.
    if (!existeColumnaFotoPath(db)) {
        db.execSQL(
            """
            ALTER TABLE ingreso
            ADD COLUMN fotoPath TEXT
            """.trimIndent()
        )
    }

    // En la versión 6 todavía no existía esta tabla.
    // En la versión 8 se conserva la tabla y sus registros.
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS tabla_evidencias (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            rutaImagen TEXT NOT NULL
        )
        """.trimIndent()
    )

    // Las fotografías anteriores permanecen sin asignar.
    // No es posible deducir a qué ingreso pertenecían.
    db.execSQL(
        """
        ALTER TABLE tabla_evidencias
        ADD COLUMN ingresoId INTEGER
        REFERENCES ingreso (ingresoId)
        ON UPDATE NO ACTION
        ON DELETE RESTRICT
        """.trimIndent()
    )

    // Los datos desconocidos de fotografías antiguas quedan en null.
    db.execSQL(
        """
        ALTER TABLE tabla_evidencias
        ADD COLUMN tecnicoId INTEGER
        """.trimIndent()
    )

    db.execSQL(
        """
        ALTER TABLE tabla_evidencias
        ADD COLUMN fecha INTEGER
        """.trimIndent()
    )

    db.execSQL(
        """
        ALTER TABLE tabla_evidencias
        ADD COLUMN descripcion TEXT NOT NULL DEFAULT ''
        """.trimIndent()
    )

    // Facilita las consultas de fotografías por ingreso.
    db.execSQL(
        """
        CREATE INDEX index_tabla_evidencias_ingresoId
        ON tabla_evidencias (ingresoId)
        """.trimIndent()
    )
}

private fun existeColumnaFotoPath(
    db: SupportSQLiteDatabase
): Boolean {
    db.query("PRAGMA table_info(ingreso)").use { cursor ->
        val indiceNombre = cursor.getColumnIndexOrThrow("name")

        while (cursor.moveToNext()) {
            if (cursor.getString(indiceNombre) == "fotoPath") {
                return true
            }
        }
    }

    return false
}