package com.example.myapplication.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Versión 2 → 3: incorpora el estado del ingreso.  Conservamos la migración existente.
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""ALTER TABLE ingreso
            ADD COLUMN estado TEXT NOT NULL DEFAULT 'NO_DISPONIBLE'
            """.trimIndent())
    }
}

// Versión 3 → 4:  incorpora el historial de operaciones de los ingresos.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS evento_ingreso (
                eventoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ingresoId INTEGER NOT NULL,
                fecha INTEGER NOT NULL,
                tecnicoId INTEGER NOT NULL,
                accion TEXT NOT NULL,
                estadoAnterior TEXT NOT NULL,
                estadoNuevo TEXT NOT NULL,
                motivo TEXT NOT NULL,
                FOREIGN KEY (ingresoId)
                    REFERENCES ingreso (ingresoId)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )""".trimIndent())
        db.execSQL("""CREATE INDEX IF NOT EXISTS index_evento_ingreso_ingresoId
            ON evento_ingreso (ingresoId)""".trimIndent())
    }
}

// Versión 5 → 6: incorpora el diagnóstico y su cotización.
// Conserva las tablas y los datos existentes.
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {

        // Un diagnóstico por ingreso.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS diagnostico (
                diagnosticoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ingresoId INTEGER NOT NULL,
                observaciones TEXT NOT NULL,
                tecnicoId INTEGER NOT NULL,
                fechaCreacion INTEGER NOT NULL,
                fechaActualizacion INTEGER NOT NULL,
                FOREIGN KEY (ingresoId)
                    REFERENCES ingreso (ingresoId)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_diagnostico_ingresoId
            ON diagnostico (ingresoId)
            """.trimIndent()
        )

        // Conceptos capturados por el mecánico.
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS concepto_cotizacion (
                conceptoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                diagnosticoId INTEGER NOT NULL,
                descripcion TEXT NOT NULL,
                importeCentavos INTEGER NOT NULL,
                orden INTEGER NOT NULL,
                FOREIGN KEY (diagnosticoId)
                    REFERENCES diagnostico (diagnosticoId)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_concepto_cotizacion_diagnosticoId
            ON concepto_cotizacion (diagnosticoId)
            """.trimIndent()
        )
    }
}