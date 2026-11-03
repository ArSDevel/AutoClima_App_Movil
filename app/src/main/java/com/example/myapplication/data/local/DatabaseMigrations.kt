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

// Versión 11 a 12: agrega el código de recuperación de contraseña. Los usuarios existentes quedan sin código hasta que
// generen uno desde su perfil.
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tecnico ADD COLUMN recuperacionHash TEXT") } }

// Versión 12 a 13: las fotos tomadas desde el registro llevaban un texto fijo como descripción y salía impreso en el
// reporte. Se vacía para que el pie de foto sea el que escriba el técnico o, si no hay, el número de foto.
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""UPDATE tabla_evidencias SET descripcion = ''
            WHERE descripcion = 'Fotografía del vehículo adjuntada desde Registro.'""".trimIndent()) } }

// Versión 13 a 14: el vehículo deja de tener kilometraje y observaciones, que nunca se capturaban. El SQLite de Android 7
// no puede borrar columnas, así que se crea la tabla sin ellas, se copian los datos y se reemplaza la anterior.
// Durante una migración las claves foráneas están apagadas (Room las enciende al abrir la base), por eso borrar la
// tabla vieja no arrastra los ingresos que dependen de ella.
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""CREATE TABLE IF NOT EXISTS vehiculo_nueva (
                vehiculoId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                clienteId INTEGER NOT NULL,
                placa TEXT NOT NULL,
                modelo TEXT NOT NULL,
                color TEXT,
                numeroSerie TEXT,
                FOREIGN KEY (clienteId)
                    REFERENCES cliente (clienteId)
                    ON UPDATE NO ACTION
                    ON DELETE CASCADE
            )""".trimIndent())
        db.execSQL("""INSERT INTO vehiculo_nueva (vehiculoId, clienteId, placa, modelo, color, numeroSerie)
            SELECT vehiculoId, clienteId, placa, modelo, color, numeroSerie FROM vehiculo""".trimIndent())
        db.execSQL("DROP TABLE vehiculo")
        db.execSQL("ALTER TABLE vehiculo_nueva RENAME TO vehiculo")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_vehiculo_clienteId ON vehiculo (clienteId)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_vehiculo_placa ON vehiculo (placa)") } }
