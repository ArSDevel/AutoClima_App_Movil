package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.ChecklistDao
import com.example.myapplication.data.local.dao.ClienteDao
import com.example.myapplication.data.local.dao.DiagnosticoDao
import com.example.myapplication.data.local.dao.EvidenciaDao
import com.example.myapplication.data.local.dao.FirmaDao
import com.example.myapplication.data.local.dao.IngresoDao
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.dao.VehiculoDao
import com.example.myapplication.data.local.entity.ChecklistDiagnostico
import com.example.myapplication.data.local.entity.Cliente
import com.example.myapplication.data.local.entity.ConceptoCotizacion
import com.example.myapplication.data.local.entity.Diagnostico
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Evidencia
import com.example.myapplication.data.local.entity.Firma
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.data.local.entity.Vehiculo

// Base de datos local; exportSchema guarda el esquema de cada versión para validar las migraciones.
@Database(entities = [
        Tecnico::class,
        Cliente::class,
        Vehiculo::class,
        Ingreso::class,
        ChecklistDiagnostico::class,
        EventoIngreso::class,
        Diagnostico::class,
        ConceptoCotizacion::class,
        Evidencia::class,
        Firma::class],
    version = 12,
    exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tecnicoDao(): TecnicoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun diagnosticoDao(): DiagnosticoDao
    abstract fun evidenciaDao(): EvidenciaDao
    abstract fun firmaDao(): FirmaDao

    companion object {
        // Instancia única compartida; @Volatile hace visible su asignación a todos los hilos.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val contextoAplicacion = context.applicationContext

            // Doble comprobación: evita bloquear si ya existe y evita crear dos bases si dos hilos llegan a la vez.
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    contextoAplicacion,
                    AppDatabase::class.java,
                    "autoclima_diag.db")
                    // Rutas disponibles:
                    // versión 5 a 12, pasando por la 6, la 9, la 10 y la 11
                    // versión 6 a 12, pasando por la 9, la 10 y la 11
                    // versión 8 a 12, pasando por la 9, la 10 y la 11
                    // versión 9 a 12, pasando por la 10 y la 11
                    // versión 10 a 12, pasando por la 11
                    // versión 11 a 12
                    .addMigrations(MIGRATION_5_6,
                        MIGRATION_6_9,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12)
                    .build()
                    .also { INSTANCE = it } } } } }
