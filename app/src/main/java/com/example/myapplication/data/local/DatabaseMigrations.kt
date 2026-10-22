package com.example.myapplication.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Versión 5 a 6: incorpora el diagnóstico y su cotización.
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
            """.trimIndent())

        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS index_diagnostico_ingresoId
            ON diagnostico (ingresoId)
            """.trimIndent())

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
            """.trimIndent())

        db.execSQL(
            """
            CREATE INDEX IF NOT EXISTS index_concepto_cotizacion_diagnosticoId
            ON concepto_cotizacion (diagnosticoId)
            """.trimIndent()) } }

// Versión 9 a 10: incorpora la firma del cliente. Conserva todos los datos existentes.
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS firma (
                firmaId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                ingresoId INTEGER NOT NULL,
                rutaImagen TEXT NOT NULL,
                tecnicoId INTEGER NOT NULL,
                fecha INTEGER NOT NULL,
                FOREIGN KEY (ingresoId)
                    REFERENCES ingreso (ingresoId)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )""".trimIndent())
        // El índice único garantiza una sola firma vigente por ingreso.
        db.execSQL("""CREATE UNIQUE INDEX IF NOT EXISTS index_firma_ingresoId
            ON firma (ingresoId)""".trimIndent()) } }
// Versión 10 → 11: incorpora el técnico encargado de la reparación en cada ingreso.
// Los ingresos existentes quedan con el valor vacío.
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""ALTER TABLE ingreso
            ADD COLUMN tecnicoEncargado TEXT NOT NULL DEFAULT ''
            """.trimIndent()) } }