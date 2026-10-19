package com.example.myapplication.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.ChecklistDao
import com.example.myapplication.data.local.dao.ClienteDao
import com.example.myapplication.data.local.dao.DiagnosticoDao
import com.example.myapplication.data.local.dao.EvidenciaDao
import com.example.myapplication.data.local.dao.IngresoDao
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.dao.VehiculoDao
import com.example.myapplication.data.local.entity.ChecklistDiagnostico
import com.example.myapplication.data.local.entity.Cliente
import com.example.myapplication.data.local.entity.ConceptoCotizacion
import com.example.myapplication.data.local.entity.Diagnostico
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Evidencia
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.data.local.entity.Vehiculo
import com.example.myapplication.util.PasswordHasher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [
        Tecnico::class,
        Cliente::class,
        Vehiculo::class,
        Ingreso::class,
        ChecklistDiagnostico::class,
        EventoIngreso::class,
        Diagnostico::class,
        ConceptoCotizacion::class,
        Evidencia::class
    ],
    version = 9,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tecnicoDao(): TecnicoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun ingresoDao(): IngresoDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun diagnosticoDao(): DiagnosticoDao
    abstract fun evidenciaDao(): EvidenciaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val contextoAplicacion = context.applicationContext

            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    contextoAplicacion,
                    AppDatabase::class.java,
                    "autoclima_diag.db"
                )
                    // Rutas disponibles:
                    // versión 5 → 6 → 9
                    // versión 6 → 9
                    // versión 8 → 9
                    .addMigrations(MIGRATION_5_6,
                        MIGRATION_6_9,
                        MIGRATION_8_9)
                    .addCallback(object : Callback() {
                        // Solo se ejecuta al crear una base nueva.
                        // Una actualización conserva los técnicos existentes.
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("CargaInicial",
                                "Se ejecutó onCreate")
                            sembrarUsuarioInicial(contextoAplicacion)
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
        // Conserva el técnico de prueba para instalaciones nuevas.
        private fun sembrarUsuarioInicial(
            context: Context
        ) { CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = getInstance(context).tecnicoDao()
                    val id = dao.insertar(
                        Tecnico(nombre = "Erik",
                            usuario = "erik",
                            passwordHash = PasswordHasher.hash("1234")))
                    Log.d("CargaInicial",
                        "Técnico insertado con ID: $id")
                } catch (cancelacion: CancellationException) {
                    throw cancelacion
                } catch (error: Exception) {
                    Log.e("CargaInicial", "Falló la inserción del técnico",
                        error)
                }
            }
        }
    }
}