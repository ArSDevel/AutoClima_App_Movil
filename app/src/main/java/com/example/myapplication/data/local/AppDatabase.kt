package com.example.myapplication.data.local

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.ClienteDao
import com.example.myapplication.data.local.dao.IngresoDao
import com.example.myapplication.data.local.dao.TecnicoDao
import com.example.myapplication.data.local.dao.VehiculoDao
import com.example.myapplication.data.local.entity.Cliente
import com.example.myapplication.data.local.entity.EventoIngreso
import com.example.myapplication.data.local.entity.Ingreso
import com.example.myapplication.data.local.entity.Tecnico
import com.example.myapplication.data.local.entity.Vehiculo
import com.example.myapplication.util.PasswordHasher

@Database(
    entities = [Tecnico::class, Cliente::class, Vehiculo::class, Ingreso::class, EventoIngreso::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tecnicoDao(): TecnicoDao
    abstract fun clienteDao(): ClienteDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun ingresoDao(): IngresoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: crearBaseDeDatos(context.applicationContext)
                    .also { baseDeDatos -> INSTANCE = baseDeDatos }
            }
        }

        private fun crearBaseDeDatos(appContext: Context): AppDatabase {
            val esDepuracion = (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            return Room.databaseBuilder(appContext, AppDatabase::class.java, "autoclima_diag.db")
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            if (esDepuracion) { insertarTecnicoDePrueba(db) }
                        }
                    }
                )
                .build()
        }

        private fun insertarTecnicoDePrueba(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                INSERT INTO tecnico (nombre,usuario,passwordHash)VALUES (?, ?, ?)
                """.trimIndent(),
                arrayOf<Any>("Erik", "erik", PasswordHasher.hash("1234")))
            Log.d("CargaInicial", "Técnico de prueba creado correctamente")
        }
    }
}